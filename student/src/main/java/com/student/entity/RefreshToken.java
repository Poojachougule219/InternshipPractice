package com.student.entity;

import java.time.Instant;

import com.student.audit.AuditFields;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    public RefreshToken() {
    }

    public RefreshToken(Long id, String token, Instant expiryDate, Student student) {
        this.id = id;
        this.token = token;
        this.expiryDate = expiryDate;
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "RefreshToken [id=" + id +
                ", token=" + token +
                ", expiryDate=" + expiryDate +
                ", student=" + (student != null ? student.getId() : null) + "]";
    }
}