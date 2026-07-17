package com.student.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.student.entity.Student;
import com.student.exception.EmailAlreadyExistsException;
import com.student.exception.ResourceNotFoundException;
import com.student.repository.StudentRepository;

@Service
public class StudentService {

	
	@Autowired
	private StudentRepository repo;
	
//	1. add Student
	public Student addStudent(Student student)
	{
		return repo.save(student);
	}
	
	
//	2. Get All Students
	public List<Student> getAllStudents()
	{
		return repo.findAll();
	}
	
	
//	3. Get student By Id
	public Student getStudentById(Long id)
	{
		return repo.findById(id)
				.orElseThrow(() ->
				new ResourceNotFoundException("Student not found with id : "+id));
	}
	
	
//	4. Update Student
	public Student updateStudent(Long id, Student updateStudent)
	{
		Student student = getStudentById(id);
		student.setName(updateStudent.getName());
		student.setAge(updateStudent.getAge());
		student.setDepartment(updateStudent.getDepartment());
		student.setEmail(updateStudent.getEmail());
		student.setCity(updateStudent.getCity());
		student.setPassword(updateStudent.getPassword());
		
		return repo.save(student);
	}
	
	
//	5. delete Student
//	public void deleteStudent(Long id)
//	{
//		repo.deleteById(id);
//	}
	
	
	
//	5. delete student
	public void deleteStudent(Long id) {

	    Student student = getStudentById(id);

	    repo.delete(student);

	}
	
	
	
	
	
//6. Search by Name
	public List<Student> searchByName(String name)
	{
		return repo.findByNameContainingIgnoreCase(name);
	}
	
	
//7. Search by Email
	public List<Student> searchByEmail(String email)
	{
		return repo.findByEmailContainingIgnoreCase(email);
	}
	
	
//	8. Search by Department
	public List<Student> searchByDepartment(String department)
	{
		return repo.findByDepartmentContainingIgnoreCase(department);
	}
	
//	9. Search by city
	public List<Student> searchByCity(String city)
	{
		return repo.findByCityContainingIgnoreCase(city);
	}
	
	
	
//	10. // Pagination + Sorting
	
	
	public Page<Student> getStudents(int page, int size, String sortBy, String direction) {

	    Sort sort = direction.equalsIgnoreCase("desc")
	            ? Sort.by(sortBy).descending()
	            : Sort.by(sortBy).ascending();

	    Pageable pageable = PageRequest.of(page, size, sort);

	    return repo.findAll(pageable);
	}
	
	
	
//	for login email and password
	public Student login(String email , String password)

	{
		return repo.findByEmailAndPassword(email, password);
	}
	
	
	
//	for sign up
	// Signup
	public Student signup(Student student) {

	    if (repo.existsByEmail(student.getEmail())) {
	        
	    	throw new EmailAlreadyExistsException("Email already exists");
	    }

	    return repo.save(student);
	}
}


