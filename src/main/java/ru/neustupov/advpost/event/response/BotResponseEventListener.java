package ru.neustupov.advpost.event.response;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.neustupov.advpost.model.Command;
import ru.neustupov.advpost.service.PostProcessService;

@Component
public class BotResponseEventListener {

    private final PostProcessService postProcessService;

    public BotResponseEventListener(PostProcessService postProcessService) {
        this.postProcessService = postProcessService;
    }

    @Async
    @EventListener
    public void handleBotResponseEvent(BotResponseEvent event) {
        Long postId = event.getId();
        Command command = event.getCommand();
        postProcessService.processBotResponse(postId, command);
    }
}
