package com.trulybare.service.impl;

import com.trulybare.entity.User;
import com.trulybare.enums.UserRole;
import com.trulybare.repository.UserRepository;
import com.trulybare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(String fullName, String email, String phone, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered!");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone number already registered!");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase());
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        user.setTermsAccepted(true);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    public User findById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public void updateProfile(User user) {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        User existingUser = findById(user.getUserId());
        existingUser.setFullName(user.getFullName());
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setDateOfBirth(user.getDateOfBirth());
        userRepository.save(existingUser);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public User getUserProfile(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        return findById(userId);
    }
}