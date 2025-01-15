package ru.neustupov.advpost.telegram.configuration;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.neustupov.advpost.event.response.BotResponseEventPublisher;
import ru.neustupov.advpost.telegram.bot.TelegramBot;

@Configuration
public class TelegramBotConfiguration {

    @SneakyThrows
    @Bean
    public TelegramBot telegramBot(@Value("${bot.key}") String botToken, TelegramBotsApi telegramBotsApi,
                                   BotResponseEventPublisher botResponseEventPublisher) {
        var botOptions = new DefaultBotOptions();
        var bot =  new TelegramBot(botOptions, botToken, botResponseEventPublisher);
        telegramBotsApi.registerBot(bot);
        return bot;
    }

    @SneakyThrows
    @Bean
    public TelegramBotsApi telegramBotsApi() {
        return new TelegramBotsApi(DefaultBotSession.class);
    }
}
