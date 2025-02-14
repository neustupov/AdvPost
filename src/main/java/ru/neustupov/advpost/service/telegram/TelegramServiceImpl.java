package ru.neustupov.advpost.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.neustupov.advpost.event.status.ChangePostStatusEventPublisher;
import ru.neustupov.advpost.exception.TelegramServiceException;
import ru.neustupov.advpost.model.*;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;
import ru.neustupov.advpost.util.S3Util;
import ru.neustupov.advpost.telegram.bot.TelegramBot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public abstract class TelegramServiceImpl implements TelegramService {

    protected final S3Util s3Util;
    protected final TelegramBot telegramBot;
    protected final ChangePostStatusEventPublisher changePostStatusEventPublisher;

    public TelegramServiceImpl(S3Util s3Util, TelegramBot telegramBot, ChangePostStatusEventPublisher changePostStatusEventPublisher) {
        this.s3Util = s3Util;
        this.telegramBot = telegramBot;
        this.changePostStatusEventPublisher = changePostStatusEventPublisher;
    }

    /*@Override
    public List<MessageResponse> sendMessage(Post post, String message) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public MessageResponse sendMessage(String message) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public List<MessageResponse> makeInlineKeyboardAndSendMessage(Post post) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public void deletePostAndKeyboard(List<Integer> messageIds) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public AdvertisingPostDTO getDocumentAsDTO(String documentId) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public MessageResponse sendAdvertisingResponse(AdvertisingPost advPost) {
        throw new TelegramServiceException("Method is not implemented");
    }*/

    protected List<MessageResponse> sendUserPhoto(String chatId, Post post, String message, Attachment attachment, PostStatus finalStatus) {
        String s3Uri = attachment.getS3Uri();
        try (InputStream inputStream = s3Util.download(s3Uri)) {
            InputFile photo = new InputFile();
            photo.setMedia(inputStream, String.valueOf(attachment.getOriginalId()));
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(photo)
                    .caption(message.length() < 200 ? message : message.substring(0, 200))
                    .parseMode(ParseMode.MARKDOWN)
                    .build();

            Message execute = telegramBot.execute(sendPhoto);
            changePostStatusEventPublisher.publishEvent(post, finalStatus);
            log.info("Send message with text => {}. And one attachment", message);
            MessageResponse messageResponse = new MessageResponse(post, execute);
            return List.of(messageResponse);
        } catch (IOException e) {
            throw new TelegramServiceException("Can`t download attachment with uri = " + s3Uri + " from S3. Error- > " + e.getMessage(), e);
        } catch (TelegramApiException e) {
            throw new TelegramServiceException("Can't send attachment message. Error- > " + e.getMessage(), e);
        }
    }

    protected List<MessageResponse> sendMediaGroup(String chatId, Post post, String message, List<Attachment> attachments, PostStatus finalStatus) {
        List<InputMedia> medias = attachments.stream()
                .map(attachment -> {
                    String s3Uri = attachment.getS3Uri();
                    try (InputStream inputStream = s3Util.download(s3Uri)) {
                        String mediaName = UUID.randomUUID().toString();
                        byte[] allBytes = inputStream.readAllBytes();
                        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allBytes)) {
                            return (InputMedia) InputMediaPhoto.builder()
                                    .media("attach://" + mediaName)
                                    .mediaName(mediaName)
                                    .isNewMedia(true)
                                    .newMediaStream(byteArrayInputStream)
                                    .parseMode(ParseMode.MARKDOWN)
                                    .build();
                        }
                    } catch (IOException e) {
                        throw new TelegramServiceException("Can`t download attachment with uri = " + s3Uri + " from S3. Error- > " + e.getMessage(), e);
                    }
                }).collect(Collectors.toList());

        InputMedia media = medias.get(0);
        String normalSizeMessage = message.length() > 200 ? message.substring(0, 200) : message;
        media.setCaption(normalSizeMessage.replaceAll("\\*", "-"));

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
            throw new TelegramServiceException("Can't send attachments with media group. Error- > " + e.getMessage(), e);
        }
    }

    protected List<MessageResponse> sendText(String chatId, Post post, String message, InlineKeyboardMarkup inlineKeyboard, PostStatus finalStatus) {
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
            throw new TelegramServiceException("Can't send photos with media group. Error- > " + e.getMessage(), e);
        }
    }

    protected boolean sendText(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.setReplyToMessageId(1);
        try {
            Message execute = telegramBot.execute(sendMessage);
            log.info("Send message with text = {}", message);
            return execute.getMessageId() != null;
        } catch (TelegramApiException e) {
            throw new TelegramServiceException("Can't send photos with media group. Error- > " + e.getMessage(), e);
        }
    }

    protected MessageResponse sendTextWithoutKeyboard(String message, String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            Message execute = telegramBot.execute(sendMessage);
            log.info("Send message with text = {}", message);
            return new MessageResponse(Long.parseLong(chatId), execute.getMessageId());
        } catch (TelegramApiException e) {
            throw new TelegramServiceException("Can't send photos with media group. Error- > " + e.getMessage(), e);
        }
    }

    protected InlineKeyboardMarkup makeInlineKeyboard(Post post) {
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
