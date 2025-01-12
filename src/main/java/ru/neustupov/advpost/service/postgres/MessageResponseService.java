package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.repository.MessageResponseRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MessageResponseService {

    private final MessageResponseRepository messageResponseRepository;

    public MessageResponseService(MessageResponseRepository messageResponseRepository) {
        this.messageResponseRepository = messageResponseRepository;
    }

    public Optional<List<MessageResponse>> findByPostId(Long postId) {
        return messageResponseRepository.findByPostId(postId);
    }

    public List<MessageResponse> saveAll(List<MessageResponse> messageResponseList) {
        return messageResponseRepository.saveAll(messageResponseList);
    }
}
