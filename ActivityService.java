package com.scheduleNetwork_version2.scheduleNetwork_version2.service;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityReportResponseDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityResponseDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.AdvancedActivityReportResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ActivityService {

    // اضافه کردن فعالیت شخصی
    ActivityDTO addPersonalActivity(ActivityDTO activityDTO , String token);

    // برای نشان دادن تمام اطلاعات فعالیت به همراه روابط بین جدول ها برای نشان دادن فعالیت های برنامه هفتگی شخصی (شخصی و گروهی)
    List<ActivityResponseDTO> getPersonalActivitiesByDateRange(LocalDate startDate , LocalDate endDate , String token);


    // ویرایش فعالیت شخصی
    ActivityResponseDTO updatePersonalActivity(Long activityId , ActivityDTO activityDTO , String token);

    //حذف فعالیت شخصی
    void deletePersonalActivity(Long activityId , String token);


    // اضافه کردن فعالیت گروهی توسط ادمین و کپی شدن به برنامه هفتگی همه اعضای گروه
    ActivityResponseDTO addGroupActivity(ActivityDTO activityDTO , Long groupId , String token);

    // نشان دادن فعالیت های گروهی در بازه زمانی که از کاربر میگیریم
     List<ActivityResponseDTO> getGroupActivityByDateRange(Long groupId,LocalDate startDate, LocalDate endDate, String token);

    // برای ویرایش فعالیت گروهی استفاده میکنم
    ActivityResponseDTO updateGroupActivity(Long activityId , ActivityDTO activityDTO, Long groupId , String token);

    //برای حذف فعالیت گروهی استفاده میکنم
    void deleteGroupActivity(Long activityId, Long groupId , String token);

    // برای نشان دادن گزارش شخصی استفاده میکنم
    ActivityReportResponseDTO getPersonalActivityReportByDate(LocalDate date, String token);


    //برای نشان دادن گزارش گروهی استفاده میکنم
    ActivityReportResponseDTO getGroupActivityReportByDate(Long groupId, LocalDate date, String token);

    // گزارش گیری حرفه ای بر اساس تاریخ شروع و پایان - برای فعالیت شخصی
    AdvancedActivityReportResponseDTO getPersonalActivityReportByDateRange(LocalDate startDate, LocalDate endDate, String token);

    // گزارش گیری حرفه ای بر اساس تاریخ شروع و پایان - برای فعالیت گروهی
    AdvancedActivityReportResponseDTO getGroupActivityReportByDateRange(Long groupId, LocalDate startDate, LocalDate endDate, String token);
}
