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

import com.legalcontract.entity.Version;
import com.legalcontract.service.VersionService;

@RestController
@RequestMapping("/api/versions")
public class VersionController {
	
	
	@Autowired
	private VersionService versionService;
	
	
//	CREATE VERSION
	@PostMapping
	public Version createVersion(@RequestBody Version version)
	{
		return versionService.createVersion(version);
	}
	
	
//	GET ALL VERSIONS
	@GetMapping
	public List<Version> getAllVersions()
	{
		return versionService.getAllVersions();
	}
	
	
//	GET VERSION BY ID
	@GetMapping("/{id}")
	public Version getVersionById(@PathVariable Long id)
	{
		return versionService.getVersionById(id);
	}
	
	
//	UPDATE VERSION
	@PutMapping("/{id}")
	public Version updateVersion(@PathVariable Long id, @RequestBody Version version)
	{
		return versionService.updateVersion(id, version);
	}
	
	
//	DELETE VERSION
	@DeleteMapping("/{id}")
	public void deleteVersion(@PathVariable Long id)
	{
		versionService.deleteVersion(id);
	}
	
	
	

}
