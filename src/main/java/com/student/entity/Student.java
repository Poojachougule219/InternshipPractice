package com.student.entity;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "students")
public class Student extends AuditFields {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name is required")
	@Size(max = 100, message = "Name cannot exceed 100 characters")
	@Column(nullable = false, length = 100)
	private String name;

	@Min(value = 18, message = "Age must be at least 18")
	@Max(value = 60, message = "Age cannot exceed 60")
	@Column(nullable = false)
	private int age;

	@NotBlank(message = "Department is required")
	@Size(max = 100, message = "Department cannot exceed 100 characters")
	@Column(nullable = false, length = 100)
	private String department;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid Email")
	@Size(max = 150, message = "Email cannot exceed 150 characters")
	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@NotBlank(message = "City is required")
	@Size(max = 100, message = "City cannot exceed 100 characters")
	@Column(nullable = false, length = 100)
	private String city;

	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must contain at least 6 characters")
	@Column(nullable = false, length = 255)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@NotBlank(message = "Contact Number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
	@Column(nullable = false, length = 10)
	private String contactNo;

	@NotBlank(message = "Address is required")
	@Size(max = 255, message = "Address cannot exceed 255 characters")
	@Column(nullable = false, length = 255)
	private String address;

	@Column(length = 500)
	private String profilePhoto;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StudentStatus status = StudentStatus.ACTIVE;

	@Column(name = "is_deleted", nullable = false, length = 10)
	private String isDeleted = "false";
	
	
	public Student() {
		super();
	}

	public Student(Long id, String name, int age, String department, String email, String city, String password,
			Role role, String contactNo, String address, String profilePhoto, StudentStatus status, String isDeleted) {

		super();

		this.id = id;
		this.name = name;
		this.age = age;
		this.department = department;
		this.email = email;
		this.city = city;
		this.password = password;
		this.role = role;
		this.contactNo = contactNo;
		this.address = address;
		this.profilePhoto = profilePhoto;
		this.status = status;
		this.isDeleted = isDeleted;
	}

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getProfilePhoto() {
		return profilePhoto;
	}

	public void setProfilePhoto(String profilePhoto) {
		this.profilePhoto = profilePhoto;
	}

	public StudentStatus getStatus() {
		return status;
	}

	public void setStatus(StudentStatus status) {
		this.status = status;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", name=" + name + ", age=" + age + ", department=" + department + ", email="
				+ email + ", city=" + city + ", role=" + (role != null ? role.getId() : null) + ", contactNo="
				+ contactNo + ", address=" + address + ", profilePhoto=" + profilePhoto + ", status=" + status
				+ ", isDeleted=" + isDeleted + "]";
	}
	
	
	@PrePersist
	public void prePersist() {

	    if(isDeleted == null) {
	        isDeleted = "false";
	    }

	    if(status == null) {
	        status = StudentStatus.ACTIVE;
	    }
	}



	@PreUpdate
	public void preUpdate() {

	    if(isDeleted == null) {
	        isDeleted = "false";
	    }
	}

}