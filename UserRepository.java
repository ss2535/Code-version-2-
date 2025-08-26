package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {

    // برای حل مشکل N+1 این کار را کردم
//    Optional<User> findUserByUsername(String username);
    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.gender g " +
            "LEFT JOIN FETCH u.accessLevel al " +
            "WHERE u.username = :username")
    Optional<User> findUserByUsername(@Param("username") String username);



}
