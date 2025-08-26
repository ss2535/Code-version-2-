package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// این کلاس را در نشان دادن لیست اعضای گروه در صفحه داخل گروه استفاده میکنم
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDTO {
    private Long id;

    private Long groupTableId;

    private Long roleId;

    private Long userTableId;

    private String firstName;
    private String lastName;
    private String email;
    private String roleTitle;
}
