package com.student.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;

import com.student.entity.AuditLog;

public interface AuditLogService {

    // =====================================================
    // CREATE AUDIT LOG
    // =====================================================

    void createLog(
            Long entityId,
            String username,
            String role,
            String action,
            String entityName,
            Long studentId,
            String description,
            String ipAddress
    );


    // =====================================================
    // GET AUDIT LOGS
    // =====================================================

    Page<AuditLog> getAuditLogs(
            String search,
            String action,
            String role,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );
}