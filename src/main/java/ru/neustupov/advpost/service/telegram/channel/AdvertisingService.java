package ru.neustupov.advpost.service.telegram.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.event.status.ChangePostStatusEventPublisher;
import ru.neustupov.advpost.exception.TelegramServiceException;
import ru.neustupov.advpost.model.*;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;
import ru.neustupov.advpost.service.telegram.TelegramServiceImpl;
import ru.neustupov.advpost.telegram.bot.TelegramBot;
import ru.neustupov.advpost.util.S3Util;

import java.util.List;

@Slf4j
@Service
@Qualifier("AdvertisingService")
public class AdvertisingService extends TelegramServiceImpl {

    @Value("${chat.advertising}")
    private String advertisingChatId;

    public AdvertisingService(S3Util s3Util, TelegramBot telegramBot, ChangePostStatusEventPublisher changePostStatusEventPublisher) {
        super(s3Util, telegramBot, changePostStatusEventPublisher);
    }

    @Override
    public AdvertisingPostDTO getTextAsDTO(String text) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AdvertisingPostDTO dto = objectMapper.reader().readValue(text);
            return dto;
        } catch (JsonProcessingException e) {
            throw new TelegramServiceException(e.getMessage(), e);
        }
        /*GetFile getFile = GetFile.builder()
                .fileId(documentId)
                .build();
        //TODO добавить обработку ошибок
        try {
            File file = telegramBot.execute(getFile);
            String filePath = file.getFilePath();
            try (InputStream inputStream = telegramBot.downloadFileAsStream(filePath)) {
                ObjectMapper objectMapper = new ObjectMapper();
                AdvertisingPostDTO dto = objectMapper.reader().readValue(inputStream, AdvertisingPostDTO.class);
                return dto;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (TelegramApiException e) {
            throw new TelegramServiceException(e.getMessage(), e);
        }*/
    }

    @Override
    public MessageResponse sendAdvertisingResponse(AdvertisingPost advPost) {
        String responseMessage = makeAdvResponseMessage(advPost);
        return super.sendTextWithoutKeyboard(responseMessage, advertisingChatId);
    }

    @Override
    public List<MessageResponse> sendMessage(Post post, String message) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public MessageResponse sendMessage(String message) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public List<MessageResponse> makeInlineKeyboardAndSendMessage(Post post) {
        throw new TelegramServiceException("Method is not implemented");
    }

    @Override
    public boolean sendPreparedMessage() {
        return sendText(advertisingChatId, makePreparedMessage());
    }

    @Override
    public void deletePostAndKeyboard(List<Integer> messageIds) {
        throw new TelegramServiceException("Method is not implemented");
    }

    private String makeAdvResponseMessage(AdvertisingPost advPost) {
        return "Рекламный пост создан\n" + advPost.toString() + "\n" + "Загрузите фото";
    }

    private String makePreparedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Подготовьте и отправьте json вида:").append("\n\n");
        sb.append("{\n \"message\": \"текст рекламного поста\",\n");
        sb.append("  \"token\": \"рекламный токен, полученный в ОРД\"\n");
        sb.append("  \"periodFrom\": \"дата начала публикации в формате дд.мм.гггг\"\n");
        sb.append("  \"periodTo\": \"дата окончания публикации в формате дд.мм.гггг\"\n");
        sb.append("  \"time\": \"время публикации в формате чч:мм\"\n");
        sb.append("  \"comments\": \"false - если необходимо отключить комментарии\"\n");
        sb.append("  \"repostId\": \"идентификатор сообщения для репоста\"\n}");
        return sb.toString();
    }
}
