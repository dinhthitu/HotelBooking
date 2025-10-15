package com.example.airbnb.service;

import com.example.airbnb.dto.HotelPrice;
import com.example.airbnb.dto.InventoryDto;
import com.example.airbnb.dto.request.HotelSearchRequest;
import com.example.airbnb.dto.request.UpdateInventoryRequest;
import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.Inventory;
import com.example.airbnb.entities.Room;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.HotelMinPriceRepository;
import com.example.airbnb.repository.InventoryRepository;
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
import org.springframework.data.jpa.repository.support.JpaEvaluationContextExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest
@TestPropertySource("/application-test.properties")
public class InventoryServiceTesting {

    @Autowired
    private InventoryService inventoryService;

    @MockitoBean
    private InventoryRepository inventoryRepository;
    @MockitoBean
    private ModelMapper modelMapper;
    @MockitoBean
    private HotelMinPriceRepository hotelMinPriceRepository;
    @MockitoBean
    private RoomRepository roomRepository;

    private Room room;
    private Inventory inventory;
    private HotelSearchRequest hotelSearchRequest;
    private HotelPrice hotelPrice;
    private InventoryDto inventoryDto;
    private User user;
    private MockedStatic<Helper> helperMockedStatic;
    private  MockedStatic<AppUtils> appUtilsMockedStatic;
    private UpdateInventoryRequest updateInventoryRequest;
    private Hotel hotel;
    @BeforeEach
    void initData(){

        user = User.builder()
                .id(1L)
                .email("dinhthitu@gmail.com")
                .name("dinh tu")
                .build();

        room = Room.builder()
                .id(1L)
                .totalCount(10)
                .amenities(new String[] {"air-conditioner", "wifi", "breakfast"})
                .basePrice(BigDecimal.valueOf(200))
                .capacity(3)
                .type("Duluxe")
                .build();

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setOwner(user);
        hotel.setActive(true);
        hotel.setRooms(List.of(room));


        inventoryDto = InventoryDto.builder()
                .id(1L)
                .price(BigDecimal.valueOf(200))
                .closed(false)
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .room(room)
                .hotel(hotel)
                .inventory_date(LocalDate.now())
                .price(BigDecimal.valueOf(200))
                .bookedCount(0)
                .reversedCount(0)
                .totalCount(10)
                .closed(false)
                .build();

        updateInventoryRequest = UpdateInventoryRequest.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(2))
                .closed(true)
                .surgeFactor(BigDecimal.valueOf(2))
                .build();

        appUtilsMockedStatic = mockStatic(AppUtils.class);
        helperMockedStatic = mockStatic(Helper.class);
    }

    @AfterEach
    void tearDown() {
        appUtilsMockedStatic.close();
        helperMockedStatic.close();
    }


    @Test
    void initialise_room_success(){
        room.setHotel(hotel);
        inventoryService.initializeRoomForYear(room);
        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
//        verify(inventoryRepository).save(inventory);
    }

    @Test
    void delete_success(){
        inventoryService.deleteAll(room);
        verify(inventoryRepository).deleteAllByRoom(room);
    }

    @Test
    void get_inventory_by_room_success(){
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        appUtilsMockedStatic.when(AppUtils::getCurrentUser).thenReturn(user);

        List<Inventory> inventoryList = List.of(inventory);
        when(inventoryRepository.findByRoomOrderByInventoryDate(room)).thenReturn(inventoryList);
        when(modelMapper.map(inventory, InventoryDto.class)).thenReturn(inventoryDto);

        List<InventoryDto> result = inventoryService.getAllInventoryByRoom(room.getId());

        assertThat(result).hasSize(1);
//        assertThat(result.getId()).isEqualTo(inventoryDto.getId());
        helperMockedStatic.verify(() -> Helper.checkRoomPermission(user, room));
    }

    @Test
    void get_inventory_by_room_failed(){

        when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> inventoryService.getAllInventoryByRoom(room.getId()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void update_inventory_success(){
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        appUtilsMockedStatic.when(AppUtils::getCurrentUser).thenReturn(user);

        inventoryService.updateInventory(room.getId(), updateInventoryRequest);

        verify(inventoryRepository).getInventoryLockedBeforeUpdating(room.getId(), updateInventoryRequest.getStartDate(), updateInventoryRequest.getEndDate());
        verify(inventoryRepository).updateInventory(room.getId(), updateInventoryRequest.getStartDate(), updateInventoryRequest.getEndDate(), updateInventoryRequest.getClosed(), updateInventoryRequest.getSurgeFactor());
        helperMockedStatic.verify(() -> Helper.checkRoomPermission(user, room));
    }

}
