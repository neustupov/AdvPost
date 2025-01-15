package ru.neustupov.advpost.exception.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.neustupov.advpost.exception.*;
import ru.neustupov.advpost.service.telegram.TelegramService;

@ControllerAdvice
public class CommonExceptionHandler {

    @Value("${chat.exception}")
    private String exceptionChatId;

    private final TelegramService telegramService;

    public CommonExceptionHandler(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @ExceptionHandler({VkException.class, DownloadServiceException.class, AttachmentServiceException.class,
            S3ServiceException.class, TelegramServiceException.class})
    public Exception handleVkException(Exception exception) {
        telegramService.sendMessage(exception.getMessage(), exceptionChatId);
        return exception;
    }
}
