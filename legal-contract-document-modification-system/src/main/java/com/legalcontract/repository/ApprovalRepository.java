package com.legalcontract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.legalcontract.entity.Approval;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByStatus(String status);

    List<Approval> findByContractId(Long contractId);

    List<Approval> findByModificationId(Long modificationId);

    List<Approval> findByApproverId(Long approverId);

    @Query("""
        SELECT DISTINCT a
        FROM Approval a
        LEFT JOIN FETCH a.modification m
        LEFT JOIN FETCH m.modifiedBy u
        LEFT JOIN FETCH a.contract c
        LEFT JOIN FETCH a.approver ap
        """)
    List<Approval> findAllWithModificationAndUser();

    @Query("""
        SELECT DISTINCT a
        FROM Approval a
        LEFT JOIN FETCH a.modification m
        LEFT JOIN FETCH m.modifiedBy u
        LEFT JOIN FETCH a.contract c
        LEFT JOIN FETCH a.approver ap
        WHERE a.status = :status
        """)
    List<Approval> findByStatusWithModificationAndUser(
            @Param("status") String status);
}