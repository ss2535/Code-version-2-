package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// برای نشان دادن گزارش استفاده میکنم
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityReportResponseDTO {
    private List<ActivityReportDTO> activities;
    private double totalHours;
}