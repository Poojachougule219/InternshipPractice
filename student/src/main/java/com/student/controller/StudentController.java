package com.student.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.student.entity.Student;
import com.student.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {
	
	@Autowired
	private StudentService service;
	
	
//	1. Add Student (POST)
	
	@PostMapping
	public Student addStudent(@RequestBody Student student)
	{
		return service.addStudent(student);
	}
	
	
//	2. Get All Students (GET)
	
	@GetMapping
	public List<Student> getAllStudents()
	{
		return service.getAllStudents();
	}
	
	
//	3. Get student By Id (GET)
	@GetMapping("/{id}")
	public Student getStudentById(@PathVariable Long id)
	{
		return service.getStudentById(id);
	}
	
	
//	4. Update Student (PUT)
	@PutMapping("/{id}")
	public Student updateStudent(@PathVariable Long id, @RequestBody Student student)
	{
		return service.updateStudent(id, student);
	}
	
	
//	5. Delete Mapping (DELETE)
	// 5. Delete Student (DELETE)

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
	    service.deleteStudent(id);
	    return ResponseEntity.ok("Student Deleted Successfully!");
	}
	

}
