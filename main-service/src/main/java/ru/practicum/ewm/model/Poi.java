package ru.practicum.ewm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "poi")
public class Poi {  // point of interest (anchor locations)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Embedded
    private Location location;
    @Column(name = "impact_radius", nullable = false)
    private double impactRadius;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PoiState state;
}
