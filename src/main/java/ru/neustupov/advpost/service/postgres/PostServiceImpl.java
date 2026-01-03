package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.PostStatus;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository repository;

    public PostServiceImpl(PostRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Post> processPosts(List<Post> postList) {
        List<Post> totalList = new ArrayList<>();
        postList.forEach(p -> findByHashAndStatusNotIn(p.getHash())
                .ifPresentOrElse(postFromDb -> log.info("Post with hash {} is present in DB",
                        postFromDb.getHash()), () -> totalList.add(p)));
        return totalList;
    }

    @Override
    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Post> saveAll(List<Post> postList) {
        return repository.saveAll(postList);
    }

    @Override
    public void changeStatus(List<Post> postList, PostStatus nextStatus) {
        postList.forEach(p -> {
            p.setStatus(nextStatus);
            log.info("Change status to {} for post with id = {}", nextStatus, p.getId());
        });
        this.saveAll(postList);
    }

    private Optional<Post> findByHashAndStatusNotIn(String hash) {
        return repository.findByHashAndStatus(hash).flatMap(posts -> posts.stream().findFirst());
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public void save(Post post) {
        repository.save(post);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
