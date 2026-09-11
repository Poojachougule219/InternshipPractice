package com.legalcontract.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Role;
import com.legalcontract.entity.User;
import com.legalcontract.service.UserService;

@Controller
public class AdminUserController {


private final UserService userService;

public AdminUserController(UserService userService) {
    this.userService = userService;
}

@GetMapping("/admin/users")
public String users(
        @RequestParam(
                value = "role",
                required = false
        ) String role,
        Model model) {

    List<User> users;

    if (role == null || role.isBlank()) {

        users = userService.getAllUsers();

        model.addAttribute(
                "pageTitle",
                "All Users"
        );

        model.addAttribute(
                "selectedRole",
                "ALL"
        );

    } else {

        users = userService.getUsersByRole(role);

        String displayRole =
                role.trim().toUpperCase();

        if ("ADMIN".equals(displayRole)
                || "ROLE_ADMIN".equals(displayRole)) {

            displayRole = "Administrators";

        } else if ("REVIEWER".equals(displayRole)
                || "ROLE_REVIEWER".equals(displayRole)) {

            displayRole = "Reviewers";

        } else if ("MANAGER".equals(displayRole)
                || "ROLE_MANAGER".equals(displayRole)) {

            displayRole = "Managers";

        } else {

            displayRole =
                    displayRole.replace(
                            "ROLE_",
                            ""
                    );
        }

        model.addAttribute(
                "pageTitle",
                displayRole
        );

        model.addAttribute(
                "selectedRole",
                displayRole
        );
    }

    model.addAttribute(
            "users",
            users
    );

    model.addAttribute(
            "totalAdmins",
            userService.getAdminCount()
    );

    model.addAttribute(
            "totalUsers",
            userService.getActiveUserCount()
    );

    model.addAttribute(
            "totalReviewers",
            userService.getReviewerCount()
    );

    model.addAttribute(
            "totalManagers",
            userService.getManagerCount()
    );

    return "Admin/users";
}

@GetMapping("/admin/users/view/{id}")
public String viewUser(
        @PathVariable Long id,
        Model model) {

    User user =
            userService.getUserById(id);

    model.addAttribute(
            "user",
            user
    );

    return "Admin/user-view";
}

@GetMapping("/admin/users/add")
public String showAddUserForm(
        Model model) {

    User user = new User();

    user.setStatus("ACTIVE");

    model.addAttribute(
            "user",
            user
    );

    List<Role> roles =
            userService.getAllRoles();

    model.addAttribute(
            "roles",
            roles
    );

    return "Admin/user-add";
}

@PostMapping("/admin/users/save")
public String saveUser(
        @ModelAttribute("user") User user,
        @RequestParam("roleName") String roleName,
        RedirectAttributes redirectAttributes) {

    try {

        userService.createUser(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName(),
                roleName
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "User created successfully."
        );

        return "redirect:/admin/users";

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
        );

        return "redirect:/admin/users/add";
    }
}

@GetMapping("/admin/users/edit/{id}")
public String showEditUserForm(
        @PathVariable Long id,
        Model model) {

    User user =
            userService.getUserById(id);

    List<Role> roles =
            userService.getAllRoles();

    model.addAttribute(
            "user",
            user
    );

    model.addAttribute(
            "roles",
            roles
    );

    return "Admin/user-edit";
}

@PostMapping("/admin/users/edit/{id}")
public String updateUser(
        @PathVariable Long id,
        @ModelAttribute("user") User user,
        RedirectAttributes redirectAttributes) {

    try {

        String roleName = null;

        if (user.getRole() != null
                && user.getRole().getName() != null
                && !user.getRole().getName().isBlank()) {

            roleName =
                    user.getRole().getName();
        }

        userService.updateUser(
                id,
                user,
                roleName
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "User updated successfully."
        );

        return "redirect:/admin/users/edit/" + id;

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage()
        );

        return "redirect:/admin/users/edit/" + id;
    }
}

@GetMapping("/admin/users/delete/{id}")
public String deleteUser(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {

    try {

        userService.deleteUser(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "User deleted successfully."
        );

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Unable to delete user: "
                        + e.getMessage()
        );
    }

    return "redirect:/admin/users";
}

@GetMapping("/admin/users/activate/{id}")
public String activateUser(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {

    try {

        userService.activateUser(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "User activated successfully."
        );

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
        );
    }

    return "redirect:/admin/users";
}

@GetMapping("/admin/users/deactivate/{id}")
public String deactivateUser(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {

    try {

        userService.deactivateUser(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "User deactivated successfully."
        );

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
        );
    }

    return "redirect:/admin/users";
}


}
