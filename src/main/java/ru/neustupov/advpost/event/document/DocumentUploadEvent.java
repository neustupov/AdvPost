package ru.neustupov.advpost.event.document;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class DocumentUploadEvent extends ApplicationEvent {

    private final String documentId;

    public DocumentUploadEvent(Object source, String documentId) {
        super(source);
        this.documentId = documentId;
    }

    public DocumentUploadEvent(Object source, Clock clock, String documentId) {
        super(source, clock);
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }
}
