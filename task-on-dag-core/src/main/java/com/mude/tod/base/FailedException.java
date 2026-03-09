package com.mude.tod.base;

public class FailedException extends RuntimeException{

    public FailedException(String message){
        super(message);
    }

    public FailedException(String message, Throwable cause){
        super(message,cause);
    }

    public FailedException(Throwable cause){
        super(cause);
    }
}
