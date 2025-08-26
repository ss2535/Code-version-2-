package com.scheduleNetwork_version2.scheduleNetwork_version2.service;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.CommonFreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.FreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.TimeDTO;

import java.time.LocalDate;
import java.util.List;

public interface TimeService {

    TimeDTO addFreeTime(TimeDTO request ,Long groupId , String token);

    List<FreeTimeDTO> getFreeTimes(Long groupId , String token);

    TimeDTO updateFreeTime(TimeDTO timeDTO ,Long groupId,String token , long timeId);

    void deleteFreeTime(Long timeId , Long groupId , String token);



    List<CommonFreeTimeDTO> findCommonFreeTimes(Long groupId, LocalDate startDateRange, LocalDate endDateRange, String token);

}
