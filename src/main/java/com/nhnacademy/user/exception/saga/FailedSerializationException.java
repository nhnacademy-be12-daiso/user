package com.nhnacademy.user.exception.saga;

public class FailedSerializationException extends RuntimeException {
    public FailedSerializationException(String message) {
        super(message);
    }
}
