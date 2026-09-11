package com.legalcontract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document> findByContractOrderByUploadedAtDesc(
            Contract contract
    );

    List<Document> findByContractIdOrderByUploadedAtDesc(
            Long contractId
    );

    boolean existsByContractIdAndStatus(
            Long contractId,
            String status
    );
}