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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource("/application-test.properties")
public class RoomServiceTesting {

    @Autowired
    private RoomService roomService;

    @MockitoBean
    private RoomRepository roomRepository;
    @MockitoBean
    private ModelMapper modelMapper;
    @MockitoBean
    private HotelRepository hotelRepository;
    @MockitoBean
    private InventoryService inventoryService;


    private Room room;
    private RoomDto roomDto;
    private User user;
    private Hotel hotel;
    private MockedStatic<AppUtils> appUtilsMocked;
    private MockedStatic<Helper> helperMocked ;

    @BeforeEach
    void initData(){
        user = User.builder()
                .id(1L)
                .email("dinhthitu@gmail.com")
                .name(" dinh tu ")
                .build();
        roomDto = RoomDto.builder()
                .id(1L)
                .totalCount(10)
                .capacity(3)
                .basePrice(BigDecimal.valueOf(200))
                .type("Duluxe")
                .build();

        room = Room.builder()
                .id(1L)
                .totalCount(10)
                .capacity(3)
                .basePrice(BigDecimal.valueOf(200))
                .type("Duluxe")
                .build();
        hotel = Hotel.builder()
                .id(1L)
                .owner(user)
                .rooms(List.of(room))
                .active(true)
                .name("hanna oh lala")
                .build();

        appUtilsMocked = mockStatic(AppUtils.class);
        helperMocked = mockStatic(Helper.class);
    }

    @AfterEach
    void tearDown(){
        appUtilsMocked.close();
        helperMocked.close();
    }

    @Test
    void create_room_success(){
        room.setHotel(hotel);
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));
        appUtilsMocked.when(AppUtils::getCurrentUser).thenReturn(user);
        when(modelMapper.map(roomDto, Room.class)).thenReturn(room);
        when(modelMapper.map(room, RoomDto.class)).thenReturn(roomDto);

        RoomDto result = roomService.createRoom(hotel.getId(), roomDto);

        assertThat(result.getId()).isEqualTo(roomDto.getId());
        helperMocked.verify(() -> Helper.checkHotelPermission(user, hotel));
        verify(roomRepository).save(room);
        verify(inventoryService).initializeRoomForYear(room);
    }

    @Test
    void invalid_hotel(){
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.createRoom(hotel.getId(), roomDto))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOTEL_NOT_FOUND);
    }

    @Test
    void update_room_success(){
        roomDto.setBasePrice(BigDecimal.valueOf(150));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        appUtilsMocked.when(AppUtils::getCurrentUser).thenReturn(user);

        when(modelMapper.map(roomDto, Room.class)).thenReturn(room);
        when(modelMapper.map(room, RoomDto.class)).thenReturn(roomDto);

        RoomDto result = roomService.updateRoom(room.getId(), roomDto);

        assertThat(result).isNotNull();
        assertThat(result.getBasePrice()).isEqualTo(BigDecimal.valueOf(150));
        assertThat(result.getId()).isEqualTo(room.getId());
        verify(roomRepository).save(room);
        helperMocked.verify(() -> Helper.checkRoomPermission(user, room));

    }

    @Test
    void invalid_room(){
        when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.updateRoom(room.getId(), roomDto))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void get_all_success(){

        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(modelMapper.map(room, RoomDto.class)).thenReturn(roomDto);

        List<RoomDto> roomDtos = roomService.getAll();

        assertThat(roomDtos).hasSize(1);


    }

    @Test
    void delete_success(){
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        appUtilsMocked.when(AppUtils::getCurrentUser).thenReturn(user);
        roomService.deleteRoomById(room.getId());

        verify(inventoryService).deleteAll(room);
        verify(roomRepository).deleteById(room.getId());
        helperMocked.verify(() -> Helper.checkRoomPermission(user, room));
    }
}
