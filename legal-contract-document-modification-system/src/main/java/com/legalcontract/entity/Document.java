package com.legalcontract.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "document")
public class Document {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "contract_id")
	private Contract contract;
	
	@Column(name = "documentName")
	private String documentName;
	
	@Column(name = "fileName")
	private String fileName;
	
	@Column(name = "filePath")
	private String filePath;
	
	@Column(name = "fileType")
	private String fileType;
	
	@Column(name = "fileSize")
	private Long fileSize;
	
	@Column(name = "status")
	private String status;
	
	@ManyToOne
	@JoinColumn(name = "uploaded_by")
	private User uploadedBy;
	
	@Column(name = "uploadedAt")
	private LocalDateTime uploadedAt;
	

	public Document() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Document(Long id, Contract contract, String documentName, String fileName, String filePath, String fileType,
			Long fileSize, String status, User uploadedBy, LocalDateTime uploadedAt) {
		super();
		this.id = id;
		this.contract = contract;
		this.documentName = documentName;
		this.fileName = fileName;
		this.filePath = filePath;
		this.fileType = fileType;
		this.fileSize = fileSize;
		this.status = status;
		this.uploadedBy = uploadedBy;
		this.uploadedAt = uploadedAt;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Contract getContract() {
		return contract;
	}


	public void setContract(Contract contract) {
		this.contract = contract;
	}


	public String getDocumentName() {
		return documentName;
	}


	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getFilePath() {
		return filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	public String getFileType() {
		return fileType;
	}


	public void setFileType(String fileType) {
		this.fileType = fileType;
	}


	public Long getFileSize() {
		return fileSize;
	}


	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public User getUploadedBy() {
		return uploadedBy;
	}


	public void setUploadedBy(User uploadedBy) {
		this.uploadedBy = uploadedBy;
	}


	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}


	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}


	@Override
	public String toString() {
		return "Document [id=" + id + ", contract=" + contract + ", documentName=" + documentName + ", fileName="
				+ fileName + ", filePath=" + filePath + ", fileType=" + fileType + ", fileSize=" + fileSize
				+ ", status=" + status + ", uploadedBy=" + uploadedBy + ", uploadedAt=" + uploadedAt + "]";
	}
	
	
	
	
}
