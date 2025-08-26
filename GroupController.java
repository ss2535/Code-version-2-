package com.scheduleNetwork_version2.scheduleNetwork_version2.controller;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupMemberDTO;

import com.scheduleNetwork_version2.scheduleNetwork_version2.service.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/groups")
@AllArgsConstructor
public class GroupController {

    private final GroupService groupService;


    // http://localhost:8080/api/groups
    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody GroupDTO groupDTO,
                                                @RequestHeader("Authorization") String token){

            GroupDTO savedGroup= groupService.createGroup(groupDTO , token);
            return new ResponseEntity<>(savedGroup , HttpStatus.CREATED);
    }

    // http://localhost:8080/api/groups/my-group
    // http://localhost:8080/api/groups/my-group?page=0&size=10&sort=createdDate,desc
    @GetMapping("my-group")
    public ResponseEntity<Page<GroupDTO>> getUserGroupName(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate,desc") String sort) {

        // پردازش مرتب‌سازی
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(sortDirection, sortField);

        // ایجاد شیء Pageable
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<GroupDTO> groupPage = groupService.getUserGroup(token, pageable);
        return new ResponseEntity<>(groupPage, HttpStatus.OK);
    }

    // http://localhost:8080/api/groups/add-member/25
    @PostMapping("add-member/{userId}")
    public ResponseEntity<String> addUserToGroup(@RequestHeader("Authorization") String token
            ,@RequestHeader("Group-Id") Long groupId
            , @PathVariable Long userId){

            if (userId == null)
                return ResponseEntity.badRequest().body("شناسه کاربر الزامی هست");

            groupService.addUserToGroup(groupId , userId, token);
            return ResponseEntity.ok("کاربر با موفقیت به گروه اضافه شد ");

    }

    // http://localhost:8080/api/groups/remove-member/25
    @DeleteMapping("/remove-member/{userId}")
    public ResponseEntity<String> removeUserFromGroup(@RequestHeader("Authorization") String token
            , @RequestHeader("group-Id") Long groupId, @PathVariable Long userId){

            if (userId == null)
                return ResponseEntity.badRequest().body("شناسه کاربر الزامی هست ");

            groupService.removeUserFromGroup(groupId , userId , token);
            return new ResponseEntity<>("کاربر با موفقیت از گروه حذف شد" , HttpStatus.OK);

    }


    // http://localhost:8080/api/groups/members
    @GetMapping("/members")
    public ResponseEntity<List<GroupMemberDTO>> getGroupMember(@RequestHeader("Authorization") String token,
                                                               @RequestHeader("Group-Id") Long groupId){

            List<GroupMemberDTO> memberDTOS= groupService.getGroupMembers(groupId ,token);
            return new ResponseEntity<>(memberDTOS , HttpStatus.OK);
    }


}
