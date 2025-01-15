package ru.neustupov.advpost.exception;

public class S3ServiceException extends ServiceException {

    public S3ServiceException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public S3ServiceException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public S3ServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
