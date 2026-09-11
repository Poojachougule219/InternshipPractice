package com.legalcontract.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "modification")
public class Modification {


@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne
@JoinColumn(name = "contract_id")
private Contract contract;

@ManyToOne
@JoinColumn(name = "document_id")
private Document document;

@ManyToOne
@JoinColumn(name = "clause_id")
private Clause clause;

@ManyToOne
@JoinColumn(name = "modified_by")
private User modifiedBy;

@Column(name = "modification_type")
private String modificationType;

@Lob
@Column(name = "old_content", columnDefinition = "TEXT")
private String oldContent;

@Lob
@Column(name = "new_content", columnDefinition = "TEXT")
private String newContent;

@Column(name = "reason")
private String reason;

@Column(name = "status")
private String status;

@Column(name = "modified_at")
private LocalDateTime modifiedAt;


public Modification() {
	super();
	// TODO Auto-generated constructor stub
}


public Modification(Long id, Contract contract, Document document, Clause clause, User modifiedBy,
		String modificationType, String oldContent, String newContent, String reason, String status,
		LocalDateTime modifiedAt) {
	super();
	this.id = id;
	this.contract = contract;
	this.document = document;
	this.clause = clause;
	this.modifiedBy = modifiedBy;
	this.modificationType = modificationType;
	this.oldContent = oldContent;
	this.newContent = newContent;
	this.reason = reason;
	this.status = status;
	this.modifiedAt = modifiedAt;
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


public Document getDocument() {
	return document;
}


public void setDocument(Document document) {
	this.document = document;
}


public Clause getClause() {
	return clause;
}


public void setClause(Clause clause) {
	this.clause = clause;
}


public User getModifiedBy() {
	return modifiedBy;
}


public void setModifiedBy(User modifiedBy) {
	this.modifiedBy = modifiedBy;
}


public String getModificationType() {
	return modificationType;
}


public void setModificationType(String modificationType) {
	this.modificationType = modificationType;
}


public String getOldContent() {
	return oldContent;
}


public void setOldContent(String oldContent) {
	this.oldContent = oldContent;
}


public String getNewContent() {
	return newContent;
}


public void setNewContent(String newContent) {
	this.newContent = newContent;
}


public String getReason() {
	return reason;
}


public void setReason(String reason) {
	this.reason = reason;
}


public String getStatus() {
	return status;
}


public void setStatus(String status) {
	this.status = status;
}


public LocalDateTime getModifiedAt() {
	return modifiedAt;
}


public void setModifiedAt(LocalDateTime modifiedAt) {
	this.modifiedAt = modifiedAt;
}



@PrePersist
protected void onCreate() {
    modifiedAt = LocalDateTime.now();
}


@Override
public String toString() {
	return "Modification [id=" + id + ", contract=" + contract + ", document=" + document + ", clause=" + clause
			+ ", modifiedBy=" + modifiedBy + ", modificationType=" + modificationType + ", oldContent=" + oldContent
			+ ", newContent=" + newContent + ", reason=" + reason + ", status=" + status + ", modifiedAt=" + modifiedAt
			+ "]";
}




}
