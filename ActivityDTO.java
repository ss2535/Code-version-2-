package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDTO {
    private Long id;

    @NotBlank(message = "عنوان الزامی است")
    private String title;

    private String description;

    @NotNull(message = "تاریخ شروع الزامی است")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "زمان شروع الزامی است")
    private LocalTime startTime;

    @NotNull(message = "زمان پایان الزامی است")
    private LocalTime endTime;

    private String location;



    @NotNull(message = " نوع فعالیت الزامی است")
    private Long activityTypeId;

//    @NotNull(message = "شناسه گروه الزامی است") الزامی نیست موقع ایجاد برنامه گروهی مقدار میگیرد و در غیر این صورت نال هست
    private Long groupTableId;

//    @NotNull(message = "شناسه برنامه هفتگی الزامی است")
    private Long scheduleId;

    @NotNull(message = "شناسه وضعیت الزامی است")
    private Long statusId;

    @NotNull(message = "شناسه روز هفته الزامی است")
    private Long weekDayId;

    @NotNull(message = "شناسه سطح دسترسی الزامی است")
    private Long accessLevelId;
}
