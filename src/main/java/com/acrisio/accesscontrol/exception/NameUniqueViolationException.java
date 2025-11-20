package com.acrisio.accesscontrol.exception;

public class NameUniqueViolationException extends RuntimeException {
    
    public  NameUniqueViolationException(String message){
        super(message);
    }
}
