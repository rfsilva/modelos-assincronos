package com.seguradora.detran.exception;

public class DetranTimeoutException extends DetranException {
    
    public DetranTimeoutException(String message) {
        super(message);
    }
    
    public DetranTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}