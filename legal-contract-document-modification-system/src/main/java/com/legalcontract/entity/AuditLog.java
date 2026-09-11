package com.legalcontract.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
public class AuditLog {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(name = "action")
	private String action;
	
	@Column(name = "entity_name")
	private String entityName;

	@Column(name = "entity_id")
	private Long entityId;

	
	@Column(name = "description")
	private String description;
	
	@Column(name = "ip_address")
	private String ipAddress;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	

	public AuditLog() {
		super();
		// TODO Auto-generated constructor stub
	}


	public AuditLog(Long id, User user, String action, String entityName, Long entityId, String description,
			String ipAddress, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.user = user;
		this.action = action;
		this.entityName = entityName;
		this.entityId = entityId;
		this.description = description;
		this.ipAddress = ipAddress;
		this.createdAt = createdAt;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}


	public String getEntityName() {
		return entityName;
	}


	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}


	public Long getEntityId() {
		return entityId;
	}


	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getIpAddress() {
		return ipAddress;
	}


	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	public LocalDateTime getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	
	@PrePersist
	protected void onCreate() {
	    createdAt = LocalDateTime.now();
	}
	

	@Override
	public String toString() {
		return "AuditLog [id=" + id + ", user=" + user + ", action=" + action + ", entityName=" + entityName
				+ ", entityId=" + entityId + ", description=" + description + ", ipAddress=" + ipAddress
				+ ", createdAt=" + createdAt + "]";
	}
	
	
	
}
