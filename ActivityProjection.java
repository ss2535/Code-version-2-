package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import java.time.LocalTime;

// در گزارش گیری روزانه استفاده میکنم
public interface ActivityProjection {
    String getTitle();
    LocalTime getStartTime();
    LocalTime getEndTime();
}