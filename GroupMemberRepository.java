package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.GroupMember;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // در متد ایجاد تایم خالی استفاده کردم
    // بررسی وجود کاربر خاص در یک گروه خاص
//    boolean existsByUser_IdAndGroupTable_Id(Long userId, Long groupId);
    // برای حل مشکل N+1 این کار را کردم
    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END " +
            "FROM GroupMember gm WHERE gm.user.id = :userId AND gm.groupTable.id = :groupId")
    boolean existsByUser_IdAndGroupTable_Id(@Param("userId") Long userId, @Param("groupId") Long groupId);


    // بررسی وجود کاربر خاص در یک گروه خاص
    // در متد افزودن اعضا داخل گروه استفاده میکنم
//    boolean existsByUser_IdAndGroupTable_IdAndRole_Title(Long userId , Long groupId , String RoleTitle);
    // برای حل مشکل N+1 این کار را کردم
    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END " +
            "FROM GroupMember gm WHERE gm.user.id = :userId AND gm.groupTable.id = :groupId AND gm.role.title = :roleTitle")
    boolean existsByUser_IdAndGroupTable_IdAndRole_Title(@Param("userId") Long userId,
                                                         @Param("groupId") Long groupId,
                                                         @Param("roleTitle") String roleTitle);


    // پیدا کردن تمام اعضای یک گروه
    // در صفحه داخل گروه برای نشان دادن لیست اعضای گروه ستفاده میکنم
//    List<GroupMember> findByGroupTable_Id(Long groupId);
    // برای حل مشکل N+1 این کار را کردم
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user u WHERE gm.groupTable.id = :groupId")
    List<GroupMember> findByGroupTable_Id(@Param("groupId") Long groupId);

    // پیدا کردن تمام گروه‌های یک کاربر
//    List<GroupMember> findByUser_Id(Long userId);
    // برای حل مشکل N+1 این کار را کردم
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.groupTable g WHERE gm.user.id = :userId")
    List<GroupMember> findByUser_Id(@Param("userId") Long userId);

    // پیدا کردن تمام اعضای یک نقش خاص
//    List<GroupMember> findByRole_Id(Long roleId);
    // برای حل مشکل N+1 این کار را کردم
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user u WHERE gm.role.id = :roleId")
    List<GroupMember> findByRole_Id(@Param("roleId") Long roleId);


    // در متد حذف کاربر در صفحه داخل گروه استفاده میکنم
    //حذف عضویت کاربر از گروه
//    void deleteByUser_IdAndGroupTable_Id(Long userId  ,Long groupId);
    // برای حل مشکل N+1 این کار را کردم
    @Modifying
    @Transactional
    @Query("DELETE FROM GroupMember gm WHERE gm.user.id = :userId AND gm.groupTable.id = :groupId")
    void deleteByUser_IdAndGroupTable_Id(@Param("userId") Long userId, @Param("groupId") Long groupId);

}
