package com.example.meetingscheduler.exception;


public class ExceedCapacity extends RuntimeException{
    public ExceedCapacity() {
        super("exceed capacity");
    }

    public ExceedCapacity(String message) {
        super(message);
    }
}
