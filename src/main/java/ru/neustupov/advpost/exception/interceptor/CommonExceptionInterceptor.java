package ru.neustupov.advpost.exception.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import ru.neustupov.advpost.service.telegram.TelegramService;
import ru.neustupov.advpost.service.telegram.channel.ExceptionService;

@Slf4j
@Configuration
@Aspect
public class CommonExceptionInterceptor {

    private final TelegramService telegramService;

    public CommonExceptionInterceptor(ExceptionService telegramService) {
        this.telegramService = telegramService;
    }

    @AfterThrowing(pointcut = "execution(* ru.neustupov.advpost.*.*.*(..))", throwing = "ex")
    public void parallelExecuteBeforeAndAfterCompose(Exception ex) {
        telegramService.sendMessage(ex.getMessage());
        log.error("Handle error: {}", ex.getMessage());
    }
}
