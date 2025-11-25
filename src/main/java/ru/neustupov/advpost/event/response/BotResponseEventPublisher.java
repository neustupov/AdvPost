package ru.neustupov.advpost.event.response;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import ru.neustupov.advpost.model.Command;

@Slf4j
@Component
public class BotResponseEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(final Long id, final Command command) {
        log.info("Publishing BotResponseEvent");
        applicationEventPublisher.publishEvent(new BotResponseEvent(this, id, command));
    }

    public void publishEvent(final Command command) {
        log.info("Publishing BotResponseEvent for start advertising message");
        applicationEventPublisher.publishEvent(new BotResponseEvent(this, null, command));
    }

}
