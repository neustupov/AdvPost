package ru.neustupov.advpost.service.telegram.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.neustupov.advpost.event.status.ChangePostStatusEventPublisher;
import ru.neustupov.advpost.exception.TelegramServiceException;
import ru.neustupov.advpost.model.*;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;
import ru.neustupov.advpost.service.telegram.TelegramServiceImpl;
import ru.neustupov.advpost.telegram.bot.TelegramBot;
import ru.neustupov.advpost.util.S3Util;

import java.util.List;

@Slf4j
@Service
@Qualifier("ModerateService")
public class ModerateService extends TelegramServiceImpl {

    @Value("${chat.moderate}")
    private String moderateChatId;

    public ModerateService(S3Util s3Util, TelegramBot telegramBot, ChangePostStatusEventPublisher changePostStatusEventPublisher) {
        super(s3Util, telegramBot, changePostStatusEventPublisher);
    }

    @Override
    public void deletePostAndKeyboard(List<Integer> messageIds) {
        messageIds.forEach(m -> {
            DeleteMessage deleteMessages = DeleteMessage.builder()
                    .chatId(moderateChatId)
                    .messageId(m)
                    .build();
            try {
                super.telegramBot.execute(deleteMessages);
                log.info("Delete message with id = {} from chat with id = {}", m, moderateChatId);
            } catch (TelegramApiException e) {
                throw new TelegramServiceException("Can`t delete messages with id = " + m + ". Error- > " + e.getMessage(), e);
            }
        });
    }

    @Override
    public List<MessageResponse> makeInlineKeyboardAndSendMessage(Post post) {
        if (post != null) {
            return sendText(moderateChatId, post, "Выберите действие", makeInlineKeyboard(post), PostStatus.PUBLISHED);
        }
        return List.of();
    }

    @Override
    public List<MessageResponse> sendMessage(Post post, String message) {
        List<Attachment> attachments = post.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            if (attachments.size() < 2) {
                return sendUserPhoto(moderateChatId, post, message, attachments.get(0), PostStatus.PUBLISHED);
            } else {
                return sendMediaGroup(moderateChatId, post, message, attachments, PostStatus.PUBLISHED);
            }
        } else if (post.getMessage() != null && !message.isBlank()) {
            return sendText(moderateChatId, post, message, null, PostStatus.PUBLISHED);
        }
        return List.of();
    }

    @Override
    public MessageResponse sendMessage(String message) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public AdvertisingPostDTO getTextAsDTO(String documentId) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public MessageResponse sendAdvertisingResponse(AdvertisingPost advPost) {
        throw new TelegramServiceException("Method is not implemented");
    }
}
