package com.legalcontract.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.legalcontract.entity.Role;
import com.legalcontract.repository.RoleRepository;
import com.legalcontract.repository.UserRepository;

@Service
public class RoleService {


private final RoleRepository roleRepository;
private final UserRepository userRepository;


public RoleService(
        RoleRepository roleRepository,
        UserRepository userRepository) {

    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
}


// =========================================================
// GET ALL ROLES
// =========================================================

public List<Role> getAllRoles() {

    return roleRepository.findAll();
}


// =========================================================
// GET ROLE BY ID
// =========================================================

public Role getRoleById(Long id) {

    return roleRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Role not found with ID: " + id)
            );
}


// =========================================================
// GET ROLE BY NAME
// =========================================================

public Role getRoleByName(String name) {

    return roleRepository.findByName(name)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Role not found: " + name
                    )
            );
}


// =========================================================
// CREATE ROLE
// =========================================================

@Transactional
public Role createRole(
        String name,
        String description) {

    String normalizedName =
            normalizeRoleName(name);

    if (roleRepository.existsByName(normalizedName)) {

        throw new RuntimeException(
                "Role already exists: " + normalizedName
        );
    }

    Role role = new Role();

    role.setName(normalizedName);

    role.setDescription(
            description != null
                    ? description.trim()
                    : ""
    );

    return roleRepository.save(role);
}


// =========================================================
// UPDATE ROLE
// =========================================================

@Transactional
public Role updateRole(
        Long id,
        String name,
        String description) {

    Role role = getRoleById(id);

    String normalizedName =
            normalizeRoleName(name);


    /*
     * Check whether another role already
     * has the same name.
     */
    roleRepository.findByName(normalizedName)
            .ifPresent(existingRole -> {

                if (!existingRole.getId().equals(id)) {

                    throw new RuntimeException(
                            "Role already exists: "
                                    + normalizedName
                    );
                }
            });


    role.setName(normalizedName);

    role.setDescription(
            description != null
                    ? description.trim()
                    : ""
    );

    return roleRepository.save(role);
}


// =========================================================
// DELETE ROLE
// =========================================================

@Transactional
public void deleteRole(Long id) {

    Role role = getRoleById(id);


    /*
     * Do not allow deletion when users
     * are assigned to this role.
     */
    Long userCount =
            getUserCountByRole(role.getName());

    if (userCount > 0) {

        throw new RuntimeException(
                "Cannot delete role. "
                + userCount
                + " user(s) are assigned to this role."
        );
    }


    roleRepository.delete(role);
}


// =========================================================
// USER COUNT BY ROLE
// =========================================================

public Long getUserCountByRole(String roleName) {

    try {

        return userRepository.countByRole_Name(roleName);

    } catch (Exception e) {

        return 0L;
    }
}


// =========================================================
// NORMALIZE ROLE NAME
// =========================================================

private String normalizeRoleName(String roleName) {

    if (roleName == null || roleName.trim().isEmpty()) {

        throw new RuntimeException(
                "Role name cannot be empty."
        );
    }

    String normalized =
            roleName.trim().toUpperCase();

    if (!normalized.startsWith("ROLE_")) {

        normalized = "ROLE_" + normalized;
    }

    return normalized;
}


}
