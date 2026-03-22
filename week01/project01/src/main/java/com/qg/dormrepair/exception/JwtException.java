package com.qg.dormrepair.exception;

import lombok.Getter;

@Getter
public class JwtException extends RuntimeException{
    private final Integer code;
    public JwtException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public JwtException(String  message){
        super(message);
        this.code = 401;
    }

}
