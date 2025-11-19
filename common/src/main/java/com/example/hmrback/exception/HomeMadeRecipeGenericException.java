package com.example.hmrback.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class HomeMadeRecipeGenericException extends RuntimeException {

    private final HttpStatus status;
    private final LogLevel logLevel;
    private final String message;

    public HomeMadeRecipeGenericException(String message, Throwable cause, HttpStatus status, LogLevel logLevel) {
        super(message, cause);
        this.status = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        this.logLevel = logLevel != null ? logLevel : LogLevel.ERROR;
        this.message = message;
    }
}
