package com.legalcontract.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.Modification;
import com.legalcontract.entity.User;
import com.legalcontract.repository.DocumentRepository;
import com.legalcontract.service.ModificationService;
import com.legalcontract.service.UserService;

@Controller
public class UserPageController {


private final UserService userService;
private final ModificationService modificationService;
private final DocumentRepository documentRepository;

public UserPageController(
        UserService userService,
        ModificationService modificationService,
        DocumentRepository documentRepository) {

    this.userService = userService;
    this.modificationService = modificationService;
    this.documentRepository = documentRepository;
}

@GetMapping("/user/dashboard")
public String dashboard(
        Authentication authentication,
        Model model) {

    String username = authentication.getName();

    User user = userService.getUserByUsername(username);

    model.addAttribute("user", user);

    List<Contract> allContracts =
            userService.getAllContracts();

    List<Document> allDocuments =
            documentRepository.findAll();

    List<Modification> userModifications =
            modificationService.getModificationsByUser(
                    user.getId());

    long pendingCount = 0;
    long approvedCount = 0;
    long rejectedCount = 0;

    for (Modification modification : userModifications) {

        String status = modification.getStatus();

        if (status == null) {
            continue;
        }

        status = status.trim().toUpperCase();

        if ("PENDING".equals(status)) {
            pendingCount++;
        } else if ("APPROVED".equals(status)) {
            approvedCount++;
        } else if ("REJECTED".equals(status)) {
            rejectedCount++;
        }
    }

    model.addAttribute(
            "contractCount",
            allContracts.size());

    model.addAttribute(
            "documentCount",
            allDocuments.size());

    model.addAttribute(
            "pendingCount",
            pendingCount);

    model.addAttribute(
            "approvedCount",
            approvedCount);

    model.addAttribute(
            "rejectedCount",
            rejectedCount);

    return "User/dashboard";
}

@GetMapping("/user/contracts")
public String contracts(
        Authentication authentication,
        Model model) {

    User user =
            userService.getUserByUsername(
                    authentication.getName());

    model.addAttribute("user", user);

    model.addAttribute(
            "contracts",
            userService.getAllContracts());

    return "User/contracts";
}

@GetMapping("/user/documents")
public String documents(
        Authentication authentication,
        Model model) {

    User user =
            userService.getUserByUsername(
                    authentication.getName());

    model.addAttribute("user", user);

    List<Document> documents =
            documentRepository.findAll();

    model.addAttribute(
            "documents",
            documents);

    return "User/documents";
}

@GetMapping("/user/modifications")
public String modifications(
        Authentication authentication,
        @RequestParam(
                value = "status",
                required = false)
        String status,
        Model model) {

    User user =
            userService.getUserByUsername(
                    authentication.getName());

    List<Modification> userModifications =
            modificationService.getModificationsByUser(
                    user.getId());

    long pendingCount = 0;
    long approvedCount = 0;
    long rejectedCount = 0;

    for (Modification modification :
            userModifications) {

        if (modification.getStatus() == null) {
            continue;
        }

        String modificationStatus =
                modification.getStatus()
                        .trim()
                        .toUpperCase();

        if ("PENDING".equals(modificationStatus)) {
            pendingCount++;
        } else if ("APPROVED".equals(modificationStatus)) {
            approvedCount++;
        } else if ("REJECTED".equals(modificationStatus)) {
            rejectedCount++;
        }
    }

    List<Modification> filteredModifications =
            new ArrayList<>();

    if (status == null
            || status.trim().isEmpty()) {

        filteredModifications.addAll(
                userModifications);

    } else {

        String requestedStatus =
                status.trim().toUpperCase();

        for (Modification modification :
                userModifications) {

            if (modification.getStatus() != null
                    && requestedStatus.equals(
                    modification.getStatus()
                            .trim()
                            .toUpperCase())) {

                filteredModifications.add(
                        modification);
            }
        }
    }

    model.addAttribute(
            "user",
            user);

    model.addAttribute(
            "modifications",
            filteredModifications);

    model.addAttribute(
            "selectedStatus",
            status);

    model.addAttribute(
            "allCount",
            userModifications.size());

    model.addAttribute(
            "pendingCount",
            pendingCount);

    model.addAttribute(
            "approvedCount",
            approvedCount);

    model.addAttribute(
            "rejectedCount",
            rejectedCount);

    return "User/modifications";
}

@GetMapping("/user/profile")
public String profile(
        Authentication authentication,
        Model model) {

    String username =
            authentication.getName();

    User user =
            userService.getUserByUsername(
                    username);

    model.addAttribute(
            "user",
            user);

    model.addAttribute(
            "username",
            username);

    return "User/profile";
}

@GetMapping("/user/profile/edit")
public String editProfile(
        Authentication authentication,
        Model model) {

    User user =
            userService.getUserByUsername(
                    authentication.getName());

    model.addAttribute(
            "user",
            user);

    return "User/profile-edit";
}

@PostMapping("/user/profile/update")
public String updateProfile(
        Authentication authentication,
        @RequestParam("fullName")
        String fullName,
        @RequestParam("email")
        String email,
        RedirectAttributes redirectAttributes) {

    try {

        User currentUser =
                userService.getUserByUsername(
                        authentication.getName());

        User updatedUser = new User();

        updatedUser.setFullName(fullName);
        updatedUser.setEmail(email);

        userService.updateUser(
                currentUser.getId(),
                updatedUser,
                null);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile updated successfully.");

        return "redirect:/user/profile";

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage());

        return "redirect:/user/profile/edit";
    }
}

@PostMapping("/user/profile/upload-photo")
public String uploadProfilePhoto(
        Authentication authentication,
        @RequestParam("file")
        MultipartFile file,
        RedirectAttributes redirectAttributes) {

    try {

        String username =
                authentication.getName();

        userService.updateProfilePhoto(
                username,
                file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile photo uploaded successfully!");

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage());
    }

    return "redirect:/user/profile";
}

@GetMapping("/user/profile/photo/{id}")
public ResponseEntity<ByteArrayResource> profilePhoto(
        @PathVariable Long id) {

    User user =
            userService.getUserById(id);

    if (user == null
            || user.getProfilePhoto() == null
            || user.getProfilePhoto().length == 0) {

        return ResponseEntity.notFound().build();
    }

    byte[] photo =
            user.getProfilePhoto();

    MediaType mediaType =
            MediaType.IMAGE_JPEG;

    if (photo.length >= 8
            && photo[0] == (byte) 0x89
            && photo[1] == (byte) 0x50
            && photo[2] == (byte) 0x4E
            && photo[3] == (byte) 0x47) {

        mediaType =
                MediaType.IMAGE_PNG;

    } else if (photo.length >= 6
            && photo[0] == 'G'
            && photo[1] == 'I'
            && photo[2] == 'F') {

        mediaType =
                MediaType.IMAGE_GIF;

    } else if (photo.length >= 12
            && photo[0] == 'R'
            && photo[1] == 'I'
            && photo[2] == 'F'
            && photo[3] == 'F'
            && photo[8] == 'W'
            && photo[9] == 'E'
            && photo[10] == 'B'
            && photo[11] == 'P') {

        mediaType =
                MediaType.parseMediaType(
                        "image/webp");
    }

    ByteArrayResource resource =
            new ByteArrayResource(photo);

    return ResponseEntity.ok()
            .contentType(mediaType)
            .header(
                    HttpHeaders.CACHE_CONTROL,
                    "no-cache, no-store, must-revalidate")
            .body(resource);
}

@GetMapping("/user/change-password")
public String changePasswordPage(
        Authentication authentication,
        Model model) {

    User user =
            userService.getUserByUsername(
                    authentication.getName());

    model.addAttribute(
            "user",
            user);

    return "User/change-password";
}

@PostMapping("/user/change-password")
public String changePassword(
        Authentication authentication,
        @RequestParam("currentPassword")
        String currentPassword,
        @RequestParam("newPassword")
        String newPassword,
        @RequestParam("confirmPassword")
        String confirmPassword,
        RedirectAttributes redirectAttributes) {

    try {

        userService.changePassword(
                authentication.getName(),
                currentPassword,
                newPassword,
                confirmPassword);

        redirectAttributes.addFlashAttribute(
                "success",
                "Password changed successfully.");

        return "redirect:/user/profile";

    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage());

        return "redirect:/user/change-password";
    }
}


}
