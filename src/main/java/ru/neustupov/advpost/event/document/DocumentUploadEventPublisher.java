package ru.neustupov.advpost.event.document;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.neustupov.advpost.model.AdvertisingResponseType;

import java.util.List;

@Slf4j
@Component
public class DocumentUploadEventPublisher implements ApplicationEventPublisherAware  {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(final String text, final List<PhotoSize> postPhoto) {
        AdvertisingResponseType responseType = null;
        if(text != null && !text.isBlank()) {
            responseType = AdvertisingResponseType.TEXT;
            log.info("Publishing DocumentUploadEvent for Text");
        } else if(postPhoto != null && !postPhoto.isEmpty()) {
            responseType = AdvertisingResponseType.PHOTO;
            log.info("Publishing DocumentUploadEvent for Photo");
        }
        applicationEventPublisher.publishEvent(new DocumentUploadEvent(this, text, postPhoto, responseType));
    }
}
