package com.legalcontract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.legalcontract.entity.Clause;
import com.legalcontract.entity.Contract;

public interface ClauseRepository extends JpaRepository<Clause, Long> {

    List<Clause> findByContract(Contract contract);

    List<Clause> findByContractOrderByClauseOrderAsc(Contract contract);

    Integer findMaxClauseOrderByContract(Contract contract);
}