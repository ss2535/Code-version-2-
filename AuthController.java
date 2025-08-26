package com.scheduleNetwork_version2.scheduleNetwork_version2.controller;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.LoginRequest;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.LoginResponse;
import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.UserDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.Role;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.User;
import com.scheduleNetwork_version2.scheduleNetwork_version2.mapper.UserMapper;
import com.scheduleNetwork_version2.scheduleNetwork_version2.security.JwtService;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info(" {} شروع فرایند لاگین برای کاربر ",loginRequest.username());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            User user = userService.findUserByUsername(loginRequest.username());
            String token = jwtService.generateToken(user);

            // تبدیل Set<Role> به لیست عنوان نقش‌ها
            List<String> roleTitles = user.getRoles().stream()
                    .map(Role::getTitle)
                    .toList();

            LoginResponse response = new LoginResponse(
                    token,
                    user.getUsername(),
                    roleTitles
            );

            logger.info(" کاربر {} با موفقیت وارد سیستم شد ",loginRequest.username());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn(" احراز هویت ناموفق برای کاربر {}: رمز عبور نادرست  ",loginRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody UserDTO userDTO) {
        logger.info(" {} شروع فرایند ثبت نام با نام کاربری ",userDTO.getUsername());
        // ایجاد و ذخیره کاربر جدید
        User savedUser = userMapper.mapToUser(userService.createUser(userDTO));

        // تولید توکن JWT از کاربر ذخیره شده
        String token = jwtService.generateToken(savedUser);

        // استخراج نقش‌های کاربر
        List<String> roleTitles = savedUser.getRoles().stream()
                .map(Role::getTitle)
                .toList();

        // ساخت پاسخ حاوی توکن
        LoginResponse response = new LoginResponse(
                token,
                savedUser.getUsername(),
                roleTitles
        );
        logger.info(" کاربر با نام کاربری {} با موفقیت ثبت نام شد ",userDTO.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}