package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.repository.MessageResponseRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MessageResponseServiceImpl implements MessageResponseService {

    private final MessageResponseRepository messageResponseRepository;

    public MessageResponseServiceImpl(MessageResponseRepository messageResponseRepository) {
        this.messageResponseRepository = messageResponseRepository;
    }

    @Override
    public Optional<List<MessageResponse>> findByPostId(Long postId) {
        return messageResponseRepository.findByPostId(postId);
    }

    @Override
    public List<MessageResponse> saveAll(List<MessageResponse> messageResponseList) {
        return messageResponseRepository.saveAll(messageResponseList);
    }
}
