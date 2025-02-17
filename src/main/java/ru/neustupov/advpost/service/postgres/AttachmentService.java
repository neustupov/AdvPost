package ru.neustupov.advpost.service.postgres;

import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.model.Post;

import java.util.List;

public interface AttachmentService {

    List<Attachment> processAttachments(Post post);

    List<Attachment> saveAll(List<Attachment> attachmentList);

    List<Attachment> getOldAttachments();
}
