package ru.neustupov.advpost.service.telegram;

import ru.neustupov.advpost.model.AdvertisingPost;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;

import java.util.List;

public interface TelegramService {

    List<MessageResponse> sendMessage(Post post, String message);

    MessageResponse sendMessage(String message);

    List<MessageResponse> makeInlineKeyboardAndSendMessage(Post post);

    void deletePostAndKeyboard(List<Integer> messageIds);

    AdvertisingPostDTO getTextAsDTO(String text);

    MessageResponse sendAdvertisingResponse(AdvertisingPost advPost);
}
