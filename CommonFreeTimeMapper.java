package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.CommonFreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Time;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommonFreeTimeMapper {
    CommonFreeTimeMapper INSTANCE = Mappers.getMapper(CommonFreeTimeMapper.class);

    @Mapping(source = "weekDay.title", target = "weekDayTitle")
    @Mapping(source = "group.id", target = "groupTableId")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    CommonFreeTimeDTO toDTO(Time time);
}