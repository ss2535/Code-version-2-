package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    @Mapping(target = "accessLevel", ignore = true)
    @Mapping(target = "groupMembers", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "times", ignore = true)
    Group toEntity(GroupDTO groupDTO);

    @Mapping(target = "accessLevelId", source = "accessLevel.id")
    GroupDTO toDTO(Group group);
}