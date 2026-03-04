package com.seguradora.detran.exception;

public abstract class DetranException extends Exception {
    
    public DetranException(String message) {
        super(message);
    }
    
    public DetranException(String message, Throwable cause) {
        super(message, cause);
    }
}