package com.legalcontract.entity;

import java.time.LocalDate;
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
@Table(name = "contract")
public class Contract {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "contractNumber")
	private String contractNumber;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "contractType")
	private String contractType;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "startDate")
	private LocalDate startDate;
	
	@Column(name = "endDate")
	private LocalDate endDate;
	
	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;
	
	@Column(name = "createdAt")
	private LocalDateTime createdAt;
	
	@Column(name = "updatedAt")
	private LocalDateTime updatedAt;
	
	
	public Contract() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Contract(Long id, String contractNumber, String title, String description, String contractType,
			String status, LocalDate startDate, LocalDate endDate, User createdBy, LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.contractNumber = contractNumber;
		this.title = title;
		this.description = description;
		this.contractType = contractType;
		this.status = status;
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getContractNumber() {
		return contractNumber;
	}


	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getContractType() {
		return contractType;
	}


	public void setContractType(String contractType) {
		this.contractType = contractType;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public LocalDate getStartDate() {
		return startDate;
	}


	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}


	public LocalDate getEndDate() {
		return endDate;
	}


	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
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


	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}


	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}


	@Override
	public String toString() {
		return "Contract [id=" + id + ", contractNumber=" + contractNumber + ", title=" + title + ", description="
				+ description + ", contractType=" + contractType + ", status=" + status + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", createdBy=" + createdBy + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}
	
	
	
}
