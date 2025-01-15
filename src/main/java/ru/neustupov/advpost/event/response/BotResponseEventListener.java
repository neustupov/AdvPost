package ru.neustupov.advpost.event.response;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.neustupov.advpost.model.Command;
import ru.neustupov.advpost.service.AdvService;

@Component
public class BotResponseEventListener {

    private final AdvService advService;

    public BotResponseEventListener(AdvService advService) {
        this.advService = advService;
    }

    @Async
    @EventListener
    public void handleBotResponseEvent(BotResponseEvent event) {
        Long postId = event.getId();
        Command command = event.getCommand();
        advService.processBotResponse(postId, command);
    }
}
