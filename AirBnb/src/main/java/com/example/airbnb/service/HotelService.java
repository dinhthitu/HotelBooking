package com.example.airbnb.service;

import com.example.airbnb.dto.HotelDto;
import com.example.airbnb.dto.HotelInforDto;
import com.example.airbnb.dto.RoomDto;
import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.HotelContactInfor;
import com.example.airbnb.entities.Room;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.HotelRepository;
import com.example.airbnb.repository.RoomRepository;
import com.example.airbnb.util.Helper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.airbnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HotelService {

    HotelRepository hotelRepository;
    ModelMapper modelMapper;
    InventoryService inventoryService;
    RoomRepository  roomRepository;
    public HotelDto createHotel (HotelDto request){
        Hotel hotel = hotelRepository.findByName(request.getName());
        if(hotel != null) {
            throw new AppException(ErrorCode.HOTEL_EXISTED);
        }
        User user= getCurrentUser();
        hotel =  modelMapper.map(request, Hotel.class);
        hotel.setOwner(user);
        hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

//    public HotelDto getHotelById(Long hotelId){
//        Hotel hotel = hotelRepository.findById(hotelId)
//                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
//
//        return modelMapper.map(hotel, HotelDto.class);
//    }

    public HotelDto updateHotelById(Long hotelId, HotelDto request){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        User user = getCurrentUser();
        Helper.checkHotelPermission(user, hotel);
        modelMapper.map(request, hotel);
        hotel.setId(hotelId);
        hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Transactional
    public void deleteHotelById(Long hotelId){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        User user = getCurrentUser();
        Helper.checkHotelPermission(user, hotel);
        for(Room room: hotel.getRooms()){
            inventoryService.deleteAll(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(hotelId);
    }

    public List<HotelDto> getAll(){
        return hotelRepository.findAll().stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .collect(Collectors.toList());
    }

    public HotelInforDto getHotelInforById(Long hotelId){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));

        List<RoomDto> rooms = hotel.getRooms().stream().map(
                room -> modelMapper.map(room, RoomDto.class)
        ).collect(Collectors.toList());

        return HotelInforDto.builder()
                .hotel(modelMapper.map(hotel, HotelDto.class))
                .rooms(rooms)
                .build();
    }

    public void genHotelContactInfor(Long hotelId, HotelContactInfor request){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        User user = getCurrentUser();
        Helper.checkHotelPermission(user, hotel);
        HotelContactInfor hotelContactInfor = HotelContactInfor.builder()
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .build();
        hotel.setContactInfor(hotelContactInfor);

        hotelRepository.save(hotel);
    }

    @Transactional
    public void activateHotel(Long hotelId){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        User user = getCurrentUser();
        Helper.checkHotelPermission(user, hotel);
        hotel.setActive(true);
        hotelRepository.save(hotel);
        for(Room room: hotel.getRooms()){
            inventoryService.initializeRoomForYear(room);
        }
    }
}
