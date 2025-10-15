package com.example.airbnb.dto.request;

import com.example.airbnb.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @NotBlank
    String email;
    @NotBlank
            @ValidPassword
    String password;
}
