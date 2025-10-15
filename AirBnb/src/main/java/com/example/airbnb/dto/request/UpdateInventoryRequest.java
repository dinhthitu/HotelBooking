package com.example.airbnb.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateInventoryRequest {
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal surgeFactor;
    Boolean closed;

}
