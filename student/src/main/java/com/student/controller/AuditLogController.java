package com.student.controller;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.student.entity.AuditLog;
import com.student.service.AuditLogService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;


    // =====================================================
    // GET AUDIT LOGS
    // SEARCH + ACTION + ROLE + DATE + PAGINATION
    // =====================================================

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(

            @RequestParam(
                    value = "search",
                    required = false
            )
            String search,

            @RequestParam(
                    value = "action",
                    required = false
            )
            String action,

            @RequestParam(
                    value = "role",
                    required = false
            )
            String role,

            @RequestParam(
                    value = "fromDate",
                    required = false
            )
            LocalDate fromDate,

            @RequestParam(
                    value = "toDate",
                    required = false
            )
            LocalDate toDate,

            @RequestParam(
                    value = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    value = "size",
                    defaultValue = "10"
            )
            int size) {


        // =================================================
        // GET AUDIT LOGS
        // =================================================

        Page<AuditLog> logs =
                auditLogService.getAuditLogs(
                        search,
                        action,
                        role,
                        fromDate,
                        toDate,
                        page,
                        size
                );


        return ResponseEntity.ok(logs);
    }


    // =====================================================
    // EXPORT AUDIT LOGS TO CSV
    // =====================================================

    @GetMapping("/export")
    public void exportAuditLogs(
            @RequestParam(
                    value = "search",
                    required = false
            )
            String search,

            @RequestParam(
                    value = "action",
                    required = false
            )
            String action,

            @RequestParam(
                    value = "role",
                    required = false
            )
            String role,

            @RequestParam(
                    value = "fromDate",
                    required = false
            )
            LocalDate fromDate,

            @RequestParam(
                    value = "toDate",
                    required = false
            )
            LocalDate toDate,

            HttpServletResponse response)
            throws Exception {


        // =================================================
        // RESPONSE TYPE
        // =================================================

        response.setContentType(
                "text/csv"
        );


        // =================================================
        // FILE NAME
        // =================================================

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=audit_logs.csv"
        );


        // =================================================
        // GET ALL FILTERED RECORDS
        // =================================================

        Page<AuditLog> pageLogs =
                auditLogService.getAuditLogs(
                        search,
                        action,
                        role,
                        fromDate,
                        toDate,
                        0,
                        Integer.MAX_VALUE
                );


        List<AuditLog> logs =
                pageLogs.getContent();


        // =================================================
        // WRITE CSV
        // =================================================

        PrintWriter writer =
                response.getWriter();


        writer.println(
                "ID,Action,Created At,Description,"
                + "Entity ID,Entity Name,IP Address,"
                + "Role,Username,Student ID"
        );


        for (AuditLog log : logs) {

            writer.println(
                    csv(log.getId())
                    + ","
                    + csv(log.getAction())
                    + ","
                    + csv(log.getCreatedAt())
                    + ","
                    + csv(log.getDescription())
                    + ","
                    + csv(log.getEntityId())
                    + ","
                    + csv(log.getEntityName())
                    + ","
                    + csv(log.getIpAddress())
                    + ","
                    + csv(log.getRole())
                    + ","
                    + csv(log.getUsername())
                    + ","
                    + csv(log.getStudentId())
            );
        }


        writer.flush();
    }


    // =====================================================
    // CSV HELPER
    // =====================================================

    private String csv(Object value) {

        if (value == null) {

            return "";
        }


        String text =
                String.valueOf(value);


        text =
                text.replace(
                        "\"",
                        "\"\""
                );


        return "\"" + text + "\"";
    }
}