package ru.neustupov.advpost.service;

import com.vk.api.sdk.objects.base.responses.OkResponse;
import com.vk.api.sdk.objects.photos.responses.GetWallUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.event.response.BotResponseEvent;
import ru.neustupov.advpost.exception.VkException;
import ru.neustupov.advpost.model.*;
import ru.neustupov.advpost.service.postgres.MessageResponseService;
import ru.neustupov.advpost.service.postgres.AttachmentService;
import ru.neustupov.advpost.service.postgres.PostService;
import ru.neustupov.advpost.service.telegram.TelegramBotService;
import ru.neustupov.advpost.service.vk.VkService;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AdvService {

    @Value("${chat.notes}")
    private String notesChatId;
    @Value("${chat.getFree}")
    private String getFreeChatId;

    private final VkService vkService;
    private final PostService postService;
    private final AttachmentService attachmentService;
    private final MessageResponseService messageResponseService;
    private final TelegramBotService telegramBotService;

    public AdvService(VkService vkService, PostService postService, AttachmentService attachmentService,
                      MessageResponseService messageResponseService, TelegramBotService telegramBotService) {
        this.vkService = vkService;
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.telegramBotService = telegramBotService;
        this.messageResponseService = messageResponseService;
    }

    @Scheduled(fixedDelayString = "${interval}")
    public void start() {

        //Скачали посты из предложки
        List<Post> postList = vkService.getPosts();

        //Посмотрим в БД - если есть - пропускаем
        List<Post> posts = postService.processPosts(postList);

        //Пройдем по фото в постах - скачаем и зальем в S3
        posts.forEach(post -> {
            List<Attachment> photos = attachmentService.processAttachments(post);
            attachmentService.saveAll(photos);
            post.setAttachments(photos);
        });

        //Сохраним посты с фотками
        List<Post> savedPostList = postService.saveAll(posts);
        log.info("Saved posts count = {}", savedPostList.size());

        //Запульнем посты в ТГ
        List<MessageResponse> messageResponseList = sendMessagesWithDelay(savedPostList);
        //Сохраним ответы в БД, чтобы потом иметь возможность выпиливать сообщения в канале
        messageResponseService.saveAll(messageResponseList);
    }

    @Async
    @EventListener
    public void handleBotResponseEvent(BotResponseEvent event) {
        Long postId = event.getId();
        Command command = event.getCommand();
        boolean result = false;
        switch (command) {
            case WITH -> result = postWithWatermark(postId);
            case WITHOUT -> result = postWithoutWatermark(postId);
            case REJECT -> result = reject(postId);
        }
        if(result) {
            //тут обработка после успешного поста\удаления, нужно удалить пост в ТГ и перерисовать клаву
            messageResponseService.findByPostId(postId)
                    .ifPresent(responses -> {
                        List<Integer> list = responses.stream().map(MessageResponse::getMessageId).toList();
                        telegramBotService.deletePostAndKeyboard(notesChatId, list);
                    });
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
        if(vkPost.isPresent()) {
            return vkPost.get();
        } else {
            throw new VkException("Post with id {} is not present in database", id);
        }
    }

    private boolean postWithWatermark(Post post) {
        //Пошла жара по ВК
        List<Attachment> attachments = post.getAttachments();
        List<SaveWallPhotoResponse> photo = null;
        if(attachments != null && !attachments.isEmpty()) {
            //Получаем сервер для загрузки фото
            GetWallUploadServerResponse photosServer = vkService.getPhotosServer();
            //Загружаем фото с вотермарками на сервер
            JSONObject uploadPhotos = vkService.addWatermarkAndUploadPhotoAttachment(post, photosServer);
            String photoString = uploadPhotos.get("photo").toString();
            String serverString = uploadPhotos.get("server").toString();
            String hash = uploadPhotos.get("hash").toString();
            //Сохраняем фото в сервисном альбоме группы
            photo = vkService.savePhoto(photoString, serverString, hash);
        }
        //Отправляем пост на стену группы
        PostResponse postResponse = vkService.postMessageToWall(post, photo);
        //Выпиливаем пост из предложки
        OkResponse deleteMessageFromSuggestedResponse = vkService.deleteMessageFromSuggested(post);
        sendMessageToTG(post);

        //если что то пошло не так = false
        return true;
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
        for(int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            int postNumber = i + 1;
            String message = "(" + postNumber + " из " + posts.size() + ") ID:" + post.getId() + " " + post.getMessage();
            List<MessageResponse> responses = telegramBotService.sendMessage(post, message, notesChatId, PostStatus.PUBLISHED);
            messageResponseList.addAll(responses);

            boolean isLastPost = i == posts.size() - 1;

            //Сформируем инлайн клавиатуру и ждём откликов по постам - дальнейшая обработка после реакции в ТГ
            if(!responses.isEmpty()) {
                messageResponseList.addAll(responses);
                List<MessageResponse> keyboardResponseList = telegramBotService.makeInlineKeyboardAndSendMessage(post, notesChatId);
                messageResponseList.addAll(keyboardResponseList);
            }
            //Пауза межу постами - 15сек для всех кроме последнего
            if(!isLastPost) {
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    log.error("You not sleep NEO");
                }
            }
        }
        return messageResponseList;
    }

    private void sendMessageToTG(Post post) {
        telegramBotService.sendMessage(post, vkService.getMessageWithUserDataForTg(post), getFreeChatId, PostStatus.PROCESSED);
    }
}
