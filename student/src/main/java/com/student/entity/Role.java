package com.student.entity;

import com.student.audit.AuditFields;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    
    
    
    
    public Role() {
    }
   


	public Role(Long id, String name) {
		
		super();
		
        this.id = id;
        this.name = name;
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



	@Override
	public String toString() {
		return "Role [id=" + id + ", name=" + name + "]";
	}
    
    
    
}