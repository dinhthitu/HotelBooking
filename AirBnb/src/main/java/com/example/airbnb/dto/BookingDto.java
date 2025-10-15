package com.example.airbnb.dto;

import com.example.airbnb.entities.Guest;
import com.example.airbnb.enums.BookingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {
    Long id;
    Integer roomsCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    String status;
    BookingStatus bookingStatus;
    Set<GuestDto> guests;
    BigDecimal amount;
}
