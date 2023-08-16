package com.example.meetingscheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NoSuchSourceException extends RuntimeException {

    public NoSuchSourceException() {
        super("not found");
    }
    public NoSuchSourceException(String message) {
        super(message);
    }

}
