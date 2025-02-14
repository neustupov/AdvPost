package ru.neustupov.advpost.exception;

public abstract class ServiceException extends RuntimeException {

    private final transient Object[] vars;
    private Integer code;

    public ServiceException(Integer code, String formatter, Object... vars) {
        super(formatter);
        this.code = code;
        this.vars = vars;
    }

    public ServiceException(String formatter, Object... vars) {
        super(formatter);
        this.vars = vars;
    }

    public ServiceException(String formatter, Throwable cause, Object... vars) {
        super(formatter, cause);
        this.vars = vars;
    }

    public String getMessage() {
        String formatter = super.getMessage();
        return format(formatter, this.vars);
    }

    protected static String format(String message, Object... vars) {
        return vars != null && vars.length != 0 ? String.format(message.replace("{}", "%s"), vars) : message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
