package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.AccessLevel;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Gender;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenderRepository extends JpaRepository<Gender,Long> {

    Optional<Gender> findByTitle(String title);

    @Cacheable("genders")
    Optional<Gender> findById(Long id);

}
