package com.scheduleNetwork_version2.scheduleNetwork_version2.service;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupMemberDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GroupService {

    GroupDTO createGroup(GroupDTO groupDTO, String token);

//    List<String> getUserGroupName(String token);
    // نشان دادن گروه هایی که یک کاربر دارد
//    List<GroupDTO> getUserGroup(String token);
    // برای صفحه بندی این گونه عوض میکنم
    Page<GroupDTO> getUserGroup(String token, Pageable pageable);

    // افزودن کاربر در گروه
    void addUserToGroup(Long groupId , Long userId , String token);

    // حذف کاربر از گروه
    void removeUserFromGroup(Long groupId ,Long userId , String token);

    //نشان دادن لیست اعضای گروه
    List<GroupMemberDTO> getGroupMembers(Long groupId , String token);

}
