package com.legalcontract.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.legalcontract.entity.Approval;
import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.Modification;
import com.legalcontract.entity.User;
import com.legalcontract.entity.Version;
import com.legalcontract.repository.ApprovalRepository;
import com.legalcontract.repository.ContractRepository;
import com.legalcontract.repository.DocumentRepository;
import com.legalcontract.repository.ModificationRepository;
import com.legalcontract.repository.UserRepository;
import com.legalcontract.repository.VersionRepository;

@Service
@Transactional
public class ApprovalService {


@Autowired
private ApprovalRepository approvalRepository;

@Autowired
private ContractRepository contractRepository;

@Autowired
private ModificationRepository modificationRepository;

@Autowired
private UserRepository userRepository;

@Autowired
private DocumentRepository documentRepository;

@Autowired
private VersionRepository versionRepository;

public Approval createApproval(Approval approval) {

    if (approval == null) {
        throw new RuntimeException("Approval data cannot be null");
    }

    if (approval.getContract() != null
            && approval.getContract().getId() != null) {

        Long contractId = approval.getContract().getId();

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Contract not found with id: " + contractId
                        ));

        approval.setContract(contract);
    }

    if (approval.getModification() != null
            && approval.getModification().getId() != null) {

        Long modificationId = approval.getModification().getId();

        Modification modification =
                modificationRepository.findById(modificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Modification not found with id: "
                                                + modificationId
                                ));

        approval.setModification(modification);
    }

    if (approval.getApprover() != null
            && approval.getApprover().getId() != null) {

        Long approverId = approval.getApprover().getId();

        User approver = userRepository.findById(approverId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Approver not found with id: "
                                        + approverId
                        ));

        approval.setApprover(approver);
    }

    approval.setStatus("PENDING");
    approval.setApprovedAt(null);

    return approvalRepository.save(approval);
}

@Transactional(readOnly = true)
public List<Approval> getAllApprovals() {

    return approvalRepository.findAll();
}

@Transactional(readOnly = true)
public Approval getApprovalById(Long id) {

    if (id == null) {
        throw new RuntimeException("Approval ID cannot be null");
    }

    return approvalRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Approval not found with id: " + id
                    ));
}

public Approval updateApproval(
        Long id,
        Approval updatedApproval) {

    if (updatedApproval == null) {
        throw new RuntimeException(
                "Approval data cannot be null"
        );
    }

    Approval existingApproval =
            getApprovalById(id);

    if (updatedApproval.getContract() != null
            && updatedApproval.getContract().getId() != null) {

        Long contractId =
                updatedApproval.getContract().getId();

        Contract contract =
                contractRepository.findById(contractId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Contract not found with id: "
                                                + contractId
                                ));

        existingApproval.setContract(contract);
    }

    if (updatedApproval.getModification() != null
            && updatedApproval.getModification().getId() != null) {

        Long modificationId =
                updatedApproval.getModification().getId();

        Modification modification =
                modificationRepository.findById(modificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Modification not found with id: "
                                                + modificationId
                                ));

        existingApproval.setModification(modification);
    }

    if (updatedApproval.getApprover() != null
            && updatedApproval.getApprover().getId() != null) {

        Long approverId =
                updatedApproval.getApprover().getId();

        User approver =
                userRepository.findById(approverId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Approver not found with id: "
                                                + approverId
                                ));

        existingApproval.setApprover(approver);
    }

    if (updatedApproval.getStatus() != null
            && !updatedApproval.getStatus().isBlank()) {

        String newStatus =
                updatedApproval.getStatus()
                        .trim()
                        .toUpperCase();

        existingApproval.setStatus(newStatus);

        if ("APPROVED".equals(newStatus)) {

            if (existingApproval.getApprovedAt() == null) {
                existingApproval.setApprovedAt(
                        LocalDateTime.now()
                );
            }

        } else {

            existingApproval.setApprovedAt(null);
        }

        synchronizeModificationStatus(
                existingApproval,
                newStatus
        );

        if ("APPROVED".equals(newStatus)) {

            createVersionForApprovedModification(
                    existingApproval
            );
        }
    }

    if (updatedApproval.getComments() != null) {

        existingApproval.setComments(
                updatedApproval.getComments()
        );
    }

    return approvalRepository.save(existingApproval);
}

public Approval approveApproval(
        Long id,
        String comments) {

    Approval approval =
            getApprovalById(id);

    if (!"PENDING".equalsIgnoreCase(
            approval.getStatus())) {

        throw new RuntimeException(
                "Only PENDING approvals can be approved."
        );
    }

    approval.setStatus("APPROVED");

    approval.setApprovedAt(
            LocalDateTime.now()
    );

    if (comments != null) {
        approval.setComments(comments);
    }

    synchronizeModificationStatus(
            approval,
            "APPROVED"
    );

    createVersionForApprovedModification(
            approval
    );

    return approvalRepository.save(approval);
}

public Approval rejectApproval(
        Long id,
        String comments) {

    Approval approval =
            getApprovalById(id);

    if (!"PENDING".equalsIgnoreCase(
            approval.getStatus())) {

        throw new RuntimeException(
                "Only PENDING approvals can be rejected."
        );
    }

    approval.setStatus("REJECTED");

    approval.setApprovedAt(null);

    if (comments != null) {
        approval.setComments(comments);
    }

    synchronizeModificationStatus(
            approval,
            "REJECTED"
    );

    return approvalRepository.save(approval);
}

private void synchronizeModificationStatus(
        Approval approval,
        String status) {

    if (approval == null
            || approval.getModification() == null
            || approval.getModification().getId() == null) {

        return;
    }

    String normalizedStatus =
            status == null
                    ? null
                    : status.trim().toUpperCase();

    if (!"APPROVED".equals(normalizedStatus)
            && !"REJECTED".equals(normalizedStatus)
            && !"PENDING".equals(normalizedStatus)) {

        return;
    }

    Long modificationId =
            approval.getModification().getId();

    Modification modification =
            modificationRepository.findById(
                    modificationId
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Modification not found with id: "
                                    + modificationId
                    ));

    modification.setStatus(
            normalizedStatus
    );

    modificationRepository.save(
            modification
    );

    approval.setModification(
            modification
    );
}

private void createVersionForApprovedModification(
        Approval approval) {

    if (approval == null) {
        throw new RuntimeException(
                "Approval cannot be null"
        );
    }

    Modification modification =
            approval.getModification();

    if (modification == null
            || modification.getId() == null) {

        throw new RuntimeException(
                "Approved modification is required"
        );
    }

    if (!"APPROVED".equalsIgnoreCase(
            modification.getStatus())) {

        throw new RuntimeException(
                "Modification must be APPROVED before creating a version"
        );
    }

    Long contractId = null;

    if (modification.getContract() != null
            && modification.getContract().getId() != null) {

        contractId =
                modification.getContract().getId();

    } else if (approval.getContract() != null
            && approval.getContract().getId() != null) {

        contractId =
                approval.getContract().getId();
    }

    if (contractId == null) {
        throw new RuntimeException(
                "Contract is required to create a version"
        );
    }

    Document document = null;

    if (modification.getDocument() != null
            && modification.getDocument().getId() != null) {

        Long documentId =
                modification.getDocument().getId();

        document =
                documentRepository.findById(documentId)
                        .orElse(null);
    }

    if (document == null) {

        List<Document> contractDocuments =
                documentRepository
                        .findByContractIdOrderByUploadedAtDesc(
                                contractId
                        );

        if (contractDocuments != null
                && !contractDocuments.isEmpty()) {

            document =
                    contractDocuments.get(0);
        }
    }

    if (document == null) {
        throw new RuntimeException(
                "No document found for contract id: "
                        + contractId
                        + ". Please upload a document before approving this modification."
        );
    }

    Long documentId =
            document.getId();

    List<Version> documentVersions =
            versionRepository.findAll()
                    .stream()
                    .filter(version ->
                            version.getDocument() != null
                            && version.getDocument().getId() != null
                            && version.getDocument()
                                    .getId()
                                    .equals(documentId)
                    )
                    .sorted(
                            Comparator.comparing(
                                    Version::getVersionNumber,
                                    Comparator.nullsLast(
                                            Comparator.reverseOrder()
                                    )
                            )
                    )
                    .collect(
                            java.util.stream.Collectors.toList()
                    );

    int nextVersionNumber = 1;

    if (!documentVersions.isEmpty()) {

        Version currentVersion =
                documentVersions.get(0);

        if (currentVersion.getVersionNumber() != null) {

            nextVersionNumber =
                    currentVersion.getVersionNumber() + 1;
        }

        for (Version version : documentVersions) {

            if (version.getStatus() != null
                    && "CURRENT".equalsIgnoreCase(
                            version.getStatus())) {

                version.setStatus("PREVIOUS");

                versionRepository.save(version);
            }
        }
    }

    User createdBy = approval.getApprover();

    if (createdBy != null
            && createdBy.getId() != null) {

        createdBy =
                userRepository.findById(
                        createdBy.getId()
                ).orElse(null);
    }

    if (createdBy == null) {

        throw new RuntimeException(
                "Approver is required to create a version"
        );
    }

    Version newVersion =
            new Version();

    newVersion.setDocument(document);

    newVersion.setVersionNumber(
            nextVersionNumber
    );

    newVersion.setFilePath(
            document.getFilePath()
    );

    String modificationType =
            modification.getModificationType();

    String reason =
            modification.getReason();

    StringBuilder changeDescription =
            new StringBuilder();

    if ("CLAUSE".equalsIgnoreCase(
            modificationType)) {

        changeDescription.append(
                "Approved clause modification"
        );

    } else {

        changeDescription.append(
                "Approved contract modification"
        );
    }

    if (reason != null
            && !reason.trim().isEmpty()) {

        changeDescription.append(
                " - Reason: "
        );

        changeDescription.append(
                reason.trim()
        );
    }

    newVersion.setChangeDescription(
            changeDescription.toString()
    );

    newVersion.setCreatedBy(
            createdBy
    );

    newVersion.setCreatedAt(
            LocalDateTime.now()
    );

    newVersion.setStatus(
            "CURRENT"
    );

    versionRepository.save(
            newVersion
    );
}

public void deleteApproval(Long id) {

    Approval approval =
            getApprovalById(id);

    approvalRepository.delete(
            approval
    );
}

@Transactional(readOnly = true)
public List<Approval> getApprovalsByStatus(
        String status) {

    if (status == null
            || status.isBlank()) {

        throw new RuntimeException(
                "Status cannot be empty"
        );
    }

    return approvalRepository.findByStatus(
            status.trim().toUpperCase()
    );
}

@Transactional(readOnly = true)
public List<Approval> getApprovalsByContract(
        Long contractId) {

    if (contractId == null) {

        throw new RuntimeException(
                "Contract ID cannot be null"
        );
    }

    return approvalRepository.findByContractId(
            contractId
    );
}

@Transactional(readOnly = true)
public List<Approval> getApprovalsByModification(
        Long modificationId) {

    if (modificationId == null) {

        throw new RuntimeException(
                "Modification ID cannot be null"
        );
    }

    return approvalRepository.findByModificationId(
            modificationId
    );
}

@Transactional(readOnly = true)
public List<Approval> getApprovalsByApprover(
        Long approverId) {

    if (approverId == null) {

        throw new RuntimeException(
                "Approver ID cannot be null"
        );
    }

    return approvalRepository.findByApproverId(
            approverId
    );
}


}
