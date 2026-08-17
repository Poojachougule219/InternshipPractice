package com.student.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.student.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long>{

	
	
	List<Student> findByNameContainingIgnoreCase(String name);
	
	List<Student> findByEmailContainingIgnoreCase(String email);
	
	List<Student> findByDepartmentContainingIgnoreCase(String department);
	
	List<Student> findByCityContainingIgnoreCase(String city);
	
	Student findByEmailAndPassword(String email, String password);
	
	boolean existsByEmail(String email);

}
