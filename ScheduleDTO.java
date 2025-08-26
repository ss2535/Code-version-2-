//package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;
//
//import jakarta.validation.constraints.NotNull;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.util.List;
//
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//public class ScheduleDTO {
//    private Long id;
//
//    @NotNull(message = "شناسه نوع برنامه الزامی است")
//    private Long scheduleTypeId;
//
//    @NotNull(message = "شناسه کاربر الزامی است")
//    private Long userId;
//
//
//    // فیلدهای مربوط به روابط یک به چند
//    private List<Long> activityIds;
//}
