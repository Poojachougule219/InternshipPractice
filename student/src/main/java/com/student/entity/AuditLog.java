package com.student.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // ACTION
    // =====================================================

    @Column(name = "action")
    private String action;


    // =====================================================
    // CREATED AT
    // =====================================================

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    // =====================================================
    // DESCRIPTION
    // =====================================================

    @Column(name = "description")
    private String description;


    // =====================================================
    // ENTITY ID
    // =====================================================

    @Column(name = "entity_id")
    private Long entityId;


    // =====================================================
    // ENTITY NAME
    // =====================================================

    @Column(name = "entity_name")
    private String entityName;


    // =====================================================
    // IP ADDRESS
    // =====================================================

    @Column(name = "ip_address")
    private String ipAddress;


    // =====================================================
    // ROLE
    // =====================================================

    @Column(name = "role")
    private String role;


    // =====================================================
    // USERNAME
    // =====================================================

    @Column(name = "username")
    private String username;


    // =====================================================
    // STUDENT ID
    // =====================================================

    @Column(name = "student_id")
    private Long studentId;


    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {

            createdAt =
                    LocalDateTime.now();
        }
    }


    // =====================================================
    // GETTERS AND SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }


    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(
            Long entityId) {

        this.entityId = entityId;
    }


    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(
            String entityName) {

        this.entityName = entityName;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(
            String ipAddress) {

        this.ipAddress = ipAddress;
    }


    public String getRole() {
        return role;
    }

    public void setRole(
            String role) {

        this.role = role;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }


    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(
            Long studentId) {

        this.studentId = studentId;
    }
}