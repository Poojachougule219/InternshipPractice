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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.legalcontract.entity.Clause;
import com.legalcontract.service.ClauseService;

@RestController
@RequestMapping("/api/clauses")
public class ClauseController {

    @Autowired
    private ClauseService clauseService;


    // =========================================================
    // CREATE CLAUSE
    // =========================================================

    @PostMapping
    public Clause createClause(
            @RequestBody Clause clause,
            @RequestParam Long contractId,
            @RequestParam Long documentId) {

        return clauseService.createClause(
                clause,
                contractId,
                documentId
        );
    }


    // =========================================================
    // GET ALL CLAUSES
    // =========================================================

    @GetMapping
    public List<Clause> getAllClauses() {

        return clauseService.getAllClauses();
    }


    // =========================================================
    // GET ALL CLAUSES FOR SPECIFIC CONTRACT
    // =========================================================

    @GetMapping("/contract/{contractId}")
    public List<Clause> getClausesByContract(
            @PathVariable Long contractId) {

        return clauseService.getClausesByContract(
                contractId
        );
    }


    // =========================================================
    // GET CLAUSE BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Clause getClauseById(
            @PathVariable Long id) {

        return clauseService.getClauseById(id);
    }


    // =========================================================
    // UPDATE CLAUSE
    // =========================================================

    @PutMapping("/{id}")
    public Clause updateClause(
            @PathVariable Long id,
            @RequestBody Clause clause,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) Long documentId) {

        return clauseService.updateClause(
                id,
                clause,
                contractId,
                documentId
        );
    }


    // =========================================================
    // DELETE CLAUSE
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteClause(
            @PathVariable Long id) {

        clauseService.deleteClause(id);
    }
}