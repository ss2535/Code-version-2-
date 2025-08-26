package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

@Repository
public interface WeekDayRepository extends JpaRepository<WeekDay , Long> {

    Optional<WeekDay> findByTitle(String title);

    @Cacheable("weekDays")
    Optional<WeekDay> findById(Long id);
}
