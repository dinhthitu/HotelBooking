package com.example.airbnb.service;

import com.example.airbnb.dto.request.LoginRequest;
import com.example.airbnb.dto.request.SignupRequest;
import com.example.airbnb.dto.response.LoginResponse;
import com.example.airbnb.dto.response.SignupResponse;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.enums.Role;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.UserRepository;

import com.example.airbnb.security.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@SpringBootTest
@TestPropertySource("/application-test.properties")
public class AuthServiceTesting {

    @Autowired
    private AuthSevice authSevice;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ModelMapper modelMapper;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private JWTService jwtService;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    private SignupResponse signupResponse;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;
    private LoginResponse loginResponse;

    @BeforeEach
    void initData(){
    signupRequest = SignupRequest.builder()
            .name("admin")
            .email("admin123@gmail.com")
            .password("Admin123@")
            .build();

    signupResponse = SignupResponse.builder()
            .id(1L)
            .name("admin")
            .password(passwordEncoder.encode(signupRequest.getPassword()))
            .email("admin123@gmail.com")
            .build();

    loginRequest = LoginRequest.builder()
            .email("admin123@gmail.com")
            .password(signupRequest.getPassword())
            .build();

    loginResponse = LoginResponse.builder()
            .accessToken("this is access token")
            .refreshToken("this is refresh token")
            .build();


    }

    @Test
    void register_success() throws Exception{
        user = new User();
        user.setEmail(signupRequest.getEmail());

        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(null);
        when(modelMapper.map(signupRequest, User.class)).thenReturn(user);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn(signupResponse.getPassword());
        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(user);
        when(modelMapper.map(user, SignupResponse.class)).thenReturn(signupResponse);

        SignupResponse response = authSevice.signup(signupRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("admin");
        verify(userRepository).save(ArgumentMatchers.any(User.class));
        assertThat(user.getRoles().contains(Role.GUEST));
    }

    @Test
    void register_failed()throws Exception{
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(new User());

        assertThatThrownBy(() -> authSevice.signup(signupRequest))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_EXISTED);
    }

    @Test
    void login_success() throws Exception{
        user = new User();
        user.setId(1L);
        user.setEmail("admin123@gamil.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(user)).thenReturn(loginResponse.getAccessToken());
        when(jwtService.refreshToken(user)).thenReturn(loginResponse.getRefreshToken());

        var response = authSevice.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo(loginResponse.getAccessToken());
        assertThat(response.getRefreshToken()).isEqualTo(loginResponse.getRefreshToken());

    }

    @Test
    void refresh_token_success() throws Exception{
        user = new User();
        user.setId(1L);

        when(jwtService.getUserIdFromToken(loginResponse.getRefreshToken())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(jwtService.generateToken(user)).thenReturn("new access-token");

        String newToken = authSevice.refreshToken(loginResponse.getRefreshToken());

        assertThat(newToken).isEqualTo("new access-token");


    }

    @Test
    void refresh_token_failed() throws Exception{
        String refreshToken = "invalid refresh token";
        when(jwtService.getUserIdFromToken(refreshToken)).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authSevice.refreshToken(refreshToken))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
