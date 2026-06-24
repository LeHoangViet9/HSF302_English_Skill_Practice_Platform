package com.edu.espp.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException{
    public BadRequestException(String message, HttpStatus status) {
        super(message, status);
    }
}
