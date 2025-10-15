package com.example.airbnb.dto;

import com.example.airbnb.entities.User;
import com.example.airbnb.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuestDto {
    User user;
    String name;
    Gender gender;
    Integer age;
}
