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

import com.legalcontract.entity.Modification;
import com.legalcontract.service.ModificationService;

@RestController
@RequestMapping("/api/modifications")
public class ModificationController {
	
	
	@Autowired
	private ModificationService modificationService;
	
	
//	CREATE MODIFICATION
	@PostMapping
	public Modification createModification(@RequestBody Modification modification)
	{
		return modificationService.createModification(modification);
	}
	
	
//	GET ALL MODIFICATIONS
	@GetMapping
	public List<Modification> getAllModifications()
	{
		return modificationService.getAllModifications();
	}
	
	
//	GET MODIFICATION BY ID
	@GetMapping("/{id}")
	public Modification getModificationById(@PathVariable Long id)
	{
		return modificationService.getModificationById(id);
	}
	
	
//	UPDATE MODIFICATION
	@PutMapping("/{id}")
	public Modification updateModification(@PathVariable Long id, @RequestBody Modification modification)
	{
		return modificationService.updateModification(id, modification);
	}
	
	
//	DELETE MODIFICATION
	@DeleteMapping("/{id}")
	public void deleteModification(@PathVariable Long id)
	{
		modificationService.deleteModification(id);
	}

}
