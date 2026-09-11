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
@Table(name = "version")
public class Version {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "document_id")
	private Document document;
	
	@Column(name = "version_number")
	private Integer versionNumber;

	@Column(name = "file_path")
	private String filePath;

	@Column(name = "change_description")
	private String changeDescription;

	
	
	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "status")
	private String status;

	
	public Version() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Version(Long id, Document document, Integer versionNumber, String filePath, String changeDescription,
			User createdBy, LocalDateTime createdAt, String status) {
		super();
		this.id = id;
		this.document = document;
		this.versionNumber = versionNumber;
		this.filePath = filePath;
		this.changeDescription = changeDescription;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.status = status;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Document getDocument() {
		return document;
	}


	public void setDocument(Document document) {
		this.document = document;
	}


	public Integer getVersionNumber() {
		return versionNumber;
	}


	public void setVersionNumber(Integer versionNumber) {
		this.versionNumber = versionNumber;
	}


	public String getFilePath() {
		return filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	public String getChangeDescription() {
		return changeDescription;
	}


	public void setChangeDescription(String changeDescription) {
		this.changeDescription = changeDescription;
	}


	public User getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}


	public LocalDateTime getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	
	@PrePersist
	protected void onCreate() {
	    createdAt = LocalDateTime.now();
	}
	
	
	@Override
	public String toString() {
		return "Version [id=" + id + ", document=" + document + ", versionNumber=" + versionNumber + ", filePath="
				+ filePath + ", changeDescription=" + changeDescription + ", createdBy=" + createdBy + ", createdAt="
				+ createdAt + ", status=" + status + "]";
	}
	
	
	
	

}
