package com.example.airbnb.dto;

import com.example.airbnb.enums.Gender;
import com.example.airbnb.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
   Long id;
   String name;
   String email;
   String phoneNumber;
   LocalDate dateOfBirth;
   Gender gender;
   Set<Role> roles;
}
