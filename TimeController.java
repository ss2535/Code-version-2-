package com.scheduleNetwork_version2.scheduleNetwork_version2.controller;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.CommonFreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.FreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.TimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.TimeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("api/time")
@AllArgsConstructor
public class TimeController {

    private final TimeService timeService;

    // http://localhost:8080/api/time/add-free-time
    @PostMapping("/add-free-time")
    public ResponseEntity<TimeDTO> addFreeTime(@Valid @RequestBody TimeDTO request
            ,@RequestHeader("Authorization") String token , @RequestHeader("Group-Id") Long groupId){

            TimeDTO savedTime = timeService.addFreeTime(request,groupId,token);
            return  new ResponseEntity<>(savedTime , HttpStatus.CREATED);
    }


    // http://localhost:8080/api/time/free-times
    @GetMapping("/free-times")
    public ResponseEntity<List<FreeTimeDTO>> getFreeTimes(@RequestHeader("Authorization") String token,
            @RequestHeader("Group-Id") Long groupId){

            List<FreeTimeDTO> times = timeService.getFreeTimes(groupId,token);
            return new ResponseEntity<>(times, HttpStatus.OK);
    }


    // http://localhost:8080/api/time/update-free-time/14
    @PutMapping("/update-free-time/{timeId}")
    public ResponseEntity<TimeDTO> updateFreeTime(@RequestHeader("Authorization") String token,
                                                  @RequestHeader("Group-Id") long groupId ,
                                                  @PathVariable long timeId,
                                                  @RequestBody TimeDTO timeDTO){
        TimeDTO updatedTime= timeService.updateFreeTime(timeDTO,groupId,token ,timeId);
        return new ResponseEntity<>(updatedTime,HttpStatus.OK);
    }


    // http://localhost:8080/api/time/delete-free-time/14
    @DeleteMapping("/delete-free-time/{timeId}")
    public ResponseEntity<Void> deleteFreeTime(@RequestHeader("Authorization") String token ,
                                               @RequestHeader("Group-Id") Long groupId,
                                               @PathVariable Long timeId){
        timeService.deleteFreeTime(timeId,groupId,token);
        return ResponseEntity.status(HttpStatus.OK).body(null);

    }


    // http://localhost:8080/api/time/common-free-times?startDate=2025-08-01&endDate=2025-08-20
    @GetMapping("/common-free-times")
    public ResponseEntity<List<CommonFreeTimeDTO>> getCommonFreeTimes(
            @RequestHeader("Authorization") String token,
            @RequestHeader("Group-Id") Long groupId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateRange,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateRange) {

            List<CommonFreeTimeDTO> commonFreeTimes = timeService.findCommonFreeTimes(groupId, startDateRange, endDateRange, token);
            return new ResponseEntity<>(commonFreeTimes, HttpStatus.OK);
    }

}
