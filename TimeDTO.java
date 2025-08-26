package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

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
public class TimeDTO {
    private Long id;

    @NotNull(message = "تاریخ شروع الزامی است")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "زمان شروع الزامی است")
    private LocalTime startTime;

    @NotNull(message = "زمان پایان الزامی است")
    private LocalTime endTime;

//    @NotNull(message = "شناسه گروه الزامی است")
    private Long groupTableId;

//    @NotNull(message = "شناسه کاربر الزامی است")
    private Long userTableId;

    @NotNull(message = "شناسه روز هفته الزامی است")
    private Long weekDayId;
}
