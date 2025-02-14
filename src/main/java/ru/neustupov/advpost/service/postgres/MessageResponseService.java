package ru.neustupov.advpost.service.postgres;

import ru.neustupov.advpost.model.MessageResponse;

import java.util.List;
import java.util.Optional;

public interface MessageResponseService {

    Optional<List<MessageResponse>> findByPostId(Long postId);

    List<MessageResponse> saveAll(List<MessageResponse> messageResponseList);
}
