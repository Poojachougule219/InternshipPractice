package com.legalcontract.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Role;
import com.legalcontract.service.RoleService;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

private final RoleService roleService;

public RoleController(RoleService roleService) {
    this.roleService = roleService;
}


// =========================================================
// ROLE LIST
// =========================================================

@GetMapping
public String roles(Model model) {

    List<Role> roles = roleService.getAllRoles();

    model.addAttribute("roles", roles);

    /*
     * User count for each role.
     */
    Map<Long, Long> roleUserCounts = new HashMap<>();

    for (Role role : roles) {

        Long count = roleService.getUserCountByRole(role.getName());

        roleUserCounts.put(role.getId(), count);
    }

    model.addAttribute("roleUserCounts", roleUserCounts);

    return "Admin/roles";
}


// =========================================================
// ADD ROLE PAGE
// =========================================================

@GetMapping("/add")
public String addRolePage(Model model) {

    model.addAttribute("role", new Role());

    return "Admin/role-add";
}


// =========================================================
// SAVE ROLE
// =========================================================

@PostMapping("/save")
public String saveRole(
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        RedirectAttributes redirectAttributes) {

    try {

        roleService.createRole(name, description);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Role added successfully."
        );

    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage()
        );
    }

    return "redirect:/admin/roles";
}


// =========================================================
// VIEW ROLE
// =========================================================

@GetMapping("/view/{id}")
public String viewRole(
        @PathVariable Long id,
        Model model) {

    Role role = roleService.getRoleById(id);

    model.addAttribute("role", role);

    model.addAttribute(
            "userCount",
            roleService.getUserCountByRole(role.getName())
    );

    return "Admin/role-view";
}


// =========================================================
// EDIT ROLE PAGE
// =========================================================

@GetMapping("/edit/{id}")
public String editRolePage(
        @PathVariable Long id,
        Model model) {

    Role role = roleService.getRoleById(id);

    model.addAttribute("role", role);

    return "Admin/role-edit";
}


// =========================================================
// UPDATE ROLE
// =========================================================

@PostMapping("/update/{id}")
public String updateRole(
        @PathVariable Long id,
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        RedirectAttributes redirectAttributes) {

    try {

        roleService.updateRole(
                id,
                name,
                description
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Role updated successfully."
        );

    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage()
        );
    }

    return "redirect:/admin/roles";
}


// =========================================================
// DELETE ROLE
// =========================================================

@PostMapping("/delete/{id}")
public String deleteRole(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {

    try {

        roleService.deleteRole(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Role deleted successfully."
        );

    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage()
        );
    }

    return "redirect:/admin/roles";
}


}
