package com.scheduleNetwork_version2.scheduleNetwork_version2.service.impl;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.CommonFreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.FreeTimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.TimeDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Group;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Time;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.User;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.WeekDay;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ResourceNotFoundException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.SecurityException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ValidationException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.mapper.FreeTimeMapper;
import com.scheduleNetwork_version2.scheduleNetwork_version2.mapper.TimeMapper;
import com.scheduleNetwork_version2.scheduleNetwork_version2.repository.*;
import com.scheduleNetwork_version2.scheduleNetwork_version2.security.JwtService;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.TimeService;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class TimeServiceImpl implements TimeService {

    private static final Logger logger = LoggerFactory.getLogger(TimeServiceImpl.class);

    private final TimeRepository timeRepository;
    private final TimeMapper timeMapper;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final WeekDayRepository weekDayRepository;
    private final JwtService jwtService;
    private final GroupMemberRepository groupMemberRepository;
    private final FreeTimeMapper freeTimeMapper;

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

    private void checkGroupMembership(Long userId, Long groupId) {
        boolean isMember = groupMemberRepository.existsByUser_IdAndGroupTable_Id(userId, groupId);
        if (!isMember) {
            logger.warn("کاربر {} عضو گروه {} نیست!", userId, groupId);
            throw new SecurityException("شما عضو گروه نیستید");
        }
    }

    private void validateDateTimeRange(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime)) {
                logger.warn("زمان شروع {} بعد از زمان پایان {} است", startTime, endTime);
                throw new ValidationException("زمان شروع باید قبل از زمان پایان باشد");
            }
        }
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                logger.warn("تاریخ شروع {} بعد از تاریخ پایان {} است", startDate, endDate);
                throw new ValidationException("تاریخ شروع باید قبل از تاریخ پایان باشد");
            }
        }
    }

    private WeekDay validateWeekDay(Long weekDayId) {
        if (weekDayId == null) {
            logger.error("شناسه روز هفته الزامی است");
            throw new IllegalArgumentException("شناسه روز الزامی است");
        }
        return weekDayRepository.findById(weekDayId)
                .orElseThrow(() -> {
                    logger.error("روز هفته با شناسه {} یافت نشد", weekDayId);
                    return new ResourceNotFoundException("WeekDay", "weekDayId", weekDayId);
                });
    }

    @Transactional
    @Override
    public TimeDTO addFreeTime(TimeDTO request,Long groupId ,String token) {
        logger.info("{}: شروع اضافه کردن زمان خالی برای گروه" , groupId);
        User requestingUser = validateUser(token);
        Group group = validateGroup(groupId);
        checkGroupMembership(requestingUser.getId(), groupId);

        WeekDay weekDay = validateWeekDay(request.getWeekDayId());
        validateDateTimeRange(request.getStartDate(), request.getEndDate(), request.getStartTime(),
                request.getEndTime());

        // ایجاد آبجکت
        Time time= timeMapper.toEntity(request);
        time.setUser(requestingUser);
        time.setGroup(group);
        time.setWeekDay(weekDay);

        Time savedTime = timeRepository.save(time);
        logger.info(" زمان خالی برای گروه {} با موفقیت اضافه شد " , groupId);
        return timeMapper.toDTO(savedTime);
    }

    @Override
    public List<FreeTimeDTO> getFreeTimes(Long groupId, String token) {

        logger.info(" {} شروع دریافت زمان های خالی برای گروه " , groupId);
        // استخراج نام کاربری از توکن و اعتبار سنجی
        User requestingUser = validateUser(token);
        Group group = validateGroup(groupId);
        checkGroupMembership(requestingUser.getId(), groupId);

        List<Time> times =timeRepository.findByGroup_Id(groupId);
        logger.debug("{}:{} تعداد زمان های خالی یافت شده برای گروه " ,groupId,times.size());
        return times.stream().map(freeTimeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TimeDTO updateFreeTime(TimeDTO timeDTO, Long groupId, String token, long timeId) {

        logger.info("{} شروع به روز رسانی زمان خالی با شناسه {} برای گروه ",timeDTO.getId(),groupId);

        User requestUser = validateUser(token);
        Group group = validateGroup(groupId);
        checkGroupMembership(requestUser.getId(), groupId);

        // پیدا کردن زمان خالی موجود توسط ایدی
        Time existingTime= timeRepository.findById(timeId)
                .orElseThrow(()->{
                   logger.error(" زمان خالی با شناسه {} یافت نشد ",timeId);
                   return new ResourceNotFoundException("Time","id",timeId);
                });

        // چک کردن اینکه زمان خالی متعلق به کاربر درخواتس دهنده است
        if (!existingTime.getUser().getId().equals(requestUser.getId())){
            logger.warn(" کاربر {} اجازه ویرایش زمان خالی با شناسه{} را ندارد ",requestUser.getId(),timeId);
            throw new SecurityException(" شما اجازه ویرایش این زمان خالی را ندارید ");
        }

        // چک کردن اینکه زمان خالی متعلق به گروه مورد نظر باشد
        if (!existingTime.getGroup().getId().equals(groupId)){
            logger.warn(" زمان خالی با شناسه{} متعلق به گروه {} نیست",timeDTO,groupId);
            throw new SecurityException(" این زمان خالی متعلق به گروه مورد نظر نیست ");
        }

        // به روز رسانی فیلد ها اگر در درخواست ارائه شده باشد
        if (timeDTO.getStartDate()!= null)
            existingTime.setStartDate(timeDTO.getStartDate());

        if (timeDTO.getEndDate()!= null)
            existingTime.setEndDate(timeDTO.getEndDate());

        if (timeDTO.getStartTime() != null)
            existingTime.setStartTime(timeDTO.getStartTime());

        if (timeDTO.getEndTime()!= null)
            existingTime.setEndTime(timeDTO.getEndTime());

        if (timeDTO.getWeekDayId() != null) {
            WeekDay weekDay = validateWeekDay(timeDTO.getWeekDayId());
            existingTime.setWeekDay(weekDay);
        }

        validateDateTimeRange(timeDTO.getStartDate() != null ? timeDTO.getStartDate() : existingTime.getStartDate(),
                timeDTO.getEndDate() != null ? timeDTO.getEndDate() : existingTime.getEndDate(),
                timeDTO.getStartTime() != null ? timeDTO.getStartTime() : existingTime.getStartTime(),
                timeDTO.getEndTime() != null ? timeDTO.getEndTime() : existingTime.getEndTime());

        // ذخیره تغییرات
        Time updateTime= timeRepository.save(existingTime);
        logger.info(" زمان خالی با شناسه {} برای گروه {} با موفقعیت به روزرسانی شد ",timeId,groupId);
        return timeMapper.toDTO(updateTime);

    }

    @Transactional
    @Override
    public void deleteFreeTime(Long timeId, Long groupId, String token) {
        logger.info(" {} شروع حذف زمان خالی با شناسه {} برای گروه ",timeId,groupId);

        User requestUser = validateUser(token);
        Group group = validateGroup(groupId);
        checkGroupMembership(requestUser.getId(), groupId);

        // پیدا کردن زمان خالی موجود توسط ایدی
        Time existingTime= timeRepository.findById(timeId)
                .orElseThrow(()->{
                    logger.error(" زمان خالی با شناسه {} یافت نشد ",timeId);
                    return new ResourceNotFoundException("Time","id",timeId);
                });

        // چک کردن اینکه زمان خالی متعلق به کاربر درخواست دهنده است
        if (!existingTime.getUser().getId().equals(requestUser.getId())){
            logger.warn(" کاربر {} اجازه ویرایش زمان خالی با شناسه{} را ندارد ",requestUser.getId(),timeId);
            throw new SecurityException(" شما اجازه ویرایش این زمان خالی را ندارید ");
        }

        // چک کردن اینکه زمان خالی متعلق به گروه مورد نظر باشد
        if (!existingTime.getGroup().getId().equals(groupId)){
            logger.warn(" زمان خالی با شناسه{} متعلق به گروه {} نیست",requestUser.getId(),groupId);
            throw new SecurityException(" این زمان خالی متعلق به گروه مورد نظر نیست ");
        }

        // حذف زمان خالی
        timeRepository.delete(existingTime);
        logger.info(" زمان خالی با شناسه {} برای گروه {} با موفقیت حذف شد ",timeId,groupId);
    }



    @Override
    public List<CommonFreeTimeDTO> findCommonFreeTimes(Long groupId, LocalDate startDateRange, LocalDate endDateRange, String token) {
        logger.info("شروع جستجوی زمان‌های خالی مشترک برای گروه {} از {} تا {}", groupId, startDateRange, endDateRange);

        User requestingUser = validateUser(token);
        Group group = validateGroup(groupId);
        checkGroupMembership(requestingUser.getId(), groupId);

        if (startDateRange == null || endDateRange == null || startDateRange.isAfter(endDateRange)) {
            logger.warn("بازه تاریخ نامعتبر است: تاریخ شروع {}، تاریخ پایان {}", startDateRange, endDateRange);
            throw new ValidationException("بازه تاریخ نامعتبر است. تاریخ شروع باید قبل از تاریخ پایان باشد");
        }

        // بارگذاری زمان‌های خالی
        List<Time> freeTimes = timeRepository.findFreeTimesByGroupAndDateRange(groupId, startDateRange, endDateRange);
        logger.debug("تعداد زمان‌های خالی یافت شده: {}", freeTimes.size());

        // پیدا کردن کاربرانی که تایم خالی تعریف کرده‌اند
        Set<Long> usersWithFreeTime = freeTimes.stream()
                .map(t -> t.getUser().getId())
                .collect(Collectors.toSet());

        // تبدیل بازه‌های چندروزه به تک‌روزه
        List<Time> expandedTimes = new ArrayList<>();
        for (Time time : freeTimes) {
            LocalDate currentDate = time.getStartDate();
            LocalDate endDate = time.getEndDate() != null ? time.getEndDate() : time.getStartDate();
            while (!currentDate.isAfter(endDate)) {
                Time singleDayTime = new Time();
                singleDayTime.setId(time.getId());
                singleDayTime.setStartDate(currentDate);
                singleDayTime.setEndDate(currentDate);
                singleDayTime.setStartTime(time.getStartTime());
                singleDayTime.setEndTime(time.getEndTime());
                singleDayTime.setUser(time.getUser());
                singleDayTime.setGroup(time.getGroup());
                singleDayTime.setWeekDay(time.getWeekDay());
                expandedTimes.add(singleDayTime);
                currentDate = currentDate.plusDays(1);
            }
        }

        // گروه‌بندی زمان‌های خالی بر اساس روز هفته و تاریخ
        Map<String, List<Time>> groupedByWeekDayAndDate = expandedTimes.stream()
                .collect(Collectors.groupingBy(
                        time -> time.getWeekDay().getId() + "_" + time.getStartDate()
                ));

        List<CommonFreeTimeDTO> commonFreeTimes = new ArrayList<>();
        final int SLOT_MINUTES = 30;

        // پردازش هر گروه (روز هفته و تاریخ)
        for (Map.Entry<String, List<Time>> entry : groupedByWeekDayAndDate.entrySet()) {
            List<Time> times = entry.getValue();
            Set<Long> userIds = times.stream().map(t -> t.getUser().getId()).collect(Collectors.toSet());

            // فقط روزهایی پردازش می‌شوند که همه کاربران با تایم خالی در آن روز تایم داشته باشند
            if (!userIds.containsAll(usersWithFreeTime)) {
                continue; // اگر یکی از کاربران تایم خالی نداشته باشد، این روز نادیده گرفته می‌شود
            }

            // ایجاد اسلات‌های زمانی
            Map<LocalTime, Integer> slotCounts = new TreeMap<>();
            for (Time time : times) {
                LocalTime start = time.getStartTime();
                LocalTime end = time.getEndTime();
                if (start != null && end != null && start.isBefore(end)) {
                    LocalTime current = start.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
                    while (current.isBefore(end) || current.equals(end)) {
                        slotCounts.merge(current, 1, Integer::sum);
                        current = current.plusMinutes(SLOT_MINUTES);
                    }
                }
            }

            // یافتن اسلات‌های مشترک
            LocalTime slotStart = null;
            LocalTime lastSlotStart = null;
            for (Map.Entry<LocalTime, Integer> slot : slotCounts.entrySet()) {
                if (slot.getValue().equals(userIds.size())) { // همه کاربرانی که تایم خالی تعریف کرده‌اند
                    if (slotStart == null) {
                        slotStart = slot.getKey();
                    }
                    lastSlotStart = slot.getKey();
                } else if (slotStart != null) {
                    CommonFreeTimeDTO dto = new CommonFreeTimeDTO();
                    dto.setId(null);
                    dto.setStartDate(times.get(0).getStartDate());
                    dto.setEndDate(times.get(0).getStartDate()); // تک‌روزه
                    dto.setStartTime(slotStart);
                    // محاسبه endTime دقیق
                    LocalTime minEndTime = times.stream()
                            .map(Time::getEndTime)
                            .filter(Objects::nonNull)
                            .min(Comparator.naturalOrder())
                            .orElse(lastSlotStart.plusMinutes(SLOT_MINUTES));
                    dto.setEndTime(minEndTime.isAfter(lastSlotStart.plusMinutes(SLOT_MINUTES)) ?
                            lastSlotStart.plusMinutes(SLOT_MINUTES) : minEndTime);
                    dto.setGroupTableId(groupId);
                    dto.setWeekDayTitle(times.get(0).getWeekDay().getTitle());
                    commonFreeTimes.add(dto);
                    slotStart = null;
                }
            }

            // افزودن آخرین بازه مشترک
            if (slotStart != null && lastSlotStart != null) {
                CommonFreeTimeDTO dto = new CommonFreeTimeDTO();
                dto.setId(null);
                dto.setStartDate(times.get(0).getStartDate());
                dto.setEndDate(times.get(0).getStartDate()); // تک‌روزه
                dto.setStartTime(slotStart);
                LocalTime minEndTime = times.stream()
                        .map(Time::getEndTime)
                        .filter(Objects::nonNull)
                        .min(Comparator.naturalOrder())
                        .orElse(lastSlotStart.plusMinutes(SLOT_MINUTES));
                dto.setEndTime(minEndTime.isAfter(lastSlotStart.plusMinutes(SLOT_MINUTES)) ?
                        lastSlotStart.plusMinutes(SLOT_MINUTES) : minEndTime);
                dto.setGroupTableId(groupId);
                dto.setWeekDayTitle(times.get(0).getWeekDay().getTitle());
                commonFreeTimes.add(dto);
            }
        }

        // ادغام بازه‌های متوالی با زمان یکسان
        List<CommonFreeTimeDTO> mergedTimes = mergeConsecutiveDays(commonFreeTimes);

        logger.info("جستجوی زمان‌های خالی مشترک برای گروه {} با موفقیت انجام شد", groupId);
        return mergedTimes;
    }

        // تابع کمکی برای ادغام بازه‌های متوالی
        private List<CommonFreeTimeDTO> mergeConsecutiveDays(List<CommonFreeTimeDTO> times) {
            if (times.isEmpty()) {
                return times;
            }

            List<CommonFreeTimeDTO> merged = new ArrayList<>();
            times.sort(Comparator.comparing(CommonFreeTimeDTO::getStartDate)
                    .thenComparing(CommonFreeTimeDTO::getStartTime));

            CommonFreeTimeDTO current = times.get(0);
            for (int i = 1; i < times.size(); i++) {
                CommonFreeTimeDTO next = times.get(i);
                if (current.getEndDate().plusDays(1).equals(next.getStartDate())
                        && current.getStartTime().equals(next.getStartTime())
                        && current.getEndTime().equals(next.getEndTime())
                        && current.getWeekDayTitle().equals(next.getWeekDayTitle())) {
                    current.setEndDate(next.getEndDate());
                } else {
                    merged.add(current);
                    current = next;
                }
            }
            merged.add(current);

            return merged;
        }




}
