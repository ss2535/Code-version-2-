package com.scheduleNetwork_version2.scheduleNetwork_version2.service.impl;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupMemberDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ResourceNotFoundException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.SecurityException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ValidationException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.mapper.GroupMapper;
import com.scheduleNetwork_version2.scheduleNetwork_version2.mapper.GroupMemberMapper;
import com.scheduleNetwork_version2.scheduleNetwork_version2.repository.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.security.JwtService;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.GroupService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private static final Logger logger= LoggerFactory.getLogger(GroupServiceImpl.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final AccessLevelRepository accessLevelRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberMapper groupMemberMapper;


    private String extractUsernameFromToken(String token) {
        logger.debug(" استخراج نام کاربری از توکن ");
        if (token == null || !token.startsWith("Bearer ")) {
            logger.error("توکن نامعتبر ارائه شده است ");
            throw new ValidationException(" توکن نامعتبر است ");
        }
        String jwtToken = token.substring(7);
        Claims claims = jwtService.parseToken(jwtToken);
        String username = claims.getSubject();
        logger.debug("{}: نام کاربری استخراج شده " , username);
        return claims.getSubject();
    }

    private User validateUser(String token) {
        String userName = extractUsernameFromToken(token);
        return userRepository.findUserByUsername(userName)
                .orElseThrow(() -> {
                    logger.error("کاربر با نام کاربری {} یافت نشد", userName);
                    return new ResourceNotFoundException("User", "username", userName);
                });
    }

    private Group validateGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    logger.error("گروه با شناسه {} یافت نشد", groupId);
                    return new ResourceNotFoundException("Group", "groupId", groupId);
                });
    }

    @Transactional
    @Override
    public GroupDTO createGroup(GroupDTO groupDTO, String token) {

        logger.info(" {}: شروع ایجاد گروه با نام " , groupDTO.getGroupName());
        User requestingUser = validateUser(token);

            //ایجاد گروه جدید
            Group newGroup =new Group();
            newGroup.setGroupName(groupDTO.getGroupName());
            newGroup.setDescription(groupDTO.getDescription());
            newGroup.setCreatedDate(LocalDate.now());

            // تنظیم سطح دسترسی
            AccessLevel accessLevel = accessLevelRepository.findById(groupDTO.getAccessLevelId())
                    .orElseThrow(()->{
                        logger.error(" سطح دسترسی با شناسه {} یافت نشد " , groupDTO.getAccessLevelId());
                        return new ResourceNotFoundException("AccessLeve" , "accessLevel" , groupDTO.getAccessLevelId());
                    });
            newGroup.setAccessLevel(accessLevel);

            // ذخیره گروه
            Group savedGroup = groupRepository.save(newGroup);
            logger.debug(" گروه با شناسه {} ذخیره شد " ,savedGroup.getId());

            // ایجاد عضویت با نقش ادمین
            Role adminRole = roleRepository.findByTitle("ROLE_ADMIN")
                    .orElseThrow(()->{
                        logger.error(" نقش  ROLE_ADMIN  یافت نشد");
                        return new ResourceNotFoundException("Role" , "title" ,"ROLE_ADMIN");
                    });

            GroupMember groupMember = new GroupMember();
            groupMember.setGroupTable(savedGroup);
            groupMember.setUser(requestingUser);
            groupMember.setRole(adminRole);

            groupMemberRepository.save(groupMember);
            logger.info(" گروه با نام {} با موفقیت ایجاد شد" , groupDTO.getGroupName());

            return groupMapper.toDTO(savedGroup);
    }

    @Override
    public Page<GroupDTO> getUserGroup(String token, Pageable pageable) {
        logger.info("شروع دریافت گروه‌های کاربر با صفحه‌بندی");
        String username = extractUsernameFromToken(token);

        // دریافت گروه‌ها با صفحه‌بندی
        Page<Group> groupPage = groupRepository.findGroupNameByUserName(username, pageable);
        logger.debug("تعداد گروه‌های یافت‌شده برای کاربر {}: {}", username, groupPage.getTotalElements());

        // تبدیل گروه‌ها به DTO
        List<GroupDTO> groupDTOs = groupPage.getContent().stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());

        // برگرداندن نتیجه به صورت Page
        return new PageImpl<>(groupDTOs, pageable, groupPage.getTotalElements());
    }

    @Transactional
    @Override
    public void addUserToGroup(Long groupId, Long userId, String token) {

        logger.info(" شروع اضافه کردن کاربر {} به گروه" ,userId ,groupId);
        String requestingUsername = extractUsernameFromToken(token);
        User requestingUser = userRepository.findUserByUsername(requestingUsername)
                .orElseThrow(() ->{
                    logger.error(" کاربر با نام کاربری {} یافت نشد " , requestingUsername);
                    return new ResourceNotFoundException("User" , "username" , requestingUsername);
                });

        Group group= groupRepository.findById(groupId)
                .orElseThrow(()->{
                    logger.error(" گروه با شناسه {} یافت نشد " , groupId);
                    return new ResourceNotFoundException("Group","groupId" , groupId);
                });

        boolean isMember = groupMemberRepository.existsByUser_IdAndGroupTable_Id(requestingUser.getId() ,groupId);
        if(!isMember) {
            logger.warn(" کاربر {} عضو گروه {} نیست!" , requestingUser.getId() , groupId);
            throw new SecurityException(" شما عضو گروه نیستید ");
        }

        boolean isAdmin= groupMemberRepository.existsByUser_IdAndGroupTable_IdAndRole_Title(requestingUser.getId() ,groupId, "ROLE_ADMIN");
        if(!isAdmin) {
            logger.warn(" کاربر {} مجوز اضافه کردن به گروه {} را ندارد" , requestingUser.getId(),groupId);
            throw  new SecurityException(" شما مجوز اضافه کردن کاربر به گروه را ندارید ");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(()->{
                    logger.error( " کاربر با شناسه {} یافت نشد" ,userId);
                    return new ResourceNotFoundException("User" , "userId" , userId);
                });

        boolean alreadyMember= groupMemberRepository.existsByUser_IdAndGroupTable_Id(userId, groupId);
        if (alreadyMember) {
            logger.warn(" کاربر {} قبلا عضو گروه {} هست" , userId, groupId);
            throw new ValidationException(" کاربر قبلا عضو گروه هست ");
        }

        Role defoultRole = roleRepository.findByTitle("ROLE_MEMBER")
                .orElseThrow(()->{
                    logger.error(" نقش  ROLE_ADMIN  یافت نشد");
                    return new ResourceNotFoundException("Role" , "title" ,"ROLE_MEMBER");
                });

        GroupMember newMember= new GroupMember();
        newMember.setRole(defoultRole);
        newMember.setUser(userToAdd);
        newMember.setGroupTable(group);

        groupMemberRepository.save(newMember);
        logger.info(" کاربر {} با موفقیت به گروه {} اضافه شد " , userId , groupId);
    }

    @Transactional
    @Override
    public void removeUserFromGroup(Long groupId, Long userId, String token) {

        logger.info(" {} شروع حذف کاربر {} از گروه " , userId , groupId);
        String requestingUsername = extractUsernameFromToken(token);
        User requestingUser= userRepository.findUserByUsername(requestingUsername)
                .orElseThrow(()->{
                    logger.error(" کاربر با نام کاربری {} یافت نشد " , requestingUsername);
                    return new ResourceNotFoundException("User" , "username" , requestingUsername);
                });

        Group group= groupRepository.findById(groupId)
                .orElseThrow(()->{
                    logger.error(" گروه با شناسه {} یافت نشد " , groupId);
                    return new ResourceNotFoundException("Group","groupId" , groupId);
                });

        // بررسی عضویت کاربر درخواست دهنده - اینکه عضو گروه هست
        boolean isMember = groupMemberRepository.existsByUser_IdAndGroupTable_Id(requestingUser.getId(), groupId);
        if (! isMember) {
            logger.warn(" کاربر {} عضو گروه {} نیست!" , requestingUser.getId() , groupId);
            throw new SecurityException(" شما عضو گروه نیستید ");
        }

        // بررسی اینکه ادمین هست؟
        boolean isAdmin = groupMemberRepository.existsByUser_IdAndGroupTable_IdAndRole_Title(requestingUser.getId() ,groupId ,"ROLE_ADMIN");
        if (!isAdmin) {
            logger.warn(" کاربر {} مجوز جذف از گروه {} را ندارد!" , requestingUser.getId() , groupId);
            throw new SecurityException(" شما مجوز حذف کاربر را ندارید ");
        }

        // جلوگیری از حذف خود ادمین
        if (requestingUser.getId().equals(userId)) {
            logger.warn(" کاربر {} نمیتواند خودش را از گروه {} حذف کند " , userId , groupId);
            throw new ValidationException(" نمیتوانید خودتان را حذف کنید ");
        }

        //  بررسی عضویت کاربر هدف (از بدنه- اینکه اصلا عضو گروه هست نخواهیم حذف کنیم)
        boolean isTargetMember = groupMemberRepository.existsByUser_IdAndGroupTable_Id(userId , groupId);
        if(!isTargetMember) {
            logger.warn(" کاربر {} در گروه {} عضو نیست " , userId, groupId);
            throw new ResourceNotFoundException("User" , "userId" , userId);
        }

        // حذف عضویت
            groupMemberRepository.deleteByUser_IdAndGroupTable_Id(userId ,groupId);
        logger.info(" کاربر {} با موفقیت از گروه {} حذف شد " , userId, groupId);
    }

    @Override
    public List<GroupMemberDTO> getGroupMembers(Long groupId, String token) {

        logger.info("{} شروع دریافت اعضای گروه " , groupId);
        String requestingUserName = extractUsernameFromToken(token);
        User requestingUser =userRepository.findUserByUsername(requestingUserName)
                .orElseThrow(()->{
                    logger.error("کاربر با نام کاربری {} یافت نشد " , requestingUserName);
                    return new ResourceNotFoundException("User" , "username" , requestingUserName);
                });

        Group group= groupRepository.findById(groupId)
                .orElseThrow(()->{
                    logger.error("گروه با شناسه {} یافت نشد " , groupId);
                    return new ResourceNotFoundException("Group" ,"groupId" ,groupId);
                });

        boolean isMember= groupMemberRepository.existsByUser_IdAndGroupTable_Id(requestingUser.getId() , groupId);
        if (!isMember) {
            logger.warn("کاربر {} عضو گروه {} نیست " , requestingUser.getId() , groupId);
            throw new SecurityException("شما عضو گروه نیستید  ");
        }

        List<GroupMember> members= groupMemberRepository.findByGroupTable_Id(groupId);
        logger.debug(" {}:{} تعداد اعضای یافت شده در گروه " , groupId , members.size());
        return members.stream().map(groupMemberMapper::toDTO)
                .collect(Collectors.toList());
    }


}
