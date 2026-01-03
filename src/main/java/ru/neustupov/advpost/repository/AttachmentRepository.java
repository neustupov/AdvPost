package ru.neustupov.advpost.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.neustupov.advpost.model.Attachment;

import java.time.LocalDateTime;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByCreatedBefore(LocalDateTime localDateTime);

    List<Attachment> findAll();

    Page<Attachment> findAll(Pageable pageable);
}
