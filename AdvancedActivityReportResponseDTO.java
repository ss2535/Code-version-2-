package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

// برای گزارش گیری حرفه ای بر اساس تاریخ شروع و پایان - برای قعالیت های شخصی و گروهی
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdvancedActivityReportResponseDTO {
    private List<GroupedActivityReportDTO> activities;
    private double totalHours;
    private double averageDailyHours;
    private LocalDate mostActiveDay;
    private List<GroupedActivityReportDTO> topActivities;
    private String summary;
}