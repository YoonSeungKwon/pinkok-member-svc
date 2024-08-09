package com.pinkok.memberService.exception;

import org.springframework.http.HttpStatus;

public class FriendException extends RuntimeException{

    private final String message;

    private final HttpStatus status;

    public String getMessage(){
        return this.message;
    }

    public HttpStatus getStatus(){
        return this.status;
    }

    public FriendException(String message, HttpStatus status){
        this.message = message;
        this.status = status;
    }

}
