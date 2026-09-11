package com.legalcontract.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Approval;
import com.legalcontract.repository.ApprovalRepository;
import com.legalcontract.service.ApprovalService;

@Controller
@RequestMapping("/approval")
public class ApprovalPageController {

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        List<Approval> approvals = approvalRepository.findAll();

        long totalApprovals = approvals.size();

        long pendingCount = approvals.stream()
                .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()))
                .count();

        long approvedCount = approvals.stream()
                .filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus()))
                .count();

        long rejectedCount = approvals.stream()
                .filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus()))
                .count();

        String currentUsername = getCurrentUsername();

        long myApprovalCount = 0;

        if (currentUsername != null) {
            myApprovalCount = approvals.stream()
                    .filter(a -> a.getApprover() != null)
                    .filter(a -> a.getApprover().getUsername() != null)
                    .filter(a -> a.getApprover().getUsername()
                            .equalsIgnoreCase(currentUsername))
                    .count();
        }

        List<Approval> pendingApprovals = approvals.stream()
                .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()))
                .sorted(
                        Comparator.comparing(
                                this::getCreatedAtSafely,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .limit(5)
                .collect(Collectors.toList());

        List<String> activityLabels = new ArrayList<>();

        List<Integer> activityValues = new ArrayList<>();

        List<Integer> pendingActivityValues = new ArrayList<>();

        List<Integer> approvedActivityValues = new ArrayList<>();

        List<Integer> rejectedActivityValues = new ArrayList<>();

        String[] months = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        for (int month = 1; month <= 12; month++) {

            activityLabels.add(months[month - 1]);

            int total = 0;
            int pending = 0;
            int approved = 0;
            int rejected = 0;

            for (Approval approval : approvals) {

                LocalDateTime createdAt =
                        getCreatedAtSafely(approval);

                if (createdAt != null
                        && createdAt.getMonthValue() == month) {

                    total++;

                    if ("PENDING".equalsIgnoreCase(
                            approval.getStatus())) {
                        pending++;
                    }

                    if ("APPROVED".equalsIgnoreCase(
                            approval.getStatus())) {
                        approved++;
                    }

                    if ("REJECTED".equalsIgnoreCase(
                            approval.getStatus())) {
                        rejected++;
                    }
                }
            }

            activityValues.add(total);
            pendingActivityValues.add(pending);
            approvedActivityValues.add(approved);
            rejectedActivityValues.add(rejected);
        }

        List<String> approvalStatusLabels =
                new ArrayList<>();

        approvalStatusLabels.add("Pending");
        approvalStatusLabels.add("Approved");
        approvalStatusLabels.add("Rejected");

        List<Integer> approvalStatusValues =
                new ArrayList<>();

        approvalStatusValues.add((int) pendingCount);
        approvalStatusValues.add((int) approvedCount);
        approvalStatusValues.add((int) rejectedCount);

        model.addAttribute(
                "totalApprovals",
                totalApprovals
        );

        model.addAttribute(
                "pendingCount",
                pendingCount
        );

        model.addAttribute(
                "approvedCount",
                approvedCount
        );

        model.addAttribute(
                "rejectedCount",
                rejectedCount
        );

        model.addAttribute(
                "myApprovalCount",
                myApprovalCount
        );

        model.addAttribute(
                "pendingApprovals",
                pendingApprovals
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
                "pendingActivityValues",
                pendingActivityValues
        );

        model.addAttribute(
                "approvedActivityValues",
                approvedActivityValues
        );

        model.addAttribute(
                "rejectedActivityValues",
                rejectedActivityValues
        );

        model.addAttribute(
                "approvalStatusLabels",
                approvalStatusLabels
        );

        model.addAttribute(
                "approvalStatusValues",
                approvalStatusValues
        );

        model.addAttribute(
                "username",
                currentUsername != null
                        ? currentUsername
                        : "Contract Manager"
        );

        model.addAttribute(
                "formattedToday",
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd MMMM yyyy"
                        )
                )
        );

        return "Approval/dashboard";
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {

        List<Approval> approvals =
                approvalRepository
                        .findAllWithModificationAndUser();

        approvals.sort(
                Comparator.comparing(
                        this::getCreatedAtSafely,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        model.addAttribute(
                "approvals",
                approvals
        );

        model.addAttribute(
                "requestedByNames",
                buildRequestedByNames(approvals)
        );

        return "Approval/approval-list";
    }

    @GetMapping("/pending")
    public String pendingApprovals(Model model) {

        List<Approval> approvals =
                approvalRepository
                        .findByStatusWithModificationAndUser(
                                "PENDING"
                        );

        approvals.sort(
                Comparator.comparing(
                        this::getCreatedAtSafely,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        model.addAttribute(
                "approvals",
                approvals
        );

        model.addAttribute(
                "requestedByNames",
                buildRequestedByNames(approvals)
        );

        model.addAttribute(
                "pageTitle",
                "Pending Approvals"
        );

        return "Approval/approval-list";
    }

    @GetMapping("/approved")
    public String approvedApprovals(Model model) {

        List<Approval> approvals =
                approvalRepository
                        .findByStatusWithModificationAndUser(
                                "APPROVED"
                        );

        approvals.sort(
                Comparator.comparing(
                        this::getCreatedAtSafely,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        model.addAttribute(
                "approvals",
                approvals
        );

        model.addAttribute(
                "requestedByNames",
                buildRequestedByNames(approvals)
        );

        model.addAttribute(
                "pageTitle",
                "Approved Approvals"
        );

        return "Approval/approval-list";
    }

    @GetMapping("/rejected")
    public String rejectedApprovals(Model model) {

        List<Approval> approvals =
                approvalRepository
                        .findByStatusWithModificationAndUser(
                                "REJECTED"
                        );

        approvals.sort(
                Comparator.comparing(
                        this::getCreatedAtSafely,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        model.addAttribute(
                "approvals",
                approvals
        );

        model.addAttribute(
                "requestedByNames",
                buildRequestedByNames(approvals)
        );

        model.addAttribute(
                "pageTitle",
                "Rejected Approvals"
        );

        return "Approval/approval-list";
    }

    @GetMapping("/my-approvals")
    public String myApprovals(Model model) {

        String currentUsername =
                getCurrentUsername();

        List<Approval> approvals =
                approvalRepository
                        .findAllWithModificationAndUser();

        if (currentUsername != null) {

            approvals = approvals.stream()
                    .filter(
                            approval ->
                                    approval.getApprover() != null
                                    && approval.getApprover()
                                            .getUsername() != null
                                    && approval.getApprover()
                                            .getUsername()
                                            .equalsIgnoreCase(
                                                    currentUsername
                                            )
                    )
                    .collect(Collectors.toList());
        }

        approvals.sort(
                Comparator.comparing(
                        this::getCreatedAtSafely,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        model.addAttribute(
                "approvals",
                approvals
        );

        model.addAttribute(
                "requestedByNames",
                buildRequestedByNames(approvals)
        );

        model.addAttribute(
                "pageTitle",
                "My Approval Requests"
        );

        return "Approval/approval-list";
    }

    @GetMapping("/view/{id}")
    public String viewApproval(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Approval approval =
                approvalRepository
                        .findById(id)
                        .orElse(null);

        if (approval == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Approval request not found."
            );

            return "redirect:/approval/approvals";
        }

        model.addAttribute(
                "approval",
                approval
        );

        return "Approval/approval-view";
    }

    @GetMapping("/edit/{id}")
    public String editApproval(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Approval approval =
                approvalRepository
                        .findById(id)
                        .orElse(null);

        if (approval == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Approval request not found."
            );

            return "redirect:/approval/approvals";
        }

        model.addAttribute(
                "approval",
                approval
        );

        return "Approval/approval-edit";
    }

    @PostMapping("/update/{id}")
    public String updateApproval(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(
                    value = "comments",
                    required = false
            ) String comments,
            RedirectAttributes redirectAttributes) {

        try {

            if (status == null || status.isBlank()) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Approval status is required."
                );

                return "redirect:/approval/approvals";
            }

            String normalizedStatus =
                    status.trim().toUpperCase();

            if (!"APPROVED".equals(normalizedStatus)
                    && !"REJECTED".equals(normalizedStatus)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Invalid approval status."
                );

                return "redirect:/approval/approvals";
            }

            Approval approval =
                    approvalService.getApprovalById(id);

            if (!"PENDING".equalsIgnoreCase(
                    approval.getStatus())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Only PENDING approvals can be updated."
                );

                return "redirect:/approval/approvals";
            }

            if ("APPROVED".equals(normalizedStatus)) {

                approvalService.approveApproval(
                        id,
                        comments
                );

                redirectAttributes.addFlashAttribute(
                        "success",
                        "Modification request approved successfully."
                );

            } else {

                approvalService.rejectApproval(
                        id,
                        comments
                );

                redirectAttributes.addFlashAttribute(
                        "success",
                        "Modification request rejected successfully."
                );
            }

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to update approval request: "
                            + e.getMessage()
            );
        }

        return "redirect:/approval/approvals";
    }

    @PostMapping("/delete/{id}")
    public String deleteApproval(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            if (!approvalRepository.existsById(id)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Approval request not found."
                );

                return "redirect:/approval/approvals";
            }

            approvalRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Approval request deleted successfully."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to delete approval request."
            );

            e.printStackTrace();
        }

        return "redirect:/approval/approvals";
    }

    private Map<Long, String> buildRequestedByNames(
            List<Approval> approvals) {

        Map<Long, String> requestedByNames =
                new HashMap<>();

        if (approvals == null) {
            return requestedByNames;
        }

        for (Approval approval : approvals) {

            if (approval == null
                    || approval.getId() == null) {
                continue;
            }

            String requestedBy = "N/A";

            if (approval.getModification() != null
                    && approval.getModification()
                            .getModifiedBy() != null) {

                String fullName =
                        approval.getModification()
                                .getModifiedBy()
                                .getFullName();

                String username =
                        approval.getModification()
                                .getModifiedBy()
                                .getUsername();

                if (fullName != null
                        && !fullName.trim().isEmpty()) {

                    requestedBy = fullName.trim();

                } else if (username != null
                        && !username.trim().isEmpty()) {

                    requestedBy = username.trim();
                }
            }

            requestedByNames.put(
                    approval.getId(),
                    requestedBy
            );
        }

        return requestedByNames;
    }

    private LocalDateTime getCreatedAtSafely(
            Approval approval) {

        if (approval == null) {
            return null;
        }

        return approval.getCreatedAt();
    }

    private String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return null;
        }

        if (!authentication.isAuthenticated()) {
            return null;
        }

        String username =
                authentication.getName();

        if (username == null
                || username.trim().isEmpty()
                || "anonymousUser".equalsIgnoreCase(username)) {

            return null;
        }

        return username;
    }
}