package com.example.airbnb.controller;

import com.example.airbnb.dto.*;
import com.example.airbnb.dto.request.HotelSearchRequest;
import com.example.airbnb.entities.HotelContactInfor;
import com.example.airbnb.service.BookingService;
import com.example.airbnb.service.HotelService;
import com.example.airbnb.service.InventoryService;
import com.example.airbnb.service.RoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/hotels")
public class HotelController {

    HotelService hotelService;
    InventoryService inventoryService;
    BookingService bookingService;

    RoomService roomService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @PostMapping("/create-hotel")
    public ApiResponse<HotelDto> createHotel(@RequestBody HotelDto request){
        return ApiResponse.<HotelDto>builder()
                .result(hotelService.createHotel(request))
                .build();
    }

//    @GetMapping("/{hotelId}")
//    public ApiResponse<HotelDto> getHotelById(@PathVariable Long hotelId){
//        return ApiResponse.<HotelDto>builder()
//                .result(hotelService.getHotelById(hotelId))
//                .build();
//    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @PatchMapping("/update/{hotelId}")
    public ApiResponse<HotelDto> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDto request){
        return ApiResponse.<HotelDto>builder()
                .result(hotelService.updateHotelById(hotelId, request))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @DeleteMapping("/{hotelId}")
    public ApiResponse<Void> deleteHotelById(@PathVariable Long hotelId){
        hotelService.deleteHotelById(hotelId);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<HotelDto>> getAll(){
        return ApiResponse.<List<HotelDto>>builder()
                .result(hotelService.getAll())
                .build();
    }

    @GetMapping("/{hotelId}")
    public ApiResponse<HotelInforDto> getHotelInforById(@PathVariable Long hotelId){
        return ApiResponse.<HotelInforDto>builder()
                .result(hotelService.getHotelInforById(hotelId))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @PostMapping("/contact/{hotelId}")
    public ApiResponse<Void> genHotelContactInfor(@PathVariable Long hotelId, @RequestBody HotelContactInfor request){
        hotelService.genHotelContactInfor(hotelId, request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<HotelPrice>> searchHotels(@RequestBody HotelSearchRequest request){
        return ApiResponse.<Page<HotelPrice>>builder()
                .result(inventoryService.searchHotels(request))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @PatchMapping("/{hotelId}")
    public ApiResponse<Void> activateHotel(@PathVariable Long hotelId){
        hotelService.activateHotel(hotelId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("rooms/{hotelId}")
    public ApiResponse<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId){
        return ApiResponse.<List<RoomDto>>builder()
                .result(roomService.getAllRoomsInHotel(hotelId))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @GetMapping("/{hotelId}/bookings")
    public ApiResponse<List<BookingDto>> getAllBookingsByHotelId(@PathVariable Long hotelId){
        return ApiResponse.<List<BookingDto>>builder()
                .result(bookingService.getAllBookingByHotel(hotelId))
                .build();
    }


}
