package com.seguradora.detran.exception;

public class DetranIndisponivelException extends DetranException {
    
    public DetranIndisponivelException(String message) {
        super(message);
    }
    
    public DetranIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}