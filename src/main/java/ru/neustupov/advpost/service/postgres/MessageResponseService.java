package ru.neustupov.advpost.service.postgres;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.neustupov.advpost.model.MessageResponse;

import java.util.List;
import java.util.Optional;

public interface MessageResponseService {

    Optional<List<MessageResponse>> findByPostId(Long postId);

    List<MessageResponse> saveAll(List<MessageResponse> messageResponseList);

    Optional<MessageResponse> findById(Long id);

    Page<MessageResponse> findAll(Pageable pageable);

    void save(MessageResponse messageResponse);

    void deleteById(Long id);
}
