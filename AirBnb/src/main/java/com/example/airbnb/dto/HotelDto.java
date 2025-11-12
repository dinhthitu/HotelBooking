package com.example.airbnb.dto;

import com.example.airbnb.entities.HotelContactInfor;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HotelDto {
    Long id;
    String name;
    String address;
//    String[] photos;
    String[] amenities;
    HotelContactInfor contactInfor;
    Boolean active;
}
