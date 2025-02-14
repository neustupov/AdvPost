package ru.neustupov.advpost.exception;

public class AdvServiceException extends ServiceException {

    public AdvServiceException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public AdvServiceException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public AdvServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
