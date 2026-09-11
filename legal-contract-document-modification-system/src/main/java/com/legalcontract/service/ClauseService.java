package com.legalcontract.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.legalcontract.entity.Clause;
import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.repository.ClauseRepository;
import com.legalcontract.repository.ContractRepository;
import com.legalcontract.repository.DocumentRepository;

@Service
@Transactional
public class ClauseService {

    @Autowired
    private ClauseRepository clauseRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private DocumentRepository documentRepository;

    // =========================================================
    // CREATE CLAUSE
    // =========================================================

    public Clause createClause(
            Clause clause,
            Long contractId,
            Long documentId) {

        Contract contract = contractRepository
                .findById(contractId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Contract not found with ID: " + contractId
                        )
                );

        clause.setContract(contract);

        // Document is optional
        if (documentId != null) {

            Document document = documentRepository
                    .findById(documentId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Document not found with ID: "
                                            + documentId
                            )
                    );

            // Make sure document belongs to selected contract
            if (document.getContract() == null
                    || !contractId.equals(
                            document.getContract().getId())) {

                throw new RuntimeException(
                        "Selected document does not belong to this contract."
                );
            }

            clause.setDocument(document);

        } else {

            clause.setDocument(null);
        }

        // =====================================================
        // AUTOMATIC CLAUSE ORDER
        // =====================================================

        Integer maxOrder =
                clauseRepository.findMaxClauseOrderByContract(contract);

        if (maxOrder == null) {
            clause.setClauseOrder(1);
        } else {
            clause.setClauseOrder(maxOrder + 1);
        }

        // Default status
        if (clause.getStatus() == null
                || clause.getStatus().trim().isEmpty()) {

            clause.setStatus("ACTIVE");
        }

        return clauseRepository.save(clause);
    }

    // =========================================================
    // GET ALL CLAUSES
    // =========================================================

    @Transactional(readOnly = true)
    public List<Clause> getAllClauses() {

        return clauseRepository.findAll();
    }

    // =========================================================
    // GET CLAUSES BY CONTRACT
    // =========================================================

    @Transactional(readOnly = true)
    public List<Clause> getClausesByContract(Long contractId) {

        Contract contract = contractRepository
                .findById(contractId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Contract not found with ID: " + contractId
                        )
                );

        return clauseRepository
                .findByContractOrderByClauseOrderAsc(contract);
    }

    // =========================================================
    // GET CLAUSE BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Clause getClauseById(Long id) {

        return clauseRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Clause not found with ID: " + id
                        )
                );
    }

    // =========================================================
    // UPDATE CLAUSE
    // =========================================================

    public Clause updateClause(
            Long id,
            Clause clause,
            Long contractId,
            Long documentId) {

        Clause existingClause =
                getClauseById(id);

        // Update contract
        if (contractId != null) {

            Contract contract = contractRepository
                    .findById(contractId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Contract not found with ID: "
                                            + contractId
                            )
                    );

            existingClause.setContract(contract);
        }

        // Update document if selected
        if (documentId != null) {

            Document document = documentRepository
                    .findById(documentId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Document not found with ID: "
                                            + documentId
                            )
                    );

            // Make sure document belongs to selected contract
            if (existingClause.getContract() == null
                    || document.getContract() == null
                    || !existingClause.getContract().getId()
                            .equals(document.getContract().getId())) {

                throw new RuntimeException(
                        "Selected document does not belong to this contract."
                );
            }

            existingClause.setDocument(document);

        } else {

            existingClause.setDocument(null);
        }

        // =====================================================
        // KEEP EXISTING CLAUSE ORDER
        // =====================================================

        // Do NOT change clauseOrder during update.
        // This preserves the clause's existing position.

        // Update fields
        existingClause.setClauseNumber(
                clause.getClauseNumber()
        );

        existingClause.setTitle(
                clause.getTitle()
        );

        existingClause.setContent(
                clause.getContent()
        );

        existingClause.setClauseType(
                clause.getClauseType()
        );

        existingClause.setStatus(
                clause.getStatus()
        );

        return clauseRepository.save(existingClause);
    }

    // =========================================================
    // DELETE CLAUSE
    // =========================================================

    public void deleteClause(Long id) {

        Clause clause =
                getClauseById(id);

        Contract contract =
                clause.getContract();

        Integer deletedOrder =
                clause.getClauseOrder();

        clauseRepository.delete(clause);

        // =====================================================
        // REORDER REMAINING CLAUSES
        // =====================================================

        if (contract != null && deletedOrder != null) {

            List<Clause> remainingClauses =
                    clauseRepository
                            .findByContractOrderByClauseOrderAsc(
                                    contract
                            );

            int order = 1;

            for (Clause remainingClause : remainingClauses) {

                remainingClause.setClauseOrder(order);

                order++;
            }

            clauseRepository.saveAll(
                    remainingClauses
            );
        }
    }
}