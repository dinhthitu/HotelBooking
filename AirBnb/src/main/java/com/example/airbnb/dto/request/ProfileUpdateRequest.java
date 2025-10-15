package com.example.airbnb.dto.request;

import com.example.airbnb.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {
    String name;
    Gender gender;
    LocalDate dateOfBirth;
}
