package com.scheduleNetwork_version2.scheduleNetwork_version2.service;

import com.scheduleNetwork_version2.scheduleNetwork_version2.dto.UserDTO;
import com.scheduleNetwork_version2.scheduleNetwork_version2.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);
//   List<UserDTO> getAllUser();
//    UserDTO getUserById(Long id);

    UserDTO updateUser(UserDTO userDTO, String token);

    void deleteUser( String token);

//    Optional<UserDTO> getUserByUsername(String username);

    // افزودن متد جدید برای احراز هویت
    User findUserByUsername(String username);

    // criteria
    List<UserDTO> searchUsers(String firstName , String lastName , String email);
}