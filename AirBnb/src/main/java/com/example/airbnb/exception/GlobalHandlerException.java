package com.example.airbnb.exception;

import com.example.airbnb.dto.ApiResponse;
import com.example.airbnb.enums.ErrorCode;
import com.stripe.exception.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

@ControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException e){
        ApiResponse apiResponse = new ApiResponse<>();
        ErrorCode errorCode = e.getErrorCode();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException e){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(404);
        apiResponse.setResult(e.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingArgumentInvalidException(MethodArgumentNotValidException e){
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setCode(400);
        apiResponse.setMessage("validation failed");
        apiResponse.setResult(
                e.getBindingResult().getAllErrors()
                        .stream().map(
                                error -> error.getDefaultMessage())
                        .toList()
        );
        return ResponseEntity.badRequest().body(apiResponse);

    }


}
