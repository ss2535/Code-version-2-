package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.UserDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring") // اضافه کردن componentModel
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "gender.id", target = "genderId")
    @Mapping(source = "accessLevel.id", target = "accessLevelId")
    UserDTO mapToUserDto(User user);

    @Mapping(target = "password", source = "password") // افزودن این خط
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "accessLevel", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User mapToUser(UserDTO userDTO);

}


