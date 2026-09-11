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

import com.legalcontract.entity.AuditLog;
import com.legalcontract.service.AuditLogService;

@RestController
@RequestMapping("/api/auditlogs")
public class AuditLogController {
	
	
	@Autowired
	private AuditLogService auditLogService;
	
	
//	CREATE
	@PostMapping
	public AuditLog createAuditLog(@RequestBody AuditLog auditLog)
	{
		return auditLogService.createAuditLog(auditLog);
	}
	
	
//	GET ALL AUDITLOGS
	@GetMapping
	public List<AuditLog> getAllAuditLogs()
	{
		return auditLogService.getAllAuditLogs();
	}
	
	
//	GET AUDITLOG BY ID
	@GetMapping("/{id}")
	public AuditLog getAuditLogById(@PathVariable Long id)
	{
		return auditLogService.getAuditLogById(id);
	}
	
	
//	UPDATE AUDITLOG
	@PutMapping("/{id}")
	public AuditLog updateAuditLog(@PathVariable Long id, @RequestBody AuditLog auditLog)
	{
		return auditLogService.updateAuditLog(id, auditLog);
	}
	
	
//	DELETE AUDIT LOG
	@DeleteMapping("/{id}")
	public void deleteAuditLog(@PathVariable Long id)
	{
	auditLogService.deleteAuditLog(id);
	}
	

}
