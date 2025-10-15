package com.example.airbnb.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDto {
    Long id;
    LocalDate inventoryDate;
    Integer bookedCount;
    Integer reversedCount;
    Integer totalCount;
    BigDecimal surgeFactor;
    BigDecimal price;
    String address;
    Boolean closed;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
