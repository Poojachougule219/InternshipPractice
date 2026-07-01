package com.student.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.student.entity.Student;
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
				.orElseThrow(() -> new RuntimeException("Student not found with ID : "+id));
	}
	
	
//	4. Update Student
	public Student updateStudent(Long id, Student updateStudent)
	{
		Student student = getStudentById(id);
		student.setName(updateStudent.getName());
		student.setAge(updateStudent.getAge());
		student.setCourse(updateStudent.getCourse());
		student.setCity(updateStudent.getCity());
		
		return repo.save(student);
	}
	
//	5. delete Student
	public void deleteStudent(Long id)
	{
		repo.deleteById(id);
	}
}
