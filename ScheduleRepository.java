package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

//    Optional<Schedule> findByUser_Id(Long userId);
// برای حل مشکل N+1 این کار را کردم
    @Query("SELECT s FROM Schedule s JOIN FETCH s.user u WHERE s.user.id = :userId")
    Optional<Schedule> findByUser_Id(@Param("userId") Long userId);

}
