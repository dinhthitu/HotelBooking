package com.example.airbnb.controller;

import com.example.airbnb.dto.ApiResponse;
import com.example.airbnb.dto.request.LoginRequest;
import com.example.airbnb.dto.request.SignupRequest;
import com.example.airbnb.dto.response.LoginResponse;
import com.example.airbnb.dto.response.SignupResponse;
import com.example.airbnb.service.AuthSevice;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {


    // da test postman oke//
    AuthSevice authSevice;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup (@Valid @RequestBody SignupRequest request){
        return ApiResponse.<SignupResponse>builder()
                .result(authSevice.signup(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login (@Valid @RequestBody LoginRequest request, HttpServletResponse response){
        LoginResponse tokens = authSevice.login(request);
        Cookie cookie = new Cookie("refreshToken", tokens.getRefreshToken());
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ApiResponse.<LoginResponse>builder()
                .result(LoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .build())
                .build();
    }

    @PostMapping("/registerAsAdmin")
    public ApiResponse<SignupResponse> signupAsAdmin (@Valid @RequestBody SignupRequest request){
     return ApiResponse.<SignupResponse>builder()
             .result(authSevice.signupAsAdmin(request))
             .build();
    }

    @PostMapping("/registerAsHotelManager")
    public ApiResponse<SignupResponse> signupAsHotelManager (@Valid @RequestBody SignupRequest request){
        return ApiResponse.<SignupResponse>builder()
                .result(authSevice.signupAsHotelManager(request))
                .build();
    }

    @PostMapping("/refreshToken")
    public ApiResponse<String> refreshToken(HttpServletRequest request){
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));
        return ApiResponse.<String>builder()
                .result(authSevice.refreshToken(refreshToken))
                .build();
    }

}
