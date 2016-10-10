package com.icfolson.sling.slingmap.api.exception;

public class MappingException extends Exception {
    public MappingException() {
    }

    public MappingException(final String message) {
        super(message);
    }

    public MappingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MappingException(final Throwable cause) {
        super(cause);
    }
}
