package ru.neustupov.advpost.event.document;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DocumentUploadEventPublisher implements ApplicationEventPublisherAware  {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(final String documentId) {
        log.info("Publishing DocumentUploadEvent");
        applicationEventPublisher.publishEvent(new DocumentUploadEvent(this, documentId));
    }
}
