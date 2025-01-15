package ru.neustupov.advpost.service.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.neustupov.advpost.event.status.ChangePostStatusEventPublisher;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.model.PostStatus;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.util.S3Util;
import ru.neustupov.advpost.telegram.bot.TelegramBot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramServiceImpl implements TelegramService {

    private final S3Util s3Util;
    private final TelegramBot telegramBot;
    private final ChangePostStatusEventPublisher changePostStatusEventPublisher;

    public TelegramServiceImpl(S3Util s3Util, TelegramBot telegramBot, ChangePostStatusEventPublisher changePostStatusEventPublisher) {
        this.s3Util = s3Util;
        this.telegramBot = telegramBot;
        this.changePostStatusEventPublisher = changePostStatusEventPublisher;
    }

    @Override
    public List<MessageResponse> sendMessage(Post post, String message, String chatId, PostStatus finalStatus) {

        List<Attachment> attachments = post.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            if (attachments.size() < 2) {
                return sendUserPhoto(chatId, post, message, attachments.get(0), finalStatus);
            } else {
                return sendMediaGroup(chatId, post, message, attachments, finalStatus);
            }
        } else if (post.getMessage() != null && !message.isBlank()) {
            return sendText(chatId, post, message, null, finalStatus);
        }
        return List.of();
    }

    @Override
    public MessageResponse sendMessage(String message, String chatId) {
        return sendTextWithoutKeyboard(message, chatId);
    }

    @Override
    public List<MessageResponse> makeInlineKeyboardAndSendMessage(Post post, String chatId) {
        if (post != null) {
            return sendText(chatId, post, "Выберите действие", makeInlineKeyboard(post), PostStatus.PUBLISHED);
        }
        return List.of();
    }

    @Override
    public void deletePostAndKeyboard(String chatId, List<Integer> messageIds) {
        messageIds.forEach(m -> {
            DeleteMessage deleteMessages = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(m)
                    .build();
            try {
                telegramBot.execute(deleteMessages);
                log.info("Delete message with id = {} from chat with id = {}", m, chatId);
            } catch (TelegramApiException e) {
                log.error("Can`t delete messages with id = {}", m);
            }
        });
    }

    private List<MessageResponse> sendUserPhoto(String chatId, Post post, String message, Attachment attachment, PostStatus finalStatus) {
        String s3Uri = attachment.getS3Uri();
        try (InputStream inputStream = s3Util.download(s3Uri)) {
            InputFile photo = new InputFile();
            photo.setMedia(inputStream, String.valueOf(attachment.getOriginalId()));
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(photo)
                    .caption(message)
                    .parseMode(ParseMode.MARKDOWN)
                    .build();

            Message execute = telegramBot.execute(sendPhoto);
            changePostStatusEventPublisher.publishEvent(post, finalStatus);
            log.info("Send message with text => {}. And one attachment", message);
            MessageResponse messageResponse = new MessageResponse(post, execute);
            return List.of(messageResponse);
        } catch (IOException e) {
            log.error("Can`t download attachment with uri = {} from S3", s3Uri);
        } catch (TelegramApiException e) {
            log.error("Can't send attachment message", e);
        }
        return List.of();
    }

    private List<MessageResponse> sendMediaGroup(String chatId, Post post, String message, List<Attachment> attachments, PostStatus finalStatus) {
        List<InputMedia> medias = attachments.stream()
                .map(attachment -> {
                    String s3Uri = attachment.getS3Uri();
                    try (InputStream inputStream = s3Util.download(s3Uri)) {
                        String mediaName = UUID.randomUUID().toString();
                        byte[] allBytes = inputStream.readAllBytes();
                        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allBytes)) {
                            return (InputMedia) InputMediaPhoto.builder()
                                    .media("attach://" + mediaName)
                                    .mediaName(mediaName)
                                    .isNewMedia(true)
                                    .newMediaStream(byteArrayInputStream)
                                    .parseMode(ParseMode.MARKDOWN)
                                    .build();
                        }
                    } catch (IOException e) {
                        log.error("Can`t download attachment with uri = {} from S3", s3Uri);
                    }
                    return null;
                }).collect(Collectors.toList());

        InputMedia media = medias.get(0);
        media.setCaption(message.replaceAll("\\*", ""));

        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(chatId)
                .medias(medias)
                .build();
        try {
            List<Message> execute = telegramBot.execute(sendMediaGroup);
            changePostStatusEventPublisher.publishEvent(post, finalStatus);
            log.info("Send message with text => {}. Count of attachments => {}", message, attachments.size());
            return execute.stream().map(e -> new MessageResponse(post, e)).toList();
        } catch (TelegramApiException e) {
            log.error("Can't send attachments with media group", e);
        }
        return List.of();
    }

    private List<MessageResponse> sendText(String chatId, Post post, String message, InlineKeyboardMarkup inlineKeyboard, PostStatus finalStatus) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(message);
            sendMessage.setReplyMarkup(inlineKeyboard);
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            try {
                Message execute = telegramBot.execute(sendMessage);
                if (inlineKeyboard == null) {
                    changePostStatusEventPublisher.publishEvent(post, finalStatus);
                }
                log.info("Send message with text = {}", message);
                return List.of(new MessageResponse(post, execute));
            } catch (TelegramApiException e) {
                log.error("Can't send photos with media group", e);
            }
        return List.of();
    }

    private MessageResponse sendTextWithoutKeyboard(String message, String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            Message execute = telegramBot.execute(sendMessage);
            log.info("Send message with text = {}", message);
            return new MessageResponse(Long.parseLong(chatId), execute.getMessageId());
        } catch (TelegramApiException e) {
            log.error("Can't send photos with media group", e);
        }
        return null;
    }

    private InlineKeyboardMarkup makeInlineKeyboard(Post post) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText(post.getId() + " с BM");
        inlineKeyboardButton1.setCallbackData(post.getId() + " with");
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText(post.getId() + " без BM");
        inlineKeyboardButton2.setCallbackData(post.getId() + " without");
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText(post.getId() + " отклонить");
        inlineKeyboardButton3.setCallbackData(post.getId() + " reject");
        rowInline.add(inlineKeyboardButton1);
        rowInline.add(inlineKeyboardButton2);
        rowInline.add(inlineKeyboardButton3);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
