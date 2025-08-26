package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.TimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Time;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TimeMapper {

    TimeMapper INSTANCE = Mappers.getMapper(TimeMapper.class);

    @Mapping(target = "weekDay" ,ignore = true)
    @Mapping(target = "group" ,ignore = true)
    @Mapping(target = "user" , ignore = true)
    Time toEntity(TimeDTO timeDTO);

    @Mapping(source = "user.id" , target = "userTableId")
    @Mapping(source = "group.id" , target = "groupTableId")
    @Mapping(source = "weekDay.id" , target = "weekDayId")
    TimeDTO toDTO(Time time);
}
