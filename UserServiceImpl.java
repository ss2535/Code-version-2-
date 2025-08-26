package com.scheduleNetwork_version2.scheduleNetwork_version2.service.impl;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.UserDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ResourceNotFoundException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ValidationException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.mapper.UserMapper;
import com.scheduleNetwork_version2.scheduleNetwork_version2.repository.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.security.JwtService;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final GenderRepository genderRepository;
    private final AccessLevelRepository accessLevelRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ScheduleTypeRepository scheduleTypeRepository;

    private User validateUser(String identifier) {
        logger.debug("جستجوی کاربر با شناسه: {}", identifier);
        return userRepository.findUserByUsername(identifier)
                .orElseThrow(() -> {
                    logger.error("کاربر با نام کاربری {} یافت نشد", identifier);
                    return new ResourceNotFoundException("User", "username", identifier);
                });
    }

    // در متد لاگین استفاده شده
    @Override
    public UserDTO createUser(UserDTO userDTO) {

        logger.info("{}: شروع ایجاد کاربر با نام کاربری " , userDTO.getUsername());
        User user = userMapper.mapToUser(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setGender(genderRepository.findById(userDTO.getGenderId())
                .orElseThrow(() -> {
                    logger.error(" جنسیت با شناسه {} یافت نشد " , userDTO.getGenderId());
                    return new ResourceNotFoundException("Gender" ,"genderId" ,userDTO.getGenderId());
                }));

        if (userDTO.getAccessLevelId() != null) {
            user.setAccessLevel(accessLevelRepository.findById(userDTO.getAccessLevelId())
                    .orElseThrow(() ->{
                        logger.error(" سطح دسترسی با شناسه {} یافت نشد " , userDTO.getAccessLevelId());
                        return new ResourceNotFoundException("AccessLeve" , "accessLevel" , userDTO.getAccessLevelId());
                    }));
        }
        Set<Role> roles = Optional.ofNullable(userDTO.getRoleTitles())
                .orElse(Collections.emptyList())
                .stream()
                .map(title -> roleRepository.findByTitle(title)
                        .orElseThrow(() ->{
                            logger.error(" نقش با عنوان {} یافت نشد " , title);
                            return new ResourceNotFoundException("Role" ,"title" , title);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);


        // ایحاد برنامه هفتگی برای کاربر - رابطه یک به یک
        Schedule schedule= new Schedule();
        ScheduleType scheduleType = scheduleTypeRepository.findScheduleTypeByTitle("private")
                .orElseThrow(()-> {
                    logger.error(" نوع برنامه هفتگی خصوصی یافت نشد");
                    return new ResourceNotFoundException("ScheduleType", "title","private");
                });

        schedule.setScheduleType(scheduleType);
        // تنظیم رابطه یک به یک
        user.setSchedule(schedule);
        schedule.setUser(user);

        User savedUser = userRepository.save(user);
        logger.info(" کاربر با نام کاربری {} با موفقیت ایجاد شد " , userDTO.getUsername());
        return userMapper.mapToUserDto(savedUser);
    }


    @Override
    public UserDTO updateUser(UserDTO userDTO, String token) {
        logger.info("{}: شروع به روزرسانی با نام کاربری " , userDTO.getUsername());
        // استخراج نام کاربری از توکن و اعتبار سنجی
        String requestingUsername = extractUsernameFromToken(token);

        // یافتن کاربر بر اساس نام کاربری
        User userToUpdate = validateUser(requestingUsername);

        // به‌روزرسانی فیلدها
        if (userDTO.getAccessLevelId() != null) {
            userToUpdate.setAccessLevel(accessLevelRepository.findById(userDTO.getAccessLevelId())
                    .orElseThrow(() ->{
                        logger.error(" سطح دسترسی با شناسه {} یافت نشد " , userDTO.getAccessLevelId());
                        return new ResourceNotFoundException("AccessLevel" ,"accessLevelId" ,userDTO.getAccessLevelId());
                    }));
        }
        if (userDTO.getBiography() != null) {
            userToUpdate.setBiography(userDTO.getBiography());
        }
        if (userDTO.getEducation() != null) {
            userToUpdate.setEducation(userDTO.getEducation());
        }

        // ذخیره کاربر به‌روزرسانی‌شده
        User updatedUser = userRepository.save(userToUpdate);
        logger.info("کاربر با نام کاربری {} با موفقیت به‌روزرسانی شد", userDTO.getUsername());
        return userMapper.mapToUserDto(updatedUser);
    }


    @Transactional
    @Override
    public void deleteUser(String token) {
        logger.info(" شروع جذف کاربر ");
        // استخراج نام کاربری از توکن و اعتبار سنجی
        String requestingUsername = extractUsernameFromToken(token);

        // یافتن کاربر درخواست‌ دهنده
        User requestingUser = validateUser(requestingUsername);

        userRepository.deleteById(requestingUser.getId());
        logger.info( " کاربر با نام کاربری {} با موفقیت حذف شد " , requestingUser);
    }

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

    @Override
    public User findUserByUsername(String username) {
        logger.debug("{}: جستجوی کاربر با نام کاربری " , username);
        return validateUser(username);
    }




    @PersistenceContext
    private EntityManager entityManager;

    // criteria API
    @Override
    public List<UserDTO> searchUsers(String firstName, String lastName, String email) {
        logger.debug("{}ایمیل و{}نام خانوادگی و{}نام : شروع جستجوی کاربران با معیار ها " ,firstName ,lastName,email);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user =query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        if(firstName !=null && !firstName.isEmpty()){
            predicates.add(cb.equal(user.get("firstName") , firstName));
        }
        if(lastName !=null && !lastName.isEmpty()){
            predicates.add(cb.equal(user.get("lastName"), lastName));
        }
        if (email != null && !email.isEmpty()){
            predicates.add(cb.equal(user.get("email"), email));
        }
        query.select(user).where(predicates.toArray(new Predicate[0]));

        List<User> users;
        try{
            users= entityManager.createQuery(query).getResultList();
        }catch (Exception my){
            return new ArrayList<>();
        }

        return users.stream()
                .map(userMapper::mapToUserDto)
                .collect(Collectors.toList());
    }
}