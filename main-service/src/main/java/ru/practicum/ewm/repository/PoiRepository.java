package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.model.Poi;

public interface PoiRepository extends JpaRepository<Poi, Long>, QuerydslPredicateExecutor<Poi> {
}
