package ru.neustupov.advpost.telegram.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.neustupov.advpost.event.document.DocumentUploadEventPublisher;
import ru.neustupov.advpost.event.response.BotResponseEventPublisher;
import ru.neustupov.advpost.exception.TelegramBotException;
import ru.neustupov.advpost.model.Command;

import java.util.List;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${chat.advertising}")
    private Long advertisingChatId;

    private final BotResponseEventPublisher botResponseEventPublisher;
    private final DocumentUploadEventPublisher documentUploadEventPublisher;

    public TelegramBot(DefaultBotOptions options, String botToken, BotResponseEventPublisher botResponseEventPublisher,
                       DocumentUploadEventPublisher documentUploadEventPublisher) {
        super(options, botToken);
        this.botResponseEventPublisher = botResponseEventPublisher;
        this.documentUploadEventPublisher = documentUploadEventPublisher;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            processChatId(update);
        } else if (update.hasCallbackQuery()) {
            processCallback(update);
        } else if (update.hasChannelPost() && update.getChannelPost().getChatId().equals(advertisingChatId)) {
            processAdvertisingMessage(update);
        }
    }

    @Override
    public String getBotUsername() {
        return "adv_vk_post_bot";
    }

    private void processChatId(Update update) {
        Message channelPost = update.getChannelPost();
        SendMessage sendMessage = new SendMessage(channelPost.getChatId().toString(), "Chat Id is " + channelPost.getChatId());
        try {
            sendApiMethod(sendMessage);
        } catch (TelegramApiException e) {
            throw new TelegramBotException(e.getMessage(), e);
        }
    }

    private void processCallback(Update update) {
        String call_data = update.getCallbackQuery().getData();
        String[] strings = call_data.split(" ");
        String id = strings[0];
        String command = strings[1];

        Long idLong = Long.valueOf(id);
        switch (command) {
            case "with" -> botResponseEventPublisher.publishEvent(idLong, Command.WITH);
            case "without" -> botResponseEventPublisher.publishEvent(idLong, Command.WITHOUT);
            case "reject" -> botResponseEventPublisher.publishEvent(idLong, Command.REJECT);
        }
    }

    private void processAdvertisingMessage(Update update) {
        Message channelPost = update.getChannelPost();
        String postText = channelPost.getText();
        //Document document = channelPost.getDocument();
        List<PhotoSize> postPhoto = channelPost.getPhoto();
        documentUploadEventPublisher.publishEvent(postText, postPhoto);
    }
}
