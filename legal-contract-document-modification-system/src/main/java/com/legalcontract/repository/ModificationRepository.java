package com.legalcontract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.legalcontract.entity.Modification;

public interface ModificationRepository extends JpaRepository <Modification, Long> {

	
	List<Modification> findByDocumentId(Long documentId);
	List<Modification> findByModifiedById(Long userId);
	List<Modification> findByContractId(Long contractId);
	List<Modification> findByStatus(String upperCase);
}
