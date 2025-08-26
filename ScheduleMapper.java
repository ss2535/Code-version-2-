package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

//import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ScheduleDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    ScheduleMapper INSTANCE = Mappers.getMapper(ScheduleMapper.class);

//    @Mapping(target = "scheduleType", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "activities", ignore = true)
//    Schedule toEntity(ScheduleDTO scheduleDTO);
//
//    @Mapping(target = "scheduleTypeId", source = "scheduleType.id")
//    @Mapping(target = "userId", source = "user.id")
//    ScheduleDTO toDTO(Schedule schedule);
}