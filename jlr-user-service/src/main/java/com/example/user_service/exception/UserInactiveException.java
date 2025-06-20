package com.example.user_service.exception;

public class UserInactiveException extends RuntimeException{

    public UserInactiveException(String message){
        super(message);
    }
}
