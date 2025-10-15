package com.example.airbnb.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HotelSearchRequest {
    String address;
    LocalDate startDate;
    LocalDate endDate;
    Integer roomsCount;
    Integer page = 0;
    Integer size = 10;
}
