package com.scheduleNetwork_version2.scheduleNetwork_version2.controller;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityReportResponseDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityResponseDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.AdvancedActivityReportResponseDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.ActivityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.internal.bytebuddy.asm.Advice;
import org.modelmapper.internal.bytebuddy.build.HashCodeAndEqualsPlugin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/activity")
@AllArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    // http://localhost:8080/api/activity/personal
    @PostMapping("/personal")
    public ResponseEntity<ActivityDTO> addPersonalActivity(@Valid @RequestBody ActivityDTO activityDTO,
                                                           @RequestHeader("Authorization") String token){

            ActivityDTO savedActivityDto = activityService.addPersonalActivity(activityDTO, token);
            return new ResponseEntity<>(savedActivityDto , HttpStatus.CREATED);
    }

    // http://localhost:8080/api/activity/personal/range?startDate=2025-07-31&endDate=2025-08-20
    @GetMapping("/personal/range")
    public ResponseEntity<List<ActivityResponseDTO>> getPersonalActivitiesByDateRange(
            @RequestParam("startDate")LocalDate startDate, @RequestParam("endDate") LocalDate endDate,
            @RequestHeader("Authorization") String token){

            List<ActivityResponseDTO> activityResponseDTOS= activityService.getPersonalActivitiesByDateRange(startDate,endDate,token);
            return new ResponseEntity<>(activityResponseDTOS , HttpStatus.OK);

    }


    // http://localhost:8080/api/activity/personal/48
    @PutMapping("/personal/{activityId}")
    public ResponseEntity<ActivityResponseDTO> updatePersonalActivity(@RequestHeader("Authorization") String token,
                                                                      @RequestBody ActivityDTO activityDTO,
                                                                      @PathVariable Long activityId){

            ActivityResponseDTO updatedActivity= activityService.updatePersonalActivity(activityId,activityDTO,token);
            return new ResponseEntity<>(updatedActivity , HttpStatus.OK);
    }


    // http://localhost:8080/api/activity/personal/26
    @DeleteMapping("/personal/{activityID}")
    public ResponseEntity<String> deletePersonalActivity(@RequestHeader("Authorization") String token ,
                                                         @PathVariable Long activityID){

            activityService.deletePersonalActivity(activityID , token);
            return new ResponseEntity<>(" فعالیت با موفقیت حذف شد ", HttpStatus.OK);

    }

    // http://localhost:8080/api/activity/group
    @PostMapping("/group")
    public ResponseEntity<ActivityResponseDTO> addGroupActivity(@Valid @RequestBody ActivityDTO activityDTO ,
                                                                @RequestHeader("Authorization") String token ,
                                                                @RequestHeader("Group-Id") Long groupId){

            ActivityResponseDTO savedActivity= activityService.addGroupActivity(activityDTO, groupId, token);
            return new ResponseEntity<>(savedActivity , HttpStatus.CREATED);
    }


    // http://localhost:8080/api/activity/group/range?startDate=2025-07-31&endDate=2025-08-30
    @GetMapping("/group/range")
    public ResponseEntity<List<ActivityResponseDTO>> getGroupActivityByDateRange(
            @RequestHeader("Authorization") String token, @RequestHeader("Group-Id") Long groupId,
            @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate){

            List<ActivityResponseDTO> activityResponseDTOS= activityService.getGroupActivityByDateRange(groupId,
                    startDate, endDate , token);
            return new ResponseEntity<>(activityResponseDTOS , HttpStatus.OK);
    }

    // http://localhost:8080/api/activity/group/37
    @PutMapping("/group/{activityId}")
    public ResponseEntity<ActivityResponseDTO> updateGroupActivity(@RequestHeader("Authorization") String token,
            @RequestHeader("Group-Id") Long groupId, @PathVariable Long activityId,
            @RequestBody ActivityDTO activityDTO) {

            ActivityResponseDTO updatedActivity = activityService.updateGroupActivity(activityId , activityDTO, groupId, token);
            return new ResponseEntity<>(updatedActivity, HttpStatus.OK);
    }

    // حذف فعالیت گروهی
    // http://localhost:8080/api/activity/group/33
    @DeleteMapping("/group/{activityId}")
    public ResponseEntity<String> deleteGroupActivity(
            @RequestHeader("Authorization") String token, @RequestHeader("Group-Id") Long groupId,
            @PathVariable Long activityId) {

            activityService.deleteGroupActivity(activityId, groupId, token);
            return new ResponseEntity<>("فعالیت گروهی با موفقیت حذف شد", HttpStatus.OK);
    }


    // گزارش شخصی
    // http://localhost:8080/api/activity/personal/report?date=2025-08-10
    @GetMapping("/personal/report")
    public ResponseEntity<ActivityReportResponseDTO> getPersonalActivityReport(
            @RequestParam("date") LocalDate date,
            @RequestHeader("Authorization") String token) {
        ActivityReportResponseDTO response = activityService.getPersonalActivityReportByDate(date, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // گزارش گروهی
    // http://localhost:8080/api/activity/group/report?date=2025-08-10
    @GetMapping("/group/report")
    public ResponseEntity<ActivityReportResponseDTO> getGroupActivityReport(
            @RequestParam("date") LocalDate date,
            @RequestHeader("Authorization") String token,
            @RequestHeader("Group-Id") Long groupId) {
        ActivityReportResponseDTO response = activityService.getGroupActivityReportByDate(groupId, date, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // گزارش گیری حرفه ای بر اساس تاریخ شروع و پایان - برای فعالیت شخصی
    // http://localhost:8080/api/activity/personal/report/range?startDate=2025-07-01&endDate=2025-08-30
    @GetMapping("/personal/report/range")
    public ResponseEntity<AdvancedActivityReportResponseDTO> getPersonalActivityReportByDateRange(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestHeader("Authorization") String token) {
        AdvancedActivityReportResponseDTO response = activityService.getPersonalActivityReportByDateRange(startDate, endDate, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // گزارش گیری حرفه ای بر اساس تاریخ شروع و پایان - برای فعالیت گروهی
    // http://localhost:8080/api/activity/group/report/range?startDate=2025-07-01&endDate=2025-08-30
    @GetMapping("/group/report/range")
    public ResponseEntity<AdvancedActivityReportResponseDTO> getGroupActivityReportByDateRange(
            @RequestHeader("Authorization") String token,
            @RequestHeader("Group-Id") Long groupId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        AdvancedActivityReportResponseDTO response = activityService.getGroupActivityReportByDateRange(groupId, startDate, endDate, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




}
