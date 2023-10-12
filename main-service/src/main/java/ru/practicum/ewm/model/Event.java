package ru.practicum.ewm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@NamedEntityGraph(name = "Event.eager",
        attributeNodes = {
                @NamedAttributeNode("category"),
                @NamedAttributeNode("author")
        }
)
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String annotation;
    @Column(name = "date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    private boolean paid = false;
    @Column(name = "request_moderation")
    private boolean requestModeration = true;
    @Column(name = "participant_limit")
    private Integer participantLimit = 0;
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "published_time")
    private LocalDateTime publishedOn;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @Embedded
    private Location location;
}
