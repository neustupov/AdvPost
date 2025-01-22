package ru.neustupov.advpost.exception.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.neustupov.advpost.exception.*;
import ru.neustupov.advpost.service.telegram.TelegramService;
import ru.neustupov.advpost.service.telegram.channel.ExceptionService;

@ControllerAdvice
public class CommonExceptionHandler {

    private final TelegramService telegramService;

    public CommonExceptionHandler(ExceptionService telegramService) {
        this.telegramService = telegramService;
    }

    @ExceptionHandler({VkException.class, DownloadServiceException.class, AttachmentServiceException.class,
            S3ServiceException.class, TelegramServiceException.class, AdvServiceException.class,
            WaterMarkServiceException.class, AdvertisingServiceException.class})
    public Exception handleVkException(Exception exception) {
        telegramService.sendMessage(exception.getMessage());
        return exception;
    }
}
