package com.example.airbnb.strategy;

import com.example.airbnb.entities.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {
    public BigDecimal calculateDynamicPrice(Inventory inventory){
        PricingStrategy pricingStrategy = new BasePriceStrategy();
        pricingStrategy = new SurgePriceStrategy(pricingStrategy);
        pricingStrategy = new HolidayPriceStrategy(pricingStrategy);
        return pricingStrategy.calculatePrice(inventory);
    }

    public BigDecimal calculateTotalPrice(List<Inventory> inventoryList){
        return inventoryList.stream()
                .map(this::calculateDynamicPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
