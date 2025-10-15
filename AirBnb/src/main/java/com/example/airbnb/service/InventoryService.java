package com.example.airbnb.service;

import com.example.airbnb.dto.HotelPrice;
import com.example.airbnb.dto.InventoryDto;
import com.example.airbnb.dto.request.HotelSearchRequest;
import com.example.airbnb.dto.request.UpdateInventoryRequest;
import com.example.airbnb.entities.Inventory;
import com.example.airbnb.entities.Room;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.HotelMinPriceRepository;
import com.example.airbnb.repository.InventoryRepository;
import com.example.airbnb.repository.RoomRepository;
import com.example.airbnb.util.Helper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.temporal.ChronoUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.airbnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryService {

    InventoryRepository inventoryRepository;
    ModelMapper modelMapper;
    HotelMinPriceRepository hotelMinPriceRepository;
    RoomRepository roomRepository;

    public void initializeRoomForYear(Room room){
        LocalDate today = LocalDate.now();
        LocalDate endDay = today.plusYears(1);
        for(; !today.isAfter(endDay); today = today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .address(room.getHotel().getAddress())
                    .price(room.getBasePrice())
                    .inventory_date(today)
                    .closed(false)
                    .totalCount(room.getTotalCount())
                    .bookedCount(0)
                    .reversedCount(0)
                    .surgeFactor(BigDecimal.valueOf(1))
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    public void deleteAll(Room room){
        inventoryRepository.deleteAllByRoom(room);
    }


    // chua test search

    public Page<HotelPrice> searchHotels(HotelSearchRequest request){
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        long dateCount = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        List<HotelPrice> hotels = hotelMinPriceRepository.findHotelsWithAvailableInventory(request.getAddress(), request.getStartDate(), request.getEndDate(), request.getRoomsCount(), dateCount);
        return new PageImpl<>(hotels, pageable, hotels.size());

    }

    public List<InventoryDto> getAllInventoryByRoom( Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        User user = getCurrentUser();
        Helper.checkRoomPermission(user, room);
        List<Inventory> inventoryList = inventoryRepository.findByRoomOrderByInventoryDate(room);
        return inventoryList.stream().map(e -> modelMapper.map(e, InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateInventory (Long roomId, UpdateInventoryRequest request){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        User user = getCurrentUser();
        Helper.checkRoomPermission(user, room);
        inventoryRepository.getInventoryLockedBeforeUpdating(roomId, request.getStartDate(), request.getEndDate());
        inventoryRepository.updateInventory(roomId, request.getStartDate(), request.getEndDate(),
                request.getClosed(), request.getSurgeFactor());

    }


}
