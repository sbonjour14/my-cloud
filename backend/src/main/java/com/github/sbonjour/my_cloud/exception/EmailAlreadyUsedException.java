package com.github.sbonjour.my_cloud.exception;

public class EmailAlreadyUsedException extends RuntimeException{
    public EmailAlreadyUsedException(String message) { super(message); }
}
