package ru.neustupov.advpost.event.response;

import org.springframework.context.ApplicationEvent;
import ru.neustupov.advpost.model.Command;

public class BotResponseEvent extends ApplicationEvent {

    private final Long id;
    private final Command command;

    public BotResponseEvent(Object source, Long id, Command command) {
        super(source);
        this.id = id;
        this.command = command;
    }

    public Long getId() {
        return id;
    }

    public Command getCommand() {
        return command;
    }
}
