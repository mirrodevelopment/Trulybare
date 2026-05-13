package com.trulybare.service;

import com.trulybare.entity.User;

public interface UserService {

    User registerUser(String fullName, String email, String phone, String password);

    User findByEmail(String email);

    User findById(Long userId);

    boolean emailExists(String email);

    boolean phoneExists(String phone);

    void updateProfile(User user);

    void changePassword(Long userId, String oldPassword, String newPassword);

    User getUserProfile(Long userId);
}