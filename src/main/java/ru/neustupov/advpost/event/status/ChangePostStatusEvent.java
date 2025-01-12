package ru.neustupov.advpost.event.status;

import org.springframework.context.ApplicationEvent;
import ru.neustupov.advpost.model.PostStatus;
import ru.neustupov.advpost.model.Post;

public class ChangePostStatusEvent extends ApplicationEvent {

    private final Post post;
    private final PostStatus nextStatus;

    public ChangePostStatusEvent(Object source, Post post, PostStatus nextStatus) {
        super(source);
        this.post = post;
        this.nextStatus = nextStatus;
    }

    public Post getPost() {
        return post;
    }

    public PostStatus getNextStatus() {
        return nextStatus;
    }
}
