package ru.neustupov.advpost.exception;

public class TelegramBotException extends ServiceException {

    public TelegramBotException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public TelegramBotException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public TelegramBotException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
