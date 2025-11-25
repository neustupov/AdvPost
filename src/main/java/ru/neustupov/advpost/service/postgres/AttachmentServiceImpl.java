package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.exception.AttachmentServiceException;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.model.AttachmentType;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.repository.AttachmentRepository;
import ru.neustupov.advpost.service.file.DownloadService;
import ru.neustupov.advpost.util.S3Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class AttachmentServiceImpl implements AttachmentService {

    public static final String PHOTO_TYPE = "image/jpeg";
    public static final String VIDEO_TYPE = "video/x-msvideo";
    public static final Long DAYS = 7L;
    private final AttachmentRepository attachmentRepository;
    private final DownloadService downloadService;
    private final S3Util s3Util;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository, DownloadService downloadService, S3Util s3Util) {
        this.attachmentRepository = attachmentRepository;
        this.downloadService = downloadService;
        this.s3Util = s3Util;
    }

    @Override
    public List<Attachment> processAttachments(Post post) {
        List<Attachment> attachmentList = post.getAttachments();
        if(attachmentList != null && !attachmentList.isEmpty()) {
            attachmentList.forEach(attachment -> {
                try (InputStream stream = downloadService.downloadAsInputStream(URI.create(attachment.getOriginalUri()))) {
                    String fileName = null;
                    String upload = null;
                    if(attachment.getType().equals(AttachmentType.PHOTO)) {
                        fileName = "photo_" + attachment.getOriginalId() + ".jpg";
                        upload = s3Util.upload(fileName, stream, PHOTO_TYPE);
                    } else if(attachment.getType().equals(AttachmentType.VIDEO)) {
                        fileName = "video_" + attachment.getOriginalId() + ".avi";
                        upload = s3Util.upload(fileName, stream, VIDEO_TYPE);
                    }
                    attachment.setName(fileName);
                    attachment.setS3Uri(upload);
                } catch (IOException e) {
                    throw new AttachmentServiceException(e.getMessage(), e);
                }
            });
        }
        return attachmentList;
    }

    @Override
    public List<Attachment> saveAll(List<Attachment> attachmentList) {
        return attachmentRepository.saveAll(attachmentList);
    }

    @Override
    public List<Attachment> getOldAttachments() {
        LocalDateTime dateTime = LocalDateTime.now().minus(DAYS, ChronoUnit.DAYS);
        List<Attachment> attachments = attachmentRepository.findByCreatedBefore(dateTime);
        return attachments;
    }
}
