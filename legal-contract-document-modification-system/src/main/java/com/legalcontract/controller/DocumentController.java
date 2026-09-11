package com.legalcontract.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legalcontract.entity.Document;
import com.legalcontract.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;


    // =========================================================
    // GET DOCUMENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Document getDocumentById(
            @PathVariable Long id) {

        return documentService.getDocumentById(id);
    }


    // =========================================================
    // GET DOCUMENTS BY CONTRACT
    // =========================================================

    @GetMapping("/contract/{contractId}")
    public List<Document> getDocumentsByContract(
            @PathVariable Long contractId) {

        return documentService
                .getDocumentsByContract(contractId);
    }


    // =========================================================
    // DELETE DOCUMENT
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteDocument(
            @PathVariable Long id) {

        documentService.deleteDocument(id);
    }
}