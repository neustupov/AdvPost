package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.repository.MessageResponseRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MessageResponseServiceImpl implements MessageResponseService {

    private final MessageResponseRepository repository;

    public MessageResponseServiceImpl(MessageResponseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<List<MessageResponse>> findByPostId(Long postId) {
        return repository.findByPostId(postId);
    }

    @Override
    public List<MessageResponse> saveAll(List<MessageResponse> messageResponseList) {
        return repository.saveAll(messageResponseList);
    }

    @Override
    public Optional<MessageResponse> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<MessageResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public void save(MessageResponse messageResponse) {
        repository.save(messageResponse);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
