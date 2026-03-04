package com.seguradora.detran.exception;

public class DetranDadosInvalidosException extends DetranException {
    
    public DetranDadosInvalidosException(String message) {
        super(message);
    }
    
    public DetranDadosInvalidosException(String message, Throwable cause) {
        super(message, cause);
    }
}