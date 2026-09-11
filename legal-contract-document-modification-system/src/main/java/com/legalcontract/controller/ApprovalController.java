package com.legalcontract.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legalcontract.entity.Approval;
import com.legalcontract.service.ApprovalService;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(
            ApprovalService approvalService) {

        this.approvalService = approvalService;
    }

    // =========================================================
    // CREATE APPROVAL
    // =========================================================

    @PostMapping
    public ResponseEntity<Approval> createApproval(
            @RequestBody Approval approval) {

        Approval savedApproval =
                approvalService.createApproval(approval);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedApproval);
    }

    // =========================================================
    // GET ALL APPROVALS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Approval>> getAllApprovals() {

        return ResponseEntity.ok(
                approvalService.getAllApprovals()
        );
    }

    // =========================================================
    // GET APPROVAL BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Approval> getApprovalById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                approvalService.getApprovalById(id)
        );
    }

    // =========================================================
    // UPDATE APPROVAL
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<Approval> updateApproval(
            @PathVariable Long id,
            @RequestBody Approval approval) {

        return ResponseEntity.ok(
                approvalService.updateApproval(
                        id,
                        approval
                )
        );
    }

    // =========================================================
    // DELETE APPROVAL
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteApproval(
            @PathVariable Long id) {

        approvalService.deleteApproval(id);

        return ResponseEntity.ok(
                "Approval deleted successfully"
        );
    }

    // =========================================================
    // GET APPROVALS BY STATUS
    // =========================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Approval>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                approvalService.getApprovalsByStatus(status)
        );
    }

    // =========================================================
    // GET APPROVALS BY CONTRACT
    // =========================================================

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<Approval>> getByContract(
            @PathVariable Long contractId) {

        return ResponseEntity.ok(
                approvalService.getApprovalsByContract(
                        contractId
                )
        );
    }

    // =========================================================
    // GET APPROVALS BY MODIFICATION
    // =========================================================

    @GetMapping("/modification/{modificationId}")
    public ResponseEntity<List<Approval>> getByModification(
            @PathVariable Long modificationId) {

        return ResponseEntity.ok(
                approvalService.getApprovalsByModification(
                        modificationId
                )
        );
    }

    // =========================================================
    // GET APPROVALS BY APPROVER
    // =========================================================

    @GetMapping("/approver/{approverId}")
    public ResponseEntity<List<Approval>> getByApprover(
            @PathVariable Long approverId) {

        return ResponseEntity.ok(
                approvalService.getApprovalsByApprover(
                        approverId
                )
        );
    }
}