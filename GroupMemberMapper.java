package com.scheduleNetwork_version2.scheduleNetwork_version2.mapper;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupMemberDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.GroupMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {
    GroupMemberMapper INSTANCE = Mappers.getMapper(GroupMemberMapper.class);

    @Mapping(target = "groupTable", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "user", ignore = true)
    GroupMember toEntity(GroupMemberDTO groupMemberDTO);


    @Mapping(target = "groupTableId", source = "groupTable.id")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "userTableId", source = "user.id")

    @Mapping( target = "firstName",source = "user.firstName")
    @Mapping(target = "lastName" ,source = "user.lastName")
    @Mapping(target = "email" ,source = "user.email" )
    @Mapping(target = "roleTitle",source = "role.title")
    GroupMemberDTO toDTO(GroupMember groupMember);
}