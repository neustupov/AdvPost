package ru.neustupov.advpost.event.document;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.neustupov.advpost.model.AdvertisingResponseType;
import ru.neustupov.advpost.service.PostProcessService;

import java.util.List;

@Component
public class DocumentUploadEventListener {

    private final PostProcessService postProcessService;

    public DocumentUploadEventListener(PostProcessService postProcessService) {
        this.postProcessService = postProcessService;
    }

    @Async
    @EventListener
    public void handleBotResponseEvent(DocumentUploadEvent event) {
        String text = event.getText();
        List<PhotoSize> photoIdList = event.getPhotoIdList();
        AdvertisingResponseType responseType = event.getResponseType();
        postProcessService.processAdvertisingPost(text, photoIdList, responseType);
    }
}
