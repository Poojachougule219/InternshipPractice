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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "clause")
public class Clause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "clause_number")
    private String clauseNumber;

    @Column(name = "clause_order")
    private Integer clauseOrder;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "clause_type")
    private String clauseType;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Clause() {
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

    public String getClauseNumber() {
        return clauseNumber;
    }

    public void setClauseNumber(String clauseNumber) {
        this.clauseNumber = clauseNumber;
    }

    public Integer getClauseOrder() {
        return clauseOrder;
    }

    public void setClauseOrder(Integer clauseOrder) {
        this.clauseOrder = clauseOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClauseType() {
        return clauseType;
    }

    public void setClauseType(String clauseType) {
        this.clauseType = clauseType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Clause [id=" + id
                + ", contract=" + contract
                + ", document=" + document
                + ", clauseNumber=" + clauseNumber
                + ", clauseOrder=" + clauseOrder
                + ", title=" + title
                + ", content=" + content
                + ", clauseType=" + clauseType
                + ", status=" + status
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + "]";
    }
}