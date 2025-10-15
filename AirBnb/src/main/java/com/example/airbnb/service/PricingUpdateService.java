package com.example.airbnb.service;

import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.HotelMinPrice;
import com.example.airbnb.entities.Inventory;
import com.example.airbnb.repository.HotelMinPriceRepository;
import com.example.airbnb.repository.HotelRepository;
import com.example.airbnb.repository.InventoryRepository;
import com.example.airbnb.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PricingUpdateService {

    HotelRepository hotelRepository;
    InventoryRepository inventoryRepository;
    HotelMinPriceRepository hotelMinPriceRepository;
    PricingService pricingService;

    @Scheduled(cron = "* 30 * * * *")
    @Transactional
    public void updatePrice(){
        int page = 0;
        int batchSize = 100;

        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().plusYears(1);
            hotelPage.getContent().forEach(hotel -> updateHotelPrice(hotel, startDate, endDate));
            page++ ;
        }
    }

    void updateHotelPrice(Hotel hotel, LocalDate startDate, LocalDate endDate){
            List<Inventory> inventoryList =
                    inventoryRepository.findByHotelAndInventoryDateBetween(hotel, startDate, endDate);

            updateInventoryPrice(inventoryList);
            updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
        }



    void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate){

        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getInventory_date,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))

                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            HotelMinPrice hotelMinPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));
            hotelMinPrice.setPrice(price);;
            hotelMinPrice.setDate(date);
            hotelPrices.add(hotelMinPrice);

        });
        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    void updateInventoryPrice(List<Inventory> inventoryList){
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice =  pricingService.calculateDynamicPrice(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }


}
