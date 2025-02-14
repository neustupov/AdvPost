package ru.neustupov.advpost.exception;

public class AttachmentServiceException extends ServiceException {

    public AttachmentServiceException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public AttachmentServiceException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public AttachmentServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
