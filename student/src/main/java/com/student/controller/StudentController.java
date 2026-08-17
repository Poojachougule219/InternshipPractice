package com.student.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.student.entity.Student;
import com.student.response.ApiResponse;
import com.student.service.StudentService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {
	
	@Autowired
	private StudentService service;
	
	
//	1. Add Student (POST)
	
//	@PostMapping
//	public Student addStudent(@RequestBody Student student)
//	{
//		return service.addStudent(student);
//	}

	
//	 1. add student
	@PostMapping
	public ResponseEntity<Student> addStudent(@Valid @RequestBody Student student) {

	    Student saved = service.addStudent(student);

	    return new ResponseEntity<>(saved, HttpStatus.CREATED);
	}
	
	
//	2. Get All Students (GET)
	
//	@GetMapping
//	public List<Student> getAllStudents()
//	{
//		return service.getAllStudents();
//	}
	
	
//	2. Get All Students
	@GetMapping
	public ResponseEntity<List<Student>> getAllStudents(){

	    return ResponseEntity.ok(service.getAllStudents());

	}
	
	
	
//	3. Get student By Id (GET)
//	@GetMapping("/{id}")
//	public Student getStudentById(@PathVariable Long id)
//	{
//		return service.getStudentById(id);
//	}
	
	
	
//	3. Get student By ID
	@GetMapping("/{id}")
	public ResponseEntity<Student> getStudentById(@PathVariable Long id){

	    return ResponseEntity.ok(service.getStudentById(id));

	}
	
	
	
//	4. Update Student (PUT)
//	@PutMapping("/{id}")
//	public Student updateStudent(@PathVariable Long id, @RequestBody Student student)
//	{
//		return service.updateStudent(id, student);
//	}
	
	
//	4. update student
	@PutMapping("/{id}")
	public ResponseEntity<Student> updateStudent(
	        @PathVariable Long id,
	        @Valid @RequestBody Student student){

	    return ResponseEntity.ok(service.updateStudent(id,student));
	}
	
	
//	5. Delete Mapping (DELETE)
	// 5. Delete Student (DELETE)

//	@DeleteMapping("/{id}")
//	public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
//	    
//		service.deleteStudent(id);
//	    
//	    //return ResponseEntity.ok("Student Deleted Successfully!");
//		
//		return ResponseEntity.noContent().build();
//	    
//	    
//	}
	
	
//	5. Delete Mapping
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteStudent(@PathVariable Long id) {

	    service.deleteStudent(id);

	    return ResponseEntity.ok(
	            new ApiResponse(true, "Student Deleted Successfully!")
	    );
	}
	
	
//	6. Search by name
//	@GetMapping("/search/name")
//	public List<Student> searchByName(@RequestParam String name)
//	{
//		return service.searchByName(name);
//	}
	
	
//	6. Search By Name
	@GetMapping("/search/name")
	public ResponseEntity<List<Student>> searchByName(@RequestParam String name){

	    return ResponseEntity.ok(service.searchByName(name));

	}
	
	
//	7. Search by Email
//	@GetMapping("/search/email")
//	List<Student> searchByEmail(@RequestParam String email)
//	{
//		return service.searchByEmail(email);
//	}
	
//	7. Search By email
	
	@GetMapping("/search/email")
	public ResponseEntity<List<Student>> searchByEmail(@RequestParam String email){

	    return ResponseEntity.ok(service.searchByEmail(email));

	}
	
	
//	8. Search by Department
//	@GetMapping("/search/department")
//	public List<Student> searchByDepartment(@RequestParam String department)
//	{
//		return service.searchByDepartment(department);
//	}
	
	
//	8.Search by Department
	
	@GetMapping("/search/department")
	public ResponseEntity<List<Student>> searchByDepartment(@RequestParam String department){

	    return ResponseEntity.ok(service.searchByDepartment(department));

	}
	
//	9. Search by City
//	@GetMapping("/search/city")
//	public List<Student> searchByCity(@RequestParam String city)
//	{
//		return service.searchByCity(city);
//	}
	
	
//	9. Search by City
	@GetMapping("/search/city")
	public ResponseEntity<List<Student>> searchByCity(@RequestParam String city){

	    return ResponseEntity.ok(service.searchByCity(city));

	}
	
//	10. Pagination sorting ascending and descending order
//	@GetMapping("/page")
//	public Page<Student> getStudents(
//	        @RequestParam(defaultValue = "0") int page,
//	        @RequestParam(defaultValue = "5") int size,
//	        @RequestParam(defaultValue = "id") String sortBy,
//	        @RequestParam(defaultValue = "asc") String direction) {
//
//	    return service.getStudents(page, size, sortBy, direction);
//	}
//	
	
//	10. Pagination sorting ascending and descending order
	@GetMapping("/page")
	public ResponseEntity<Page<Student>> getStudents(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "id") String sortBy,
	        @RequestParam(defaultValue = "asc") String direction) {

	    return ResponseEntity.ok(
	            service.getStudents(page, size, sortBy, direction));
	}
	
	
	
	
//	logout

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpSession session) {

	    session.invalidate();

	    return ResponseEntity.ok("Logout Successful");

	}
	
	
//	sign up
//	@PostMapping("/signup")
//	public ResponseEntity<?> signup(@RequestBody Student student) {
//
//	    try {
//	        Student savedStudent = service.signup(student);
//	        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
//
//	    } catch (RuntimeException e) {
//	        return ResponseEntity.badRequest().body(e.getMessage());
//	    }
//	}
	
	
//	 Sign Up
	@PostMapping("/signup")
	public ResponseEntity<Student> signup(@Valid @RequestBody Student student){

	    Student saved = service.signup(student);

	    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

}
