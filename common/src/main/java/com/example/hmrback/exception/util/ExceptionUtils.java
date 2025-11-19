package com.example.hmrback.exception.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static ResponseEntity<ApiError> buildResponseEntity(HttpStatus status, String message, String path) {
        ApiError error = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
        return new ResponseEntity<>(error, status);
    }
}
