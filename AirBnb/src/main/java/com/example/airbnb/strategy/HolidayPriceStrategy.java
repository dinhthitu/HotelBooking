package com.example.airbnb.strategy;

import com.example.airbnb.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class HolidayPriceStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory){
        BigDecimal price = wrapped.calculatePrice(inventory);

        List<LocalDate> holidays = new ArrayList<>();
        holidays.add(LocalDate.of(2025, 12,25));
        holidays.add(LocalDate.of(2025, 10, 20));
        holidays.add(LocalDate.of(2025, 12, 30));
        holidays.add(LocalDate.of(2025, 9, 02));

        if(holidays.contains(inventory.getInventory_date())){
            price = price.multiply(BigDecimal.valueOf(1.5));
        }

        return price;
    }
}
