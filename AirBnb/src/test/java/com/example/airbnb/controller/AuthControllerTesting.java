package com.example.airbnb.controller;

import com.example.airbnb.dto.request.LoginRequest;
import com.example.airbnb.dto.request.SignupRequest;
import com.example.airbnb.dto.response.LoginResponse;
import com.example.airbnb.dto.response.SignupResponse;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.service.AuthSevice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTesting {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthSevice authSevice;
    @Autowired
    PasswordEncoder passwordEncoder;
    private SignupRequest signupRequest;
    private SignupResponse signupResponse;

    private final static ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void initData() {
        signupRequest = SignupRequest.builder()
                .name("Dinh Thi Tu")
                .email("dinhthitu@gmail.com")
                .password("Dinhtu123@")
                .build();

        signupResponse = SignupResponse.builder()
                .id(1234356L)
                .email("dinhthitu@gmail.com")
                .name("Dinh Thi Tu")
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .build();
    }

    @Test
    void signup_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(signupRequest);
        Mockito.when(authSevice.signup(ArgumentMatchers.any()))
                .thenReturn(signupResponse);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email")
                        .value("dinhthitu@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name")
                        .value("Dinh Thi Tu"))
        ;
    }

    @Test
    void signup_fail() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(signupRequest);
        Mockito.when(authSevice.signup(ArgumentMatchers.any()))
                .thenThrow(new AppException(ErrorCode.EMAIL_EXISTED));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(302))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("EMAIL ALREADY REGISTER"));
    }

    @Test
    void signup_blank_name() throws Exception {
        signupRequest.setName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(signupRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("validation failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("result")
                        .value("must not be blank"));
    }

    @Test
    void signup_invalid_format_email() throws Exception {
        signupRequest.setEmail("ohahaha");
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(signupRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("validation failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("result")
                        .value("must be a well-formed email address"));
    }

    @Test
    void signup_invalid_password() throws Exception {
        signupRequest.setPassword("123");
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(signupRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("validation failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("result")
                        .value("Password must be at least 8 characters long and contain a mix of uppercase, lowercase, digit, and special character"));
    }

    @Test
    void login_success() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@gmail.com")
                .password("Hanu01022004@")
                .build();
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("this is access token")
                .refreshToken("this is refresh token")
                .build();
        Mockito.when(authSevice.login(ArgumentMatchers.any()))
                .thenReturn(loginResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(loginRequest);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value("200"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.accessToken")
                        .value("this is access token"))
                .andExpect(MockMvcResultMatchers.cookie().exists("refreshToken"))
                .andExpect(MockMvcResultMatchers.cookie().value("refreshToken", "this is refresh token"));
    }

    @Test
    void login_fail() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("AAAAAA")
                .password("Hanu01022004@")
                .build();

        Mockito.when(authSevice.login(ArgumentMatchers.any()))
                .thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value("400"))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("USER NOT FOUND"));
    }

    @Test
    void login_empty_email() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("   ")
                .password("Hanu01022004@")
                .build();

        String content = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value("400"))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("validation failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("result")
                        .value("must not be blank"));
    }

    @Test
    void login_invalid_password() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@gmail.com")
                .password("   ")
                .build();

        String content = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value("400"))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("validation failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("result[0]").value(
                        "Password must be at least 8 characters long and contain a mix of uppercase, lowercase, digit, and special character"
                ))
                .andExpect(MockMvcResultMatchers.jsonPath("result[1]").value("must not be blank"))
                ;
    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void refresh_token_success() throws Exception{
        String oldRefreshToken = "old_refresh_token";
        String newAccessToken = "new access token";

        Mockito.when(authSevice.refreshToken(oldRefreshToken))
                .thenReturn(newAccessToken);
        Cookie cookie = new Cookie("refreshToken", oldRefreshToken);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/refreshToken")
                .cookie(cookie))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result")
                        .value(newAccessToken));
    }


}
