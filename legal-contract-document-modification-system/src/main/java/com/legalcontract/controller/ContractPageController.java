package com.legalcontract.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.User;
import com.legalcontract.service.ContractService;
import com.legalcontract.service.DocumentService;
import com.legalcontract.service.UserService;

@Controller
@RequestMapping("/contract")
public class ContractPageController {

    private final ContractService contractService;
    private final UserService userService;
    private final DocumentService documentService;
    private final PasswordEncoder passwordEncoder;

    public ContractPageController(
            ContractService contractService,
            UserService userService,
            DocumentService documentService,
            PasswordEncoder passwordEncoder) {

        this.contractService = contractService;
        this.userService = userService;
        this.documentService = documentService;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // CONTRACT DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        List<Contract> contracts =
                contractService.getAllContracts();

        LocalDate today = LocalDate.now();

        long contractCount =
                contracts.size();

        long draftCount =
                contracts.stream()
                        .filter(contract ->
                                hasStatus(contract, "DRAFT"))
                        .count();

        long activeCount =
                contracts.stream()
                        .filter(contract ->
                                hasStatus(contract, "ACTIVE"))
                        .count();

        long expiredCount =
                contracts.stream()
                        .filter(contract ->
                                hasStatus(contract, "EXPIRED"))
                        .count();

        long terminatedCount =
                contracts.stream()
                        .filter(contract ->
                                hasStatus(contract, "TERMINATED"))
                        .count();

        long expiringSoonCount =
                contracts.stream()
                        .filter(contract ->
                                isExpiringSoon(contract, today))
                        .count();

        List<Contract> attentionContracts =
                contracts.stream()
                        .filter(contract ->
                                requiresAttention(contract, today))
                        .sorted(
                                Comparator.comparing(
                                        Contract::getEndDate,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                        )
                        .limit(5)
                        .collect(Collectors.toList());

        List<Contract> upcomingContracts =
                contracts.stream()
                        .filter(contract ->
                                contract.getEndDate() != null)
                        .filter(contract -> {

                            long days =
                                    ChronoUnit.DAYS.between(
                                            today,
                                            contract.getEndDate()
                                    );

                            return days >= 0 && days <= 90;
                        })
                        .sorted(
                                Comparator.comparing(
                                        Contract::getEndDate
                                )
                        )
                        .limit(5)
                        .collect(Collectors.toList());

        List<Contract> recentContracts =
                contracts.stream()
                        .filter(contract ->
                                contract.getCreatedAt() != null)
                        .sorted(
                                Comparator.comparing(
                                        Contract::getCreatedAt
                                ).reversed()
                        )
                        .limit(5)
                        .collect(Collectors.toList());

        Map<String, Long> contractTypeCounts =
                new LinkedHashMap<>();

        contracts.forEach(contract -> {

            String type =
                    contract.getContractType();

            if (type == null
                    || type.trim().isEmpty()) {

                type = "Other";
            }

            type = type.trim();

            contractTypeCounts.put(
                    type,
                    contractTypeCounts.getOrDefault(
                            type,
                            0L
                    ) + 1
            );
        });

        List<String> typeLabels =
                new ArrayList<>(
                        contractTypeCounts.keySet()
                );

        List<Long> typeValues =
                new ArrayList<>(
                        contractTypeCounts.values()
                );

        List<String> activityLabels =
                new ArrayList<>();

        List<Long> activityValues =
                new ArrayList<>();

        YearMonth currentMonth =
                YearMonth.now();

        for (int i = 5; i >= 0; i--) {

            YearMonth month =
                    currentMonth.minusMonths(i);

            String monthName =
                    month.getMonth()
                            .toString()
                            .substring(0, 1)
                    +
                    month.getMonth()
                            .toString()
                            .substring(1)
                            .toLowerCase();

            activityLabels.add(
                    monthName + " " + month.getYear()
            );

            long count =
                    contracts.stream()
                            .filter(contract ->
                                    contract.getCreatedAt() != null)
                            .filter(contract ->
                                    YearMonth.from(
                                            contract.getCreatedAt()
                                    ).equals(month))
                            .count();

            activityValues.add(count);
        }

        model.addAttribute(
                "contractCount",
                contractCount
        );

        model.addAttribute(
                "draftCount",
                draftCount
        );

        model.addAttribute(
                "activeCount",
                activeCount
        );

        model.addAttribute(
                "expiredCount",
                expiredCount
        );

        model.addAttribute(
                "terminatedCount",
                terminatedCount
        );

        model.addAttribute(
                "expiringSoonCount",
                expiringSoonCount
        );

        model.addAttribute(
                "attentionContracts",
                attentionContracts
        );

        model.addAttribute(
                "upcomingContracts",
                upcomingContracts
        );

        model.addAttribute(
                "recentContracts",
                recentContracts
        );

        model.addAttribute(
                "contractTypeLabels",
                typeLabels
        );

        model.addAttribute(
                "contractTypeValues",
                typeValues
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
                "username",
                getCurrentUsername()
        );

        model.addAttribute(
                "today",
                today
        );

        model.addAttribute(
                "formattedToday",
                today.format(
                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy"
                        )
                )
        );

        return "Contract/dashboard";
    }

    // =========================================================
    // CONTRACTS
    // =========================================================

    @GetMapping("/contracts")
    public String contracts(Model model) {

        List<Contract> contracts =
                contractService.getAllContracts();

        Map<Long, List<Document>> documentsByContract =
                new LinkedHashMap<>();

        for (Contract contract : contracts) {

            if (contract.getId() != null) {

                List<Document> documents =
                        documentService.getDocumentsByContract(
                                contract.getId()
                        );

                documentsByContract.put(
                        contract.getId(),
                        documents
                );
            }
        }

        model.addAttribute(
                "contracts",
                contracts
        );

        model.addAttribute(
                "documentsByContract",
                documentsByContract
        );

        model.addAttribute(
                "username",
                getCurrentUsername()
        );

        return "Contract/contract";
    }

    // =========================================================
    // ADD CONTRACT
    // =========================================================

    @GetMapping("/contracts/add")
    public String addContract(Model model) {

        model.addAttribute(
                "contract",
                new Contract()
        );

        model.addAttribute(
                "username",
                getCurrentUsername()
        );

        return "Contract/contract-add";
    }

    // =========================================================
    // SAVE CONTRACT
    // =========================================================

    @PostMapping("/contracts/save")
    public String saveContract(
            Contract contract) {

        contract.setStatus(
                normalizeStatus(
                        contract.getStatus()
                )
        );

        contractService.createContract(
                contract
        );

        return "redirect:/contract/contracts";
    }

    // =========================================================
    // VIEW CONTRACT
    // =========================================================

    @GetMapping("/contracts/view/{id}")
    public String viewContract(
            @PathVariable Long id,
            Model model) {

        Contract contract =
                contractService.getContractById(id);

        model.addAttribute(
                "contract",
                contract
        );

        // -----------------------------------------------------
        // GET ALL DOCUMENTS FOR THIS CONTRACT
        // -----------------------------------------------------

        List<Document> allDocuments =
                documentService.getDocumentsByContract(id);

        // -----------------------------------------------------
        // SHOW ONLY ONE LATEST DOCUMENT
        // -----------------------------------------------------

        List<Document> documents =
                allDocuments.stream()
                        .sorted(
                                Comparator.comparing(
                                        Document::getUploadedAt,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                ).reversed()
                        )
                        .limit(1)
                        .collect(Collectors.toList());

        model.addAttribute(
                "documents",
                documents
        );

        model.addAttribute(
                "username",
                getCurrentUsername()
        );

        return "Contract/contract-view";
    }

    // =========================================================
    // EDIT CONTRACT
    // =========================================================

    @GetMapping("/contracts/edit/{id}")
    public String editContract(
            @PathVariable Long id,
            Model model) {

        Contract contract =
                contractService.getContractById(id);

        model.addAttribute(
                "contract",
                contract
        );

        model.addAttribute(
                "username",
                getCurrentUsername()
        );

        return "Contract/contract-edit";
    }

    // =========================================================
    // UPDATE CONTRACT
    // =========================================================

    @PostMapping("/contracts/update/{id}")
    public String updateContract(
            @PathVariable Long id,
            Contract contract) {

        contract.setStatus(
                normalizeStatus(
                        contract.getStatus()
                )
        );

        contractService.updateContract(
                id,
                contract
        );

        return "redirect:/contract/contracts";
    }

    // =========================================================
    // DELETE CONTRACT
    // =========================================================

    @PostMapping("/contracts/delete/{id}")
    public String deleteContract(
            @PathVariable Long id) {

        contractService.deleteContract(id);

        return "redirect:/contract/contracts";
    }

    // =========================================================
    // DOCUMENT UPLOAD PAGE
    // =========================================================

    @GetMapping("/contracts/{id}/documents/upload")
    public String uploadDocumentPage(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Contract contract =
                contractService.getContractById(id);

        if (contract == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Contract not found."
            );

            return "redirect:/contract/contracts";
        }

        List<Document> documents =
                documentService.getDocumentsByContract(id);

        model.addAttribute(
                "contract",
                contract
        );

        model.addAttribute(
                "username",
                getCurrentUsername()
        );

        // -----------------------------------------------------
        // CHECK IF ANY DOCUMENT ALREADY EXISTS
        // -----------------------------------------------------

        Document existingDocument =
                documents.stream()
                        .sorted(
                                Comparator.comparing(
                                        Document::getUploadedAt,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                ).reversed()
                        )
                        .findFirst()
                        .orElse(null);

        if (existingDocument != null) {

            model.addAttribute(
                    "documentAlreadyUploaded",
                    true
            );

            model.addAttribute(
                    "existingDocument",
                    existingDocument
            );

        } else {

            model.addAttribute(
                    "documentAlreadyUploaded",
                    false
            );
        }

        return "Contract/document-upload";
    }

    // =========================================================
    // DOCUMENT UPLOAD
    // =========================================================

    @PostMapping("/contracts/{id}/documents/upload")
    public String uploadDocument(
            @PathVariable Long id,
            @RequestParam("documentName") String documentName,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {

            // -------------------------------------------------
            // CHECK CONTRACT
            // -------------------------------------------------

            Contract contract =
                    contractService.getContractById(id);

            if (contract == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Contract not found."
                );

                return "redirect:/contract/contracts";
            }

            // -------------------------------------------------
            // CHECK IF ANY DOCUMENT ALREADY EXISTS
            // -------------------------------------------------

            List<Document> existingDocuments =
                    documentService.getDocumentsByContract(id);

            boolean documentAlreadyExists =
                    !existingDocuments.isEmpty();

            if (documentAlreadyExists) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "A PDF document is already uploaded for this contract. Please delete the existing document before uploading a new one."
                );

                return "redirect:/contract/contracts/"
                        + id
                        + "/documents/upload";
            }

            // -------------------------------------------------
            // CHECK FILE
            // -------------------------------------------------

            if (file == null || file.isEmpty()) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Please select a PDF document."
                );

                return "redirect:/contract/contracts/"
                        + id
                        + "/documents/upload";
            }

            // -------------------------------------------------
            // CHECK FILE NAME
            // -------------------------------------------------

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null
                    || originalFileName.trim().isEmpty()) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Invalid PDF document."
                );

                return "redirect:/contract/contracts/"
                        + id
                        + "/documents/upload";
            }

            // -------------------------------------------------
            // PDF ONLY
            // -------------------------------------------------

            String lowerFileName =
                    originalFileName.toLowerCase();

            if (!lowerFileName.endsWith(".pdf")) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Only PDF files are allowed."
                );

                return "redirect:/contract/contracts/"
                        + id
                        + "/documents/upload";
            }

            // -------------------------------------------------
            // DOCUMENT NAME
            // -------------------------------------------------

            if (documentName == null
                    || documentName.trim().isEmpty()) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Please enter a document name."
                );

                return "redirect:/contract/contracts/"
                        + id
                        + "/documents/upload";
            }

            // -------------------------------------------------
            // UPLOAD DOCUMENT
            // -------------------------------------------------

            documentService.uploadDocument(
                    id,
                    file,
                    documentName.trim()
            );

            // -------------------------------------------------
            // SUCCESS MESSAGE
            // -------------------------------------------------

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Document uploaded successfully."
            );

            return "redirect:/contract/contracts/view/"
                    + id;

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unable to upload document."
            );

            return "redirect:/contract/contracts/"
                    + id
                    + "/documents/upload";
        }
    }

    // =========================================================
    // PROFILE
    // =========================================================

    @GetMapping("/profile")
    public String profile(Model model) {

        String username =
                getCurrentUsername();

        User user =
                userService.findByUsername(username);

        model.addAttribute(
                "user",
                user
        );

        model.addAttribute(
                "username",
                username
        );

        return "Contract/profile";
    }

    
 // =========================================================
 // EDIT PROFILE
 // =========================================================

 @GetMapping("/profile/edit")
 public String editProfile(Model model) {

     String username =
             getCurrentUsername();

     User user =
             userService.findByUsername(username);

     model.addAttribute(
             "user",
             user
     );

     model.addAttribute(
             "username",
             username
     );

     return "Contract/profile-edit";
 }

 // =========================================================
 // UPDATE PROFILE
 // =========================================================

 @PostMapping("/profile/edit")
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

         if (existingUser == null) {

             redirectAttributes.addFlashAttribute(
                     "errorMessage",
                     "User not found."
             );

             return "redirect:/contract/profile/edit";
         }

         if (fullName == null
                 || fullName.trim().isEmpty()) {

             redirectAttributes.addFlashAttribute(
                     "errorMessage",
                     "Full name is required."
             );

             return "redirect:/contract/profile/edit";
         }

         if (email == null
                 || email.trim().isEmpty()) {

             redirectAttributes.addFlashAttribute(
                     "errorMessage",
                     "Email is required."
             );

             return "redirect:/contract/profile/edit";
         }

         User updatedUser =
                 new User();

         updatedUser.setFullName(
                 fullName.trim()
         );

         updatedUser.setEmail(
                 email.trim()
         );

         userService.updateUser(
                 existingUser.getId(),
                 updatedUser,
                 existingUser.getRole().getName()
         );

         redirectAttributes.addFlashAttribute(
                 "successMessage",
                 "Profile updated successfully."
         );

         return "redirect:/contract/profile";

     } catch (RuntimeException e) {

         redirectAttributes.addFlashAttribute(
                 "errorMessage",
                 e.getMessage() != null
                         ? e.getMessage()
                         : "Unable to update profile."
         );

         return "redirect:/contract/profile/edit";
     }
 }
 
 
    // =========================================================
    // PROFILE PHOTO UPLOAD
    // =========================================================

    @PostMapping("/profile/upload-photo")
    public String uploadProfilePhoto(
            @RequestParam("photo") MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        if (photo == null || photo.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a photo."
            );

            return "redirect:/contract/profile";
        }

        String contentType =
                photo.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a valid image file."
            );

            return "redirect:/contract/profile";
        }

        long maxSize =
                5L * 1024L * 1024L;

        if (photo.getSize() > maxSize) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Photo size must be less than 5 MB."
            );

            return "redirect:/contract/profile";
        }

        try {

            String username =
                    getCurrentUsername();

            userService.updateProfilePhoto(
                    username,
                    photo
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Profile photo uploaded successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to upload profile photo."
            );
        }

        return "redirect:/contract/profile";
    }

    // =========================================================
    // PROFILE PHOTO
    // =========================================================

    @GetMapping("/profile/photo")
    @ResponseBody
    public ResponseEntity<byte[]> getProfilePhoto() {

        String username =
                getCurrentUsername();

        User user =
                userService.findByUsername(username);

        if (user == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (user.getProfilePhoto() == null
                || user.getProfilePhoto().length == 0) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(user.getProfilePhoto());
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    @GetMapping("/settings")
    public String settings(Model model) {

        model.addAttribute(
                "username",
                getCurrentUsername()
        );

        return "Contract/settings";
    }

    // =========================================================
    // CONTRACT STATUS HELPERS
    // =========================================================

    private boolean hasStatus(
            Contract contract,
            String status) {

        if (contract == null
                || contract.getStatus() == null) {

            return false;
        }

        return normalizeStatus(
                contract.getStatus()
        ).equals(status);
    }

    private boolean isExpiringSoon(
            Contract contract,
            LocalDate today) {

        if (contract == null
                || contract.getEndDate() == null) {

            return false;
        }

        if (hasStatus(
                contract,
                "TERMINATED")
                || hasStatus(
                        contract,
                        "EXPIRED")) {

            return false;
        }

        long days =
                ChronoUnit.DAYS.between(
                        today,
                        contract.getEndDate()
                );

        return days >= 0 && days <= 30;
    }

    private boolean requiresAttention(
            Contract contract,
            LocalDate today) {

        if (contract == null) {
            return false;
        }

        if (hasStatus(
                contract,
                "DRAFT")) {

            return true;
        }

        if (contract.getEndDate() != null
                && !hasStatus(
                        contract,
                        "TERMINATED")
                && !hasStatus(
                        contract,
                        "EXPIRED")) {

            long days =
                    ChronoUnit.DAYS.between(
                            today,
                            contract.getEndDate()
                    );

            return days >= 0 && days <= 30;
        }

        return false;
    }

    private String normalizeStatus(
            String status) {

        if (status == null
                || status.trim().isEmpty()) {

            return "DRAFT";
        }

        String normalized =
                status.trim().toUpperCase();

        switch (normalized) {

            case "ACTIVE":
                return "ACTIVE";

            case "DRAFT":
                return "DRAFT";

            case "EXPIRED":
                return "EXPIRED";

            case "TERMINATED":
                return "TERMINATED";

            default:
                return normalized;
        }
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getPrincipal())) {

            return "Contract Manager";
        }

        return authentication.getName();
    }

    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @GetMapping("/change-password")
    public String changePasswordPage() {

        return "Contract/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String username =
                authentication.getName();

        User user =
                userService.getUserByUsername(username);

        if (user == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "User not found."
            );

            return "redirect:/contract/change-password";
        }

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Current password is incorrect."
            );

            return "redirect:/contract/change-password";
        }

        if (newPassword.length() < 8) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password must contain at least 8 characters."
            );

            return "redirect:/contract/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password and confirm password do not match."
            );

            return "redirect:/contract/change-password";
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password must be different from your current password."
            );

            return "redirect:/contract/change-password";
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userService.saveUser(user);

        redirectAttributes.addFlashAttribute(
                "success",
                "Password changed successfully."
        );

        return "redirect:/contract/profile";
    }
}