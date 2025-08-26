package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.GroupDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupName(String groupName);

    // در صفحه گرفتن گروه های کاربر استفاده کردم
// برای اینکه N+1 کوئری نشود این کار را کردم
//    @Query(value = "select g.* from group_table g " +
//            "Join group_member gm on g.id= gm.group_table_id  " +
//            "Join user_table u on u.id= gm.user_table_id  where u.username = :userName " +
//            "Order By g.created_date Desc "
//            , nativeQuery = true)
//    List<Group> findGroupNameByUserName(@Param("userName")String userName);
    @Query("SELECT DISTINCT g FROM Group g " +
            "JOIN g.groupMembers gm " +
            "JOIN gm.user u " +
            "JOIN FETCH g.accessLevel " +
            "WHERE u.username = :userName " +
            "ORDER BY g.createdDate DESC")
    Page<Group> findGroupNameByUserName(@Param("userName") String userName, Pageable pageable);



}
