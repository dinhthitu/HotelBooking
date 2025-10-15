package com.example.airbnb.exception;

import com.example.airbnb.enums.ErrorCode;
import com.stripe.model.tax.Registration;
import lombok.Data;

@Data
public class AppException extends RuntimeException{
    private ErrorCode errorCode;
    public AppException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
