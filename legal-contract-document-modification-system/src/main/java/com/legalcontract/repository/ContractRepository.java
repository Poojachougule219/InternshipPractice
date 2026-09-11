package com.legalcontract.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.legalcontract.entity.Contract;

public interface ContractRepository extends JpaRepository <Contract, Long> {

}
