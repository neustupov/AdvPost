package ru.neustupov.advpost.exception;

public class AdvertisingServiceException extends ServiceException {

    public AdvertisingServiceException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public AdvertisingServiceException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public AdvertisingServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
