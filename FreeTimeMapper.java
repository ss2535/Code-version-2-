package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.FreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Time;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

// برای مپ کردن در متد نشان دادن تایم های خالی استفاده میکنم
@Mapper(componentModel = "spring")
public interface FreeTimeMapper {
    FreeTimeMapper INSTANCE = Mappers.getMapper(FreeTimeMapper.class);

    @Mapping(source = "user.id" , target = "userTableId")
    @Mapping(source = "group.id" , target = "groupTableId")
    @Mapping(source = "user.firstName" , target = "firstName")
    @Mapping(source = "user.lastName" , target = "lastName")
    @Mapping(source = "user.email" , target = "email")
    @Mapping(source = "weekDay.title" , target = "weekDayTitle")
    FreeTimeDTO toDTO(Time time);

}
