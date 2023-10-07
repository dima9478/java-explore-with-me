package ru.practicum.ewm.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NewCategoryDto {
    @Size(min = 1, max = 50)
    @NotBlank
    private String name;
}
