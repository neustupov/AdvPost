package ru.neustupov.advpost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.neustupov.advpost.model.MessageResponse;

import java.util.List;
import java.util.Optional;

public interface MessageResponseRepository extends JpaRepository<MessageResponse, Long> {

    Optional<List<MessageResponse>> findByPostId(Long postId);
}
