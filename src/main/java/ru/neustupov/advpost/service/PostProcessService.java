package ru.neustupov.advpost.service;

import com.vk.api.sdk.objects.base.responses.OkResponse;
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.neustupov.advpost.exception.AdvServiceException;
import ru.neustupov.advpost.exception.VkException;
import ru.neustupov.advpost.model.*;
import ru.neustupov.advpost.model.dto.AdvertisingPhotoDTO;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;
import ru.neustupov.advpost.service.postgres.*;
import ru.neustupov.advpost.service.s3.S3Service;
import ru.neustupov.advpost.service.telegram.TelegramService;
import ru.neustupov.advpost.service.vk.VkService;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PostProcessService {

    private final VkService vkService;
    private final PostService postService;
    private final AttachmentService attachmentService;
    private final MessageResponseService messageResponseService;
    private final TelegramService advertisingService;
    private final TelegramService getFreeTelegramService;
    private final TelegramService moderateTelegramService;
    private final AdvertisingPostService advertisingPostService;
    private final S3Service s3Service;

    public PostProcessService(VkService vkService, PostService postService, AttachmentService attachmentService,
                              MessageResponseService messageResponseService,
                              @Qualifier("AdvertisingService") TelegramService advertisingService,
                              @Qualifier("GetFreeService") TelegramService getFreeTelegramService,
                              @Qualifier("ModerateService") TelegramService moderateTelegramService,
                              AdvertisingPostServiceImpl advertisingPostService,
                              S3Service s3Service) {
        this.vkService = vkService;
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.messageResponseService = messageResponseService;
        this.advertisingService = advertisingService;
        this.getFreeTelegramService = getFreeTelegramService;
        this.moderateTelegramService = moderateTelegramService;
        this.advertisingPostService = advertisingPostService;
        this.s3Service = s3Service;
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

    @Scheduled(cron = "0 0 12 * * *")
    public void deleteUnusedImage() {
        List<Attachment> oldAttachments = attachmentService.getOldAttachments();
        List<String> uriList = new ArrayList<>();
        oldAttachments.forEach(a -> {
            String s3Uri = a.getS3Uri();
            if(s3Uri.contains("watermark")) {
                uriList.add(a.getOriginalUri());
            }
            uriList.add(s3Uri);
        });
        s3Service.deleteUnusedImage(uriList);
    }

    public void processBotResponse(Long postId, Command command) {
        boolean result = false;
        if (postId != null) {
            switch (command) {
                case WITH -> result = postWithWatermark(postId);
                case WITHOUT -> result = postWithoutWatermark(postId);
                case REJECT -> result = reject(postId);
            }
            if (result) {
                messageResponseService.findByPostId(postId).ifPresent(responses ->
                        moderateTelegramService.deletePostAndKeyboard(responses.stream()
                                .map(MessageResponse::getMessageId)
                                .toList()));
            } else {
                log.error("Result of processed command is false");
            }
        } else {
            switch (command) {
                case START_ADV -> result = advertisingService.sendPreparedMessage();
            }
            if (result) {
                log.info("Command: {} is processed", command);
            } else {
                log.error("Result of processed command is false");
            }
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

    public boolean processAdvertisingPost(String text, List<PhotoSize> photoIdList, AdvertisingResponseType responseType) {
        switch (responseType) {
            case TEXT -> processAdvertisingDocument(advertisingService.getTextAsDTO(text));
            case PHOTO -> processAdvertisingPhotos(photoIdList);
        }
        return true;
    }

    private void processAdvertisingDocument(AdvertisingPostDTO document) {
        AdvertisingPost advertisingPost = advertisingPostService.processAdvertisingDocument(document);
        MessageResponse response = advertisingService.sendAdvertisingResponse(advertisingPost);
    }

    private void processAdvertisingPhotos(List<PhotoSize> photoIdList) {
        List<AdvertisingPhotoDTO> advPhotoDTOList = List.of();
        advertisingPostService.processAdvertisingPhotos(advPhotoDTOList);
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
            String message = "(" + postNumber + " из " + posts.size() + ") ID:" + post.getId() + "\n" +
                    "Дата публикации: " + post.getOriginalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) +
                    "\n" + post.getMessage();
            if (message.contains("http")) {
                message = message.substring(0, message.lastIndexOf("http"));
            }
            List<MessageResponse> responses = moderateTelegramService.sendMessage(post, message);
            messageResponseList.addAll(responses);

            boolean isLastPost = i == posts.size() - 1;
            if (!responses.isEmpty()) {
                messageResponseList.addAll(responses);
                List<MessageResponse> keyboardResponseList = moderateTelegramService.makeInlineKeyboardAndSendMessage(post);
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
        getFreeTelegramService.sendMessage(post, vkService.getMessageWithUserDataForTg(post));
    }
}
