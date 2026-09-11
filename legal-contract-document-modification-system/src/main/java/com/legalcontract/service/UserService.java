package com.legalcontract.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Role;
import com.legalcontract.entity.User;
import com.legalcontract.repository.ContractRepository;
import com.legalcontract.repository.RoleRepository;
import com.legalcontract.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContractRepository contractRepository;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            ContractRepository contractRepository) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.contractRepository = contractRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findByStatusNot("DELETED");
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(user ->
                        !"DELETED".equalsIgnoreCase(user.getStatus()))
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with username: "
                                        + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user ->
                        !"DELETED".equalsIgnoreCase(user.getStatus()))
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: "
                                        + email));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(user ->
                        !"DELETED".equalsIgnoreCase(user.getStatus()))
                .orElse(null);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByName(String roleName) {

        String normalizedRoleName =
                normalizeRoleName(roleName);

        return roleRepository.findByName(normalizedRoleName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found: "
                                        + normalizedRoleName));
    }

    public User createUser(
            String username,
            String email,
            String password,
            String fullName,
            String roleName) {

        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("Full name is required");
        }

        if (roleName == null || roleName.isBlank()) {
            throw new RuntimeException("Role is required");
        }

        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase();
        String cleanFullName = fullName.trim();

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new RuntimeException(
                    "Username already exists: "
                            + cleanUsername);
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException(
                    "Email already exists: "
                            + cleanEmail);
        }

        Role role = getRoleByName(roleName);

        User user = new User();

        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setFullName(cleanFullName);
        user.setPassword(
                passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public User updateUser(
            Long id,
            User updatedUser,
            String roleName) {

        User existingUser = getUserById(id);

        if ("DELETED".equalsIgnoreCase(
                existingUser.getStatus())) {

            throw new RuntimeException(
                    "Cannot update a deleted user");
        }

        if (updatedUser.getUsername() != null
                && !updatedUser.getUsername().isBlank()) {

            String newUsername =
                    updatedUser.getUsername().trim();

            if (!newUsername.equals(
                    existingUser.getUsername())) {

                if (userRepository.existsByUsername(
                        newUsername)) {

                    throw new RuntimeException(
                            "Username already exists: "
                                    + newUsername);
                }
            }

            existingUser.setUsername(newUsername);
        }

        if (updatedUser.getEmail() != null
                && !updatedUser.getEmail().isBlank()) {

            String newEmail =
                    updatedUser.getEmail()
                            .trim()
                            .toLowerCase();

            if (!newEmail.equals(
                    existingUser.getEmail())) {

                if (userRepository.existsByEmail(
                        newEmail)) {

                    throw new RuntimeException(
                            "Email already exists: "
                                    + newEmail);
                }
            }

            existingUser.setEmail(newEmail);
        }

        if (updatedUser.getFullName() != null
                && !updatedUser.getFullName().isBlank()) {

            existingUser.setFullName(
                    updatedUser.getFullName().trim());
        }

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            existingUser.setPassword(
                    passwordEncoder.encode(
                            updatedUser.getPassword()));
        }

        if (roleName != null
                && !roleName.isBlank()) {

            String normalizedRoleName =
                    normalizeRoleName(roleName);

            Role role =
                    roleRepository
                            .findByName(normalizedRoleName)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Role not found: "
                                                    + normalizedRoleName));

            existingUser.setRole(role);
        }

        if (updatedUser.getStatus() != null
                && !updatedUser.getStatus().isBlank()) {

            String newStatus =
                    updatedUser.getStatus()
                            .trim()
                            .toUpperCase();

            if ("DELETED".equals(newStatus)) {
                throw new RuntimeException(
                        "Use activate operation to restore "
                                + "a deleted user");
            }

            existingUser.setStatus(newStatus);
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {

        User user = getUserById(id);

        if ("DELETED".equalsIgnoreCase(
                user.getStatus())) {

            throw new RuntimeException(
                    "User is already deleted");
        }

        user.setStatus("DELETED");

        userRepository.save(user);
    }

    public void deactivateUser(Long id) {

        User user = getUserById(id);

        if ("DELETED".equalsIgnoreCase(
                user.getStatus())) {

            throw new RuntimeException(
                    "Cannot deactivate a deleted user");
        }

        user.setStatus("INACTIVE");

        userRepository.save(user);
    }

    public void activateUser(Long id) {

        User user = getUserById(id);

        user.setStatus("ACTIVE");

        userRepository.save(user);
    }

    public boolean usernameExists(String username) {

        if (username == null || username.isBlank()) {
            return false;
        }

        return userRepository.existsByUsername(
                username.trim());
    }

    public boolean emailExists(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        return userRepository.existsByEmail(
                email.trim().toLowerCase());
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User registerUser(
            String fullName,
            String username,
            String email,
            String password,
            String roleName) {

        return createUser(
                username,
                email,
                password,
                fullName,
                roleName);
    }

    public long getAdminCount() {

        return userRepository.countByRole_NameAndStatus(
                "ROLE_ADMIN",
                "ACTIVE");
    }

    public long getReviewerCount() {

        return userRepository.countByRole_NameAndStatus(
                "ROLE_REVIEWER",
                "ACTIVE");
    }

    public long getManagerCount() {

        return userRepository.countByRole_NameAndStatus(
                "ROLE_MANAGER",
                "ACTIVE");
    }

    public long getUserCount() {

        return userRepository.countByRole_NameAndStatus(
                "ROLE_USER",
                "ACTIVE");
    }

    public long getActiveUserCount() {

        return userRepository
                .findByStatus("ACTIVE")
                .size();
    }

    public List<User> getUsersByRole(
            String roleName) {

        return userRepository
                .findByRole_NameAndStatus(
                        normalizeRoleName(roleName),
                        "ACTIVE");
    }

    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    private String normalizeRoleName(
            String roleName) {

        String normalized =
                roleName.trim().toUpperCase();

        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }

        return normalized;
    }

    public User updateProfilePhoto(
            String username,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Please select a photo");
        }

        if (file.getContentType() == null
                || !file.getContentType()
                        .startsWith("image/")) {

            throw new RuntimeException(
                    "Only image files are allowed");
        }

        User user =
                getUserByUsername(username);

        try {

            user.setProfilePhoto(
                    file.getBytes());

            return userRepository.save(user);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to upload profile photo",
                    e);
        }
    }

    public void changePassword(
            String username,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        if (currentPassword == null
                || currentPassword.isBlank()) {

            throw new RuntimeException(
                    "Current password is required");
        }

        if (newPassword == null
                || newPassword.isBlank()) {

            throw new RuntimeException(
                    "New password is required");
        }

        if (confirmPassword == null
                || confirmPassword.isBlank()) {

            throw new RuntimeException(
                    "Please confirm your new password");
        }

        if (!newPassword.equals(confirmPassword)) {

            throw new RuntimeException(
                    "New password and confirm password do not match");
        }

        if (newPassword.length() < 6) {

            throw new RuntimeException(
                    "New password must be at least 6 characters");
        }

        User user =
                getUserByUsername(username);

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword())) {

            throw new RuntimeException(
                    "Current password is incorrect");
        }

        user.setPassword(
                passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}