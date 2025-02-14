package ru.neustupov.advpost.exception;

public class TelegramServiceException extends ServiceException {

    public TelegramServiceException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public TelegramServiceException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public TelegramServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
