package com.jw.stockfetch.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateParamsException extends RuntimeException {
    public InvalidDateParamsException(String message) {
        super(message);
    }
}
