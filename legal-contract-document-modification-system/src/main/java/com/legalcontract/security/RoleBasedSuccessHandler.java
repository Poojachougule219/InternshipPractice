package com.legalcontract.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.legalcontract.service.AuditLogService;

@Component
public class RoleBasedSuccessHandler
implements AuthenticationSuccessHandler {


private final AuditLogService auditLogService;

public RoleBasedSuccessHandler(
        AuditLogService auditLogService) {

    this.auditLogService = auditLogService;
}

@Override
public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication)
        throws IOException, ServletException {

    try {
        auditLogService.logLogin(authentication);
    } catch (Exception e) {
        System.err.println(
                "Audit login logging failed: "
                        + e.getMessage());
    }

    for (GrantedAuthority authority :
            authentication.getAuthorities()) {

        String role =
                authority.getAuthority();

        if ("ROLE_ADMIN".equals(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/dashboard");

            return;
        }

        if ("ROLE_MANAGER".equals(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/approval/dashboard");

            return;
        }

        if ("ROLE_REVIEWER".equals(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/contract/dashboard");

            return;
        }

        if ("ROLE_USER".equals(role)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/user/dashboard");

            return;
        }
    }

    response.sendRedirect(
            request.getContextPath()
                    + "/login?error=true");
}


}
