package ru.neustupov.advpost.service.telegram;

import org.json.JSONObject;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.model.PostStatus;

import java.util.List;

public interface TelegramService {

    List<MessageResponse> sendMessage(Post post, String message, String chatId, PostStatus finalStatus);

    MessageResponse sendMessage(String message, String chatId);

    List<MessageResponse> makeInlineKeyboardAndSendMessage(Post post, String chatId);

    void deletePostAndKeyboard(String chatId, List<Integer> messageIds);

    JSONObject getDocumentAsJson(String documentId);
}
