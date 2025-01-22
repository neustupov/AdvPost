package ru.neustupov.advpost.event.document;

import org.springframework.context.ApplicationEvent;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.neustupov.advpost.model.AdvertisingResponseType;

import java.time.Clock;
import java.util.List;

public class DocumentUploadEvent extends ApplicationEvent {

    private final String text;
    private final List<PhotoSize> photoIdList;
    private final AdvertisingResponseType responseType;

    public DocumentUploadEvent(Object source, String text, List<PhotoSize> photoIdList, AdvertisingResponseType responseType) {
        super(source);
        this.text = text;
        this.photoIdList = photoIdList;
        this.responseType = responseType;
    }

    public DocumentUploadEvent(Object source, Clock clock, String text, List<PhotoSize> photoIdList, AdvertisingResponseType responseType) {
        super(source, clock);
        this.text = text;
        this.photoIdList = photoIdList;
        this.responseType = responseType;
    }

    public String getText() {
        return text;
    }

    public List<PhotoSize> getPhotoIdList() {
        return photoIdList;
    }

    public AdvertisingResponseType getResponseType() {
        return responseType;
    }
}
