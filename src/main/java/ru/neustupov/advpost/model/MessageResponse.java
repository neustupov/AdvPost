package ru.neustupov.advpost.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Message;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse extends AbstractEntity {

    private Long postId;
    private Integer messageId;

    public MessageResponse(Post post, Message message) {
        this.postId = post.getId();
        this.messageId = message.getMessageId();
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
}
