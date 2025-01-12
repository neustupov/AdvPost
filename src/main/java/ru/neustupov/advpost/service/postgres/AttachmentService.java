package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.repository.AttachmentRepository;
import ru.neustupov.advpost.service.file.DownloadService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final DownloadService downloadService;

    public AttachmentService(AttachmentRepository attachmentRepository, DownloadService downloadService) {
        this.attachmentRepository = attachmentRepository;
        this.downloadService = downloadService;
    }

    public List<Attachment> downloadPhotoAndSetHash(Post post) {
        List<Attachment> attachmentList = post.getAttachments();
        List<Attachment> attachments = new ArrayList<>();
        attachmentList.forEach(attachment -> {
            File file = downloadService.downloadFile(attachment.getOriginalId(), URI.create(attachment.getOriginalUri()));
            attachment.setName(file.getName());
            try {
                attachment.setData(Files.readAllBytes(file.toPath()));
                attachmentRepository.findByHash(attachment.getHash()).ifPresentOrElse(photoFromDB -> {
                    photoFromDB.setData(attachment.getData());
                    attachments.add(photoFromDB);
                    log.info("Attachment with hash {} is present in DB", photoFromDB.getHash());
                }, () -> attachments.add(attachment));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return attachments;
    }

    public List<Attachment> saveAll(List<Attachment> attachmentList) {
        return attachmentRepository.saveAll(attachmentList);
    }
}
