package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.AccessLevel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccessLevelRepository extends JpaRepository<AccessLevel , Long> {

    Optional<AccessLevel> findByTitle(String title);

    @Cacheable("accessLevels")
    Optional<AccessLevel> findById(Long id);

}
