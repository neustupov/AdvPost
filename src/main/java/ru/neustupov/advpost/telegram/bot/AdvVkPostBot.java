package ru.neustupov.advpost.telegram.bot;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.neustupov.advpost.event.response.BotResponseEventPublisher;
import ru.neustupov.advpost.model.Command;

@Slf4j
public class AdvVkPostBot extends TelegramLongPollingBot {

    private final BotResponseEventPublisher botResponseEventPublisher;

    public AdvVkPostBot(DefaultBotOptions options, String botToken, BotResponseEventPublisher botResponseEventPublisher) {
        super(options, botToken);
        this.botResponseEventPublisher = botResponseEventPublisher;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message channelPost = update.getChannelPost();
            SendMessage sendMessage = new SendMessage(channelPost.getChatId().toString(), "Chat Id is " + channelPost.getChatId());
            sendApiMethod(sendMessage);
        } else if (update.hasCallbackQuery()) {
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
    }

    @Override
    public String getBotUsername() {
        return "adv_vk_post_bot";
    }
}
