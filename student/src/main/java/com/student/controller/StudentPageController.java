package com.student.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.student.entity.Student;
import com.student.exception.ResourceNotFoundException;
import com.student.service.StudentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class StudentPageController {

    @Autowired
    private StudentService service;


    // =====================================================
    // STUDENT DASHBOARD
    // =====================================================

    @GetMapping("/student/dashboard")
    public String studentDashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        try {

            Student student =
                    service.getStudentByEmail(email);

            model.addAttribute(
                    "student",
                    student
            );

        } catch (ResourceNotFoundException e) {

            model.addAttribute(
                    "error",
                    "Student not found"
            );
        }

        return "Student/dashboard";
    }


    // =====================================================
    // STUDENT PROFILE PHOTO
    // =====================================================

    @GetMapping("/student/profile-photo")
    @ResponseBody
    public ResponseEntity<byte[]> getProfilePhoto(
            Authentication authentication) {

        String email = authentication.getName();

        try {

            Student student =
                    service.getStudentByEmail(email);

            byte[] profilePhoto =
                    student.getProfilePhoto();

            if (profilePhoto == null ||
                    profilePhoto.length == 0) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(profilePhoto);

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }


    // =====================================================
    // VIEW PROFILE
    // =====================================================

    @GetMapping("/student/profile")
    public String viewProfile(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        try {

            Student student =
                    service.getStudentByEmail(email);

            model.addAttribute(
                    "student",
                    student
            );

        } catch (ResourceNotFoundException e) {

            model.addAttribute(
                    "error",
                    "Student not found"
            );
        }

        return "Student/profile";
    }


    // =====================================================
    // EDIT PROFILE PAGE
    // =====================================================

    @GetMapping("/student/edit-profile")
    public String editProfile(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        try {

            Student student =
                    service.getStudentByEmail(email);

            model.addAttribute(
                    "student",
                    student
            );

            return "Student/edit_profile";

        } catch (ResourceNotFoundException e) {

            return "redirect:/student/profile";
        }
    }


    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    @PostMapping("/student/update-profile")
    public String updateProfile(

            @ModelAttribute("student")
            Student updatedStudent,

            @RequestParam(
                    value = "profilePhotoFile",
                    required = false
            )
            MultipartFile profilePhotoFile,

            Authentication authentication,

            HttpServletRequest request,

            RedirectAttributes redirectAttributes) {

        String email =
                authentication.getName();

        String ipAddress =
                request.getRemoteAddr();


        // =================================================
        // CONVERT IPv6 LOCALHOST TO IPv4
        // =================================================

        if ("0:0:0:0:0:0:0:1".equals(ipAddress)
                || "::1".equals(ipAddress)) {

            ipAddress = "127.0.0.1";
        }


        try {

            service.updateProfile(
                    email,
                    updatedStudent,
                    profilePhotoFile,
                    ipAddress
            );


            // =================================================
            // SUCCESS MESSAGE
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profile updated successfully"
            );


            return "redirect:/student/profile";


        } catch (Exception e) {


            // =================================================
            // ERROR MESSAGE
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to update profile: "
                            + e.getMessage()
            );


            return "redirect:/student/edit-profile";
        }
    }


    // =====================================================
    // CHANGE PASSWORD PAGE
    // =====================================================

    @GetMapping("/student/change-password")
    public String changePasswordPage() {

        return "Student/change_password";
    }


    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @PostMapping("/student/change-password")
    public String changePassword(

            @RequestParam("oldPassword")
            String oldPassword,

            @RequestParam("newPassword")
            String newPassword,

            @RequestParam("confirmPassword")
            String confirmPassword,

            Authentication authentication,

            RedirectAttributes redirectAttributes,

            HttpServletRequest request) {

        String email =
                authentication.getName();


        // =================================================
        // GET CLIENT IP ADDRESS
        // =================================================

        String ipAddress =
                request.getRemoteAddr();

        if ("0:0:0:0:0:0:0:1".equals(ipAddress)
                || "::1".equals(ipAddress)) {

            ipAddress = "127.0.0.1";
        }


        // =================================================
        // VALIDATE NEW PASSWORD
        // =================================================

        if (newPassword == null
                || newPassword.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password is required"
            );

            return "redirect:/student/change-password";
        }


        // =================================================
        // VALIDATE CONFIRM PASSWORD
        // =================================================

        if (confirmPassword == null
                || confirmPassword.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Confirm password is required"
            );

            return "redirect:/student/change-password";
        }


        // =================================================
        // CHECK PASSWORD MATCH
        // =================================================

        if (!newPassword.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password and confirm password do not match"
            );

            return "redirect:/student/change-password";
        }


        // =================================================
        // PASSWORD LENGTH
        // =================================================

        if (newPassword.length() < 6) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password must contain at least 6 characters"
            );

            return "redirect:/student/change-password";
        }


        try {

            // =================================================
            // CHANGE PASSWORD
            // =================================================

            String result =
                    service.changePassword(
                            email,
                            oldPassword,
                            newPassword,
                            ipAddress
                    );


            // =================================================
            // WRONG OLD PASSWORD
            // =================================================

            if (result == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Current password is incorrect"
                );

                return "redirect:/student/change-password";
            }


            // =================================================
            // NEW PASSWORD SAME AS OLD
            // =================================================

            if ("New password must be different from current password"
                    .equals(result)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        result
                );

                return "redirect:/student/change-password";
            }


            // =================================================
            // SUCCESS
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Password changed successfully"
            );


            return "redirect:/student/profile";


        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to change password: "
                            + e.getMessage()
            );

            return "redirect:/student/change-password";
        }
    }


    // =====================================================
    // UPLOAD PHOTO PAGE
    // =====================================================

    @GetMapping("/student/upload-photo")
    public String uploadPhotoPage(
            Authentication authentication,
            Model model) {

        String email =
                authentication.getName();

        try {

            Student student =
                    service.getStudentByEmail(email);

            model.addAttribute(
                    "student",
                    student
            );

            return "Student/upload_photo";

        } catch (ResourceNotFoundException e) {

            return "redirect:/student/profile";
        }
    }


    // =====================================================
    // UPLOAD PROFILE PHOTO
    // =====================================================

    @PostMapping("/student/upload-photo")
    public String uploadPhoto(

            @RequestParam("profilePhotoFile")
            MultipartFile profilePhotoFile,

            Authentication authentication,

            HttpServletRequest request,

            RedirectAttributes redirectAttributes) {

        String email =
                authentication.getName();


        String ipAddress =
                request.getRemoteAddr();


        // =================================================
        // CONVERT IPv6 LOCALHOST TO IPv4
        // =================================================

        if ("0:0:0:0:0:0:0:1".equals(ipAddress)
                || "::1".equals(ipAddress)) {

            ipAddress = "127.0.0.1";
        }


        // =================================================
        // CHECK PHOTO
        // =================================================

        if (profilePhotoFile == null
                || profilePhotoFile.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please select a profile photo"
            );

            return "redirect:/student/profile";
        }


        try {

            // =================================================
            // LOAD EXISTING STUDENT
            // =================================================

            Student student =
                    service.getStudentByEmail(email);


            // =================================================
            // UPDATE ONLY PHOTO
            // =================================================

            service.updateProfile(
                    email,
                    student,
                    profilePhotoFile,
                    ipAddress
            );


            // =================================================
            // SUCCESS
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profile photo uploaded successfully"
            );


            return "redirect:/student/profile";


        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to upload profile photo: "
                            + e.getMessage()
            );

            return "redirect:/student/profile";
        }
    }
}