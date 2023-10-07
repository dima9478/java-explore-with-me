package ru.practicum.ewm.common.model;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class Location {
    private double lat;
    private double lon;
}
