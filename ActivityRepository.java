package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityProjection;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityProjection2;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityReportDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Activity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity , Long> {

    Optional<Activity> findByTitle(String title);

//    // در متد افزودن فعالیت استفاده میکنم تا بررسی کنم تداخل با فعالیت های قبلی دارد یا نه- البته فعالیت های روزانه
//    @Query(value = "SELECT * FROM activity a WHERE a.schedule_id = :scheduleId " +
//            "AND a.start_date = :startDate " +
//            "AND ((a.start_time <= :endTime AND a.end_time >= :startTime) " +
//            "OR (a.start_time >= :startTime AND a.start_time < :endTime))", nativeQuery = true)
//    List<Activity> findConflictingActivities(
//            @Param("scheduleId") Long scheduleId,
//            @Param("startDate") LocalDate startDate,
//            @Param("startTime") LocalTime startTime,
//            @Param("endTime") LocalTime endTime);
    // برای حل مشکل n+1 این کار را کردم
    // بررسی تداخل زمانی برای فعالیت‌های شخصی با در نظر گرفتن فعالیت‌های طولانی (با یا بدون تاریخ پایان)
    @Query("SELECT DISTINCT a FROM Activity a " +
            "JOIN FETCH a.weekDay wd " +
            "JOIN FETCH a.status s " +
            "JOIN FETCH a.activityType at " +
            "JOIN FETCH a.accessLevel al " +
            "JOIN FETCH a.schedule sch " +
            "WHERE sch.id = :scheduleId " +
            "AND (a.startDate <= CAST(:endDate AS DATE) OR CAST(:endDate AS DATE) IS NULL) " +
            "AND (a.endDate IS NULL OR a.endDate >= :startDate) " +
            "AND ((a.startTime <= :endTime AND a.endTime >= :startTime) " +
            "OR (a.startTime >= :startTime AND a.startTime < :endTime))")
    List<Activity> findConflictingActivities(
            @Param("scheduleId") Long scheduleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);


    // در برنامه هفنگی شخصی (شخصی و گروهی)- نشان دادن تمام اطلاعات و روابط جدول فعالیت به همراه مرتب سازی
//    @Query(value = "SELECT a.* FROM activity a " +
//            "JOIN week_day wd ON a.week_day_id = wd.id " +
//            "JOIN status s ON a.status_id = s.id " +
//            "JOIN activity_type at ON a.activity_type_id = at.id " +
//            "JOIN access_level al ON a.access_level_id = al.id " +
//            "JOIN schedule sch ON a.schedule_id = sch.id " +
//            "WHERE sch.user_table_id = :userId " +
//            "AND (a.start_date BETWEEN :startDate AND :endDate " +
//            "OR a.end_date BETWEEN :startDate AND :endDate " +
//            "OR (a.start_date <= :startDate AND a.end_date >= :endDate)) " +
//            "ORDER BY wd.id, a.start_time",
//            nativeQuery = true)
//    List<Activity> findPersonalActivitiesByDateRange(@Param("userId") Long userId ,
//                                                     @Param("startDate") LocalDate startData ,
//                                                     @Param("endDate") LocalDate endDate);
    // برای حل مشکل n+1 این کار را کردم

    @Query("SELECT DISTINCT a FROM Activity a " +
            "JOIN FETCH a.weekDay wd " +
            "JOIN FETCH a.status s " +
            "JOIN FETCH a.activityType at " +
            "JOIN FETCH a.accessLevel al " +
            "JOIN FETCH a.schedule sch " +
            "WHERE sch.user.id = :userId " +
            "AND (a.startDate BETWEEN :startDate AND :endDate " +
            "OR a.endDate BETWEEN :startDate AND :endDate " +
            "OR (a.startDate <= :startDate AND a.endDate >= :endDate)) " +
            "ORDER BY wd.id, a.startTime")
    List<Activity> findPersonalActivitiesByDateRange(@Param("userId") Long userId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // نشان دادن برنامه گروهی در صفحه مذیریت برنامه هفتگی گروهی
//    @Query(value = "select a.* from activity a " +
//            "join week_day wd on a.week_day_id = wd.id " +
//            "join status s on a.status_id = s.id " +
//            "join activity_type at on a.activity_type_id = at.id " +
//            "join access_level al on a.access_level_id = al.id " +
//            "where a.group_table_id = :groupId " +
//            "and a.schedule_id is NULL " +
//            "and (a.start_date between :startDate and :endDate " +
//            "or a.end_date between :startDate and :endDate " +
//            "or (a.start_date <= :startDate and a.end_date >= :endDate)) " +
//            "order by wd.id , a.start_time"
//            ,nativeQuery = true)
//    List<Activity> findGroupActivityByDateRange(@Param("groupId") Long groupId,
//                                                @Param("startDate") LocalDate startDate,
//                                                @Param("endDate") LocalDate endDate);
    // برای حل مشکل n+1 این کار را کردم
    @Query("SELECT DISTINCT a FROM Activity a " +
            "JOIN FETCH a.weekDay wd " +
            "JOIN FETCH a.status s " +
            "JOIN FETCH a.activityType at " +
            "JOIN FETCH a.accessLevel al " +
            "JOIN FETCH a.group g " +
            "WHERE a.group.id = :groupId " +
            "AND a.schedule IS NULL " +
            "AND (a.startDate BETWEEN :startDate AND :endDate " +
            "OR a.endDate BETWEEN :startDate AND :endDate " +
            "OR (a.startDate <= :startDate AND (a.endDate >= :endDate OR a.endDate IS NULL))) " +
            "ORDER BY wd.id, a.startTime")
    List<Activity> findGroupActivityByDateRange(@Param("groupId") Long groupId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

//  به‌روزرسانی فعالیت‌های اعضای گروه در برنامه‌های هفتگی
    @Query("SELECT a FROM Activity a WHERE a.group.id = :groupId AND a.schedule IS NOT NULL")
    List<Activity> findByGroupIdAndScheduleNotNull(@Param("groupId") Long groupId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE activity a " +
            "SET title = :title, " +
            "description = :description, " +
            "start_date = :startDate, " +
            "end_date = :endDate, " +
            "start_time = :startTime, " +
            "end_time = :endTime, " +
            "location = :location, " +
            "week_day_id = :weekDayId, " +
            "status_id = :statusId, " +
            "activity_type_id = :activityTypeId, " +
            "access_level_id = :accessLevelId " +
            "WHERE a.group_table_id = :groupId " +
            "AND a.schedule_id IS NOT NULL",
            nativeQuery = true)
    int updateGroupMemberActivities(@Param("title") String title, @Param("description") String description,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
            @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
            @Param("location") String location, @Param("weekDayId") Long weekDayId,
            @Param("statusId") Long statusId, @Param("activityTypeId") Long activityTypeId,
            @Param("accessLevelId") Long accessLevelId, @Param("groupId") Long groupId
    );


    //  حذف فعالیت های اعضای گروه از برنامه هفتگی
    // نمیشه یدونه متد کنم - از CAST فقط در SELECT میشد
    @Modifying
    @Transactional
    @Query("DELETE FROM Activity a " +
            "WHERE a.group.id = :groupId " +
            "AND a.schedule IS NOT NULL " +
            "AND a.title = :title " +
            "AND a.startDate = :startDate " +
            "AND a.startTime = :startTime " +
            "AND a.endTime = :endTime " +
            "AND a.endDate = :endDate")
    void deleteGroupMemberActivitiesWithEndDate(@Param("groupId") Long groupId, @Param("title") String title,
            @Param("startDate") LocalDate startDate, @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime, @Param("endDate") LocalDate endDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM Activity a " +
            "WHERE a.group.id = :groupId " +
            "AND a.schedule IS NOT NULL " +
            "AND a.title = :title " +
            "AND a.startDate = :startDate " +
            "AND a.startTime = :startTime " +
            "AND a.endTime = :endTime " +
            "AND a.endDate IS NULL")
    void deleteGroupMemberActivitiesWithoutEndDate(@Param("groupId") Long groupId, @Param("title") String title,
            @Param("startDate") LocalDate startDate, @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);


    // در نشان دادن گزارش استفاده میکنم که هر روز چه قدر فعالیت داشتم
    // در اینجا مدت زمان توسط این کوئری تولید میشد ولی با jpql خطا میداد برای همین محاسبه به کد در سرویس منتقل شد
//    @Query("SELECT new com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityReportDTO(" +
//            "a.title, a.startTime, a.endTime, " +
//            "EXTRACT(EPOCH FROM (a.endTime - a.startTime))/60) " +
//            "FROM Activity a " +
//            "JOIN FETCH a.schedule sch " +
//            "JOIN FETCH sch.user u " +
//            "WHERE u.id = :userId " +
//            "AND (a.startDate = :date OR a.endDate = :date OR " +
//            "(a.startDate <= :date AND (a.endDate >= :date OR a.endDate IS NULL))) " +
//            "AND a.group IS NULL " +
//            "ORDER BY a.startTime")
//    List<ActivityReportDTO> findPersonalActivitiesByDate(
//            @Param("userId") Long userId,
//            @Param("date") LocalDate date);
    // برای حل مشکل N+1 این کار را کردم
//    @Query("SELECT a " +
//            "FROM Activity a " +
//            "JOIN FETCH a.schedule sch " +
//            "JOIN FETCH sch.user u " +
//            "WHERE u.id = :userId " +
//            "AND (a.startDate = :date OR a.endDate = :date OR " +
//            "(a.startDate <= :date AND (a.endDate >= :date OR a.endDate IS NULL))) " +
//            "AND a.group IS NULL " +
//            "ORDER BY a.startTime")
//    List<Activity> findPersonalActivitiesByDate(
//            @Param("userId") Long userId,
//            @Param("date") LocalDate date);
//
//    @Query("SELECT DISTINCT a " +
//            "FROM Activity a " +
//            "JOIN FETCH a.group g " +
//            "WHERE g.id = :groupId " +
//            "AND (a.startDate = :date OR a.endDate = :date OR " +
//            "(a.startDate <= :date AND (a.endDate >= :date OR a.endDate IS NULL))) " +
//            "AND a.group IS NOT NULL " +
//            "AND a.schedule IS NULL " +
//            "ORDER BY a.startTime")
//    List<Activity> findGroupActivitiesByDate(
//            @Param("groupId") Long groupId,
//            @Param("date") LocalDate date);
// با پروجکشن مینویسم
    @Query("SELECT a.title AS title, a.startTime AS startTime, a.endTime AS endTime " +
            "FROM Activity a " +
            "JOIN a.schedule sch " +
            "JOIN sch.user u " +
            "WHERE u.id = :userId " +
            "AND (a.startDate = :date OR a.endDate = :date OR " +
            "(a.startDate <= :date AND (a.endDate >= :date OR a.endDate IS NULL))) " +
//            "AND a.group IS NULL " +
            "ORDER BY a.startTime")
    List<ActivityProjection> findPersonalActivitiesByDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);

    @Query("SELECT DISTINCT a.title AS title, a.startTime AS startTime, a.endTime AS endTime " +
            "FROM Activity a " +
            "JOIN a.group g " +
            "WHERE g.id = :groupId " +
            "AND (a.startDate = :date OR a.endDate = :date OR " +
            "(a.startDate <= :date AND (a.endDate >= :date OR a.endDate IS NULL))) " +
            "AND a.group IS NOT NULL " +
            "AND a.schedule IS NULL " +
            "ORDER BY a.startTime")
    List<ActivityProjection> findGroupActivitiesByDate(
            @Param("groupId") Long groupId,
            @Param("date") LocalDate date);



    // برای گزارش گیری حرف ای با پروجکشن
    @Query("SELECT a.title AS title, a.startTime AS startTime, a.endTime AS endTime, a.startDate AS startDate " +
            "FROM Activity a " +
            "JOIN a.schedule sch " +
            "JOIN sch.user u " +
            "WHERE u.id = :userId " +
            "AND (a.startDate BETWEEN :startDate AND :endDate OR a.endDate BETWEEN :startDate AND :endDate OR " +
            "(a.startDate <= :endDate AND (a.endDate >= :startDate OR a.endDate IS NULL))) " +
//            "AND a.group IS NULL " +
            "ORDER BY a.startTime")
    List<ActivityProjection2> findPersonalActivitiesByDateRange2(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT a.title AS title, a.startTime AS startTime, a.endTime AS endTime, a.startDate AS startDate " +
            "FROM Activity a " +
            "JOIN a.group g " +
            "WHERE g.id = :groupId " +
            "AND (a.startDate BETWEEN :startDate AND :endDate OR a.endDate BETWEEN :startDate AND :endDate OR " +
            "(a.startDate <= :endDate AND (a.endDate >= :startDate OR a.endDate IS NULL))) " +
            "AND a.group IS NOT NULL " +
            "AND a.schedule IS NULL " +
            "ORDER BY a.startTime")
    List<ActivityProjection2> findGroupActivityByDateRange2(
            @Param("groupId") Long groupId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);



}
