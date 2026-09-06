package com.student.entity;

import java.time.LocalDateTime;

import com.student.audit.AuditFields;
import com.student.enums.StudentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student extends AuditFields {

    // =====================================================
    // PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // BASIC DETAILS
    // =====================================================

    @Column(nullable = false)
    private String name;

    private int age;

    private String department;

    @Column(nullable = false, unique = true)
    private String email;

    private String city;


    // =====================================================
    // PASSWORD
    // =====================================================

    @Column(nullable = false)
    private String password;


    // =====================================================
    // CONTACT DETAILS
    // =====================================================

    private String contactNo;

    @Column(columnDefinition = "TEXT")
    private String address;


    // =====================================================
    // PROFILE PHOTO
    // =====================================================

    @Lob
    @Column(
        name = "profile_photo",
        columnDefinition = "LONGBLOB"
    )
    private byte[] profilePhoto;


    // =====================================================
    // ROLE
    // =====================================================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;


    // =====================================================
    // STATUS
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status;


    // =====================================================
    // SOFT DELETE
    // =====================================================

    @Column(
        name = "is_deleted",
        nullable = false
    )
    private String isDeleted;


    // =====================================================
    // CREATED DATE
    // =====================================================

    @Column(name = "created_date")
    private LocalDateTime createdDate;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Student() {
    }


    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    protected void onCreate() {

        if (status == null) {
            status = StudentStatus.ACTIVE;
        }

        if (isDeleted == null) {
            isDeleted = "false";
        }

        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }


    // =====================================================
    // ID
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // =====================================================
    // NAME
    // =====================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // =====================================================
    // AGE
    // =====================================================

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    // =====================================================
    // DEPARTMENT
    // =====================================================

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    // =====================================================
    // EMAIL
    // =====================================================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // =====================================================
    // CITY
    // =====================================================

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    // =====================================================
    // PASSWORD
    // =====================================================

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    // =====================================================
    // CONTACT NUMBER
    // =====================================================

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }


    // =====================================================
    // ADDRESS
    // =====================================================

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    // =====================================================
    // PROFILE PHOTO
    // =====================================================

    public byte[] getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
        this.profilePhoto = profilePhoto;
    }


    // =====================================================
    // ROLE
    // =====================================================

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    // =====================================================
    // STATUS
    // =====================================================

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }


    // =====================================================
    // IS DELETED
    // =====================================================

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }


    // =====================================================
    // CREATED DATE
    // =====================================================

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

}