package com.example.airbnb.enums;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    //Error code here:

    EMAIL_EXISTED(302, "EMAIL ALREADY REGISTER", HttpStatus.FOUND),
    USER_NOT_FOUND(400, "USER NOT FOUND", HttpStatus.BAD_REQUEST),
    HOTEL_NOT_FOUND(404, "HOTEL NOT FOUND", HttpStatus.BAD_REQUEST),
    ROOM_NOT_FOUND(400, "ROOM NOT FOUND", HttpStatus.BAD_REQUEST),

    HOTEL_EXISTED(302, "HOTEL EXISTED", HttpStatus.FOUND),
    UNAUTHORIZED(401, "YOU ARE UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    BOOKING_NOT_FOUND(400, "BOOKING NOT AVAILABLE", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode){
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
