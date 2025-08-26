package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.ActivityResponseDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    ActivityMapper INSTANCE = Mappers.getMapper(ActivityMapper.class);

    @Mapping(target = "weekDay", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "activityType", ignore = true)
//    @Mapping(target = "group", ignore = true)
    @Mapping(target = "accessLevel", ignore = true)
    Activity toEntity(ActivityDTO activityDTO);

    @Mapping(target = "weekDayId", source = "weekDay.id")
    @Mapping(target = "scheduleId", source = "schedule.id")
    @Mapping(target = "statusId", source = "status.id")
    @Mapping(target = "activityTypeId", source = "activityType.id")
//    @Mapping(target = "groupTableId", source = "group.id")
    @Mapping(target = "accessLevelId", source = "accessLevel.id")
    ActivityDTO toDTO(Activity activity);


    // برای برگرداندن تمام اطلاعات جدول فعالیت و رابطه هایش با جدل های دیگر استفاده کنم
    @Mapping(source = "weekDay.title" ,target = "weekDayTitle")
    @Mapping(source = "status.title" ,target = "statusTitle")
    @Mapping(source = "activityType.title" ,target = "activityTypeTitle")
    @Mapping(source = "accessLevel.title" ,target = "accessLevelTitle")
    ActivityResponseDTO toResponseDTO(Activity activity);

}