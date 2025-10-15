package com.example.airbnb.controller;

import com.example.airbnb.dto.ApiResponse;
import com.example.airbnb.dto.BookingDto;
import com.example.airbnb.dto.UserDto;
import com.example.airbnb.dto.request.ProfileUpdateRequest;
import com.example.airbnb.service.BookingService;
import com.example.airbnb.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    BookingService bookingService;

    @PatchMapping("/update-profile")
    public ApiResponse<UserDto> updateProfile(@Valid @RequestBody ProfileUpdateRequest request){
        return ApiResponse.<UserDto>builder()
                .result(userService.updateProfile(request))
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<UserDto> getMyProfile(){
        return ApiResponse.<UserDto>builder()
                .result(userService.getMyProfile())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{userId}")
    public ApiResponse<UserDto> updateUserById(@PathVariable Long userId, @RequestBody ProfileUpdateRequest request){
        return ApiResponse.<UserDto>builder()
                .result(userService.updateUserById(userId, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/search/{userName}")
    public ApiResponse<UserDto> searchByUserName(@PathVariable String userName){
        return ApiResponse.<UserDto>builder()
                .result(userService.searchByUserName(userName))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ApiResponse<List<UserDto>> getAll(){
        return ApiResponse.<List<UserDto>>builder()
                .result(userService.getAllUsers())
                .build();
    }



    @GetMapping("/bookings")
    public ApiResponse<List<BookingDto>> getAllBookings(){
        return ApiResponse.<List<BookingDto>>builder()
                .result(bookingService.getMyBookings())
                .build();
    }


}
