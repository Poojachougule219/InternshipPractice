
package com.student.dto;

import java.time.LocalDateTime;

public class AuditLogResponse {

    
    // AUDIT LOG ID

    private Long id;


    
    // USER INFORMATION

    private Long studentId;

    private String username;

    private String role;


    
    // ACTIVITY INFORMATION
    
    private String action;

    private String entityName;

    private Long entityId;

    private String description;


    
    // REQUEST INFORMATION
    
    private String ipAddress;


   
    // DATE / TIME
    
    private LocalDateTime createdAt;


    
    // CONSTRUCTORS

    public AuditLogResponse() {
    }


    
    // GETTERS AND SETTERS
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }


    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

