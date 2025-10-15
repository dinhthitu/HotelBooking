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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthSevice {

    UserRepository userRepository;
    ModelMapper modelMapper;
    PasswordEncoder passwordEncoder;
    JWTService jwtService;
    AuthenticationManager authenticationManager;

    public SignupResponse signup (SignupRequest request){
        var user = userRepository.findByEmail(request.getEmail());
        if(user != null ){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        user = modelMapper.map(request, User.class);
        user.setRoles(Set.of(Role.GUEST));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return modelMapper.map(user, SignupResponse.class);
    }

    public LoginResponse login(LoginRequest request){

       Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = (User)authentication.getPrincipal();
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.refreshToken(user);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public SignupResponse signupAsAdmin (SignupRequest request){
        User user  = userRepository.findByEmail(request.getEmail());
        if(user != null){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ADMIN));
        userRepository.save(user);
        return modelMapper.map(user, SignupResponse.class);

    }

    public SignupResponse signupAsHotelManager(SignupRequest request){
        User user  = userRepository.findByEmail(request.getEmail());
        if(user != null){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        user = modelMapper.map(request, User.class);
        user.setRoles(Set.of(Role.HOTEL_MANAGER));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return modelMapper.map(user, SignupResponse.class);
    }


    public String refreshToken( String refreshToken){
        Long id = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return jwtService.generateToken(user);
    }

}
