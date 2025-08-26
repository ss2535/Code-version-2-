package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

// در متد نشان دادن تایم های خالی استفاده میکنم چون میخواهم فیلد های بیشتری را نشان دهم
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FreeTimeDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long groupTableId;
    private Long userTableId;

    private String firstName;
    private String lastName;
    private String email;
    private String weekDayTitle;


}
