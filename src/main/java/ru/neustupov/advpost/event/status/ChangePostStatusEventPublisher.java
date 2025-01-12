package ru.neustupov.advpost.event.status;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import ru.neustupov.advpost.model.PostStatus;
import ru.neustupov.advpost.model.Post;

@Slf4j
@Component
public class ChangePostStatusEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(final Post post, final PostStatus nextStatus) {
        log.info("Publishing ChangePostStatusEvent event");
        applicationEventPublisher.publishEvent(new ChangePostStatusEvent(this, post, nextStatus));
    }
}
