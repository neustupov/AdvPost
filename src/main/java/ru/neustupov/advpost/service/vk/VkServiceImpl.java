package ru.neustupov.advpost.service.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.base.responses.OkResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.photos.responses.GetWallUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse;
import com.vk.api.sdk.objects.video.VideoFiles;
import com.vk.api.sdk.objects.video.VideoFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import com.vk.api.sdk.queries.wall.WallPostQuery;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.http.impl.client.*;
import ru.neustupov.advpost.exception.VkException;
import ru.neustupov.advpost.model.AttachmentType;
import ru.neustupov.advpost.model.PostStatus;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.model.Post;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.vk.api.sdk.objects.users.Fields;
import ru.neustupov.advpost.service.watermark.WaterMarkService;
import ru.neustupov.advpost.service.watermark.WaterMarkServiceImpl;

import static com.vk.api.sdk.objects.wall.GetFilter.SUGGESTS;

@Slf4j
@Service
public class VkServiceImpl implements VkService {

    @Value("${vk.accessToken}")
    private String accessToken;
    @Value("${vk.userId}")
    private Long userId;
    @Value("${vk.groupId}")
    private Long groupId;
    @Value("${vk.domain}")
    private String domain;
    private UserActor actor;
    private VkApiClient vk;
    private final WaterMarkService waterMarkService;

    public VkServiceImpl(WaterMarkService waterMarkService) {
        this.waterMarkService = waterMarkService;
    }

    @PostConstruct
    public void setup() {
        TransportClient transportClient = new HttpTransportClient();
        vk = new VkApiClient(transportClient);
        actor = new UserActor(userId, accessToken);
    }

    @Override
    public List<Post> getPosts() {
        GetResponse getResponse;
        try {
            getResponse = vk.wall().get(actor)
                    .domain(domain)
                    .filter(SUGGESTS)
                    .extended(true)
                    .execute();
        } catch (ApiException | ClientException e) {
            throw new VkException(e.getMessage(), e);
        }

        List<Post> postList = new ArrayList<>();
        getResponse.getItems().forEach(p -> {
            List<Attachment> attachmentList = new ArrayList<>();
            p.getAttachments().forEach(a -> {
                Photo photo = a.getPhoto();
                if (photo != null) {
                    List<PhotoSizes> sizes = photo.getSizes();
                    if (sizes.size() > 0) {
                        Integer h = sizes.stream().map(PhotoSizes::getHeight).sorted(Comparator.reverseOrder()).findFirst().get();
                        Integer w = sizes.stream().map(PhotoSizes::getWidth).sorted(Comparator.reverseOrder()).findFirst().get();
                        Integer t = h > w ? h : w;
                        PhotoSizes photoSizes = sizes.stream().filter(sz -> ((sz.getHeight().equals(t)) || (sz.getWidth().equals(t)))).findFirst().get();
                        Attachment attachment = Attachment.builder()
                                .originalId(photo.getId())
                                .originalUri(photoSizes.getUrl().toString())
                                .type(AttachmentType.PHOTO)
                                .build();
                        attachmentList.add(attachment);
                    }
                }

                VideoFull video = a.getVideo();
                //TODO добавить обработку видео
                if(video != null) {
                    VideoFiles files = video.getFiles();
                }
            });
            Post post = Post.builder()
                    .originalPostId(p.getId())
                    .ownerId(p.getOwnerId())
                    .fromId(p.getFromId())
                    .message(p.getText().replaceAll("\\*", ""))
                    .attachments(attachmentList)
                    .status(PostStatus.NEW)
                    .hash(p.getHash())
                    .build();
            postList.add(post);
        });

        AtomicInteger imageCount = new AtomicInteger();
        postList.forEach(p -> imageCount.addAndGet(p.getAttachments().size()));
        log.info("Get {} photos from attachments", imageCount.get());
        return postList;
    }

    @Override
    public GetWallUploadServerResponse getPhotosServer() {
        try {
            return vk.photos().getWallUploadServer(actor)
                    .groupId(groupId)
                    .execute();
        } catch (ApiException | ClientException e) {
            throw new VkException(e.getMessage(), e);
        }
    }

    @Override
    public JSONObject addWatermarkAndUploadPhotoAttachment(Post post, GetWallUploadServerResponse photosServer) {

        JSONObject jsonResult = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost(photosServer.getUploadUrl());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            int count = 1;
            List<Attachment> attachments = post.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                for (Attachment attachment : attachments) {
                    byte[] watermarkToPhoto = waterMarkService.addImageWatermarkToPhoto(attachment);
                    builder.addBinaryBody("file" + count, watermarkToPhoto, ContentType.IMAGE_JPEG, "file" + count + ".jpg");
                    HttpEntity multipart = builder.build();
                    uploadFile.setEntity(multipart);
                    count++;
                }

                try {
                    CloseableHttpResponse response = httpClient.execute(uploadFile);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        String result;
                        while ((result = rd.readLine()) != null) {
                            jsonResult = new JSONObject(result);
                        }
                    }
                    return jsonResult;
                } catch (ClientProtocolException ex) {
                    log.error("ClientProtocolException -> {} {}", ex.getMessage(), ex);
                }
            }
        } catch (IOException e) {
            log.error("IOException -> {} {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<SaveWallPhotoResponse> savePhoto(String photoString, String serverString, String hash) {
        List<SaveWallPhotoResponse> photoResponses = null;
        try {
            photoResponses = vk.photos().saveWallPhoto(actor)
                    .groupId(groupId)
                    .photo(photoString)
                    .server(Integer.parseInt(serverString))
                    .hash(hash)
                    .execute();
            log.info("Photos saved on server");
        } catch (ApiException | ClientException e) {
            log.error(e.getMessage());
        }
        return photoResponses;
    }

    @Override
    public PostResponse postMessageToWall(Post post, List<SaveWallPhotoResponse> attachments) {
        String totalMessage = getMessageWithUserDataForVk(post);
        try {
            WallPostQuery query = vk.wall().post(actor)
                    .ownerId(-groupId)
                    .fromGroup(true)
                    .message(totalMessage);
            if (attachments != null && !attachments.isEmpty()) {
                List<String> strings = attachments.stream().map(a -> "photo" + a.getOwnerId() + "_" + a.getId()).toList();
                query.attachments(strings);
            }
            PostResponse postResponse = query.execute();
            log.info("Post message to VK with id = {}", postResponse.getPostId());
            return postResponse;
        } catch (ApiException | ClientException e) {
            log.error("Can`t post to wall for vkPost with id = {}", post.getId());
        }
        return null;
    }

    @Override
    public PostResponse postMessageFromSuggested(Post post) {
        String userDataAsUrlForVk = getMessageWithUserDataForVk(post);
        try {
            WallPostQuery query = vk.wall().post(actor)
                    .ownerId(-groupId)
                    .fromGroup(true)
                    .signed(true)
                    .message(userDataAsUrlForVk)
                    .postId(post.getOriginalPostId());
            PostResponse postResponse = query.execute();
            log.info("Post message from suggested to VK with id = {}", postResponse.getPostId());
            return postResponse;
        } catch (ApiException | ClientException e) {
            log.error("Can`t post from suggested to wall for vkPost with id = {}", post.getId());
        }
        return null;
    }

    @Override
    public OkResponse deleteMessageFromSuggested(Post post) {
        Integer originalPostId = post.getOriginalPostId();
        try {
            OkResponse response = vk.wall().delete(actor)
                    .ownerId(-groupId)
                    .postId(originalPostId)
                    .execute();
            log.info("Message with id = {} is deleted from suggested", originalPostId);
            return response;
        } catch (ApiException | ClientException e) {
            log.error("Can`t delete message with id = {} from suggested", originalPostId);
        }
        return null;
    }

    @Override
    public String getMessageWithUserDataForTg(Post post) {
        com.vk.api.sdk.objects.users.responses.GetResponse userGetResponse = getUserData(post);
        return post.getMessage() + "\n" + "[" + userGetResponse.getFirstName() + " " +
                userGetResponse.getLastName() + "](https://vk.com/" + userGetResponse.getDomain() + ")";
    }

    private String getMessageWithUserDataForVk(Post post) {
        return post.getMessage() + "\n" + getUserDataAsUrlForVk(post);
    }

    private String getUserDataAsUrlForVk(Post post) {
        com.vk.api.sdk.objects.users.responses.GetResponse userGetResponse = getUserData(post);
        return "[https://vk.com/" + userGetResponse.getDomain() + "|" +
                userGetResponse.getFirstName() + " " + userGetResponse.getLastName() + "]";
    }

    private com.vk.api.sdk.objects.users.responses.GetResponse getUserData(Post post) {
        Long fromId = post.getFromId();
        List<com.vk.api.sdk.objects.users.responses.GetResponse> userGetResponse;
        try {
            userGetResponse = new ArrayList<>(vk.users().get(actor)
                    .userIds(fromId.toString())
                    .fields(Fields.DOMAIN)
                    .execute());
        } catch (ApiException | ClientException e) {
            throw new VkException("Can`t get user info for id = {}", e, fromId);
        }
        if (!userGetResponse.isEmpty()) {
            return userGetResponse.stream().findFirst().orElse(null);
        }
        throw new VkException("User with id = {} is not present in VK service", fromId);
    }
}
