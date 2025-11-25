package ru.neustupov.advpost.service.telegram.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.event.status.ChangePostStatusEventPublisher;
import ru.neustupov.advpost.exception.TelegramServiceException;
import ru.neustupov.advpost.model.AdvertisingPost;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;
import ru.neustupov.advpost.service.telegram.TelegramServiceImpl;
import ru.neustupov.advpost.telegram.bot.TelegramBot;
import ru.neustupov.advpost.util.S3Util;

import java.util.List;

@Slf4j
@Service
@Qualifier("ExceptionService")
public class ExceptionService extends TelegramServiceImpl {

    @Value("${chat.exception}")
    private String exceptionChatId;

    public ExceptionService(S3Util s3Util, TelegramBot telegramBot, ChangePostStatusEventPublisher changePostStatusEventPublisher) {
        super(s3Util, telegramBot, changePostStatusEventPublisher);
    }

    @Override
    public MessageResponse sendMessage(String message) {
        return super.sendTextWithoutKeyboard(message, exceptionChatId);
    }

    @Override
    public List<MessageResponse> sendMessage(Post post, String message) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public List<MessageResponse> makeInlineKeyboardAndSendMessage(Post post) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public boolean sendPreparedMessage() {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public void deletePostAndKeyboard(List<Integer> messageIds) {
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
