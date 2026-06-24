package com.edu.espp.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException{
    public ConflictException(String message, HttpStatus status) {
        super(message, status);
    }
}
