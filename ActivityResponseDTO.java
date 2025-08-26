package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

// این کلاس را برای برگرداندن تمام فعالیت ها بر اساس تاریخ محدود استفاده میکنم
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityResponseDTO {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private String location;
    private String weekDayTitle;
    private String statusTitle;
    private String activityTypeTitle;
    private String accessLevelTitle;

}
