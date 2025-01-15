package ru.neustupov.advpost.service.vk;

import com.vk.api.sdk.objects.base.responses.OkResponse;
import com.vk.api.sdk.objects.photos.responses.GetWallUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import org.json.JSONObject;
import ru.neustupov.advpost.model.Post;

import java.util.List;

public interface VkService {

    List<Post> getPosts();

    GetWallUploadServerResponse getPhotosServer();

    JSONObject addWatermarkAndUploadPhotoAttachment(Post post, GetWallUploadServerResponse photosServer);

    List<SaveWallPhotoResponse> savePhoto(String photoString, String serverString, String hash);

    PostResponse postMessageToWall(Post post, List<SaveWallPhotoResponse> attachments);

    PostResponse postMessageFromSuggested(Post post);

    OkResponse deleteMessageFromSuggested(Post post);

    String getMessageWithUserDataForTg(Post post);
}
