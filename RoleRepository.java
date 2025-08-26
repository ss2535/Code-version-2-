package com.scheduleNetwork_version2.scheduleNetwork_version2.repository;

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.AccessLevel;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Role;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role , Long> {

    Optional<Role> findByTitle(String title);

    @Cacheable("roles")
    Optional<Role> findById(Long id);

}
