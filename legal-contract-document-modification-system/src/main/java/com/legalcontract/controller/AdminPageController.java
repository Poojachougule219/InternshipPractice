package com.legalcontract.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Approval;
import com.legalcontract.entity.AuditLog;
import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.User;
import com.legalcontract.service.ApprovalService;
import com.legalcontract.service.AuditLogService;
import com.legalcontract.service.ContractService;
import com.legalcontract.service.DocumentService;
import com.legalcontract.service.ModificationService;
import com.legalcontract.service.UserService;

@Controller
public class AdminPageController {


private final DocumentService documentService;
private final ApprovalService approvalService;
private final AuditLogService auditLogService;
private final UserService userService;
private final ContractService contractService;
private final ModificationService modificationService;

public AdminPageController(
        DocumentService documentService,
        ApprovalService approvalService,
        AuditLogService auditLogService,
        UserService userService,
        ContractService contractService,
        ModificationService modificationService) {

    this.documentService = documentService;
    this.approvalService = approvalService;
    this.auditLogService = auditLogService;
    this.userService = userService;
    this.contractService = contractService;
    this.modificationService = modificationService;
}

@GetMapping("/admin/dashboard")
public String adminDashboard(
        Authentication authentication,
        Model model) {

    String username = authentication.getName();

    User user = userService.findByUsername(username);

    if (user != null && user.getFullName() != null) {

        model.addAttribute(
                "username",
                user.getFullName()
        );

    } else {

        model.addAttribute(
                "username",
                username
        );
    }

    model.addAttribute(
            "user",
            user
    );

    List<Contract> contracts =
            contractService.getAllContracts();

    List<Approval> approvals =
            approvalService.getAllApprovals();

    long totalAdmins =
            userService.getAdminCount();

    long totalUsers =
            userService.getActiveUserCount();

    long totalReviewers =
            userService.getReviewerCount();

    long totalManagers =
            userService.getManagerCount();

    long totalContracts =
            contracts.size();

    long totalDocuments =
            documentService
                    .getAllDocuments()
                    .size();

    long totalApprovals =
            approvals.size();

    long pendingApprovals =
            approvals.stream()
                    .filter(approval ->
                            approval.getStatus() != null
                            &&
                            "PENDING".equalsIgnoreCase(
                                    approval.getStatus().trim()
                            )
                    )
                    .count();

    long approvedApprovals =
            approvals.stream()
                    .filter(approval ->
                            approval.getStatus() != null
                            &&
                            "APPROVED".equalsIgnoreCase(
                                    approval.getStatus().trim()
                            )
                    )
                    .count();

    long rejectedApprovals =
            approvals.stream()
                    .filter(approval ->
                            approval.getStatus() != null
                            &&
                            "REJECTED".equalsIgnoreCase(
                                    approval.getStatus().trim()
                            )
                    )
                    .count();

    List<String> activityLabels =
            new ArrayList<>();

    List<Long> activityValues =
            new ArrayList<>();

    LocalDate currentDate =
            LocalDate.now();

    for (int i = 5; i >= 0; i--) {

        LocalDate monthDate =
                currentDate
                        .withDayOfMonth(1)
                        .minusMonths(i);

        YearMonth yearMonth =
                YearMonth.from(monthDate);

        long count =
                contracts.stream()
                        .filter(contract ->
                                contract.getCreatedAt() != null
                                &&
                                YearMonth.from(
                                        contract.getCreatedAt()
                                ).equals(yearMonth)
                        )
                        .count();

        activityLabels.add(
                monthDate
                        .getMonth()
                        .getDisplayName(
                                TextStyle.SHORT,
                                Locale.ENGLISH
                        )
        );

        activityValues.add(count);
    }

    model.addAttribute(
            "totalAdmins",
            totalAdmins
    );

    model.addAttribute(
            "totalUsers",
            totalUsers
    );

    model.addAttribute(
            "totalReviewers",
            totalReviewers
    );

    model.addAttribute(
            "totalManagers",
            totalManagers
    );

    model.addAttribute(
            "totalContracts",
            totalContracts
    );

    model.addAttribute(
            "totalDocuments",
            totalDocuments
    );

    model.addAttribute(
            "totalApprovals",
            totalApprovals
    );

    model.addAttribute(
            "pendingApprovals",
            pendingApprovals
    );

    model.addAttribute(
            "approvedApprovals",
            approvedApprovals
    );

    model.addAttribute(
            "rejectedApprovals",
            rejectedApprovals
    );

    model.addAttribute(
            "activityLabels",
            activityLabels
    );

    model.addAttribute(
            "activityValues",
            activityValues
    );

    model.addAttribute(
            "userCount",
            totalUsers
    );

    model.addAttribute(
            "adminCount",
            totalAdmins
    );

    model.addAttribute(
            "reviewerCount",
            totalReviewers
    );

    model.addAttribute(
            "managerCount",
            totalManagers
    );

    model.addAttribute(
            "contractCount",
            totalContracts
    );

    model.addAttribute(
            "documentCount",
            totalDocuments
    );

    model.addAttribute(
            "approvalCount",
            totalApprovals
    );

    return "Admin/dashboard";
}

@GetMapping("/admin/documents")
public String documents(Model model) {

    List<Document> documents =
            documentService.getAllDocuments();

    model.addAttribute(
            "documents",
            documents
    );

    return "Admin/documents";
}

@GetMapping("/admin/contracts")
public String contracts(Model model) {

    model.addAttribute(
            "contracts",
            contractService.getAllContracts()
    );

    return "Admin/contracts";
}

@GetMapping("/admin/approvals")
public String approvals(Model model) {

    List<Approval> approvals =
            approvalService.getAllApprovals();

    model.addAttribute(
            "approvals",
            approvals
    );

    return "Admin/approvals";
}

@GetMapping("/admin/audit-logs")
public String auditLogs(Model model) {

    List<AuditLog> auditLogs =
            auditLogService.getAllAuditLogs();

    model.addAttribute(
            "auditLogs",
            auditLogs
    );

    return "Admin/audit-logs";
}

@GetMapping("/admin/contracts/add")
public String showAddContractPage(Model model) {

    model.addAttribute(
            "contract",
            new Contract()
    );

    return "Admin/contract-add";
}

@PostMapping("/admin/contracts/add")
public String addContract(
        @ModelAttribute("contract") Contract contract,
        RedirectAttributes redirectAttributes) {

    contractService.createContract(contract);

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Contract created successfully."
    );

    return "redirect:/admin/contracts";
}

@GetMapping("/admin/approvals/add")
public String showAddApprovalPage(Model model) {

    Approval approval =
            new Approval();

    approval.setStatus("PENDING");

    model.addAttribute(
            "approval",
            approval
    );

    model.addAttribute(
            "contracts",
            contractService.getAllContracts()
    );

    model.addAttribute(
            "modifications",
            modificationService.getAllModifications()
    );

    model.addAttribute(
            "approvers",
            userService.getAllUsers()
    );

    return "Admin/approval-add";
}

@PostMapping("/admin/approvals/add")
public String addApproval(
        @ModelAttribute("approval") Approval approval,
        RedirectAttributes redirectAttributes) {

    approval.setStatus("PENDING");
    approval.setApprovedAt(null);

    approvalService.createApproval(approval);

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Approval created successfully."
    );

    return "redirect:/admin/approvals";
}

@GetMapping("/admin/profile")
public String profile(
        Authentication authentication,
        Model model) {

    String username =
            authentication.getName();

    User user =
            userService.getUserByUsername(username);

    model.addAttribute(
            "user",
            user
    );

    return "Admin/profile";
}


@GetMapping("/admin/profile/edit")
public String editProfile(
Authentication authentication,
Model model) {

String username =
        authentication.getName();

User user =
        userService.getUserByUsername(username);

model.addAttribute(
        "user",
        user
);

return "Admin/profile-edit";


}

@PostMapping("/admin/profile/edit")
public String updateProfile(
Authentication authentication,
@RequestParam("fullName") String fullName,
@RequestParam("email") String email,
RedirectAttributes redirectAttributes) {


String username =
        authentication.getName();

try {

    User existingUser =
            userService.getUserByUsername(username);

    User updatedUser =
            new User();

    updatedUser.setFullName(fullName);
    updatedUser.setEmail(email);

    userService.updateUser(
            existingUser.getId(),
            updatedUser,
            existingUser.getRole().getName()
    );

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Profile updated successfully."
    );

} catch (RuntimeException e) {

    redirectAttributes.addFlashAttribute(
            "errorMessage",
            e.getMessage()
    );
}

return "redirect:/admin/profile";


}


@PostMapping("/admin/profile/upload-photo")
public String uploadProfilePhoto(
        Authentication authentication,
        @RequestParam("photo") MultipartFile photo,
        RedirectAttributes redirectAttributes) {

    String username =
            authentication.getName();

    try {

        userService.updateProfilePhoto(
                username,
                photo
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile photo updated successfully."
        );

    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                e.getMessage()
        );
    }

    return "redirect:/admin/profile";
}

@GetMapping("/admin/profile/photo/{id}")
@ResponseBody
public ResponseEntity<byte[]> getProfilePhoto(
        @PathVariable Long id) {

    User user =
            userService.getUserById(id);

    if (user == null
            || user.getProfilePhoto() == null
            || user.getProfilePhoto().length == 0) {

        return ResponseEntity
                .notFound()
                .build();
    }

    byte[] photo =
            user.getProfilePhoto();

    MediaType mediaType =
            MediaType.IMAGE_JPEG;

    if (photo.length >= 8
            && (photo[0] & 0xFF) == 0x89
            && (photo[1] & 0xFF) == 0x50
            && (photo[2] & 0xFF) == 0x4E
            && (photo[3] & 0xFF) == 0x47) {

        mediaType =
                MediaType.IMAGE_PNG;

    } else if (photo.length >= 3
            && (photo[0] & 0xFF) == 0xFF
            && (photo[1] & 0xFF) == 0xD8
            && (photo[2] & 0xFF) == 0xFF) {

        mediaType =
                MediaType.IMAGE_JPEG;

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
                MediaType.parseMediaType("image/webp");
    }

    return ResponseEntity
            .ok()
            .contentType(mediaType)
            .body(photo);
}

@GetMapping("/admin/change-password")
public String changePasswordPage(
        Authentication authentication,
        Model model) {

    String username =
            authentication.getName();

    User user =
            userService.getUserByUsername(username);

    model.addAttribute(
            "user",
            user
    );

    return "Admin/change-password";
}

@PostMapping("/admin/change-password")
public String changePassword(
        Authentication authentication,
        @RequestParam("currentPassword") String currentPassword,
        @RequestParam("newPassword") String newPassword,
        @RequestParam("confirmPassword") String confirmPassword,
        RedirectAttributes redirectAttributes) {

    String username =
            authentication.getName();

    try {

        userService.changePassword(
                username,
                currentPassword,
                newPassword,
                confirmPassword
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Password changed successfully."
        );

    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
        );
    }

    return "redirect:/admin/change-password";
}


}
