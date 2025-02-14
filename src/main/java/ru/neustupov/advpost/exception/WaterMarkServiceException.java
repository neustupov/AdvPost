package ru.neustupov.advpost.exception;

public class WaterMarkServiceException extends ServiceException {

    public WaterMarkServiceException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public WaterMarkServiceException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public WaterMarkServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
