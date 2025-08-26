package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeRepository extends JpaRepository<Time, Long> {

        List<Time> findByGroup_Id(Long groupTableId);

        // کوئری برای پیدا کردن تایم هایی که همه تایم خالی دارند
//        @Query(value = "WITH MemberTimes AS (" +
//        "    SELECT t.week_day_id, t.start_date, t.end_date, " +
//        "           MIN(t.start_time) AS min_start_time, MAX(t.end_time) AS max_end_time, " +
//        "           ARRAY_AGG(t.user_table_id) AS user_ids " +
//        "    FROM time t " +
//        "    JOIN group_member gm ON t.user_table_id = gm.user_table_id " +
//        "    WHERE gm.group_table_id = ?1 " +
//        "    AND t.start_date BETWEEN ?2 AND ?3 " +
//        "    GROUP BY t.week_day_id, t.start_date, t.end_date " +
//        "), " +
//        "CommonTimes AS (" +
//        "    SELECT mt.week_day_id, mt.start_date, mt.end_date, " +
//        "           (SELECT MAX(start_time) FROM time t2 " +
//        "            WHERE t2.week_day_id = mt.week_day_id " +
//        "            AND t2.start_date = mt.start_date " +
//        "            AND t2.end_date = mt.end_date " +
//        "            AND t2.user_table_id = ANY(mt.user_ids)) AS common_start_time, " +
//        "           (SELECT MIN(end_time) FROM time t2 " +
//        "            WHERE t2.week_day_id = mt.week_day_id " +
//        "            AND t2.start_date = mt.start_date " +
//        "            AND t2.end_date = mt.end_date " +
//        "            AND t2.user_table_id = ANY(mt.user_ids)) AS common_end_time " +
//        "    FROM MemberTimes mt " +
//        "    WHERE (SELECT COUNT(DISTINCT user_table_id) FROM time t2 " +
//        "           WHERE t2.week_day_id = mt.week_day_id " +
//        "           AND t2.start_date = mt.start_date " +
//        "           AND t2.end_date = mt.end_date " +
//        "           AND t2.user_table_id IN (SELECT user_table_id FROM group_member WHERE group_table_id = ?1)) " +
//        "           = (SELECT COUNT(DISTINCT user_table_id) FROM time t2 " +
//        "              WHERE t2.user_table_id IN (SELECT user_table_id FROM group_member WHERE group_table_id = ?1)) " +
//        ") " +
//        "SELECT ct.week_day_id, wd.title AS week_day_title, ct.start_date, ct.end_date, " +
//        "       ct.common_start_time AS start_time, ct.common_end_time AS end_time, ?1 AS group_table_id " +
//        "FROM CommonTimes ct " +
//        "JOIN week_day wd ON wd.id = ct.week_day_id " +
//        "WHERE ct.common_start_time < ct.common_end_time;",
//        nativeQuery = true)
//        List<Object[]> findCommonFreeTimes(Long groupId, LocalDate startDateRange, LocalDate endDateRange);
        // این سری الگوریتم اسلات دار استفاده میکنم و منطقش به زبان جاوا هست ولی قبلی خود پایگاه داده بررسی و محاسبات میکرد که البته یکم اشتباه جواب میداد
        @Query("SELECT t FROM Time t " +
                "JOIN FETCH t.weekDay wd " +
                "JOIN FETCH t.user u " +
                "JOIN FETCH t.group g " +
                "WHERE g.id = :groupId " +
                "AND t.startDate BETWEEN :startDateRange AND :endDateRange")
        List<Time> findFreeTimesByGroupAndDateRange(@Param("groupId") Long groupId,
                                                    @Param("startDateRange") LocalDate startDateRange,
                                                    @Param("endDateRange") LocalDate endDateRange);


}
