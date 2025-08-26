package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private Long id;

    @NotBlank(message = "نام گروه الزامی است")
    private String groupName;

    private String description;

//    @NotNull(message = "تاریخ ایجاد الزامی است") خودش پر میشود
    private LocalDate createdDate;

    @NotNull(message = "شناسه سطح دسترسی الزامی است")
    private Long accessLevelId;

    //
    // فیلدهای مربوط به روابط یک به چند
    private List<Long> groupMemberIds;
    private List<Long> activityIds;
    private List<Long> timeIds;
}
