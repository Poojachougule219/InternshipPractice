package com.student.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;



@Entity
@Table(name = "students")
public class Student {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	@NotBlank(message="Name is required")
	private String name;
	
	
	@Min(value = 18, message = "Age must be at least 18")
	@Max(value = 60, message = "Age cannot exceed 60")
	@Column(nullable = false)
	private int age;
	
	@NotBlank(message = "Department is required")
	@Column(nullable = false)
	private String department;
	
	
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid Email")
	@Column(nullable = false, unique = true)
	private String email;
	
	
	@NotBlank(message = "City is required")
	@Column(nullable = false)
	private String city;
	
	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must contain at least 6 characters")
	@Column(nullable = false)
	private String password;
	
	

	public Student() {
		super();
		// TODO Auto-generated constructor stub
	}



	public Student(Long id, String name, int age, String department, String email, String city, String password) {
		super();
		this.id = id;
		this.name = name;
		this.age = age;
		this.department = department;
		this.email = email;
		this.city = city;
		this.password = password;
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



	@Override
	public String toString() {
		return "Student [id=" + id + ", name=" + name + ", age=" + age + ", department=" + department + ", email="
				+ email + ", city=" + city + ", password=" + password + "]";
	}
	

		
	
	
}
	