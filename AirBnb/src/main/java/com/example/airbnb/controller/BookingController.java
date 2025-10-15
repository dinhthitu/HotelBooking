package com.example.airbnb.controller;

import com.example.airbnb.dto.ApiResponse;
import com.example.airbnb.dto.BookingDto;
import com.example.airbnb.dto.GuestDto;
import com.example.airbnb.dto.request.BookingRequest;
import com.example.airbnb.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/bookings")
public class BookingController {

    BookingService bookingService;

    @PostMapping("/initBooking")
    public ApiResponse<BookingDto> initialiseBooking(@RequestBody BookingRequest request){
        return ApiResponse.<BookingDto>builder()
                .result(bookingService.initialiseBooking(request))
                .build();
    }

    @PostMapping("/{bookingId}/addGuests")
    public ApiResponse<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guestDtoList){
        return ApiResponse.<BookingDto>builder()
                .result(bookingService.addGuests(bookingId, guestDtoList))
                .build();
    }

    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<Void> cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/{bookingId}/payment")
    public ApiResponse<String> initPayment(@PathVariable Long bookingId){
        return ApiResponse.<String>builder()
                .result(bookingService.initPayment(bookingId)).
                build();
    }


}
