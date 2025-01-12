package ru.neustupov.advpost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.neustupov.advpost.model.Post;

import java.util.List;
import java.util.Optional;

@Transactional
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p.* FROM post p where p.hash = ?1 and not(p.status is null or p.status = 'NEW')", nativeQuery = true)
    Optional<List<Post>> findByHashAndStatus(String id);
}
