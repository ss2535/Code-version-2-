package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// در گزارش گیری حرفه ای استفاده میکنم
public interface ActivityProjection2 {
    String getTitle();
    LocalTime getStartTime();
    LocalTime getEndTime();
    LocalDate getStartDate();
}
