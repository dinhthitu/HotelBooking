package com.example.airbnb.service;

import com.example.airbnb.dto.UserDto;
import com.example.airbnb.dto.request.ProfileUpdateRequest;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.airbnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements UserDetailsService {

    UserRepository userRepository;
    ModelMapper modelMapper;

    public User getUserById(Long id){
        var user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user;
    }

    public UserDto updateUserById(Long id, ProfileUpdateRequest request){
        var user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        modelMapper.map(request, user);
        userRepository.save(user);
        return modelMapper.map(user, UserDto.class);
    }
    public UserDto getMyProfile(){
        var user = getCurrentUser();
        return modelMapper.map(user, UserDto.class);
    }

    public UserDto updateProfile (ProfileUpdateRequest request){
        var user = getCurrentUser();
        modelMapper.map(request, user);
        userRepository.save(user);
        return modelMapper.map(user, UserDto.class);

    }

    public UserDto searchByUserName(String userName){

        var user = userRepository.findByName(userName);
        if(user == null){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return modelMapper.map(user, UserDto.class);
    }

    public List<UserDto> getAllUsers (){
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username);
        if(user == null){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
