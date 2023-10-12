package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String email;
}
