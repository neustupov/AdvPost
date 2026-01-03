package ru.neustupov.advpost.service.postgres;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.model.PostStatus;

import java.util.List;
import java.util.Optional;

public interface PostService {

    List<Post> processPosts(List<Post> postList);

    Optional<Post> findById(Long id);

    List<Post> saveAll(List<Post> postList);

    void changeStatus(List<Post> postList, PostStatus nextStatus);

    Page<Post> findAll(Pageable pageable);

    void save(Post post);

    void deleteById(Long id);
}
