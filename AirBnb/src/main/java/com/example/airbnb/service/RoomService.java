package com.example.airbnb.service;

import com.example.airbnb.dto.RoomDto;
import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.Room;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.HotelRepository;
import com.example.airbnb.repository.RoomRepository;
import com.example.airbnb.util.AppUtils;
import com.example.airbnb.util.Helper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;


@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomService {

    RoomRepository roomRepository;
    ModelMapper modelMapper;
    HotelRepository hotelRepository;
    InventoryService inventoryService;

    public RoomDto createRoom (Long hotelId, RoomDto request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        User user = AppUtils.getCurrentUser();
        Helper.checkHotelPermission(user, hotel);
        Room room = modelMapper.map(request, Room.class);
        room.setHotel(hotel);
        roomRepository.save(room);
        if(hotel.getActive()){
           inventoryService.initializeRoomForYear(room);
        }
        return modelMapper.map(room, RoomDto.class);
    }

    public RoomDto updateRoom(Long roomId, RoomDto request){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        User user = AppUtils.getCurrentUser();
        Helper.checkRoomPermission(user, room);
        modelMapper.map(request, room);
        room.setId(roomId);
        roomRepository.save(room);
        return modelMapper.map(room, RoomDto.class);
    }

    public List<RoomDto> getAll(){
        return roomRepository.findAll().stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .toList();
    }

    public List<RoomDto> getAllRoomsInHotel(Long hotelId){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));

        return hotel.getRooms().stream().map(
                room -> modelMapper.map(room, RoomDto.class))
                .toList();

    }

    public RoomDto getRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        return modelMapper.map(room, RoomDto.class);
    }

    @Transactional
    public void deleteRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        User user = AppUtils.getCurrentUser();
        Helper.checkRoomPermission(user, room);
        inventoryService.deleteAll(room);
        roomRepository.deleteById(roomId);
    }

}
