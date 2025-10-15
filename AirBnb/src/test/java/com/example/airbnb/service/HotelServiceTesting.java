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
import com.example.airbnb.util.AppUtils;
import com.example.airbnb.util.Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
public class HotelServiceTesting {

    @Autowired
    private HotelService hotelService;

    @MockitoBean
    private HotelRepository hotelRepository;
    @MockitoBean
    private ModelMapper modelMapper;
    @MockitoBean
    private  InventoryService inventoryService;
    @MockitoBean
    private RoomRepository roomRepository;

    private HotelDto hotelDto;
    private User user;
    private Room room;
    private HotelContactInfor hotelContactInfor;
    private Hotel hotel;
    private RoomDto roomDto;
    private MockedStatic<AppUtils> mockedAppUtils;
    private MockedStatic<Helper> mockedHelper;

    @BeforeEach
    void initData(){
        room = new Room();
        room.setHotel(hotel);
        room.setId(1L);

         hotel =  new Hotel();
         hotel.setId(1L);
         hotel.setOwner(user);
         hotel.setRooms(List.of(room));

         user = User.builder()
                 .id(1L)
                 .email("user@gmail.com")
                 .build();
         hotelDto = HotelDto.builder()
                 .id(1L)
                 .name("abc")
                 .address("abc hong ha")
                 .contactInfor(hotelContactInfor).build();

         hotelContactInfor= HotelContactInfor.builder()
                 .address("hotel.vn")
                 .phoneNumber("+98563646335")
                 .email("abc@gmail.com").build();

        mockedAppUtils = mockStatic(AppUtils.class);
        mockedHelper = mockStatic(Helper.class);
        mockedAppUtils.when(AppUtils::getCurrentUser).thenReturn(user);



    }
 // --------------------- createHotel ----------------------------
    @Test
    void create_hotel_success(){

        when(hotelRepository.findByName(hotelDto.getName())).thenReturn(null);
        when(modelMapper.map(hotelDto, Hotel.class)).thenReturn(hotel);
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(hotelDto);

        HotelDto result = hotelService.createHotel(hotelDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(hotelRepository).save(hotel);
        assertThat(hotel.getOwner()).isEqualTo(user);

    }

    @Test
    void verify_hotel_existed(){
        when(hotelRepository.findByName(hotelDto.getName()))
                .thenReturn(hotel);

        assertThatThrownBy(() -> hotelService.createHotel(hotelDto))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOTEL_EXISTED);

    }

    // --------------------- updateHotel ----------------------

    @Test
    void update_hotel_success(){

        when(hotelRepository.findById(hotel.getId()))
                .thenReturn(Optional.of(hotel));

        when(modelMapper.map(hotelDto, Hotel.class)).thenReturn(hotel);
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(hotelDto);

        HotelDto result = hotelService.updateHotelById(hotel.getId(), hotelDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result).isNotNull();
        verify(hotelRepository).save(hotel);
        mockedHelper.verify(() -> Helper.checkHotelPermission(user, hotel));

    }

    @Test
    void unauthorized_user_access_failed(){

        User unauthorizedUser = new User();
        unauthorizedUser.setEmail("abcd@gmail.com");
        unauthorizedUser.setId(2L);

        when(hotelRepository.findById(hotel.getId()))
                .thenReturn(Optional.of(hotel));

        mockedAppUtils.when(AppUtils::getCurrentUser).thenReturn(unauthorizedUser);

        mockedHelper.when(() -> Helper.checkHotelPermission(unauthorizedUser,hotel))
                        .thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        assertThatThrownBy(() -> hotelService.updateHotelById(hotel.getId(), hotelDto))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);


    }

    //verify phai nam o cuoi
    //verify de dung cho cac mock, assert cho object that
    //tai sao lai la verify ?

    // ------------------------- deleteHotel -----------------------
    @Test
    void delete_hotel_success(){
        when(hotelRepository.findById(hotel.getId()))
                .thenReturn(Optional.of(hotel));

        hotelService.deleteHotelById(hotel.getId());

        mockedHelper.verify(() -> Helper.checkHotelPermission(user, hotel));
        verify(inventoryService).deleteAll(room);
        verify(roomRepository).deleteById(room.getId());
        verify(hotelRepository).deleteById(1L);

    }

    //----------------------  getAll----------------------
    @Test
    void get_all_success(){
        when(hotelRepository.findAll()).thenReturn(List.of(hotel));
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(hotelDto);

        List<HotelDto> hotelDtos = hotelService.getAll();

        assertThat(hotelDtos).hasSize(1);
    }

    // --------------------- getHotelInforById -------------------------
    @Test
    void get_hotel_infor_success(){

        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));
        when(modelMapper.map(hotel.getRooms(), RoomDto.class)).thenReturn( new RoomDto());
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(hotelDto);

        HotelInforDto result = hotelService.getHotelInforById(hotel.getId());

        assertThat(result.getHotel()).isEqualTo(hotelDto);
        assertThat(result).isNotNull();

    }

    @Test
    void generate_hotel_contact_success(){
        when(hotelRepository.findById(hotel.getId()))
                .thenReturn(Optional.of(hotel));

        hotelService.genHotelContactInfor(hotel.getId(), hotelContactInfor);

        assertThat(hotel.getContactInfor().getAddress()).isEqualTo(hotelContactInfor.getAddress());
        assertThat(hotel.getContactInfor().getEmail()).isEqualTo(hotelContactInfor.getEmail());
        mockedHelper.verify(() -> Helper.checkHotelPermission(user, hotel));

    }

    @Test
    void active_hotel_success(){
        hotel.setActive(true);
        when(hotelRepository.findById(hotel.getId()))
                .thenReturn(Optional.of(hotel));

        hotelService.activateHotel(hotel.getId());

        assertThat(hotel.getActive()).isTrue();
        mockedHelper.verify(() -> Helper.checkHotelPermission(user, hotel));
        verify(hotelRepository).save(hotel);
        verify(inventoryService).initializeRoomForYear(room);



    }


}
