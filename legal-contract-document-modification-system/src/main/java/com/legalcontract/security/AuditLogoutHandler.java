package com.legalcontract.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.legalcontract.service.AuditLogService;

@Component
public class AuditLogoutHandler
        implements LogoutHandler {

    private final AuditLogService auditLogService;

    public AuditLogoutHandler(
            AuditLogService auditLogService) {

        this.auditLogService =
                auditLogService;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        if (authentication != null) {

            auditLogService.logLogout(
                    authentication.getName()
            );
        }
    }
}