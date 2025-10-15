package com.example.airbnb.service;

import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.HotelMinPrice;
import com.example.airbnb.entities.Inventory;
import com.example.airbnb.repository.HotelMinPriceRepository;
import com.example.airbnb.repository.HotelRepository;
import com.example.airbnb.repository.InventoryRepository;
import com.example.airbnb.strategy.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
public class PricingUpdateServiceTesting {

    @Autowired
    private PricingUpdateService pricingUpdateService;

    @MockitoBean
    private HotelRepository hotelRepository;
    @MockitoBean
    private InventoryRepository inventoryRepository;
    @MockitoBean
    private HotelMinPriceRepository hotelMinPriceRepository;
    @MockitoBean
    private PricingService pricingService;

    private Hotel hotel;
    private Inventory inventory;

    @BeforeEach
    void initData() {
        hotel = Hotel.builder()
                .id(1L)
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .hotel(hotel)
                .inventory_date(LocalDate.now())
                .price(BigDecimal.valueOf(100))
                .build();
    }

    // ------------------------------------------------------------
    // TEST 1: updateInventoryPrice
    // ------------------------------------------------------------
    @Test
    void updateInventoryPrice_success() {
        // Giả lập dynamic pricing
        when(pricingService.calculateDynamicPrice(inventory)).thenReturn(BigDecimal.valueOf(120));

        // Gọi hàm
        pricingUpdateService.updateInventoryPrice(List.of(inventory));

        // Kiểm tra
        assertThat(inventory.getPrice()).isEqualTo(BigDecimal.valueOf(120));
        verify(pricingService).calculateDynamicPrice(inventory);
        verify(inventoryRepository).saveAll(anyList());
    }

    // ------------------------------------------------------------
    // TEST 2: updateHotelMinPrice
    // ------------------------------------------------------------
    @Test
    void updateHotelMinPrice_success() {
        LocalDate date = LocalDate.now();
        List<Inventory> inventories = List.of(
                Inventory.builder().hotel(hotel).inventory_date(date).price(BigDecimal.valueOf(150)).build(),
                Inventory.builder().hotel(hotel).inventory_date(date).price(BigDecimal.valueOf(100)).build()
        );

        // Không có hotelMinPrice sẵn trong DB
        when(hotelMinPriceRepository.findByHotelAndDate(hotel, date)).thenReturn(Optional.empty());

        pricingUpdateService.updateHotelMinPrice(hotel, inventories, date, date);

        verify(hotelMinPriceRepository).saveAll(anyList());
    }

    // ------------------------------------------------------------
    // TEST 3: updateHotelPrice (gộp inventory và minPrice)
    // ------------------------------------------------------------
    @Test
    void updateHotelPrice_success() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(1);

        List<Inventory> inventories = List.of(inventory);

        when(inventoryRepository.findByHotelAndInventoryDateBetween(hotel, start, end))
                .thenReturn(inventories);
        when(pricingService.calculateDynamicPrice(any())).thenReturn(BigDecimal.valueOf(200));
        when(hotelMinPriceRepository.findByHotelAndDate(any(), any()))
                .thenReturn(Optional.empty());

        pricingUpdateService.updateHotelPrice(hotel, start, end);

        verify(inventoryRepository).findByHotelAndInventoryDateBetween(hotel, start, end);
        verify(inventoryRepository).saveAll(anyList());
        verify(hotelMinPriceRepository).saveAll(anyList());
    }

    // ------------------------------------------------------------
    // TEST 4: updatePrice (cron job)
    // ------------------------------------------------------------
    @Test
    void updatePrice_paging_success() {
        Page<Hotel> page1 = new PageImpl<>(List.of(hotel));
        Page<Hotel> page2 = Page.empty();

        when(hotelRepository.findAll(PageRequest.of(0, 100))).thenReturn(page1);
        when(hotelRepository.findAll(PageRequest.of(1, 100))).thenReturn(page2);

//        doNothing().when(inventoryRepository).saveAll(anyList());
//        doNothing().when(hotelMinPriceRepository).saveAll(anyList());
        when(inventoryRepository.findByHotelAndInventoryDateBetween(any(), any(), any()))
                .thenReturn(List.of(inventory));
        when(pricingService.calculateDynamicPrice(any())).thenReturn(BigDecimal.valueOf(250));
        when(hotelMinPriceRepository.findByHotelAndDate(any(), any())).thenReturn(Optional.empty());

        pricingUpdateService.updatePrice();

        verify(hotelRepository, times(2)).findAll(any(PageRequest.class));
        verify(inventoryRepository, atLeastOnce()).saveAll(anyList());
        verify(hotelMinPriceRepository, atLeastOnce()).saveAll(anyList());
    }
}
