package ru.neustupov.advpost.service.postgres;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.model.Post;

import java.util.List;
import java.util.Optional;

public interface AttachmentService {

    List<Attachment> processAttachments(Post post);

    List<Attachment> saveAll(List<Attachment> attachmentList);

    List<Attachment> getOldAttachments();

    List<Attachment> findAll();

    Page<Attachment> findAll(Pageable pageable);

    Optional<Attachment> findById(Long id);

    void save(Attachment attachment);

    void deleteById(Long id);
}
