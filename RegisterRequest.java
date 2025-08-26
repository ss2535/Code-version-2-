package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import java.time.LocalDate;
import java.util.List;

public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private LocalDate birthDate;
    private String username;
    private String password;
    private String education;
    private String biography;
    private Long genderId;
    private Long accessLevelId;
    private List<String> roleTitles; // لیست نام‌های نقش‌ها

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Long getGenderId() {
        return genderId;
    }

    public void setGenderId(Long genderId) {
        this.genderId = genderId;
    }

    public Long getAccessLevelId() {
        return accessLevelId;
    }

    public void setAccessLevelId(Long accessLevelId) {
        this.accessLevelId = accessLevelId;
    }

    public List<String> getRoleTitles() {
        return roleTitles;
    }

    public void setRoleTitles(List<String> roleTitles) {
        this.roleTitles = roleTitles;
    }
}