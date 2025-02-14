package ru.neustupov.advpost.exception;

public class VkException extends ServiceException{

    public VkException(Integer code, String formatter, Object... vars) {
        super(code, formatter, vars);
    }

    public VkException(String formatter, Object... vars) {
        super(formatter, vars);
    }

    public VkException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause, vars);
    }
}
