package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank(message = "نام الزامی است")
    private String firstName;

    @NotBlank(message = "نام خانوادگی الزامی است")
    private String lastName;

    @NotBlank(message = "نام کاربری الزامی است")
    @Size(min = 3, max = 20, message = "نام کاربری باید بین ۳ تا ۲۰ کاراکتر باشد")
    private String username;

    @NotBlank(message = "رمز عبور الزامی است")
    @Size(min = 6, message = "رمز عبور باید حداقل ۶ کاراکتر باشد")
    private String password;

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "ایمیل باید معتبر باشد")
    private String email;

    @NotBlank(message = "شماره تلفن الزامی است")
    @Size(max = 11, message = "شماره تلفن باید حداکثر 11 کاراکتر باشد")
    private String phoneNumber;

    private String biography;

    private String education;

    @NotNull(message = "تاریخ تولد الزامی است")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

//    @NotNull(message = "شناسه سطح دسترسی الزامی است")
    private Long accessLevelId;

    @NotNull(message = "شناسه جنست الزامی است")
    private Long genderId;

    private List<String> roleTitles = new ArrayList<>();


    // فیلدهای مربوط به روابط یک به چند
    private List<Long> groupMemberIds;
    private List<Long> timeIds;
    private Long scheduleId;
}
