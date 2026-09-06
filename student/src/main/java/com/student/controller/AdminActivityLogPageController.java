package com.student.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.student.entity.AuditLog;
import com.student.service.AuditLogService;

@Controller
public class AdminActivityLogPageController {

	@Autowired
	private AuditLogService auditLogService;

	// DISPLAY ADMIN ACTIVITY LOGS PAGE

	@GetMapping("/admin/activity-logs")
	public String activityLogsPage(

			@RequestParam(required = false) String search,

			@RequestParam(required = false) String action,

			@RequestParam(required = false) String role,

			@RequestParam(required = false) String fromDate,

			@RequestParam(required = false) String toDate,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "7") int size,

			Model model) {

		LocalDate from = null;
		LocalDate to = null;

		// From date
		if (fromDate != null && !fromDate.trim().isEmpty()) {
			from = LocalDate.parse(fromDate);
		}

		// To date
		if (toDate != null && !toDate.trim().isEmpty()) {
			to = LocalDate.parse(toDate);
		}

		// Fetch audit logs
		Page<AuditLog> logs = auditLogService.getAuditLogs(search, action, role, from, to, page, size);

		// Send logs to Thymeleaf
		model.addAttribute("auditLogs", logs.getContent());
		
		// Current page
		model.addAttribute("currentPage", logs.getNumber());

		// Total pages
		model.addAttribute("totalPages", logs.getTotalPages());

		// Total records
		model.addAttribute("totalLogs", logs.getTotalElements());

		// Page size
		model.addAttribute("pageSize", size);

		// Search/filter values
		model.addAttribute("search", search);
		model.addAttribute("action", action);
		model.addAttribute("role", role);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);

		return "Admin/activity_logs";
	}
}