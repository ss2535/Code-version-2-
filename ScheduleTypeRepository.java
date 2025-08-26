package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.AccessLevel;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Schedule;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.ScheduleType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleTypeRepository extends JpaRepository<ScheduleType, Long> {

    Optional<ScheduleType> findScheduleTypeByTitle(String title);

    @Cacheable("scheduleTypes")
    Optional<ScheduleType> findById(Long id);

}
