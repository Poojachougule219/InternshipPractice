package com.student.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.student.entity.Student;
import com.student.enums.StudentStatus;
import com.student.repository.StudentRepository;
import com.student.service.StudentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @Autowired
    private StudentService service;

    @Autowired
    private StudentRepository repository;


    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            Authentication authentication) {

        // =================================================
        // SAFETY CHECK
        // =================================================

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        String email = authentication.getName();

        Student admin =
                service.getStudentByEmail(email);

        // =================================================
        // SAFETY CHECK FOR ADMIN
        // =================================================

        if (admin == null) {

            return "redirect:/login";
        }

        model.addAttribute(
                "admin",
                admin
        );

        // =================================================
        // DASHBOARD COUNTS
        // =================================================

        model.addAttribute(
                "totalUsers",
                service.getTotalUsers()
        );

        model.addAttribute(
                "totalStudents",
                service.getTotalStudents()
        );

        model.addAttribute(
                "totalAdmins",
                service.getTotalAdmins()
        );

        model.addAttribute(
                "activeCount",
                service.getActiveCount()
        );

        model.addAttribute(
                "activeStudents",
                service.getActiveStudents()
        );

        model.addAttribute(
                "activeAdmins",
                service.getActiveAdmins()
        );

        model.addAttribute(
                "departmentCount",
                service.getDepartmentCount()
        );

        model.addAttribute(
                "recentStudentCount",
                service.getRecentStudentCount()
        );

        // =================================================
        // DASHBOARD CHART DATA
        // =================================================

        model.addAttribute(
                "departmentData",
                service.getStudentCountByDepartment()
        );

        model.addAttribute(
                "cityData",
                service.getStudentCountByCity()
        );

        return "Admin/dashboard";
    }


    // =====================================================
    // USERS LIST
    // SEARCH + ROLE + PAGINATION + SORTING
    // =====================================================

    @GetMapping("/users")
    public String users(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String role,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "7")
            int size,

            @RequestParam(defaultValue = "id")
            String sortField,

            @RequestParam(defaultValue = "desc")
            String sortDir,

            Model model) {

        Page<Student> students =
                service.getAllUsers(
                        keyword,
                        role,
                        page,
                        size,
                        sortField,
                        sortDir
                );

        model.addAttribute(
                "users",
                students
        );

        model.addAttribute(
                "currentPage",
                students.getNumber()
        );

        model.addAttribute(
                "totalPages",
                students.getTotalPages()
        );

        model.addAttribute(
                "totalElements",
                students.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "role",
                role
        );

        model.addAttribute(
                "size",
                size
        );

        model.addAttribute(
                "sortField",
                sortField
        );

        model.addAttribute(
                "sortDir",
                sortDir
        );

        return "Admin/users";
    }


    // =====================================================
    // ADMIN LIST
    // =====================================================

    @GetMapping("/admins")
    public String admins(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "7")
            int size,

            @RequestParam(defaultValue = "id")
            String sortField,

            @RequestParam(defaultValue = "desc")
            String sortDir,

            Model model,

            Authentication authentication) {

        Page<Student> admins =
                service.getAllAdmins(
                        keyword,
                        page,
                        size,
                        sortField,
                        sortDir
                );

        model.addAttribute(
                "students",
                admins
        );

        model.addAttribute(
                "admins",
                admins
        );

        // =================================================
        // GET LOGGED-IN ADMIN
        // =================================================

        if (authentication != null) {

            String loggedInEmail =
                    authentication.getName();

            Student loggedInAdmin =
                    service.getStudentByEmail(
                            loggedInEmail
                    );

            if (loggedInAdmin != null) {

                model.addAttribute(
                        "loggedInAdminId",
                        loggedInAdmin.getId()
                );
            }
        }

        model.addAttribute(
                "currentPage",
                admins.getNumber()
        );

        model.addAttribute(
                "totalPages",
                admins.getTotalPages()
        );

        model.addAttribute(
                "totalElements",
                admins.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "size",
                size
        );

        model.addAttribute(
                "sortField",
                sortField
        );

        model.addAttribute(
                "sortDir",
                sortDir
        );

        return "Admin/admins";
    }


    // =====================================================
    // STUDENT LIST
    // =====================================================

    @GetMapping("/students")
    public String students(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "7")
            int size,

            @RequestParam(defaultValue = "id")
            String sortField,

            @RequestParam(defaultValue = "desc")
            String sortDir,

            Model model) {

        Page<Student> students =
                service.getAllStudentsOnly(
                        keyword,
                        page,
                        size,
                        sortField,
                        sortDir
                );

        model.addAttribute(
                "students",
                students
        );

        model.addAttribute(
                "currentPage",
                students.getNumber()
        );

        model.addAttribute(
                "totalPages",
                students.getTotalPages()
        );

        model.addAttribute(
                "totalElements",
                students.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "size",
                size
        );

        model.addAttribute(
                "sortField",
                sortField
        );

        model.addAttribute(
                "sortDir",
                sortDir
        );

        return "Admin/students_list";
    }


    // =====================================================
    // RECENT REGISTRATIONS
    // =====================================================

    @GetMapping("/recent")
    public String recentRegistrations(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "7")
            int size,

            @RequestParam(defaultValue = "createdDate")
            String sortField,

            @RequestParam(defaultValue = "desc")
            String sortDir,

            Model model) {

        Page<Student> students =
                service.getRecentStudents(
                        keyword,
                        page,
                        size,
                        sortField,
                        sortDir
                );

        model.addAttribute(
                "students",
                students
        );

        model.addAttribute(
                "currentPage",
                students.getNumber()
        );

        model.addAttribute(
                "totalPages",
                students.getTotalPages()
        );

        model.addAttribute(
                "totalElements",
                students.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "size",
                size
        );

        model.addAttribute(
                "sortField",
                sortField
        );

        model.addAttribute(
                "sortDir",
                sortDir
        );

        return "Admin/recent_registration";
    }


    // =====================================================
    // DELETE USER - SOFT DELETE
    // ADMIN CANNOT DELETE ANOTHER ADMIN
    // =====================================================

    @GetMapping("/user/delete/{id}")
    public String deleteUser(

            @PathVariable Long id,

            @RequestParam(
                    value = "source",
                    defaultValue = "users"
            )
            String source,

            HttpServletRequest request,

            Authentication authentication,

            RedirectAttributes redirectAttributes) {

        try {

            Student student =
                    repository.findById(id).orElse(null);

            if (student == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "User not found."
                );

                return getDeleteRedirect(source);
            }

            // =================================================
            // CHECK ADMIN
            // =================================================

            if (student.getRole() != null
                    && "ROLE_ADMIN".equalsIgnoreCase(
                            student.getRole().getName())) {

                String loggedInEmail =
                        authentication != null
                                ? authentication.getName()
                                : null;

                if (loggedInEmail == null
                        || !student.getEmail()
                                .equalsIgnoreCase(loggedInEmail)) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "You cannot delete admin profile: "
                                    + student.getName()
                                    + " (" + student.getEmail() + ")."
                    );

                    return getDeleteRedirect(source);
                }
            }

            String ipAddress =
                    getClientIpAddress(request);

            service.softDelete(
                    id,
                    ipAddress,
                    authentication
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "User deleted successfully."
            );

            return getDeleteRedirect(source);

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to delete user: "
                            + e.getMessage()
            );

            return getDeleteRedirect(source);
        }
    }


    // =====================================================
    // DELETE REDIRECT HELPER
    // =====================================================

    private String getDeleteRedirect(String source) {

        if ("admins".equalsIgnoreCase(source)) {
            return "redirect:/admin/admins";
        }

        if ("students".equalsIgnoreCase(source)) {
            return "redirect:/admin/students-list";
        }

        if ("recent".equalsIgnoreCase(source)) {
            return "redirect:/admin/recent";
        }

        return "redirect:/admin/users";
    }


    // =====================================================
    // EDIT REDIRECT HELPER
    // =====================================================

    private String getEditRedirect(String source) {

        if ("admins".equalsIgnoreCase(source)) {
            return "redirect:/admin/admins";
        }

        if ("students".equalsIgnoreCase(source)) {
            return "redirect:/admin/students-list";
        }

        if ("recent".equalsIgnoreCase(source)) {
            return "redirect:/admin/recent";
        }

        return "redirect:/admin/users";
    }


    // =====================================================
    // EXPORT USERS CSV
    // =====================================================

    @GetMapping("/users/export")
    public void exportUsers(
            HttpServletResponse response)
            throws IOException {

        response.setContentType("text/csv");

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=users.csv"
        );

        List<Student> students =
                service.getAllStudents();

        PrintWriter writer =
                response.getWriter();

        writer.println(
                "ID,Name,Email,Role,Department,City,Status"
        );

        for (Student student : students) {

            String role = "";

            if (student.getRole() != null) {
                role = student.getRole().getName();
            }

            writer.println(
                    student.getId()
                    + ","
                    + escapeCsv(student.getName())
                    + ","
                    + escapeCsv(student.getEmail())
                    + ","
                    + escapeCsv(role)
                    + ","
                    + escapeCsv(student.getDepartment())
                    + ","
                    + escapeCsv(student.getCity())
                    + ","
                    + student.getStatus()
            );
        }

        writer.flush();
    }


    // =====================================================
    // EXPORT ADMINS CSV
    // =====================================================

    @GetMapping("/admins/export")
    public void exportAdmins(
            HttpServletResponse response)
            throws IOException {

        response.setContentType("text/csv");

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=admins.csv"
        );

        List<Student> admins =
                service.getAllAdmins();

        PrintWriter writer =
                response.getWriter();

        writer.println(
                "ID,Name,Email,Role,Department,City,Status"
        );

        for (Student student : admins) {

            String role = "";

            if (student.getRole() != null) {
                role = student.getRole().getName();
            }

            writer.println(
                    student.getId()
                    + ","
                    + escapeCsv(student.getName())
                    + ","
                    + escapeCsv(student.getEmail())
                    + ","
                    + escapeCsv(role)
                    + ","
                    + escapeCsv(student.getDepartment())
                    + ","
                    + escapeCsv(student.getCity())
                    + ","
                    + student.getStatus()
            );
        }

        writer.flush();
    }


    // =====================================================
    // EXPORT STUDENTS CSV
    // =====================================================

    @GetMapping("/students/export")
    public void exportStudents(
            HttpServletResponse response)
            throws IOException {

        response.setContentType("text/csv");

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=students.csv"
        );

        List<Student> students =
                service.getAllStudentsOnly();

        PrintWriter writer =
                response.getWriter();

        writer.println(
                "ID,Name,Email,Role,Department,City,Status"
        );

        for (Student student : students) {

            String role = "";

            if (student.getRole() != null) {
                role = student.getRole().getName();
            }

            writer.println(
                    student.getId()
                    + ","
                    + escapeCsv(student.getName())
                    + ","
                    + escapeCsv(student.getEmail())
                    + ","
                    + escapeCsv(role)
                    + ","
                    + escapeCsv(student.getDepartment())
                    + ","
                    + escapeCsv(student.getCity())
                    + ","
                    + student.getStatus()
            );
        }

        writer.flush();
    }


    // =====================================================
    // CSV ESCAPE
    // =====================================================

    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")) {

            return "\""
                    + value.replace("\"", "\"\"")
                    + "\"";
        }

        return value;
    }


    // =====================================================
    // ADMIN PROFILE
    // =====================================================

    @GetMapping("/profile")
    public String profile(
            Model model,
            Authentication authentication) {

        String email =
                authentication.getName();

        Student admin =
                service.getStudentByEmail(email);

        model.addAttribute(
                "admin",
                admin
        );

        return "Admin/profile";
    }


    // =====================================================
    // ADMIN EDIT PROFILE PAGE
    // =====================================================

    @GetMapping("/edit-profile")
    public String editProfile(
            Model model,
            Authentication authentication) {

        String email =
                authentication.getName();

        Student admin =
                service.getStudentByEmail(email);

        model.addAttribute(
                "admin",
                admin
        );

        return "Admin/edit_profile";
    }


    // =====================================================
    // ADMIN UPDATE PROFILE
    // =====================================================

    @PostMapping("/update-profile")
    public String updateProfile(

            @RequestParam("name")
            String name,

            @RequestParam("age")
            Integer age,

            @RequestParam("contactNo")
            String contactNo,

            @RequestParam("department")
            String department,

            @RequestParam("city")
            String city,

            @RequestParam("address")
            String address,

            @RequestParam(
                    value = "profilePhoto",
                    required = false
            )
            MultipartFile profilePhoto,

            Authentication authentication,

            HttpServletRequest request,

            RedirectAttributes redirectAttributes) {

        try {

            String email =
                    authentication.getName();

            Student updatedStudent =
                    new Student();

            updatedStudent.setName(name);
            updatedStudent.setAge(age);
            updatedStudent.setContactNo(contactNo);
            updatedStudent.setDepartment(department);
            updatedStudent.setCity(city);
            updatedStudent.setAddress(address);

            String ipAddress =
                    getClientIpAddress(request);

            service.updateProfile(
                    email,
                    updatedStudent,
                    profilePhoto,
                    ipAddress
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profile updated successfully."
            );

            return "redirect:/admin/profile";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to update profile: "
                            + e.getMessage()
            );

            return "redirect:/admin/edit-profile";
        }
    }


    // =====================================================
    // ADMIN UPLOAD PROFILE PHOTO PAGE
    // =====================================================

    @GetMapping("/upload-photo")
    public String uploadPhotoPage(
            Model model,
            Authentication authentication) {

        String email =
                authentication.getName();

        Student admin =
                service.getStudentByEmail(email);

        if (admin == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "admin",
                admin
        );

        return "Admin/upload_photo";
    }


    // =====================================================
    // ADMIN UPLOAD PROFILE PHOTO
    // =====================================================

    @PostMapping("/upload-photo")
    public String uploadPhoto(

            @RequestParam(
                    value = "image",
                    required = false
            )
            MultipartFile image,

            Authentication authentication,

            HttpServletRequest request,

            RedirectAttributes redirectAttributes) {

        try {

            if (image == null || image.isEmpty()) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Please select a profile photo."
                );

                return "redirect:/admin/upload-photo";
            }

            String email =
                    authentication.getName();

            Student admin =
                    service.getStudentByEmail(email);

            if (admin == null) {
                return "redirect:/login";
            }

            String ipAddress =
                    getClientIpAddress(request);

            Student updatedStudent =
                    new Student();

            updatedStudent.setName(admin.getName());
            updatedStudent.setAge(admin.getAge());

            updatedStudent.setDepartment(
                    admin.getDepartment()
            );

            updatedStudent.setCity(
                    admin.getCity()
            );

            updatedStudent.setContactNo(
                    admin.getContactNo()
            );

            updatedStudent.setAddress(
                    admin.getAddress()
            );

            service.updateProfile(
                    email,
                    updatedStudent,
                    image,
                    ipAddress
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profile photo uploaded successfully."
            );

            return "redirect:/admin/profile";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to upload profile photo: "
                            + e.getMessage()
            );

            return "redirect:/admin/upload-photo";
        }
    }


    // =====================================================
    // ADMIN CHANGE PASSWORD PAGE
    // =====================================================

    @GetMapping("/change-password")
    public String changePasswordPage(
            Model model,
            Authentication authentication) {

        String email =
                authentication.getName();

        Student admin =
                service.getStudentByEmail(email);

        if (admin == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "admin",
                admin
        );

        return "Admin/change_password";
    }


    // =====================================================
    // ADMIN CHANGE PASSWORD
    // =====================================================

    @PostMapping("/change-password")
    public String changePassword(

            @RequestParam("oldPassword")
            String oldPassword,

            @RequestParam("newPassword")
            String newPassword,

            @RequestParam("confirmPassword")
            String confirmPassword,

            Authentication authentication,

            HttpServletRequest request,

            RedirectAttributes redirectAttributes) {

        try {

            String email =
                    authentication.getName();

            if (newPassword == null
                    || newPassword.trim().isEmpty()) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "New password cannot be empty."
                );

                return "redirect:/admin/change-password";
            }

            if (!newPassword.equals(confirmPassword)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "New password and confirm password do not match."
                );

                return "redirect:/admin/change-password";
            }

            String ipAddress =
                    getClientIpAddress(request);

            String result =
                    service.changePassword(
                            email,
                            oldPassword,
                            newPassword,
                            ipAddress
                    );

            if (result == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Current password is incorrect."
                );

                return "redirect:/admin/change-password";
            }

            redirectAttributes.addFlashAttribute(
                    "success",
                    result
            );

            return "redirect:/admin/profile";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to change password: "
                            + e.getMessage()
            );

            return "redirect:/admin/change-password";
        }
    }


    // =====================================================
    // GET CLIENT IP
    // =====================================================

    private String getClientIpAddress(
            HttpServletRequest request) {

        String ip =
                request.getHeader("X-Forwarded-For");

        if (ip == null
                || ip.isEmpty()
                || "unknown".equalsIgnoreCase(ip)) {

            ip =
                    request.getHeader(
                            "Proxy-Client-IP"
                    );
        }

        if (ip == null
                || ip.isEmpty()
                || "unknown".equalsIgnoreCase(ip)) {

            ip =
                    request.getHeader(
                            "WL-Proxy-Client-IP"
                    );
        }

        if (ip == null
                || ip.isEmpty()
                || "unknown".equalsIgnoreCase(ip)) {

            ip =
                    request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {

            ip =
                    ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip)) {

            ip = "127.0.0.1";
        }

        return ip;
    }


    // =====================================================
    // VIEW USER / STUDENT DETAILS
    // =====================================================

    @GetMapping("/user/view/{id}")
    public String viewUser(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            Student student =
                    repository.findById(id).orElse(null);

            if (student == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "User not found."
                );

                return "redirect:/admin/users";
            }

            model.addAttribute(
                    "student",
                    student
            );

            model.addAttribute(
                    "user",
                    student
            );

            return "Admin/view_user";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to view user: "
                            + e.getMessage()
            );

            return "redirect:/admin/users";
        }
    }


    // =====================================================
    // EDIT USER PAGE
    // ADMIN CAN EDIT STUDENT
    // ADMIN CAN EDIT OWN PROFILE
    // ADMIN CANNOT EDIT ANOTHER ADMIN
    // =====================================================

    @GetMapping("/user/edit/{id}")
    public String editUser(

            @PathVariable Long id,

            @RequestParam(
                    value = "source",
                    defaultValue = "users"
            )
            String source,

            Model model,

            Authentication authentication,

            RedirectAttributes redirectAttributes) {

        try {

            Student student =
                    repository.findById(id).orElse(null);

            if (student == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "User not found."
                );

                return getEditRedirect(source);
            }

            String loggedInEmail =
                    authentication != null
                            ? authentication.getName()
                            : null;

            if (student.getRole() != null
                    && "ROLE_ADMIN".equalsIgnoreCase(
                            student.getRole().getName())) {

                if (loggedInEmail == null
                        || !student.getEmail()
                                .equalsIgnoreCase(loggedInEmail)) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "You cannot edit another admin profile: "
                                    + student.getName()
                                    + " (" + student.getEmail() + ")."
                    );

                    return getEditRedirect(source);
                }
            }

            model.addAttribute(
                    "user",
                    student
            );

            model.addAttribute(
                    "source",
                    source
            );

            return "Admin/edit_user";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to edit user: "
                            + e.getMessage()
            );

            return getEditRedirect(source);
        }
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    @PostMapping("/user/update")
    public String updateUser(

            @RequestParam("id")
            Long id,

            @RequestParam("name")
            String name,

            @RequestParam("age")
            Integer age,

            @RequestParam("contactNo")
            String contactNo,

            @RequestParam("department")
            String department,

            @RequestParam("city")
            String city,

            @RequestParam("address")
            String address,

            @RequestParam("status")
            StudentStatus status,

            @RequestParam(
                    value = "source",
                    defaultValue = "users"
            )
            String source,

            Authentication authentication,

            HttpServletRequest request,

            RedirectAttributes redirectAttributes) {

        try {

            Student existingUser =
                    repository.findById(id).orElse(null);

            if (existingUser == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "User not found."
                );

                return getEditRedirect(source);
            }

            Student updatedStudent =
                    new Student();

            updatedStudent.setName(name);
            updatedStudent.setAge(age);
            updatedStudent.setContactNo(contactNo);
            updatedStudent.setDepartment(department);
            updatedStudent.setCity(city);
            updatedStudent.setAddress(address);
            updatedStudent.setStatus(status);

            String ipAddress =
                    getClientIpAddress(request);

            service.updateUserByAdmin(
                    id,
                    updatedStudent,
                    ipAddress,
                    authentication
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "User updated successfully."
            );

            return getEditRedirect(source);

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to update user: "
                            + e.getMessage()
            );

            return getEditRedirect(source);
        }
    }


    // =====================================================
    // EXPORT RECENT REGISTRATIONS CSV
    // =====================================================

    @GetMapping("/recent-registration/export")
    public void exportRecentRegistrations(
            HttpServletResponse response)
            throws IOException {

        response.setContentType("text/csv");

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=recent_registrations.csv"
        );

        List<Student> students =
                service.getRecentStudents();

        PrintWriter writer =
                response.getWriter();

        writer.println(
                "ID,Name,Email,Role,Department,City,Status,Created Date"
        );

        for (Student student : students) {

            String role = "";

            if (student.getRole() != null) {
                role = student.getRole().getName();
            }

            writer.println(
                    student.getId()
                    + ","
                    + escapeCsv(student.getName())
                    + ","
                    + escapeCsv(student.getEmail())
                    + ","
                    + escapeCsv(role)
                    + ","
                    + escapeCsv(student.getDepartment())
                    + ","
                    + escapeCsv(student.getCity())
                    + ","
                    + student.getStatus()
                    + ","
                    + student.getCreatedDate()
            );
        }

        writer.flush();
    }


    // =====================================================
    // STUDENT LIST - /admin/students-list
    // USED BY TOTAL STUDENTS CARD
    // =====================================================

    @GetMapping("/students-list")
    public String studentsList(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "7")
            int size,

            @RequestParam(defaultValue = "id")
            String sortField,

            @RequestParam(defaultValue = "desc")
            String sortDir,

            Model model) {

        Page<Student> students =
                service.getAllStudentsOnly(
                        keyword,
                        page,
                        size,
                        sortField,
                        sortDir
                );

        model.addAttribute(
                "students",
                students
        );

        model.addAttribute(
                "currentPage",
                students.getNumber()
        );

        model.addAttribute(
                "totalPages",
                students.getTotalPages()
        );

        model.addAttribute(
                "totalElements",
                students.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "size",
                size
        );

        model.addAttribute(
                "sortField",
                sortField
        );

        model.addAttribute(
                "sortDir",
                sortDir
        );

        return "Admin/students_list";
    }
}