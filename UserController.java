package com.scheduleNetwork_version2.scheduleNetwork_version2.controller;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.UserDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.User;
import com.scheduleNetwork_version2.scheduleNetwork_version2.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/user")
public class UserController {

    private UserService userService;


    @SecurityRequirement(name = "Bearer Authentication")
    // http://localhost:8080/api/user
    @PutMapping()
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO,
            @RequestHeader("Authorization") String token) {

            UserDTO updatedUser = userService.updateUser(userDTO, token);
            return ResponseEntity.ok(updatedUser);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    // http://localhost:8080/api/user
    @DeleteMapping()
    public ResponseEntity<String> deleteUser(
//            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        userService.deleteUser(token);
        return new ResponseEntity<>("کاربر با موفقیت حذف شد" , HttpStatus.OK);
    }

    // http://localhost:8080/api/user/search?firstName=samira
    // http://localhost:8080/api/user/search?lastName=saremi
    // http://localhost:8080/api/user/search?email=samira@example.com
    // http://localhost:8080/api/user/search?firstName=samira&lastName=saremi
    // criteria API
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email) {

            List<UserDTO> users = userService.searchUsers(firstName, lastName, email);
            return new ResponseEntity<>(users, HttpStatus.OK);

    }


}
