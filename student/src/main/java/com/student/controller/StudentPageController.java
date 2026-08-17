package com.student.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.student.entity.Student;
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

        Optional<Student> student =
                service.findByEmail(email);

        if (student.isPresent()) {

            model.addAttribute(
                    "student",
                    student.get()
            );
        }

        return "Student/dashboard";
    }

    // =====================================================
    // VIEW PROFILE
    // =====================================================

    @GetMapping("/student/profile")
    public String viewProfile(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Optional<Student> student =
                service.findByEmail(email);

        if (student.isPresent()) {

            model.addAttribute(
                    "student",
                    student.get()
            );

        } else {

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

        Optional<Student> student =
                service.findByEmail(email);

        if (student.isPresent()) {

            model.addAttribute(
                    "student",
                    student.get()
            );

            return "Student/edit_profile";
        }

        return "redirect:/student/profile";
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    @PostMapping("/student/update-profile")
    public String updateProfile(
            @ModelAttribute("student") Student updatedStudent,
            @RequestParam(
                    value = "profilePhotoFile",
                    required = false
            ) MultipartFile profilePhotoFile,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        String ipAddress =
                request.getRemoteAddr();

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

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profile updated successfully"
            );

            return "redirect:/student/profile";

        } catch (Exception e) {

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
    
    
//=====================================================
//CHANGE PASSWORD
//=====================================================

@PostMapping("/student/change-password")
public String changePassword(
      @RequestParam("oldPassword") String oldPassword,
      @RequestParam("newPassword") String newPassword,
      @RequestParam("confirmPassword") String confirmPassword,
      Authentication authentication,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {

  String email = authentication.getName();

  // =================================================
  // GET CLIENT IP ADDRESS
  // =================================================

  String ipAddress = request.getRemoteAddr();

  // Convert IPv6 localhost to IPv4 localhost
  if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
      ipAddress = "127.0.0.1";
  }

  // =================================================
  // CHECK NEW PASSWORD AND CONFIRM PASSWORD
  // =================================================

  if (!newPassword.equals(confirmPassword)) {

      redirectAttributes.addFlashAttribute(
              "error",
              "New password and confirm password do not match"
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
      // WRONG OLD PASSWORD / STUDENT NOT FOUND
      // =================================================

      if (result == null) {

          redirectAttributes.addFlashAttribute(
                  "error",
                  "Current password is incorrect"
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


 
 
 
//=====================================================
//UPLOAD PHOTO PAGE
//=====================================================

@GetMapping("/student/upload-photo")
public String uploadPhotoPage(
      Authentication authentication,
      Model model) {

  String email = authentication.getName();

  Optional<Student> student =
          service.findByEmail(email);

  if (student.isPresent()) {

      model.addAttribute(
              "student",
              student.get()
      );

      return "Student/upload_photo";
  }

  return "redirect:/student/profile";
}

 
 
//=====================================================
//UPLOAD PROFILE PHOTO
//=====================================================

@PostMapping("/student/upload-photo")
public String uploadPhoto(
      @RequestParam("profilePhotoFile") MultipartFile profilePhotoFile,
      Authentication authentication,
      HttpServletRequest request,
      RedirectAttributes redirectAttributes) {

  String email = authentication.getName();

  String ipAddress = request.getRemoteAddr();

  if ("0:0:0:0:0:0:0:1".equals(ipAddress)
          || "::1".equals(ipAddress)) {

      ipAddress = "127.0.0.1";
  }

  try {

      // Load existing student
      Optional<Student> student =
              service.findByEmail(email);

      if (student.isEmpty()) {

          redirectAttributes.addFlashAttribute(
                  "error",
                  "Student not found"
          );

          return "redirect:/student/profile";
      }

      // Use existing profile update method.
      // Pass the existing student so only the photo is changed.
      service.updateProfile(
              email,
              student.get(),
              profilePhotoFile,
              ipAddress
      );

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