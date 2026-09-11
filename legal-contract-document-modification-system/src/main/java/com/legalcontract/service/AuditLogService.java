
package com.legalcontract.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.legalcontract.entity.AuditLog;
import com.legalcontract.entity.User;
import com.legalcontract.repository.AuditLogRepository;
import com.legalcontract.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepo;
    private final UserRepository userRepo;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AuditLogService(
            AuditLogRepository auditLogRepo,
            UserRepository userRepo) {

        this.auditLogRepo = auditLogRepo;
        this.userRepo = userRepo;
    }

    // =========================================================
    // AUTOMATIC AUDIT LOG
    // =========================================================

    public void log(
            String action,
            String entityName,
            Long entityId,
            String description) {

        AuditLog auditLog = new AuditLog();

        // -----------------------------------------------------
        // CURRENT LOGGED-IN USER
        // -----------------------------------------------------

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(
                        authentication.getPrincipal())) {

            String username = authentication.getName();

            User user = userRepo
                    .findByUsername(username)
                    .orElse(null);

            auditLog.setUser(user);
        }

        // -----------------------------------------------------
        // AUDIT INFORMATION
        // -----------------------------------------------------

        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);

        // -----------------------------------------------------
        // IP ADDRESS
        // -----------------------------------------------------

        HttpServletRequest request = getCurrentRequest();

        auditLog.setIpAddress(
                getClientIpAddress(request)
        );

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        auditLog.setCreatedAt(
                LocalDateTime.now()
        );

        auditLogRepo.save(auditLog);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public void logLogin(Authentication authentication) {

        if (authentication == null) {
            return;
        }

        String username = authentication.getName();

        User user = userRepo
                .findByUsername(username)
                .orElse(null);

        AuditLog auditLog = new AuditLog();

        // -----------------------------------------------------
        // USER
        // -----------------------------------------------------

        auditLog.setUser(user);

        // -----------------------------------------------------
        // ACTION
        // -----------------------------------------------------

        auditLog.setAction("LOGIN");

        // -----------------------------------------------------
        // ENTITY = ROLE
        // ADMIN / USER / REVIEWER
        // -----------------------------------------------------

        String entityName = getUserEntityName(user);

        auditLog.setEntityName(entityName);

        // -----------------------------------------------------
        // ENTITY ID
        // -----------------------------------------------------

        if (user != null) {
            auditLog.setEntityId(user.getId());
        }

        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        auditLog.setDescription(
                entityName
                        + " '"
                        + username
                        + "' logged into the system"
        );

        // -----------------------------------------------------
        // IP ADDRESS
        // -----------------------------------------------------

        HttpServletRequest request = getCurrentRequest();

        auditLog.setIpAddress(
                getClientIpAddress(request)
        );

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        auditLog.setCreatedAt(
                LocalDateTime.now()
        );

        auditLogRepo.save(auditLog);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    public void logLogout(String username) {

        AuditLog auditLog = new AuditLog();

        User user = null;

        if (username != null && !username.isBlank()) {

            user = userRepo
                    .findByUsername(username)
                    .orElse(null);
        }

        // -----------------------------------------------------
        // USER
        // -----------------------------------------------------

        auditLog.setUser(user);

        // -----------------------------------------------------
        // ACTION
        // -----------------------------------------------------

        auditLog.setAction("LOGOUT");

        // -----------------------------------------------------
        // ENTITY = ROLE
        // ADMIN / USER / REVIEWER
        // -----------------------------------------------------

        String entityName = getUserEntityName(user);

        auditLog.setEntityName(entityName);

        // -----------------------------------------------------
        // ENTITY ID
        // -----------------------------------------------------

        if (user != null) {
            auditLog.setEntityId(user.getId());
        }

        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        auditLog.setDescription(
                entityName
                        + " '"
                        + username
                        + "' logged out of the system"
        );

        // -----------------------------------------------------
        // IP ADDRESS
        // -----------------------------------------------------

        HttpServletRequest request = getCurrentRequest();

        auditLog.setIpAddress(
                getClientIpAddress(request)
        );

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        auditLog.setCreatedAt(
                LocalDateTime.now()
        );

        auditLogRepo.save(auditLog);
    }

    // =========================================================
    // REGISTER
    // =========================================================

    public void logRegister(User user) {

        AuditLog auditLog = new AuditLog();

        auditLog.setUser(user);

        auditLog.setAction("REGISTER");

        auditLog.setEntityName("USER");

        if (user != null) {

            auditLog.setEntityId(
                    user.getId()
            );
        }

        String username =
                user != null
                        ? user.getUsername()
                        : "Unknown";

        auditLog.setDescription(
                "New user '"
                        + username
                        + "' registered"
        );

        // -----------------------------------------------------
        // IP ADDRESS
        // -----------------------------------------------------

        HttpServletRequest request = getCurrentRequest();

        auditLog.setIpAddress(
                getClientIpAddress(request)
        );

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        auditLog.setCreatedAt(
                LocalDateTime.now()
        );

        auditLogRepo.save(auditLog);
    }

    // =========================================================
    // CREATE
    // =========================================================

    public void logCreate(
            String entityName,
            Long entityId) {

        log(
                "CREATE",
                entityName,
                entityId,
                entityName + " created"
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void logUpdate(
            String entityName,
            Long entityId) {

        log(
                "UPDATE",
                entityName,
                entityId,
                entityName + " updated"
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void logDelete(
            String entityName,
            Long entityId) {

        log(
                "DELETE",
                entityName,
                entityId,
                entityName + " deleted"
        );
    }

    // =========================================================
    // APPROVE
    // =========================================================

    public void logApprove(Long approvalId) {

        log(
                "APPROVE",
                "Approval",
                approvalId,
                "Approval approved"
        );
    }

    // =========================================================
    // REJECT
    // =========================================================

    public void logReject(Long approvalId) {

        log(
                "REJECT",
                "Approval",
                approvalId,
                "Approval rejected"
        );
    }

    // =========================================================
    // GET USER ENTITY NAME
    // =========================================================
    //
    // ROLE_ADMIN    -> ADMIN
    // ROLE_USER     -> USER
    // ROLE_REVIEWER -> REVIEWER
    //
    // =========================================================

    private String getUserEntityName(User user) {

        if (user == null) {
            return "USER";
        }

        if (user.getRole() == null) {
            return "USER";
        }

        if (user.getRole().getName() == null) {
            return "USER";
        }

        String roleName =
                user.getRole()
                        .getName()
                        .trim()
                        .toUpperCase();

        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

        if ("ROLE_ADMIN".equals(roleName)) {
            return "ADMIN";
        }

        // -----------------------------------------------------
        // REVIEWER
        // -----------------------------------------------------

        if ("ROLE_REVIEWER".equals(roleName)) {
            return "REVIEWER";
        }

        // -----------------------------------------------------
        // USER
        // -----------------------------------------------------

        if ("ROLE_USER".equals(roleName)) {
            return "USER";
        }

        // -----------------------------------------------------
        // FALLBACK
        // -----------------------------------------------------

        if (roleName.startsWith("ROLE_")) {
            return roleName.substring(5);
        }

        return roleName;
    }

    // =========================================================
    // GET CURRENT HTTP REQUEST
    // =========================================================

    private HttpServletRequest getCurrentRequest() {

        try {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder
                                    .getRequestAttributes();

            if (attributes != null) {

                return attributes.getRequest();
            }

        } catch (Exception e) {

            // No HTTP request available
        }

        return null;
    }

    // =========================================================
    // GET CLIENT IP ADDRESS
    // =========================================================

    private String getClientIpAddress(
            HttpServletRequest request) {

        if (request == null) {
            return "UNKNOWN";
        }

        // -----------------------------------------------------
        // X-FORWARDED-FOR
        // -----------------------------------------------------

        String ip =
                request.getHeader("X-Forwarded-For");

        // -----------------------------------------------------
        // PROXY-CLIENT-IP
        // -----------------------------------------------------

        if (ip == null
                || ip.isBlank()
                || "unknown".equalsIgnoreCase(ip)) {

            ip = request.getHeader(
                    "Proxy-Client-IP"
            );
        }

        // -----------------------------------------------------
        // WL-PROXY-CLIENT-IP
        // -----------------------------------------------------

        if (ip == null
                || ip.isBlank()
                || "unknown".equalsIgnoreCase(ip)) {

            ip = request.getHeader(
                    "WL-Proxy-Client-IP"
            );
        }

        // -----------------------------------------------------
        // NORMAL REQUEST IP
        // -----------------------------------------------------

        if (ip == null
                || ip.isBlank()
                || "unknown".equalsIgnoreCase(ip)) {

            ip = request.getRemoteAddr();
        }

        // -----------------------------------------------------
        // MULTIPLE FORWARDED IPs
        // -----------------------------------------------------

        if (ip != null && ip.contains(",")) {

            ip = ip.split(",")[0].trim();
        }

        // -----------------------------------------------------
        // IPV6 LOCALHOST
        // -----------------------------------------------------

        if ("0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip)) {

            ip = "127.0.0.1";
        }

        // -----------------------------------------------------
        // FINAL FALLBACK
        // -----------------------------------------------------

        if (ip == null || ip.isBlank()) {
            return "UNKNOWN";
        }

        return ip.trim();
    }

    // =========================================================
    // CREATE AUDIT LOG MANUALLY
    // =========================================================

    public AuditLog createAuditLog(
            AuditLog auditLog) {

        if (auditLog == null) {

            throw new RuntimeException(
                    "Audit log data cannot be null"
            );
        }

        // -----------------------------------------------------
        // USER
        // -----------------------------------------------------

        if (auditLog.getUser() != null
                && auditLog.getUser().getId() != null) {

            User user = userRepo
                    .findById(
                            auditLog.getUser().getId()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with id : "
                                            + auditLog.getUser().getId()
                            )
                    );

            auditLog.setUser(user);
        }

        // -----------------------------------------------------
        // IP ADDRESS
        // -----------------------------------------------------

        if (auditLog.getIpAddress() == null
                || auditLog.getIpAddress().isBlank()
                || "0".equals(auditLog.getIpAddress())) {

            HttpServletRequest request =
                    getCurrentRequest();

            auditLog.setIpAddress(
                    getClientIpAddress(request)
            );
        }

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        if (auditLog.getCreatedAt() == null) {

            auditLog.setCreatedAt(
                    LocalDateTime.now()
            );
        }

        return auditLogRepo.save(auditLog);
    }

    // =========================================================
    // GET ALL AUDIT LOGS
    // =========================================================

    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {

        return auditLogRepo
                .findAllByOrderByCreatedAtDesc();
    }

    // =========================================================
    // GET AUDIT LOG BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public AuditLog getAuditLogById(Long id) {

        if (id == null) {

            throw new RuntimeException(
                    "AuditLog ID cannot be null"
            );
        }

        return auditLogRepo
                .findById(id)

                .orElseThrow(() ->
                        new RuntimeException(
                                "AuditLog not found with id : "
                                        + id
                        )
                );
    }

    // =========================================================
    // UPDATE AUDIT LOG
    // =========================================================

    public AuditLog updateAuditLog(
            Long id,
            AuditLog auditLog) {

        if (auditLog == null) {

            throw new RuntimeException(
                    "Audit log data cannot be null"
            );
        }

        AuditLog existingAuditLog =
                getAuditLogById(id);

        // -----------------------------------------------------
        // USER
        // -----------------------------------------------------

        if (auditLog.getUser() != null
                && auditLog.getUser().getId() != null) {

            User user = userRepo
                    .findById(
                            auditLog.getUser().getId()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with id : "
                                            + auditLog.getUser().getId()
                            )
                    );

            existingAuditLog.setUser(user);
        }

        // -----------------------------------------------------
        // ACTION
        // -----------------------------------------------------

        if (auditLog.getAction() != null) {

            existingAuditLog.setAction(
                    auditLog.getAction()
            );
        }

        // -----------------------------------------------------
        // ENTITY NAME
        // -----------------------------------------------------

        if (auditLog.getEntityName() != null) {

            existingAuditLog.setEntityName(
                    auditLog.getEntityName()
            );
        }

        // -----------------------------------------------------
        // ENTITY ID
        // -----------------------------------------------------

        existingAuditLog.setEntityId(
                auditLog.getEntityId()
        );

        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        if (auditLog.getDescription() != null) {

            existingAuditLog.setDescription(
                    auditLog.getDescription()
            );
        }

        // -----------------------------------------------------
        // IP ADDRESS
        // -----------------------------------------------------

        if (auditLog.getIpAddress() != null
                && !auditLog.getIpAddress().isBlank()) {

            existingAuditLog.setIpAddress(
                    auditLog.getIpAddress()
            );
        }

        return auditLogRepo.save(
                existingAuditLog
        );
    }

    // =========================================================
    // DELETE AUDIT LOG
    // =========================================================

    public void deleteAuditLog(Long id) {

        AuditLog auditLog =
                getAuditLogById(id);

        auditLogRepo.delete(auditLog);
    }
}
