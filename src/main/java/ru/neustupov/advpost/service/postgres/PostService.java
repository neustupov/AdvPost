package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.event.status.ChangePostStatusEvent;
import ru.neustupov.advpost.model.PostStatus;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> processPosts(List<Post> postList) {
        List<Post> totalList = new ArrayList<>();
        postList.forEach(p -> {
            String hash = p.getHash();
            findByHashAndStatusNotIn(hash)
                    .ifPresentOrElse(postFromDb -> log.info("Post with hash {} is present in DB",
                            postFromDb.getHash()), () -> totalList.add(p));
        });
        return totalList;
    }

    public Optional<Post> findByHashAndStatusNotIn(String hash) {
        return postRepository.findByHashAndStatus(hash).get().stream().findFirst();
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> saveAll(List<Post> postList) {
        return postRepository.saveAll(postList);
    }

    public void changeStatus(List<Post> postList, PostStatus nextStatus) {
        postList.forEach(p -> {
            p.setStatus(nextStatus);
            log.info("Change status to {} for post with id = {}", nextStatus, p.getId());
        });
        this.saveAll(postList);
    }

    @Async
    @EventListener
    public void handleChangePostStatusEvent(ChangePostStatusEvent event) {
        Post post = event.getPost();
        PostStatus nextStatus = event.getNextStatus();
        this.changeStatus(List.of(post), nextStatus);
    }
}
