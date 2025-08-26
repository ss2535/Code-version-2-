package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// برای گزارش گیری حرفه ای بر اساس تاریخ شروع و پایان - برای فعالیت شخصی و گروهی
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupedActivityReportDTO {
    private String title;
    private long totalMinutes;
    private double percentage;
    private long count; // تعداد تکرار فعالیت (برای topActivities)

    // سازنده برای گروه‌بندی بدون تعداد
    public GroupedActivityReportDTO(String title, long totalMinutes, double percentage) {
        this.title = title;
        this.totalMinutes = totalMinutes;
        this.percentage = percentage;
        this.count = 0;
    }
}