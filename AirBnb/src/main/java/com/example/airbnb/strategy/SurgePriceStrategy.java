package com.example.airbnb.strategy;

import com.example.airbnb.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
@RequiredArgsConstructor
public class SurgePriceStrategy implements PricingStrategy{
    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculatePrice(Inventory inventory){
        return wrapped.calculatePrice(inventory).multiply(inventory.getSurgeFactor());
    }
}
