package ru.neustupov.advpost.exception;

public class DownloadServiceException extends ServiceException {

    public DownloadServiceException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public DownloadServiceException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public DownloadServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
