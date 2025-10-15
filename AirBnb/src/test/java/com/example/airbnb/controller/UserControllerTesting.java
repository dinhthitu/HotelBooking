package com.example.airbnb.controller;

import com.example.airbnb.dto.BookingDto;
import com.example.airbnb.dto.UserDto;
import com.example.airbnb.dto.request.ProfileUpdateRequest;
import com.example.airbnb.enums.BookingStatus;
import com.example.airbnb.enums.Gender;
import com.example.airbnb.enums.Role;
import com.example.airbnb.service.BookingService;
import com.example.airbnb.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTesting {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private BookingService bookingService;

    private UserDto userDto;
    private ProfileUpdateRequest profileUpdateRequest;
    private BookingDto bookingDto;
    private List<BookingDto> bookingDtos;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void initData(){
        userDto = UserDto.builder()
                .dateOfBirth(LocalDate.of(2004,02,01))
                .id(302L)
                .email("happybirthday@gmail.com")
                .roles(Set.of(Role.HOTEL_MANAGER))
                .gender(Gender.FEMALE)
                .name("hotel owner")
                .build();
        profileUpdateRequest = ProfileUpdateRequest.builder()
                .gender(Gender.FEMALE)
                .name("hotel owner")
                .build();
        bookingDto = BookingDto.builder()
                .id(1L)
                .roomsCount(4)
                .updatedAt(LocalDateTime.of(2025,10,01,18,9,02))
                .checkInDate(LocalDate.of(2025,10,06))
                .checkOutDate(LocalDate.of(2025,10,10))
                .bookingStatus(BookingStatus.RESERVED)
                .guests(Set.of())
                .amount(BigDecimal.valueOf(2000.00))
                .build();
        bookingDtos =List.of(bookingDto);

    }

    @Test
    @WithMockUser(username = "happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void update_profile_success() throws Exception{
        Mockito.when(userService.updateProfile(profileUpdateRequest))
                .thenReturn(userDto);
        mockMvc.perform(MockMvcRequestBuilders
                .patch("/users/update-profile")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(302))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("hotel owner"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.gender").value("FEMALE"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email").value("happybirthday@gmail.com"));

    }

    @Test
    @WithMockUser(username = "happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void get_profile_success() throws Exception{
        Mockito.when(userService.getMyProfile()).thenReturn(userDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/profile"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(302))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("hotel owner"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.gender").value("FEMALE"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email").value("happybirthday@gmail.com"));

    }

    @Test
    @WithMockUser(username = "happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void get_bookings_success() throws Exception{
        Mockito.when(bookingService.getMyBookings()).thenReturn(bookingDtos);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/bookings"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.length()").value(1));

    }

    @Test
    @WithMockUser(username = "admin123@gmail.com", roles = {"ADMIN"})
    void search_user_success() throws Exception{
        userDto.setId(3L);
        userDto.setName("user");
        userDto.setEmail("user@gmail.com");
        userDto.setRoles(Set.of(Role.GUEST));
        Mockito.when(userService.searchByUserName("user"))
                .thenReturn(userDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/admin/search/user"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("user"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.roles").value("GUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email").value("user@gmail.com"));

    }


}
