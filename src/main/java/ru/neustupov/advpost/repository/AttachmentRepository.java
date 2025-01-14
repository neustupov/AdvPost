package ru.neustupov.advpost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.neustupov.advpost.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

}
