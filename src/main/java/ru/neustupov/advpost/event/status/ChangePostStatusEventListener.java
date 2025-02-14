package ru.neustupov.advpost.event.status;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.model.PostStatus;
import ru.neustupov.advpost.service.postgres.PostService;

import java.util.List;

@Component
public class ChangePostStatusEventListener {

    private final PostService postService;

    public ChangePostStatusEventListener(PostService postService) {
        this.postService = postService;
    }

    @Async
    @EventListener
    public void handleChangePostStatusEvent(ChangePostStatusEvent event) {
        Post post = event.getPost();
        PostStatus nextStatus = event.getNextStatus();
        postService.changeStatus(List.of(post), nextStatus);
    }
}
