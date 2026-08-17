package com.student.service.impl;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.student.entity.AuditLog;
import com.student.repository.AuditLogRepository;
import com.student.service.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;


    // =====================================================
    // CREATE AUDIT LOG
    // =====================================================

    @Override
    public void createLog(
            Long entityId,
            String username,
            String role,
            String action,
            String entityName,
            Long studentId,
            String description,
            String ipAddress) {

        AuditLog auditLog = new AuditLog();

        auditLog.setEntityId(entityId);
        auditLog.setUsername(username);
        auditLog.setRole(role);
        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setStudentId(studentId);
        auditLog.setDescription(description);

        // IMPORTANT:
        // IP address received from controller/service
        auditLog.setIpAddress(ipAddress);

        auditLogRepository.save(auditLog);
    }


    // =====================================================
    // GET AUDIT LOGS
    // SEARCH + ACTION + ROLE + DATE + PAGINATION
    // =====================================================

    @Override
    public Page<AuditLog> getAuditLogs(
            String search,
            String action,
            String role,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDate != null) {
            fromDateTime =
                    fromDate.atStartOfDay();
        }

        if (toDate != null) {
            toDateTime =
                    toDate.atTime(LocalTime.MAX);
        }

        if (search != null
                && search.trim().isEmpty()) {
            search = null;
        }

        if (action != null
                && action.trim().isEmpty()) {
            action = null;
        }

        if (role != null
                && role.trim().isEmpty()) {
            role = null;
        }

        return auditLogRepository.searchAuditLogs(
                search,
                action,
                role,
                fromDateTime,
                toDateTime,
                pageable
        );
    }
   }