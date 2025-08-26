package com.scheduleNetwork_version2.scheduleNetwork_version2.config;

// ... imports

import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Role;
import com.scheduleNetwork_version2.scheduleNetwork_version2.repository.AccessLevelRepository;
import com.scheduleNetwork_version2.scheduleNetwork_version2.repository.GenderRepository;
import com.scheduleNetwork_version2.scheduleNetwork_version2.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final GenderRepository genderRepository;
    private final AccessLevelRepository accessLevelRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setTitle("ROLE_ADMIN");
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setTitle("ROLE_USER");
            roleRepository.save(userRole);
        }

        // ... initialize other data
    }
}