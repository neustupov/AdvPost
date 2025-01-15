package ru.neustupov.advpost.service;

import com.vk.api.sdk.objects.base.responses.OkResponse;
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.exception.AdvServiceException;
import ru.neustupov.advpost.exception.VkException;
import ru.neustupov.advpost.model.*;
import ru.neustupov.advpost.service.postgres.AttachmentService;
import ru.neustupov.advpost.service.postgres.MessageResponseService;
import ru.neustupov.advpost.service.postgres.PostService;
import ru.neustupov.advpost.service.telegram.TelegramService;
import ru.neustupov.advpost.service.vk.VkService;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AdvService {

    @Value("${chat.moderate}")
    private String moderateChatId;
    @Value("${chat.getFree}")
    private String getFreeChatId;

    private final VkService vkService;
    private final PostService postService;
    private final AttachmentService attachmentService;
    private final MessageResponseService messageResponseService;
    private final TelegramService telegramService;

    public AdvService(VkService vkService, PostService postService, AttachmentService attachmentService,
                      MessageResponseService messageResponseService, TelegramService telegramService) {
        this.vkService = vkService;
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.telegramService = telegramService;
        this.messageResponseService = messageResponseService;
    }

    @Scheduled(fixedDelayString = "${interval}")
    public void start() {

        List<Post> posts = postService.processPosts(vkService.getPosts());

        posts.forEach(post -> {
            List<Attachment> photos = attachmentService.processAttachments(post);
            attachmentService.saveAll(photos);
            post.setAttachments(photos);
        });

        List<Post> savedPostList = postService.saveAll(posts);
        log.info("Saved posts count = {}", savedPostList.size());

        messageResponseService.saveAll(sendMessagesWithDelay(savedPostList));
    }

    public void processBotResponse(Long postId, Command command) {
        boolean result = false;
        switch (command) {
            case WITH -> result = postWithWatermark(postId);
            case WITHOUT -> result = postWithoutWatermark(postId);
            case REJECT -> result = reject(postId);
        }
        if (result) {
            //тут обработка после успешного поста\удаления, нужно удалить пост в ТГ и перерисовать клаву
            messageResponseService.findByPostId(postId)
                    .ifPresent(responses ->
                            telegramService.deletePostAndKeyboard(moderateChatId,
                                    responses.stream()
                                            .map(MessageResponse::getMessageId)
                                            .toList()));
        } else {
            log.error("Result of processed command is false");
        }
    }

    public boolean postWithWatermark(Long id) {
        return postWithWatermark(checkPostIdAndGet(id));
    }

    public boolean postWithoutWatermark(Long id) {
        return postWithoutWatermark(checkPostIdAndGet(id));
    }

    public boolean reject(Long id) {
        return rejectMessage(checkPostIdAndGet(id)).getValue() == 1;
    }

    private Post checkPostIdAndGet(Long id) {
        Optional<Post> vkPost = postService.findById(id);
        if (vkPost.isPresent()) {
            return vkPost.get();
        } else {
            throw new VkException("Post with id {} is not present in database", id);
        }
    }

    private boolean postWithWatermark(Post post) {
        List<Attachment> attachments = post.getAttachments();
        List<SaveWallPhotoResponse> photo = null;
        if (attachments != null && !attachments.isEmpty()) {
            JSONObject uploadPhotos = vkService.addWatermarkAndUploadPhotoAttachment(post, vkService.getPhotosServer());
            String photoString = uploadPhotos.get("photo").toString();
            String serverString = uploadPhotos.get("server").toString();
            String hash = uploadPhotos.get("hash").toString();
            log.debug("photo -> {}\n server -> {}\n hash -> {}", uploadPhotos.get("photo"), uploadPhotos.get("server"),
                    uploadPhotos.get("hash"));
            photo = vkService.savePhoto(photoString, serverString, hash);
        }
        PostResponse postResponse = vkService.postMessageToWall(post, photo);
        if (postResponse != null && postResponse.getPostId() > 0) {
            //Удаляем пост из предложки только если у нас все ок запостилось на стену
            OkResponse deleteMessageFromSuggestedResponse = vkService.deleteMessageFromSuggested(post);
            sendMessageToTG(post);
            return true;
        }
        return false;
    }

    private Boolean postWithoutWatermark(Post post) {
        PostResponse response = vkService.postMessageFromSuggested(post);
        sendMessageToTG(post);

        //если что то пошло не так = false
        return true;
    }

    private OkResponse rejectMessage(Post post) {
        return vkService.deleteMessageFromSuggested(post);
    }

    private List<MessageResponse> sendMessagesWithDelay(List<Post> posts) {
        List<MessageResponse> messageResponseList = new ArrayList<>();
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            int postNumber = i + 1;
            String message = "(" + postNumber + " из " + posts.size() + ") ID:" + post.getId() + " " + post.getMessage();
            List<MessageResponse> responses = telegramService.sendMessage(post, message, moderateChatId, PostStatus.PUBLISHED);
            messageResponseList.addAll(responses);

            boolean isLastPost = i == posts.size() - 1;
            if (!responses.isEmpty()) {
                messageResponseList.addAll(responses);
                List<MessageResponse> keyboardResponseList = telegramService.makeInlineKeyboardAndSendMessage(post, moderateChatId);
                messageResponseList.addAll(keyboardResponseList);
            }
            //Delay 15 sec
            if (!isLastPost) {
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    throw new AdvServiceException(e.getMessage(), e);
                }
            }
        }
        return messageResponseList;
    }

    private void sendMessageToTG(Post post) {
        telegramService.sendMessage(post, vkService.getMessageWithUserDataForTg(post), getFreeChatId, PostStatus.PROCESSED);
    }
}
