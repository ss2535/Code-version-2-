package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
public class ActivityReportDTO {
    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    private long durationInMinutes;

//    public ActivityReportDTO(String title, Time startTime, Time endTime, long durationInMinutes) {
//        this.title = title;
//        this.startTime = startTime != null ? startTime.toLocalTime() : null;
//        this.endTime = endTime != null ? endTime.toLocalTime() : null;
//        this.durationInMinutes = durationInMinutes;
//    }
    public ActivityReportDTO(String title, LocalTime startTime, LocalTime endTime, long durationInMinutes) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationInMinutes = durationInMinutes;
    }
}