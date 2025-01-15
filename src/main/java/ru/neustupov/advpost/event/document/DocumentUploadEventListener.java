package ru.neustupov.advpost.event.document;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.neustupov.advpost.service.PostProcessService;

@Component
public class DocumentUploadEventListener {

    private final PostProcessService postProcessService;

    public DocumentUploadEventListener(PostProcessService postProcessService) {
        this.postProcessService = postProcessService;
    }

    @Async
    @EventListener
    public void handleBotResponseEvent(DocumentUploadEvent event) {
        String documentId = event.getDocumentId();
        postProcessService.processDocument(documentId);
    }
}
