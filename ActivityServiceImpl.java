package com.scheduleNetwork_version2.scheduleNetwork_version2.service.impl;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ResourceNotFoundException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.SecurityException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ValidationException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.mapper.ActivityMapper;
import com.scheduleNetwork_version2.scheduleNetwork_version2.repository.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.security.JwtService;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.ActivityService;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private static final Logger logger= LoggerFactory.getLogger(ActivityServiceImpl.class);

    private final ActivityRepository activityRepository;
    private final JwtService jwtService;
    private final ActivityMapper activityMapper;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final WeekDayRepository weekDayRepository;
    private final StatusRepository statusRepository;
    private final AccessLevelRepository accessLevelRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

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

    private Group validateGroup(Long groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    logger.error("گروه با شناسه {} یافت نشد", groupId);
                    return new ResourceNotFoundException("Group", "groupId", groupId);
                });
        boolean isMember = groupMemberRepository.existsByUser_IdAndGroupTable_Id(user.getId(), groupId);
        if (!isMember) {
            logger.warn("کاربر {} عضو گروه {} نیست", user.getUsername(), groupId);
            throw new SecurityException("شما عضو گروه نیستید");
        }
        return group;
    }

    private void validateAdminRole(User user, Long groupId) {
        boolean isAdmin = groupMemberRepository.existsByUser_IdAndGroupTable_IdAndRole_Title(user.getId(), groupId, "ROLE_ADMIN");
        if (!isAdmin) {
            logger.warn("کاربر {} با نقش ادمین برای گروه {} ندارد", user.getUsername(), groupId);
            throw new SecurityException("فقط ادمین می‌تواند فعالیت گروهی اضافه کند");
        }
    }

    private void validateActivityTime(ActivityDTO activityDTO) {
        if (activityDTO.getEndTime() != null && activityDTO.getStartTime() != null) {
            if (activityDTO.getEndTime().isBefore(activityDTO.getStartTime())) {
                logger.warn("زمان شروع {} بعد از زمان پایان {} است", activityDTO.getStartTime(), activityDTO.getEndTime());
                throw new ValidationException("زمان شروع باید قبل از زمان پایان باشد");
            }
        }
        if (activityDTO.getEndDate() != null && activityDTO.getStartDate() != null) {
            if (activityDTO.getEndDate().isBefore(activityDTO.getStartDate())) {
                logger.warn("تاریخ شروع {} بعد از تاریخ پایان {} است", activityDTO.getStartDate(), activityDTO.getEndDate());
                throw new ValidationException("تاریخ شروع باید قبل از تاریخ پایان باشد");
            }
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            logger.error("تاریخ شروع یا پایان نامعتبر است");
            throw new ValidationException("تاریخ شروع و پایان نمی‌توانند خالی باشند");
        }
        if (startDate.isAfter(endDate)) {
            logger.warn("تاریخ شروع {} بعد از تاریخ پایان {} است", startDate, endDate);
            throw new ValidationException("تاریخ شروع باید قبل از تاریخ پایان باشد");
        }
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 180) {
            logger.warn("بازه زمانی بیش از 6 ماه است");
            throw new ValidationException("بازه زمانی نمی‌تواند بیش از 6 ماه باشد");
        }
    }

    // اینو توی ابدیت استفاده میکنم که فیلدها میتواند اختیاری بفرستند
    private void validateActivityTime2(ActivityDTO activityDTO, Activity existingActivity) {
        // چک کردن زمان‌ها فقط اگر endTime یا startTime در DTO مقدار داشته باشند
        if (activityDTO.getEndTime() != null && activityDTO.getStartTime() != null) {
            if (activityDTO.getEndTime().isBefore(activityDTO.getStartTime())) {
                logger.warn("زمان شروع {} بعد از زمان پایان {} است", activityDTO.getStartTime(), activityDTO.getEndTime());
                throw new ValidationException("زمان شروع باید قبل از زمان پایان باشد");
            }
        } else if (activityDTO.getEndTime() != null) {
            // اگر فقط endTime مقدار داره، با startTime موجود در دیتابیس مقایسه کن
            LocalTime currentStartTime = existingActivity.getStartTime();
            if (currentStartTime == null) {
                logger.warn("زمان شروع فعالیت موجود null است و نمی‌توان با endTime جدید مقایسه کرد");
                throw new ValidationException("زمان شروع فعالیت موجود باید مشخص باشد");
            }
            if (activityDTO.getEndTime().isBefore(currentStartTime)) {
                logger.warn("زمان پایان جدید {} قبل از زمان شروع موجود {} است", activityDTO.getEndTime(), currentStartTime);
                throw new ValidationException("زمان پایان جدید باید بعد از زمان شروع موجود باشد");
            }
        }

        // چک کردن تاریخ‌ها
        if (activityDTO.getEndDate() != null && activityDTO.getStartDate() != null) {
            if (activityDTO.getEndDate().isBefore(activityDTO.getStartDate())) {
                logger.warn("تاریخ شروع {} بعد از تاریخ پایان {} است", activityDTO.getStartDate(), activityDTO.getEndDate());
                throw new ValidationException("تاریخ شروع باید قبل از تاریخ پایان باشد");
            }
        } else if (activityDTO.getEndDate() != null) {
            // اگر فقط endDate مقدار داره، با startDate موجود در دیتابیس مقایسه کن
            LocalDate currentStartDate = existingActivity.getStartDate();
            if (currentStartDate == null) {
                logger.warn("تاریخ شروع فعالیت موجود null است و نمی‌توان با endDate جدید مقایسه کرد");
                throw new ValidationException("تاریخ شروع فعالیت موجود باید مشخص باشد");
            }
            if (activityDTO.getEndDate().isBefore(currentStartDate)) {
                logger.warn("تاریخ پایان جدید {} قبل از تاریخ شروع موجود {} است", activityDTO.getEndDate(), currentStartDate);
                throw new ValidationException("تاریخ پایان جدید باید بعد از تاریخ شروع موجود باشد");
            }
        }
    }


    @Transactional
    @Override
    public ActivityDTO addPersonalActivity(ActivityDTO activityDTO, String token) {
        logger.info(" شروع اضافه کردن فعالیت شخصی برای کاربر ");
        // استخراج نام کاربری از توکن و اعتبار سنجی
        User user = validateUser(token);

        //پیدا کردن برنامه هفتگی
        Schedule schedule= scheduleRepository.findByUser_Id(user.getId())
                .orElseThrow(()->{
                    logger.error(" برنامه هفتگی برای کاربر {} یافت نشد " , user.getUsername());
                    return new ResourceNotFoundException("Schedule" ,"userId" ,user.getId());
                });

        // بررسی تداخل زمانی
        List<Activity> conflictingActivities = activityRepository.findConflictingActivities(
                schedule.getId(),
                activityDTO.getStartDate(),
                activityDTO.getEndDate(),
                activityDTO.getStartTime(),
                activityDTO.getEndTime());
        if(!conflictingActivities.isEmpty()) {
            logger.warn(" تداخل زمانی برای کاربر {} با فعالیت های موجود " ,user.getUsername());
            throw new ValidationException(" تداخل زمانی با فعالیت های موجود وجود دارد");
        }

        // بررسی اینکه زمان و تاریخ شروع بزرگتر از زمان پایان نباشد
        validateActivityTime(activityDTO);

        Activity activity= activityMapper.toEntity(activityDTO);
        activity.setSchedule(schedule);

        WeekDay weekDay= weekDayRepository.findById(activityDTO.getWeekDayId())
                .orElseThrow(()->{
                    logger.error("روز هفته با شناسه {} یافت نشد " ,activityDTO.getWeekDayId());
                    return new ResourceNotFoundException("WeekDay","weekDayId" ,activityDTO.getWeekDayId());
                });
        activity.setWeekDay(weekDay);

        Status status= statusRepository.findById(activityDTO.getStatusId())
                .orElseThrow(()->{
                    logger.error(" وضعیت با شناسه {} یافت نشد " , activityDTO.getStatusId());
                    return new ResourceNotFoundException("Status" ,"statusId" ,activityDTO.getStatusId());
                });
        activity.setStatus(status);

        ActivityType activityType= activityTypeRepository.findById(activityDTO.getActivityTypeId())
                .orElseThrow(()->{
                    logger.error(" نوع فعالیت با شناسه {} یافت نشد" ,activityDTO.getActivityTypeId());
                    return new ResourceNotFoundException("ActivityType","activityType",activityDTO.getActivityTypeId());
                });
        activity.setActivityType(activityType);

        AccessLevel accessLevel= accessLevelRepository.findById(activityDTO.getAccessLevelId())
                .orElseThrow(()->{
                    logger.error(" سطح دسترسی با شناسه {} یافت نشد " , activityDTO.getAccessLevelId());
                    return new ResourceNotFoundException("AccessLeve" , "accessLevel" , activityDTO.getAccessLevelId());
                });
        activity.setAccessLevel(accessLevel);

        // تنظیم مقدار نال برای شناسه گروه چون فعالیت شخصی هست و گروهی نیست
        activity.setGroup(null);

        Activity savedActivity  = activityRepository.save(activity);
        logger.info(" فعالیت شخصی با عنوان {} برای کاربر {} اضافه شد " , activityDTO.getTitle(),user.getUsername());
        return activityMapper.toDTO(savedActivity);
    }


    // برای نشان دادن تمام اطلاعات فعالیت به همراه روابط بین جدول ها برای نشان دادن فعالیت های برنامه هفتگی شخصی نه گروهی
    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getPersonalActivitiesByDateRange(LocalDate startDate, LocalDate endDate ,
                                                                      String token) {
        logger.info(" شروع دریافت فعالیت های شخصی ");
        User user = validateUser(token);

        List<Activity> activities=activityRepository.findPersonalActivitiesByDateRange(user.getId(),startDate ,endDate) ;

        return activities.stream().map(activity -> activityMapper.toResponseDTO(activity))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ActivityResponseDTO updatePersonalActivity(Long activityId, ActivityDTO activityDTO, String token) {
        logger.info(" شروع ویرایش فعالیت شخصی برای کاربر ");
        User user = validateUser(token);

        Activity activity= activityRepository.findById(activityId)
                .orElseThrow(()->{
                    logger.error(" فعالیت با شناسه {} یافت نشد " ,activityDTO.getId());
                    return new ResourceNotFoundException("Activity","activityId" , activityDTO.getId());
                });

        //چک کردن فعالیت که ایا به کاربر فعلی ربط دارد
        if(! activity.getSchedule().getUser().getId().equals(user.getId())){
            logger.warn(" کاربر {} مجوز به روزرسانی فعالیت {} را ندارد!",user.getUsername() ,activityDTO.getId());
            throw new SecurityException(" شما مجوز به روزرسانی این فعالیت را ندارید ");
        }

        // چک کردن اینکه فعالیت شخصی باشد
        if (activity.getGroup() != null) {
            logger.warn(" فعالیت با شناسه {} گروهی است و قابل ویرایش نیست ",activityId);
            throw new SecurityException(" این فعالیت گروهی است و قابل ویرایش نیست ");
        }

        // بررسی اینکه زمان‌ها null نباشند و زمان شروع قبل از پایان باشد
        validateActivityTime2(activityDTO, activity);

        // به روزرسانی فیلد هایی که نال نیستند
        if (activityDTO.getTitle() != null)
            activity.setTitle(activityDTO.getTitle());

        if (activityDTO.getDescription() != null)
            activity.setDescription(activityDTO.getDescription());

        if (activityDTO.getStartDate() != null)
            activity.setStartDate(activityDTO.getStartDate());

        if (activityDTO.getEndDate() != null)
            activity.setEndDate(activityDTO.getEndDate());

        if (activityDTO.getStartTime() !=null)
            activity.setStartTime(activityDTO.getStartTime());

        if (activityDTO.getEndTime()!= null)
            activity.setEndTime(activityDTO.getEndTime());

        if (activityDTO.getLocation() != null)
            activity.setLocation(activityDTO.getLocation());

        // به روزرسانی روابط فقط اگر ایدی ها مقدار داشته باشن
        if (activityDTO.getWeekDayId() != null) {
                WeekDay weekDay = weekDayRepository.findById(activityDTO.getWeekDayId())
                        .orElseThrow(() ->{
                            logger.error("روز هفته با شناسه {} یافت نشد " ,activityDTO.getWeekDayId());
                            return new ResourceNotFoundException("WeekDay","weekDayId" ,activityDTO.getWeekDayId());
                        });
                activity.setWeekDay(weekDay);
        }

        if (activityDTO.getStatusId() != null){
            Status status= statusRepository.findById(activityDTO.getStatusId())
                    .orElseThrow(()->{
                        logger.error(" وضعیت با شناسه {} یافت نشد " , activityDTO.getStatusId());
                        return new ResourceNotFoundException("Status" ,"statusId" ,activityDTO.getStatusId());
                    });
            activity.setStatus(status);
        }

        if (activityDTO.getActivityTypeId() != null){
            ActivityType activityType = activityTypeRepository.findById(activityDTO.getActivityTypeId())
                    .orElseThrow(()->{
                        logger.error(" نوع فعالیت با شناسه {} یافت نشد" ,activityDTO.getActivityTypeId());
                        return new ResourceNotFoundException("ActivityType","activityType",activityDTO.getActivityTypeId());
                    });
            activity.setActivityType(activityType);
        }

        if (activityDTO.getAccessLevelId() != null){
            AccessLevel accessLevel= accessLevelRepository.findById(activityDTO.getAccessLevelId())
                    .orElseThrow(()->{
                        logger.error(" سطح دسترسی با شناسه {} یافت نشد " , activityDTO.getAccessLevelId());
                        return new ResourceNotFoundException("AccessLeve" , "accessLevel" , activityDTO.getAccessLevelId());
                    });
            activity.setAccessLevel(accessLevel);
        }

        //حفظ برنامه هفتگی فعلی و اطمینان از اینکه گروهی نیست
        activity.setSchedule(activity.getSchedule());
        activity.setGroup(null);

        //ذخیره تغییرات
        Activity updatedActivity= activityRepository.save(activity);
        logger.info(" فعالیت شخصی با شناسه {} با موفقیت به روزرسانی شد " ,activityDTO.getId());
        return activityMapper.toResponseDTO(updatedActivity);
    }

    @Transactional
    @Override
    public void deletePersonalActivity(Long activityId, String token) {
        logger.info("{} شروع حذف فعالیت شخصی با شناسه " ,activityId);
        String userName =extractUsernameFromToken(token);
        User user = validateUser(token);

        Activity activity= activityRepository.findById(activityId)
                .orElseThrow(()->{
                    logger.error(" فعالیت با شناسه {} یافت نشد " ,activityId);
                    return new ResourceNotFoundException("Activity","activityId" , activityId);
                });

        // اعتبار سنجی به جهت اینکه فعالیت به کاربر فعلی ربط داره
        if (!activity.getSchedule().getUser().getId().equals(user.getId())){
            logger.warn(" کاربر {} مجوز حذف فعالیت {} را ندارد " ,userName ,activityId);
            throw new SecurityException(" شما مجوز حذف این فعالیت را ندارید ");
        }

        //چک کردن اینکه فعالیت شخصی هست یعنی فعالیت گروهی نباشد
        if (activity.getGroup() != null){
            logger.warn(" فعالیت با شناسه {} گروهی است و قابل حذف نیست ",activityId);
            throw new SecurityException("این فعالیت گروهی است و قابل حذف نیست");
        }

        // حذف فعالیت
        activityRepository.deleteById(activityId);
        logger.info(" فعالیت شخصی با شناسه {} با موفقیت حذف شد " ,activityId);
    }

    @Transactional
    @Override
    public ActivityResponseDTO addGroupActivity(ActivityDTO activityDTO, Long groupId, String token) {
        logger.info("{} شروع اضافه کردن فعالیت گروهی برای گروه" , groupId);
        User user = validateUser(token);

        // بررسی وجود گروه و بررسی عضویت کاربر در گروه
        Group group = validateGroup(groupId, user);

        //بررسی نقش ادمین
        validateAdminRole(user, groupId);

        // بررسی اینکه زمان و تاریخ شروع بزرگتر از زمان و تاریخ پایان نباشد
        validateActivityTime(activityDTO);

        // ایجاد فعالیت گروهی
        Activity groupActivity= activityMapper.toEntity(activityDTO);
        groupActivity.setGroup(group);

        WeekDay weekDay= weekDayRepository.findById(activityDTO.getWeekDayId())
                .orElseThrow(()->{
                    logger.error("روز هفته با شناسه {} یافت نشد " ,activityDTO.getWeekDayId());
                    return new ResourceNotFoundException("WeekDay","weekDayId" ,activityDTO.getWeekDayId());
                });
        groupActivity.setWeekDay(weekDay);

        Status status =statusRepository.findById(activityDTO.getStatusId())
                .orElseThrow(()->{
                    logger.error(" وضعیت با شناسه {} یافت نشد " , activityDTO.getStatusId());
                    return new ResourceNotFoundException("Status" ,"statusId" ,activityDTO.getStatusId());
                });
        groupActivity.setStatus(status);

        ActivityType activityType= activityTypeRepository.findById(activityDTO.getActivityTypeId())
                .orElseThrow(()->{
                    logger.error(" نوع فعالیت با شناسه {} یافت نشد" ,activityDTO.getActivityTypeId());
                    return new ResourceNotFoundException("ActivityType","activityType",activityDTO.getActivityTypeId());
                });
        groupActivity.setActivityType(activityType);

        AccessLevel accessLevel= accessLevelRepository.findById(activityDTO.getAccessLevelId())
                .orElseThrow(()->{
                    logger.error(" سطح دسترسی با شناسه {} یافت نشد " , activityDTO.getAccessLevelId());
                    return new ResourceNotFoundException("AccessLeve" , "accessLevel" , activityDTO.getAccessLevelId());
                });
        groupActivity.setAccessLevel(accessLevel);

        // تنظیم برنامه هفتگی به مقدار نال برای فعالیت گروهی اصلی
        groupActivity.setSchedule(null);

        // ذخیره فعالیت گروهی
        Activity savedGroupActivity = activityRepository.save(groupActivity);
        logger.debug(" فعالیت گروهی با شناسه {} ذخیره شد " ,savedGroupActivity.getId());

        // یافتن اعضای گروه
        List<GroupMember> members = groupMemberRepository.findByGroupTable_Id(groupId);

        //افزودن فعالیت به برنامه هفتگی هر عضو
        for (GroupMember member :members){
            User memberUser =member.getUser();
            Schedule memberSchedule = scheduleRepository.findByUser_Id(memberUser.getId())
                    .orElseThrow(()->{
                        logger.error(" برنامه هفتگی برای کاربر {} یافت نشد ",memberUser.getUsername());
                        return new ResourceNotFoundException("Schedule" ,"scheduleId",memberUser.getId());
                    });

            // ایجاد یک نسخه از فعالیت برنامه هفتگی هر عضو
            Activity memberActivity = new Activity();
            memberActivity.setTitle(groupActivity.getTitle());
            memberActivity.setDescription(groupActivity.getDescription());
            memberActivity.setStartDate(groupActivity.getStartDate());
            memberActivity.setEndDate(groupActivity.getEndDate());
            memberActivity.setStartTime(groupActivity.getStartTime());
            memberActivity.setEndTime(groupActivity.getEndTime());
            memberActivity.setLocation(groupActivity.getLocation());
            memberActivity.setWeekDay(groupActivity.getWeekDay());
            memberActivity.setStatus(groupActivity.getStatus());
            memberActivity.setActivityType(groupActivity.getActivityType());
            memberActivity.setAccessLevel(groupActivity.getAccessLevel());
            memberActivity.setGroup(group);
            memberActivity.setSchedule(memberSchedule);

            activityRepository.save(memberActivity);
            logger.debug(" فعالیت برای عضو {} در گروه {} ذخیره شد ",memberUser.getUsername(),groupId);
        }

        logger.info(" فعالیت گروهی با عنوان {} برای گروه {} اضافه شد ",activityDTO.getTitle(),groupId);
        return activityMapper.toResponseDTO(savedGroupActivity);
    }

    @Transactional
    @Override
    public List<ActivityResponseDTO> getGroupActivityByDateRange(Long groupId, LocalDate startDate,
                                                                 LocalDate endDate, String token) {
        logger.info("{} شروع دریافت فعالیت های گروهی برای گروه {} از {} تا",groupId,startDate,endDate);
        User user = validateUser(token);
        validateGroup(groupId, user);
        validateDateRange(startDate, endDate);

        List<Activity> activities = activityRepository.findGroupActivityByDateRange(groupId ,startDate, endDate);
        logger.debug("{}: تعداد فعایلت های گروهی یافت شده" ,activities.size());
        return activities.stream().map(activity -> activityMapper.toResponseDTO(activity))
                .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public ActivityResponseDTO updateGroupActivity(Long activityId, ActivityDTO activityDTO, Long groupId, String token) {
        logger.info("شروع ویرایش فعالیت گروهی با شناسه {} برای گروه {}", activityId, groupId);

        User user = validateUser(token);
        Group group = validateGroup(groupId, user);
        validateAdminRole(user, groupId);

        // یافتن فعالیت گروهی
        Activity existingActivity = activityRepository.findById(activityId)
                .orElseThrow(() -> {
                    logger.error("فعالیت با شناسه {} یافت نشد", activityId);
                    return new ResourceNotFoundException("Activity", "activityId", activityId);
                });

        // اعتبارسنجی فعالیت گروهی
        if (!existingActivity.getGroup().getId().equals(groupId) || existingActivity.getSchedule() != null) {
            logger.warn("فعالیت با شناسه {} یک فعالیت گروهی معتبر برای گروه {} نیست", activityId, groupId);
            throw new ValidationException("فعالیت مورد نظر یک فعالیت گروهی معتبر نیست");
        }

        validateActivityTime2(activityDTO,existingActivity);

        // به‌روزرسانی فیلدهای غیر null
        if (activityDTO.getTitle() != null) {
            existingActivity.setTitle(activityDTO.getTitle());
        }
        if (activityDTO.getDescription() != null) {
            existingActivity.setDescription(activityDTO.getDescription());
        }
        if (activityDTO.getStartDate() != null) {
            existingActivity.setStartDate(activityDTO.getStartDate());
        }
        if (activityDTO.getEndDate() != null) {
            existingActivity.setEndDate(activityDTO.getEndDate());
        } else {
            existingActivity.setEndDate(null);
        }
        if (activityDTO.getStartTime() != null) {
            existingActivity.setStartTime(activityDTO.getStartTime());
        }
        if (activityDTO.getEndTime() != null) {
            existingActivity.setEndTime(activityDTO.getEndTime());
        }
        if (activityDTO.getLocation() != null) {
            existingActivity.setLocation(activityDTO.getLocation());
        }

        // به‌روزرسانی روابط
        if (activityDTO.getWeekDayId() != null) {
            WeekDay weekDay = weekDayRepository.findById(activityDTO.getWeekDayId())
                    .orElseThrow(() -> {
                        logger.error("WeekDay با شناسه {} یافت نشد", activityDTO.getWeekDayId());
                        return new ResourceNotFoundException("WeekDay", "weekDayId", activityDTO.getWeekDayId());
                    });
            existingActivity.setWeekDay(weekDay);
        }
        if (activityDTO.getStatusId() != null) {
            Status status = statusRepository.findById(activityDTO.getStatusId())
                    .orElseThrow(() -> {
                        logger.error("Status با شناسه {} یافت نشد", activityDTO.getStatusId());
                        return new ResourceNotFoundException("Status", "statusId", activityDTO.getStatusId());
                    });
            existingActivity.setStatus(status);
        }
        if (activityDTO.getActivityTypeId() != null) {
            ActivityType activityType = activityTypeRepository.findById(activityDTO.getActivityTypeId())
                    .orElseThrow(() -> {
                        logger.error("ActivityType با شناسه {} یافت نشد", activityDTO.getActivityTypeId());
                        return new ResourceNotFoundException("ActivityType", "activityTypeId", activityDTO.getActivityTypeId());
                    });
            existingActivity.setActivityType(activityType);
        }
        if (activityDTO.getAccessLevelId() != null) {
            AccessLevel accessLevel = accessLevelRepository.findById(activityDTO.getAccessLevelId())
                    .orElseThrow(() -> {
                        logger.error("AccessLevel با شناسه {} یافت نشد", activityDTO.getAccessLevelId());
                        return new ResourceNotFoundException("AccessLevel", "accessLevelId", activityDTO.getAccessLevelId());
                    });
            existingActivity.setAccessLevel(accessLevel);
        }

        // حفظ گروه و تنظیم schedule به null
        existingActivity.setGroup(group);
        existingActivity.setSchedule(null);

        // ذخیره فعالیت گروهی اصلی
        Activity updatedActivity = activityRepository.save(existingActivity);

        // بررسی وجود فعالیت‌های کپی‌شده
        List<Activity> groupMemberActivities = activityRepository.findByGroupIdAndScheduleNotNull(groupId);
        logger.debug("تعداد فعالیت‌های کپی‌شده برای گروه {}: {}", groupId, groupMemberActivities.size());
        if (!groupMemberActivities.isEmpty()) {
            logger.debug("به‌روزرسانی فعالیت‌های کپی‌شده برای گروه {} با مقادیر جدید: title={}, startDate={}, endDate={}, startTime={}, endTime={}",
                    groupId, updatedActivity.getTitle(), updatedActivity.getStartDate(), updatedActivity.getEndDate(),
                    updatedActivity.getStartTime(), updatedActivity.getEndTime());
            int updatedRows = activityRepository.updateGroupMemberActivities(
                    updatedActivity.getTitle(),
                    updatedActivity.getDescription(),
                    updatedActivity.getStartDate(),
                    updatedActivity.getEndDate(),
                    updatedActivity.getStartTime(),
                    updatedActivity.getEndTime(),
                    updatedActivity.getLocation(),
                    updatedActivity.getWeekDay().getId(),
                    updatedActivity.getStatus().getId(),
                    updatedActivity.getActivityType().getId(),
                    updatedActivity.getAccessLevel().getId(),
                    groupId
            );
            logger.info("تعداد {} فعالیت کپی‌شده برای گروه {} به‌روزرسانی شد", updatedRows, groupId);
        } else {
            logger.warn("هیچ فعالیت کپی‌شده‌ای برای گروه {} یافت نشد", groupId);
        }

        logger.info("فعالیت گروهی با شناسه {} برای گروه {} با موفقیت به‌روزرسانی شد", activityId, groupId);
        return activityMapper.toResponseDTO(updatedActivity);
    }

    @Override
    @Transactional
    public void deleteGroupActivity(Long activityId, Long groupId, String token) {
        logger.info("شروع حذف فعالیت گروهی با شناسه {} برای گروه {}", activityId, groupId);

        User user = validateUser(token);
        Group group = validateGroup(groupId, user);
        validateAdminRole(user, groupId);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> {
                    logger.error("فعالیت با شناسه {} یافت نشد", activityId);
                    return new ResourceNotFoundException("Activity", "activityId", activityId);
                });

        logger.debug("اجرای کوئری حذف فعالیت‌های کپی‌شده با پارامترها: groupId={}, title={}, startDate={}, startTime={}, endTime={}, endDate={}",
                groupId, activity.getTitle(), activity.getStartDate(), activity.getStartTime(), activity.getEndTime(), activity.getEndDate());

        if (activity.getEndDate() != null) {
            activityRepository.deleteGroupMemberActivitiesWithEndDate(groupId, activity.getTitle(),
                    activity.getStartDate(), activity.getStartTime(), activity.getEndTime(), activity.getEndDate()
            );
        } else {
            activityRepository.deleteGroupMemberActivitiesWithoutEndDate(groupId, activity.getTitle(),
                    activity.getStartDate(), activity.getStartTime(), activity.getEndTime()
            );
        }

        activityRepository.delete(activity);
        logger.info("فعالیت گروهی با شناسه {} برای گروه {} حذف شد", activityId, groupId);
    }

    // این بدون پروجکشن بود ولی در پایین با پروجکشن مینویسم
//        @Transactional(readOnly = true)
//        @Override
//        public ActivityReportResponseDTO getPersonalActivityReportByDate (LocalDate date, String token){
//            logger.info("شروع دریافت گزارش فعالیت‌های شخصی برای تاریخ {}", date);
//
//            User user = validateUser(token);
//            Long userId = user.getId();
//
//            // اعتبارسنجی تاریخ
//            if (date == null) {
//                logger.error("تاریخ ارائه‌شده نامعتبر است");
//                throw new ValidationException("تاریخ نمی‌تواند خالی باشد");
//            }
//            if (date.isAfter(LocalDate.now().plusYears(1)) ||
//                    date.isBefore(LocalDate.now().minusYears(1))) {
//                logger.warn("تاریخ {} خارج از محدوده مجاز است", date);
//                throw new ValidationException("تاریخ باید در محدوده یک سال گذشته یا آینده باشد");
//            }
//
//            // دریافت فعالیت‌های شخصی
//            List<Activity> activities = activityRepository.findPersonalActivitiesByDate(userId, date);
//            List<ActivityReportDTO> activityReportDTOS = activities.stream()
//                    .map(activity -> {
//                        long durationInMinutes = activity.getStartTime() != null && activity.getEndTime() != null
//                                ? ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime())
//                                : 0;
//                        return new ActivityReportDTO(activity.getTitle(), activity.getStartTime(), activity.getEndTime(), durationInMinutes);
//                    })
//                    .collect(Collectors.toList());
//
//            // محاسبه کل ساعات
//            double totalHours = activityReportDTOS.stream()
//                    .mapToDouble(ActivityReportDTO::getDurationInMinutes)
//                    .sum() / 60.0;
//            return new ActivityReportResponseDTO(activityReportDTOS, totalHours);
//        }
//
//        @Transactional(readOnly = true)
//        @Override
//        public ActivityReportResponseDTO getGroupActivityReportByDate (Long groupId, LocalDate date, String token){
//            logger.info("شروع دریافت گزارش فعالیت‌های گروهی برای گروه {} و تاریخ {}", groupId, date);
//
//            User user = validateUser(token);
//            validateGroup(groupId, user);
//
//            // اعتبارسنجی تاریخ
//            if (date == null) {
//                logger.error("تاریخ ارائه‌شده نامعتبر است");
//                throw new ValidationException("تاریخ نمی‌تواند خالی باشد");
//            }
//            if (date.isAfter(LocalDate.now().plusYears(1)) ||
//                    date.isBefore(LocalDate.now().minusYears(1))) {
//                logger.warn("تاریخ {} خارج از محدوده مجاز است", date);
//                throw new ValidationException("تاریخ باید در محدوده یک سال گذشته یا آینده باشد");
//            }
//
//            // دریافت فعالیت‌های گروهی
//            List<Activity> activities = activityRepository.findGroupActivitiesByDate(groupId, date);
//            List<ActivityReportDTO> activityReportDTOS = activities.stream()
//                    .map(activity -> {
//                        long durationInMinutes = activity.getStartTime() != null && activity.getEndTime() != null
//                                ? ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime())
//                                : 0;
//                        return new ActivityReportDTO(activity.getTitle(), activity.getStartTime(), activity.getEndTime(), durationInMinutes);
//                    })
//                    .collect(Collectors.toList());
//
//            // محاسبه کل ساعات
//            double totalHours = activityReportDTOS.stream()
//                    .mapToDouble(ActivityReportDTO::getDurationInMinutes)
//                    .sum() / 60.0;
//            return new ActivityReportResponseDTO(activityReportDTOS, totalHours);
//        }
    // با پروجکشن مینویسم
    @Transactional(readOnly = true)
    @Override
    public ActivityReportResponseDTO getPersonalActivityReportByDate(LocalDate date, String token) {
        logger.info("شروع دریافت گزارش فعالیت‌های شخصی برای تاریخ {}", date);
        User user = validateUser(token);

        if (date == null) {
            logger.error("تاریخ ارائه‌شده نامعتبر است");
            throw new ValidationException("تاریخ نمی‌تواند خالی باشد");
        }
        if (date.isAfter(LocalDate.now().plusYears(1)) ||
                date.isBefore(LocalDate.now().minusYears(1))) {
            logger.warn("تاریخ {} خارج از محدوده مجاز است", date);
            throw new ValidationException("تاریخ باید در محدوده یک سال گذشته یا آینده باشد");
        }

        List<ActivityProjection> activities = activityRepository.findPersonalActivitiesByDate(user.getId(), date);
        List<ActivityReportDTO> activityReportDTOS = activities.stream()
                .map(activity -> {
                    long durationInMinutes = activity.getStartTime() != null && activity.getEndTime() != null
                            ? ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime())
                            : 0;
                    return new ActivityReportDTO(activity.getTitle(), activity.getStartTime(), activity.getEndTime(), durationInMinutes);
                })
                .collect(Collectors.toList());

        double totalHours = activityReportDTOS.stream()
                .mapToDouble(ActivityReportDTO::getDurationInMinutes)
                .sum() / 60.0;
        return new ActivityReportResponseDTO(activityReportDTOS, totalHours);
    }

    @Transactional(readOnly = true)
    @Override
    public ActivityReportResponseDTO getGroupActivityReportByDate(Long groupId, LocalDate date, String token) {
        logger.info("شروع دریافت گزارش فعالیت‌های گروهی برای گروه {} و تاریخ {}", groupId, date);
        User user = validateUser(token);
        validateGroup(groupId, user);

        if (date == null) {
            logger.error("تاریخ ارائه‌شده نامعتبر است");
            throw new ValidationException("تاریخ نمی‌تواند خالی باشد");
        }
        if (date.isAfter(LocalDate.now().plusYears(1)) ||
                date.isBefore(LocalDate.now().minusYears(1))) {
            logger.warn("تاریخ {} خارج از محدوده مجاز است", date);
            throw new ValidationException("تاریخ باید در محدوده یک سال گذشته یا آینده باشد");
        }

        List<ActivityProjection> activities = activityRepository.findGroupActivitiesByDate(groupId, date);
        List<ActivityReportDTO> activityReportDTOS = activities.stream()
                .map(activity -> {
                    long durationInMinutes = activity.getStartTime() != null && activity.getEndTime() != null
                            ? ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime())
                            : 0;
                    return new ActivityReportDTO(activity.getTitle(), activity.getStartTime(), activity.getEndTime(), durationInMinutes);
                })
                .collect(Collectors.toList());

        double totalHours = activityReportDTOS.stream()
                .mapToDouble(ActivityReportDTO::getDurationInMinutes)
                .sum() / 60.0;
        return new ActivityReportResponseDTO(activityReportDTOS, totalHours);
    }




//        // گزارش گیری حرفه ای بر اساس تاریخ شروع و پایان
//        @Transactional(readOnly = true)
//        @Override
//        public AdvancedActivityReportResponseDTO getPersonalActivityReportByDateRange (LocalDate startDate,
//                LocalDate endDate,
//                String token){
//
//            logger.info("شروع دریافت گزارش فعالیت‌های شخصی برای بازه {} تا {}", startDate, endDate);
//
//            User user = validateUser(token);
//            validateDateRange(startDate, endDate);
//
//            // دریافت فعالیت‌های شخصی
//            List<Activity> activities = activityRepository.findPersonalActivitiesByDateRange(user.getId(), startDate, endDate);
//
//            // محاسبه کل زمان (دقیقه)
//            double totalMinutes = activities.stream()
//                    .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
//                    .mapToLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
//                    .sum();
//            double totalHours = totalMinutes / 60.0;
//            // گروه‌بندی فعالیت‌ها بر اساس عنوان
//            Map<String, Long> groupedByTitle = activities.stream()
//                    .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
//                    .collect(Collectors.groupingBy(
//                            Activity::getTitle,
//                            Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
//                    ));
//
//            List<GroupedActivityReportDTO> groupedActivities = groupedByTitle.entrySet().stream()
//                    .map(entry -> new GroupedActivityReportDTO(
//                            entry.getKey(),
//                            entry.getValue(),
//                            totalMinutes > 0 ? (entry.getValue() / totalMinutes) * 100 : 0.0
//                    ))
//                    .sorted(Comparator.comparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
//                    .collect(Collectors.toList());
//
//            // محاسبه میانگین روزانه
//            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
//            double averageDailyHours = daysBetween > 0 ? totalHours / daysBetween : 0.0;
//
//            // یافتن فعال‌ترین روز
//            Map<LocalDate, Long> minutesByDate = activities.stream()
//                    .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
//                    .collect(Collectors.groupingBy(
//                            Activity::getStartDate,
//                            Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
//                    ));
//            LocalDate mostActiveDay = minutesByDate.entrySet().stream()
//                    .max(Map.Entry.comparingByValue())
//                    .map(Map.Entry::getKey)
//                    .orElse(null);
//
//            // یافتن فعالیت‌های پرتکرار (بر اساس تعداد)
//            Map<String, Long> activityCounts = activities.stream()
//                    .collect(Collectors.groupingBy(Activity::getTitle, Collectors.counting()));
//            List<GroupedActivityReportDTO> topActivities = activityCounts.entrySet().stream()
//                    .map(entry -> new GroupedActivityReportDTO(
//                            entry.getKey(),
//                            groupedByTitle.getOrDefault(entry.getKey(), 0L),
//                            totalMinutes > 0 ? (groupedByTitle.getOrDefault(entry.getKey(), 0L) / totalMinutes) * 100 : 0.0,
//                            entry.getValue()
//                    ))
//                    .sorted(Comparator.comparing(GroupedActivityReportDTO::getCount).reversed()
//                            .thenComparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
//                    .limit(3)
//                    .collect(Collectors.toList());
//
//            // ساخت خلاصه
//            String summary = String.format(
//                    "در بازه %s تا %s، شما %.1f ساعت فعالیت انجام دادید. " +
//                            "میانگین روزانه %.2f ساعت. %s%s",
//                    startDate, endDate, totalHours, averageDailyHours,
//                    mostActiveDay != null ? "فعال‌ترین روز: " + mostActiveDay + ". " : "",
//                    topActivities.isEmpty() ? "" : "فعالیت‌های برتر: " + topActivities.get(0).getTitle() + "."
//            );
//
//            logger.debug("تعداد فعالیت‌ها: {}, کل ساعات: {}, میانگین روزانه: {}, فعال‌ترین روز: {}",
//                    groupedActivities.size(), totalHours, averageDailyHours, mostActiveDay);
//
//            return new AdvancedActivityReportResponseDTO(groupedActivities, totalHours, averageDailyHours, mostActiveDay, topActivities, summary);
//        }
//
//
//        @Transactional(readOnly = true)
//        public AdvancedActivityReportResponseDTO getGroupActivityReportByDateRange (Long groupId, LocalDate
//        startDate, LocalDate endDate, String token){
//            logger.info("شروع دریافت گزارش فعالیت‌های گروهی برای گروه {} و بازه {} تا {}", groupId, startDate, endDate);
//
//            User user = validateUser(token);
//            validateGroup(groupId, user);
//            validateDateRange(startDate, endDate);
//
//            // دریافت فعالیت‌های گروهی
//            List<Activity> activities = activityRepository.findGroupActivityByDateRange(groupId, startDate, endDate);
//
//            // محاسبه کل زمان (دقیقه)
//            double totalMinutes = activities.stream()
//                    .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
//                    .mapToLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
//                    .sum();
//            double totalHours = totalMinutes / 60.0;
//
//            // گروه‌بندی فعالیت‌ها بر اساس عنوان
//            Map<String, Long> groupedByTitle = activities.stream()
//                    .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
//                    .collect(Collectors.groupingBy(
//                            Activity::getTitle,
//                            Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
//                    ));
//
//            List<GroupedActivityReportDTO> groupedActivities = groupedByTitle.entrySet().stream()
//                    .map(entry -> new GroupedActivityReportDTO(
//                            entry.getKey(),
//                            entry.getValue(),
//                            totalMinutes > 0 ? (entry.getValue() / totalMinutes) * 100 : 0.0
//                    ))
//                    .sorted(Comparator.comparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
//                    .collect(Collectors.toList());
//
//            // محاسبه میانگین روزانه
//            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
//            double averageDailyHours = daysBetween > 0 ? totalHours / daysBetween : 0.0;
//
//            // یافتن فعال‌ترین روز
//            Map<LocalDate, Long> minutesByDate = activities.stream()
//                    .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
//                    .collect(Collectors.groupingBy(
//                            Activity::getStartDate,
//                            Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
//                    ));
//            LocalDate mostActiveDay = minutesByDate.entrySet().stream()
//                    .max(Map.Entry.comparingByValue())
//                    .map(Map.Entry::getKey)
//                    .orElse(null);
//
//            // یافتن فعالیت‌های پرتکرار (بر اساس تعداد)
//            Map<String, Long> activityCounts = activities.stream()
//                    .collect(Collectors.groupingBy(Activity::getTitle, Collectors.counting()));
//            List<GroupedActivityReportDTO> topActivities = activityCounts.entrySet().stream()
//                    .map(entry -> new GroupedActivityReportDTO(
//                            entry.getKey(),
//                            groupedByTitle.getOrDefault(entry.getKey(), 0L),
//                            totalMinutes > 0 ? (groupedByTitle.getOrDefault(entry.getKey(), 0L) / totalMinutes) * 100 : 0.0,
//                            entry.getValue()
//                    ))
//                    .sorted(Comparator.comparing(GroupedActivityReportDTO::getCount).reversed()
//                            .thenComparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
//                    .limit(3)
//                    .collect(Collectors.toList());
//
//            // ساخت خلاصه
//            String summary = String.format(
//                    "در بازه %s تا %s، گروه %.1f ساعت فعالیت انجام داد. " +
//                            "میانگین روزانه %.2f ساعت. %s%s",
//                    startDate, endDate, totalHours, averageDailyHours,
//                    mostActiveDay != null ? "فعال‌ترین روز: " + mostActiveDay + ". " : "",
//                    topActivities.isEmpty() ? "" : "فعالیت‌های برتر: " + topActivities.get(0).getTitle() + "."
//            );
//
//            logger.debug("تعداد فعالیت‌ها: {}, کل ساعات: {}, میانگین روزانه: {}, فعال‌ترین روز: {}",
//                    groupedActivities.size(), totalHours, averageDailyHours, mostActiveDay);
//
//            return new AdvancedActivityReportResponseDTO(groupedActivities, totalHours, averageDailyHours, mostActiveDay, topActivities, summary);
//        }
// با پروجکشن مینویسم
    @Transactional(readOnly = true)
    @Override
    public AdvancedActivityReportResponseDTO getPersonalActivityReportByDateRange(LocalDate startDate, LocalDate endDate, String token) {
        logger.info("شروع دریافت گزارش فعالیت‌های شخصی برای بازه {} تا {}", startDate, endDate);
        User user = validateUser(token);
        validateDateRange(startDate, endDate);

        // دریافت فعالیت‌های شخصی
        List<ActivityProjection2> activities = activityRepository.findPersonalActivitiesByDateRange2(user.getId(), startDate, endDate);

        // محاسبه کل زمان (دقیقه)
        double totalMinutes = activities.stream()
                .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
                .mapToLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
                .sum();
        double totalHours = totalMinutes / 60.0;

        // گروه‌بندی فعالیت‌ها بر اساس عنوان
        Map<String, Long> groupedByTitle = activities.stream()
                .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
                .collect(Collectors.groupingBy(
                        ActivityProjection2::getTitle,
                        Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
                ));

        List<GroupedActivityReportDTO> groupedActivities = groupedByTitle.entrySet().stream()
                .map(entry -> new GroupedActivityReportDTO(
                        entry.getKey(),
                        entry.getValue(),
                        totalMinutes > 0 ? (entry.getValue() / totalMinutes) * 100 : 0.0
                ))
                .sorted(Comparator.comparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
                .collect(Collectors.toList());

        // محاسبه میانگین روزانه
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        double averageDailyHours = daysBetween > 0 ? totalHours / daysBetween : 0.0;

        // یافتن فعال‌ترین روز
        Map<LocalDate, Long> minutesByDate = activities.stream()
                .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
                .collect(Collectors.groupingBy(
                        ActivityProjection2::getStartDate,
                        Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
                ));
        LocalDate mostActiveDay = minutesByDate.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // یافتن فعالیت‌های پرتکرار (بر اساس تعداد)
        Map<String, Long> activityCounts = activities.stream()
                .collect(Collectors.groupingBy(ActivityProjection2::getTitle, Collectors.counting()));
        List<GroupedActivityReportDTO> topActivities = activityCounts.entrySet().stream()
                .map(entry -> new GroupedActivityReportDTO(
                        entry.getKey(),
                        groupedByTitle.getOrDefault(entry.getKey(), 0L),
                        totalMinutes > 0 ? (groupedByTitle.getOrDefault(entry.getKey(), 0L) / totalMinutes) * 100 : 0.0,
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(GroupedActivityReportDTO::getCount).reversed()
                        .thenComparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // ساخت خلاصه
        String summary = String.format(
                "در بازه %s تا %s، شما %.1f ساعت فعالیت انجام دادید. " +
                        "میانگین روزانه %.2f ساعت. %s%s",
                startDate, endDate, totalHours, averageDailyHours,
                mostActiveDay != null ? "فعال‌ترین روز: " + mostActiveDay + ". " : "",
                topActivities.isEmpty() ? "" : "فعالیت‌های برتر: " + topActivities.get(0).getTitle() + "."
        );
        logger.debug("تعداد فعالیت‌ها: {}, کل ساعات: {}, میانگین روزانه: {}, فعال‌ترین روز: {}",
                groupedActivities.size(), totalHours, averageDailyHours, mostActiveDay);

        return new AdvancedActivityReportResponseDTO(groupedActivities, totalHours, averageDailyHours, mostActiveDay, topActivities, summary);
    }

    @Transactional(readOnly = true)
    @Override
    public AdvancedActivityReportResponseDTO getGroupActivityReportByDateRange(Long groupId, LocalDate startDate, LocalDate endDate, String token) {
        logger.info("شروع دریافت گزارش فعالیت‌های گروهی برای گروه {} و بازه {} تا {}", groupId, startDate, endDate);
        User user = validateUser(token);
        validateGroup(groupId, user);
        validateDateRange(startDate, endDate);

        // دریافت فعالیت‌های گروهی
        List<ActivityProjection2> activities = activityRepository.findGroupActivityByDateRange2(groupId, startDate, endDate);

        // محاسبه کل زمان (دقیقه)
        double totalMinutes = activities.stream()
                .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
                .mapToLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
                .sum();
        double totalHours = totalMinutes / 60.0;

        // گروه‌بندی فعالیت‌ها بر اساس عنوان
        Map<String, Long> groupedByTitle = activities.stream()
                .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
                .collect(Collectors.groupingBy(
                        ActivityProjection2::getTitle,
                        Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
                ));

        List<GroupedActivityReportDTO> groupedActivities = groupedByTitle.entrySet().stream()
                .map(entry -> new GroupedActivityReportDTO(
                        entry.getKey(),
                        entry.getValue(),
                        totalMinutes > 0 ? (entry.getValue() / totalMinutes) * 100 : 0.0
                ))
                .sorted(Comparator.comparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
                .collect(Collectors.toList());

        // محاسبه میانگین روزانه
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        double averageDailyHours = daysBetween > 0 ? totalHours / daysBetween : 0.0;

        // یافتن فعال‌ترین روز
        Map<LocalDate, Long> minutesByDate = activities.stream()
                .filter(activity -> activity.getStartTime() != null && activity.getEndTime() != null)
                .collect(Collectors.groupingBy(
                        ActivityProjection2::getStartDate,
                        Collectors.summingLong(activity -> ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
                ));
        LocalDate mostActiveDay = minutesByDate.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // یافتن فعالیت‌های پرتکرار (بر اساس تعداد)
        Map<String, Long> activityCounts = activities.stream()
                .collect(Collectors.groupingBy(ActivityProjection2::getTitle, Collectors.counting()));
        List<GroupedActivityReportDTO> topActivities = activityCounts.entrySet().stream()
                .map(entry -> new GroupedActivityReportDTO(
                        entry.getKey(),
                        groupedByTitle.getOrDefault(entry.getKey(), 0L),
                        totalMinutes > 0 ? (groupedByTitle.getOrDefault(entry.getKey(), 0L) / totalMinutes) * 100 : 0.0,
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(GroupedActivityReportDTO::getCount).reversed()
                        .thenComparing(GroupedActivityReportDTO::getTotalMinutes).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // ساخت خلاصه
        String summary = String.format(
                "در بازه %s تا %s، گروه %.1f ساعت فعالیت انجام داد. " +
                        "میانگین روزانه %.2f ساعت. %s%s",
                startDate, endDate, totalHours, averageDailyHours,
                mostActiveDay != null ? "فعال‌ترین روز: " + mostActiveDay + ". " : "",
                topActivities.isEmpty() ? "" : "فعالیت‌های برتر: " + topActivities.get(0).getTitle() + "."
        );

        logger.debug("تعداد فعالیت‌ها: {}, کل ساعات: {}, میانگین روزانه: {}, فعال‌ترین روز: {}",
                groupedActivities.size(), totalHours, averageDailyHours, mostActiveDay);

        return new AdvancedActivityReportResponseDTO(groupedActivities, totalHours, averageDailyHours, mostActiveDay, topActivities, summary);
    }

    }
