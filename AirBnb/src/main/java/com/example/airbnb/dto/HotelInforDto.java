package com.example.airbnb.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HotelInforDto {
    HotelDto hotel;
    List<RoomDto> rooms;
}
