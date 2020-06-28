package com.helidon.se.http;

import lombok.Getter;

@Getter
public class HttpStatusException extends Exception {

    private final int status;

    public HttpStatusException(int status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatusException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
