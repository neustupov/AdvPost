package ru.neustupov.advpost.service.postgres;

import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.model.PostStatus;

import java.util.List;
import java.util.Optional;

public interface PostService {

    List<Post> processPosts(List<Post> postList);

    Optional<Post> findById(Long id);

    List<Post> saveAll(List<Post> postList);

    void changeStatus(List<Post> postList, PostStatus nextStatus);
}
