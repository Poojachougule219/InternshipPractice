
package com.legalcontract.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.legalcontract.entity.Approval;
import com.legalcontract.entity.Clause;
import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.Modification;
import com.legalcontract.entity.User;
import com.legalcontract.service.AuditLogService;

@Aspect
@Component
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    public AuditLogAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // CREATE
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.create*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))",
            returning = "result"
    )
    public void afterCreate(
            JoinPoint joinPoint,
            Object result) {

        if (result == null) {
            return;
        }

        Long id = getId(result);
        String entityName = getEntityName(result);

        safeLogCreate(entityName, id);
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.register*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))",
            returning = "result"
    )
    public void afterRegister(
            JoinPoint joinPoint,
            Object result) {

        if (result instanceof User user) {
            safeLogRegister(user);
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.update*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))",
            returning = "result"
    )
    public void afterUpdate(
            JoinPoint joinPoint,
            Object result) {

        if (result != null) {

            Long id = getId(result);
            String entityName = getEntityName(result);

            safeLogUpdate(entityName, id);

        } else {

            Long argumentId =
                    getFirstLongArgument(
                            joinPoint.getArgs()
                    );

            if (argumentId != null) {

                safeLogUpdate(
                        getEntityNameFromMethod(joinPoint),
                        argumentId
                );
            }
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.delete*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))"
    )
    public void afterDelete(
            JoinPoint joinPoint) {

        Long id =
                getFirstLongArgument(
                        joinPoint.getArgs()
                );

        if (id == null) {
            return;
        }

        String entityName =
                getEntityNameFromMethod(joinPoint);

        safeLogDelete(entityName, id);
    }

    // =========================================================
    // APPROVE
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.approve*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))",
            returning = "result"
    )
    public void afterApprove(
            JoinPoint joinPoint,
            Object result) {

        Long id = getId(result);

        if (id == null) {
            id = getFirstLongArgument(joinPoint.getArgs());
        }

        safeLog(
                "APPROVE",
                "Approval",
                id,
                "Approval approved"
        );
    }

    // =========================================================
    // REJECT
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.reject*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))",
            returning = "result"
    )
    public void afterReject(
            JoinPoint joinPoint,
            Object result) {

        Long id = getId(result);

        if (id == null) {
            id = getFirstLongArgument(joinPoint.getArgs());
        }

        safeLog(
                "REJECT",
                "Approval",
                id,
                "Approval rejected"
        );
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.activate*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))"
    )
    public void afterActivate(
            JoinPoint joinPoint) {

        Long id =
                getFirstLongArgument(
                        joinPoint.getArgs()
                );

        safeLog(
                "ACTIVATE",
                "User",
                id,
                "User activated"
        );
    }

    // =========================================================
    // DEACTIVATE
    // =========================================================

    @AfterReturning(
            pointcut =
                    "execution(* com.legalcontract.service..*.deactivate*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))"
    )
    public void afterDeactivate(
            JoinPoint joinPoint) {

        Long id =
                getFirstLongArgument(
                        joinPoint.getArgs()
                );

        safeLog(
                "DEACTIVATE",
                "User",
                id,
                "User deactivated"
        );
    }

    // =========================================================
    // FAILED OPERATION
    // =========================================================

    @AfterThrowing(
            pointcut =
                    "execution(* com.legalcontract.service..*(..))"
                    + " && !execution(* com.legalcontract.service.AuditLogService.*(..))",
            throwing = "exception"
    )
    public void afterFailure(
            JoinPoint joinPoint,
            Exception exception) {

        String method =
                joinPoint.getSignature()
                        .getName();

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }

        safeLog(
                "FAILED",
                "System",
                null,
                "Operation '" + method
                        + "' failed: "
                        + message
        );
    }

    // =========================================================
    // SAFE CREATE LOG
    // =========================================================

    private void safeLogCreate(
            String entityName,
            Long id) {

        try {

            auditLogService.logCreate(
                    entityName,
                    id
            );

        } catch (Exception e) {

            System.err.println(
                    "Audit log CREATE failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SAFE REGISTER LOG
    // =========================================================

    private void safeLogRegister(User user) {

        try {

            auditLogService.logRegister(user);

        } catch (Exception e) {

            System.err.println(
                    "Audit log REGISTER failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SAFE UPDATE LOG
    // =========================================================

    private void safeLogUpdate(
            String entityName,
            Long id) {

        try {

            auditLogService.logUpdate(
                    entityName,
                    id
            );

        } catch (Exception e) {

            System.err.println(
                    "Audit log UPDATE failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SAFE DELETE LOG
    // =========================================================

    private void safeLogDelete(
            String entityName,
            Long id) {

        try {

            auditLogService.logDelete(
                    entityName,
                    id
            );

        } catch (Exception e) {

            System.err.println(
                    "Audit log DELETE failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SAFE GENERAL LOG
    // =========================================================

    private void safeLog(
            String action,
            String entityName,
            Long id,
            String description) {

        try {

            auditLogService.log(
                    action,
                    entityName,
                    id,
                    description
            );

        } catch (Exception e) {

            System.err.println(
                    "Audit log failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // GET ID
    // =========================================================

    private Long getId(Object object) {

        if (object == null) {
            return null;
        }

        try {

            if (object instanceof User user) {
                return user.getId();
            }

            if (object instanceof Contract contract) {
                return contract.getId();
            }

            if (object instanceof Document document) {
                return document.getId();
            }

            if (object instanceof Modification modification) {
                return modification.getId();
            }

            if (object instanceof Approval approval) {
                return approval.getId();
            }

            if (object instanceof Clause clause) {
                return clause.getId();
            }

        } catch (Exception e) {

            return null;
        }

        return null;
    }

    // =========================================================
    // ENTITY NAME
    // =========================================================

    private String getEntityName(Object object) {

        if (object instanceof User) {
            return "User";
        }

        if (object instanceof Contract) {
            return "Contract";
        }

        if (object instanceof Document) {
            return "Document";
        }

        if (object instanceof Modification) {
            return "Modification";
        }

        if (object instanceof Approval) {
            return "Approval";
        }

        if (object instanceof Clause) {
            return "Clause";
        }

        return object.getClass()
                .getSimpleName();
    }

    // =========================================================
    // METHOD ENTITY NAME
    // =========================================================

    private String getEntityNameFromMethod(
            JoinPoint joinPoint) {

        String method =
                joinPoint.getSignature()
                        .getName()
                        .toLowerCase();

        if (method.contains("user")) {
            return "User";
        }

        if (method.contains("contract")) {
            return "Contract";
        }

        if (method.contains("document")) {
            return "Document";
        }

        if (method.contains("modification")) {
            return "Modification";
        }

        if (method.contains("approval")) {
            return "Approval";
        }

        if (method.contains("clause")) {
            return "Clause";
        }

        return "Unknown";
    }

    // =========================================================
    // FIRST LONG ARGUMENT
    // =========================================================

    private Long getFirstLongArgument(
            Object[] args) {

        if (args == null) {
            return null;
        }

        for (Object arg : args) {

            if (arg instanceof Long id) {
                return id;
            }
        }

        return null;
    }
}
