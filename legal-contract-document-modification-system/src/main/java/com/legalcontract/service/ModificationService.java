package com.legalcontract.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.legalcontract.entity.Approval;
import com.legalcontract.entity.Clause;
import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.Modification;
import com.legalcontract.entity.User;
import com.legalcontract.repository.ApprovalRepository;
import com.legalcontract.repository.ClauseRepository;
import com.legalcontract.repository.ContractRepository;
import com.legalcontract.repository.DocumentRepository;
import com.legalcontract.repository.ModificationRepository;
import com.legalcontract.repository.UserRepository;

@Service
@Transactional
public class ModificationService {

    @Autowired
    private ModificationRepository modificationRepo;

    @Autowired
    private ContractRepository contractRepo;

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private ClauseRepository clauseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ApprovalRepository approvalRepo;

    // =========================================================
    // CREATE MODIFICATION
    // =========================================================

    public Modification createModification(
            Modification modification) {

        if (modification == null) {
            throw new RuntimeException(
                    "Modification data cannot be null"
            );
        }

        // -----------------------------------------------------
        // CONTRACT
        // -----------------------------------------------------

        if (modification.getContract() == null
                || modification.getContract().getId() == null) {

            throw new RuntimeException(
                    "Contract is required"
            );
        }

        Long contractId =
                modification.getContract().getId();

        Contract contract =
                contractRepo.findById(contractId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Contract not found with id: "
                                                + contractId
                                )
                        );

        modification.setContract(contract);

        // -----------------------------------------------------
        // DOCUMENT
        // -----------------------------------------------------

        if (modification.getDocument() != null
                && modification.getDocument().getId() != null) {

            Long documentId =
                    modification.getDocument().getId();

            Document document =
                    documentRepo.findById(documentId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Document not found with id: "
                                                    + documentId
                                    )
                            );

            if (document.getContract() == null
                    || document.getContract().getId() == null
                    || !document.getContract().getId()
                            .equals(contractId)) {

                throw new RuntimeException(
                        "Selected document does not belong to the selected contract"
                );
            }

            modification.setDocument(document);
        }

        // -----------------------------------------------------
        // CLAUSE
        // -----------------------------------------------------

        if (modification.getClause() != null
                && modification.getClause().getId() != null) {

            Long clauseId =
                    modification.getClause().getId();

            Clause clause =
                    clauseRepo.findById(clauseId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Clause not found with id: "
                                                    + clauseId
                                    )
                            );

            if (clause.getContract() == null
                    || clause.getContract().getId() == null
                    || !clause.getContract().getId()
                            .equals(contractId)) {

                throw new RuntimeException(
                        "Selected clause does not belong to the selected contract"
                );
            }

            modification.setClause(clause);
        }

        // -----------------------------------------------------
        // MODIFICATION TYPE
        // -----------------------------------------------------

        if (modification.getModificationType() == null
                || modification.getModificationType().isBlank()) {

            if (modification.getClause() != null) {
                modification.setModificationType("CLAUSE");
            } else {
                modification.setModificationType("CONTRACT");
            }

        } else {

            modification.setModificationType(
                    modification.getModificationType()
                            .trim()
                            .toUpperCase()
            );
        }

        if (!"CONTRACT".equals(
                modification.getModificationType())
                && !"CLAUSE".equals(
                modification.getModificationType())) {

            throw new RuntimeException(
                    "Invalid modification type"
            );
        }

        // -----------------------------------------------------
        // CLAUSE / CONTRACT TYPE VALIDATION
        // -----------------------------------------------------

        if ("CLAUSE".equals(
                modification.getModificationType())) {

            if (modification.getClause() == null) {

                throw new RuntimeException(
                        "Clause is required for a clause modification"
                );
            }

        } else {

            modification.setClause(null);
        }

        // -----------------------------------------------------
        // MODIFIED BY USER
        // -----------------------------------------------------

        if (modification.getModifiedBy() != null
                && modification.getModifiedBy().getId() != null) {

            Long userId =
                    modification.getModifiedBy().getId();

            User user =
                    userRepo.findById(userId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found with id: "
                                                    + userId
                                    )
                            );

            modification.setModifiedBy(user);

        } else {

            // -------------------------------------------------
            // GET CURRENTLY LOGGED-IN USER
            // -------------------------------------------------

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getName() != null
                    && !"anonymousUser".equalsIgnoreCase(
                    authentication.getName())) {

                String username =
                        authentication.getName().trim();

                User currentUser =
                        userRepo.findByUsername(username)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Logged-in user not found: "
                                                        + username
                                        )
                                );

                modification.setModifiedBy(currentUser);
            }
        }

        // -----------------------------------------------------
        // DEFAULT STATUS
        // -----------------------------------------------------

        if (modification.getStatus() == null
                || modification.getStatus().isBlank()) {

            modification.setStatus("PENDING");

        } else {

            modification.setStatus(
                    modification.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }

        // -----------------------------------------------------
        // MODIFIED AT
        // -----------------------------------------------------

        if (modification.getModifiedAt() == null) {

            modification.setModifiedAt(
                    LocalDateTime.now()
            );
        }

        // -----------------------------------------------------
        // SAVE MODIFICATION
        // -----------------------------------------------------

        Modification savedModification =
                modificationRepo.save(modification);

        // -----------------------------------------------------
        // AUTOMATIC MANAGER APPROVAL
        // -----------------------------------------------------

        if ("PENDING".equalsIgnoreCase(
                savedModification.getStatus())) {

            User manager =
                    userRepo.findByUsername("Manager")
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Manager user not found"
                                    )
                            );

            // -------------------------------------------------
            // PREVENT DUPLICATE PENDING APPROVAL
            // -------------------------------------------------

            List<Approval> existingApprovals =
                    approvalRepo.findByModificationId(
                            savedModification.getId()
                    );

            boolean pendingApprovalExists =
                    existingApprovals.stream()
                            .anyMatch(approval ->
                                    approval.getApprover() != null
                                            && approval.getApprover().getId() != null
                                            && approval.getApprover().getId()
                                            .equals(manager.getId())
                                            && "PENDING".equalsIgnoreCase(
                                            approval.getStatus()
                                    )
                            );

            // -------------------------------------------------
            // CREATE APPROVAL
            // -------------------------------------------------

            if (!pendingApprovalExists) {

                Approval approval =
                        new Approval();

                approval.setContract(
                        savedModification.getContract()
                );

                approval.setModification(
                        savedModification
                );

                approval.setApprover(
                        manager
                );

                approval.setStatus(
                        "PENDING"
                );

                approval.setComments(
                        null
                );

                approval.setApprovedAt(
                        null
                );

                approvalRepo.save(approval);
            }
        }

        return savedModification;
    }

    // =========================================================
    // GET ALL MODIFICATIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Modification> getAllModifications() {

        return modificationRepo.findAll();
    }

    // =========================================================
    // GET MODIFICATION BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Modification getModificationById(Long id) {

        if (id == null) {

            throw new RuntimeException(
                    "Modification ID cannot be null"
            );
        }

        return modificationRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Modification not found with id: "
                                        + id
                        )
                );
    }

    // =========================================================
    // UPDATE MODIFICATION
    // =========================================================

    public Modification updateModification(
            Long id,
            Modification modification) {

        if (modification == null) {

            throw new RuntimeException(
                    "Modification data cannot be null"
            );
        }

        Modification existingModification =
                getModificationById(id);

        // -----------------------------------------------------
        // CONTRACT
        // -----------------------------------------------------

        if (modification.getContract() == null
                || modification.getContract().getId() == null) {

            throw new RuntimeException(
                    "Contract is required"
            );
        }

        Long contractId =
                modification.getContract().getId();

        Contract contract =
                contractRepo.findById(contractId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Contract not found with id: "
                                                + contractId
                                )
                        );

        existingModification.setContract(contract);

        // -----------------------------------------------------
        // DOCUMENT
        // -----------------------------------------------------

        if (modification.getDocument() != null
                && modification.getDocument().getId() != null) {

            Long documentId =
                    modification.getDocument().getId();

            Document document =
                    documentRepo.findById(documentId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Document not found with id: "
                                                    + documentId
                                    )
                            );

            if (document.getContract() == null
                    || document.getContract().getId() == null
                    || !document.getContract().getId()
                            .equals(contractId)) {

                throw new RuntimeException(
                        "Selected document does not belong to the selected contract"
                );
            }

            existingModification.setDocument(document);

        } else {

            existingModification.setDocument(null);
        }

        // -----------------------------------------------------
        // CLAUSE
        // -----------------------------------------------------

        if (modification.getClause() != null
                && modification.getClause().getId() != null) {

            Long clauseId =
                    modification.getClause().getId();

            Clause clause =
                    clauseRepo.findById(clauseId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Clause not found with id: "
                                                    + clauseId
                                    )
                            );

            if (clause.getContract() == null
                    || clause.getContract().getId() == null
                    || !clause.getContract().getId()
                            .equals(contractId)) {

                throw new RuntimeException(
                        "Selected clause does not belong to the selected contract"
                );
            }

            existingModification.setClause(clause);

        } else {

            existingModification.setClause(null);
        }

        // -----------------------------------------------------
        // MODIFICATION TYPE
        // -----------------------------------------------------

        String modificationType =
                modification.getModificationType();

        if (modificationType == null
                || modificationType.isBlank()) {

            if (modification.getClause() != null) {

                modificationType = "CLAUSE";

            } else {

                modificationType = "CONTRACT";
            }
        }

        modificationType =
                modificationType.trim().toUpperCase();

        if (!"CONTRACT".equals(modificationType)
                && !"CLAUSE".equals(modificationType)) {

            throw new RuntimeException(
                    "Invalid modification type"
            );
        }

        if ("CLAUSE".equals(modificationType)
                && existingModification.getClause() == null) {

            throw new RuntimeException(
                    "Clause is required for a clause modification"
            );
        }

        if ("CONTRACT".equals(modificationType)) {

            existingModification.setClause(null);
        }

        existingModification.setModificationType(
                modificationType
        );

        // -----------------------------------------------------
        // MODIFIED BY
        // -----------------------------------------------------

        if (modification.getModifiedBy() != null
                && modification.getModifiedBy().getId() != null) {

            Long userId =
                    modification.getModifiedBy().getId();

            User user =
                    userRepo.findById(userId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found with id: "
                                                    + userId
                                    )
                            );

            existingModification.setModifiedBy(user);
        }

        // -----------------------------------------------------
        // OLD CONTENT
        // -----------------------------------------------------

        if (modification.getOldContent() != null) {

            existingModification.setOldContent(
                    modification.getOldContent()
            );
        }

        // -----------------------------------------------------
        // NEW CONTENT
        // -----------------------------------------------------

        if (modification.getNewContent() != null) {

            existingModification.setNewContent(
                    modification.getNewContent()
            );
        }

        // -----------------------------------------------------
        // REASON
        // -----------------------------------------------------

        if (modification.getReason() != null) {

            existingModification.setReason(
                    modification.getReason()
            );
        }

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        if (modification.getStatus() != null
                && !modification.getStatus().isBlank()) {

            existingModification.setStatus(
                    modification.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }

        // -----------------------------------------------------
        // MODIFIED AT
        // -----------------------------------------------------

        existingModification.setModifiedAt(
                LocalDateTime.now()
        );

        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        return modificationRepo.save(
                existingModification
        );
    }

    // =========================================================
    // DELETE MODIFICATION
    // =========================================================

    public void deleteModification(Long id) {

        Modification modification =
                getModificationById(id);

        modificationRepo.delete(modification);
    }

    // =========================================================
    // GET MODIFICATIONS BY STATUS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Modification> getModificationsByStatus(
            String status) {

        if (status == null || status.isBlank()) {

            throw new RuntimeException(
                    "Status cannot be empty"
            );
        }

        return modificationRepo.findByStatus(
                status.trim().toUpperCase()
        );
    }

    // =========================================================
    // GET MODIFICATIONS BY CONTRACT
    // =========================================================

    @Transactional(readOnly = true)
    public List<Modification> getModificationsByContract(
            Long contractId) {

        if (contractId == null) {

            throw new RuntimeException(
                    "Contract ID cannot be null"
            );
        }

        return modificationRepo.findByContractId(
                contractId
        );
    }

    // =========================================================
    // GET MODIFICATIONS BY DOCUMENT
    // =========================================================

    @Transactional(readOnly = true)
    public List<Modification> getModificationsByDocument(
            Long documentId) {

        if (documentId == null) {

            throw new RuntimeException(
                    "Document ID cannot be null"
            );
        }

        return modificationRepo.findByDocumentId(
                documentId
        );
    }

    // =========================================================
    // GET MODIFICATIONS BY USER
    // =========================================================

    @Transactional(readOnly = true)
    public List<Modification> getModificationsByUser(
            Long userId) {

        if (userId == null) {

            throw new RuntimeException(
                    "User ID cannot be null"
            );
        }

        return modificationRepo.findByModifiedById(
                userId
        );
    }
}