package com.example.airbnb.controller;

import com.example.airbnb.dto.ApiResponse;
import com.example.airbnb.dto.RoomDto;
import com.example.airbnb.service.RoomService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomController {
    RoomService roomService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @PostMapping("/create-room/{hotelId}")
    public ApiResponse<RoomDto> createRoom(@PathVariable Long hotelId, @Valid @RequestBody RoomDto request){
        return ApiResponse.<RoomDto>builder()
                .result(roomService.createRoom(hotelId, request))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @PatchMapping("/update-room/{roomId}")
    public ApiResponse<RoomDto> updateRoom(@PathVariable Long roomId, @RequestBody RoomDto request){
        return ApiResponse.<RoomDto>builder()
                .result(roomService.updateRoom(roomId, request))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<RoomDto>> getAll(){
        return ApiResponse.<List<RoomDto>>builder()
                .result(roomService.getAll())
                .build();
    }


    @GetMapping("/{roomId}")
    public ApiResponse<RoomDto> getRoomById(@PathVariable Long roomId){
        return ApiResponse.<RoomDto>builder()
                .result(roomService.getRoomById(roomId))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @DeleteMapping("/{roomId}")
        public ApiResponse<Void> deleteRoomById (@PathVariable Long roomId){
            roomService.deleteRoomById(roomId);
            return ApiResponse.<Void>builder()
                    .build();
        }


}
