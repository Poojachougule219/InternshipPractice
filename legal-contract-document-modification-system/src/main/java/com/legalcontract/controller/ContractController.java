package com.legalcontract.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legalcontract.entity.Contract;
import com.legalcontract.service.ContractService;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
	
	
	@Autowired
	private ContractService contractService;
	
	
//	CREATE
	@PostMapping
	public Contract createContract(@RequestBody Contract contract)
	{
		return contractService.createContract(contract);
	}
	
	
//	GET ALL CONTRACTS
	@GetMapping
	public List<Contract> getAllContracts()
	{
		return contractService.getAllContracts();
	}
	
	
//	GET CONTRACT BY ID
	@GetMapping("/{id}")
	public Contract getContractById(@PathVariable Long id)
	{
		return contractService.getContractById(id);
	}
	
	
//	UPDATE CONTRACT BY ID
	@PutMapping("/{id}")
	public Contract updateContract(@PathVariable Long id, @RequestBody Contract contract)
	{
		return contractService.updateContract(id, contract);
	}

	
//	DELETE CONTRACT
	@DeleteMapping("/{id}")
	public void deleteContract(@PathVariable Long id)
	{
		contractService.deleteContract(id);
	}
}
