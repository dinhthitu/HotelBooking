package com.example.airbnb.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomDto {
    Long id;
    String type;
    String[] images;
    BigDecimal basePrice;
    String[] amenities;
    Integer totalCount;
    Integer capacity;
}
