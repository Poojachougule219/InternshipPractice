package com.student.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.student.entity.AuditLog;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a
        FROM AuditLog a
        WHERE
            (
                :search IS NULL
                OR LOWER(a.username) LIKE
                   LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.description) LIKE
                   LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.entityName) LIKE
                   LOWER(CONCAT('%', :search, '%'))
            )
        AND
            (
                :action IS NULL
                OR a.action = :action
            )
        AND
            (
                :role IS NULL
                OR a.role = :role
            )
        AND
            (
                :fromDate IS NULL
                OR a.createdAt >= :fromDate
            )
        AND
            (
                :toDate IS NULL
                OR a.createdAt <= :toDate
            )
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> searchAuditLogs(

            @Param("search")
            String search,

            @Param("action")
            String action,

            @Param("role")
            String role,

            @Param("fromDate")
            LocalDateTime fromDate,

            @Param("toDate")
            LocalDateTime toDate,

            Pageable pageable
    );
}