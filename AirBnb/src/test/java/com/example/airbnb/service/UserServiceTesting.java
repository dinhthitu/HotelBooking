package com.example.airbnb.service;

import com.example.airbnb.dto.UserDto;
import com.example.airbnb.dto.request.ProfileUpdateRequest;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.UserRepository;
import com.example.airbnb.util.AppUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
public class UserServiceTesting {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ModelMapper modelMapper;

    private MockedStatic<AppUtils> mockedStatic;
    private ProfileUpdateRequest profileUpdateRequest;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void initData(){
        mockedStatic =  mockStatic(AppUtils.class);

        userDto = UserDto.builder()
                .id(1L)
                .email("dinhthitu@gmail.com")
                .name("dinh tu")
                .build();
        profileUpdateRequest = ProfileUpdateRequest.builder()
                .name("tu")
                .build();

        user = User.builder()
                .id(1L)
                .email("dinhthitu@gmail.com")
                .name("dinh tu")
                .build();
    }

    @AfterEach
    void tearDown(){
        mockedStatic.close();
    }

    @Test
    void get_user_success(){
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User user1 = userService.getUserById(user.getId());

        assertThat(user1).isNotNull();
        assertThat(user1.getId()).isEqualTo(user.getId());
    }

    @Test
    void invalid_user(){
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(user.getId()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "admin123@gmail.com", roles={"ADMIN"})
    void update_user_success(){
        user.setName("tu");
        userDto.setName("tu");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(modelMapper.map(profileUpdateRequest, User.class)).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto userDto1 = userService.updateUserById(user.getId(), profileUpdateRequest);

        assertThat(userDto1.getName()).isEqualTo("tu");
        verify(userRepository).save(user);

    }

    @Test
    void get_profile(){
        mockedStatic.when(AppUtils::getCurrentUser).thenReturn(user);

        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto userDto1 = userService.getMyProfile();

        assertThat(userDto1).isNotNull();
        assertThat(userDto1.getId()).isEqualTo(1);
        verify(modelMapper).map(user, UserDto.class);

    }

}
