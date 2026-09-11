package com.legalcontract.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.service.ContractService;
import com.legalcontract.service.DocumentService;

@Controller
public class DocumentPageController {

    private final DocumentService documentService;
    private final ContractService contractService;

    public DocumentPageController(DocumentService documentService,
                                  ContractService contractService) {
        this.documentService = documentService;
        this.contractService = contractService;
    }

    @GetMapping("/contract/documents/{contractId}")
    public String documentList(@PathVariable Long contractId,
                               Model model) {

        Contract contract = contractService.getContractById(contractId);

        List<Document> documents =
                documentService.getDocumentsByContract(contractId);

        model.addAttribute("contract", contract);
        model.addAttribute("documents", documents);

        return "Document/document-list";
    }

    @GetMapping("/contract/documents/upload/{contractId}")
    public String uploadDocumentPage(@PathVariable Long contractId,
                                     Model model) {

        Contract contract = contractService.getContractById(contractId);

        model.addAttribute("contract", contract);
        model.addAttribute("document", new Document());

        return "Document/document-upload";
    }

    @PostMapping("/contract/documents/upload")
    public String uploadDocument(
            @RequestParam("contractId") Long contractId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentName") String documentName,
            RedirectAttributes redirectAttributes) {

        try {

            documentService.uploadDocument(
                    contractId,
                    file,
                    documentName
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Document uploaded successfully."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to upload document: " + e.getMessage()
            );
        }

        return "redirect:/contract/documents/" + contractId;
    }

    @GetMapping("/contract/documents/view/{id}")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long id) {

        try {

            Document document = documentService.getDocumentById(id);

            if (document == null) {
                System.out.println("DOCUMENT NOT FOUND FOR ID = " + id);
                return ResponseEntity.notFound().build();
            }

            System.out.println(
                    "FILE PATH FROM DB = " + document.getFilePath()
            );

            if (document.getFilePath() == null ||
                    document.getFilePath().trim().isEmpty()) {

                System.out.println("FILE PATH IS EMPTY");
                return ResponseEntity.notFound().build();
            }

            Path filePath = resolveFilePath(document.getFilePath());

            System.out.println(
                    "RESOLVED FILE PATH = " + filePath
            );

            System.out.println(
                    "FILE EXISTS = " + Files.exists(filePath)
            );

            System.out.println(
                    "FILE READABLE = " + Files.isReadable(filePath)
            );

            if (!Files.exists(filePath)) {
                System.out.println(
                        "FILE DOES NOT EXIST = " + filePath
                );

                return ResponseEntity.notFound().build();
            }

            if (!Files.isReadable(filePath)) {
                System.out.println(
                        "FILE IS NOT READABLE = " + filePath
                );

                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {

                System.out.println(
                        "RESOURCE DOES NOT EXIST OR IS NOT READABLE"
                );

                return ResponseEntity.notFound().build();
            }

            String contentType = document.getFileType();

            if (contentType == null ||
                    contentType.trim().isEmpty()) {

                contentType = Files.probeContentType(filePath);
            }

            if (contentType == null ||
                    contentType.trim().isEmpty()) {

                contentType =
                        MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            MediaType mediaType;

            try {

                mediaType =
                        MediaType.parseMediaType(contentType);

            } catch (Exception e) {

                mediaType =
                        MediaType.APPLICATION_OCTET_STREAM;
            }

            String fileName = document.getFileName();

            if (fileName == null ||
                    fileName.trim().isEmpty()) {

                fileName = "document.pdf";
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileName + "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/contract/documents/download/{id}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id) {

        try {

            Document document = documentService.getDocumentById(id);

            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            if (document.getFilePath() == null ||
                    document.getFilePath().trim().isEmpty()) {

                return ResponseEntity.notFound().build();
            }

            Path filePath =
                    resolveFilePath(document.getFilePath());

            System.out.println(
                    "DOWNLOAD FILE PATH = " + filePath
            );

            System.out.println(
                    "DOWNLOAD FILE EXISTS = " + Files.exists(filePath)
            );

            if (!Files.exists(filePath) ||
                    !Files.isReadable(filePath)) {

                return ResponseEntity.notFound().build();
            }

            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists() ||
                    !resource.isReadable()) {

                return ResponseEntity.notFound().build();
            }

            String contentType =
                    document.getFileType();

            if (contentType == null ||
                    contentType.trim().isEmpty()) {

                contentType =
                        Files.probeContentType(filePath);
            }

            if (contentType == null ||
                    contentType.trim().isEmpty()) {

                contentType =
                        MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            MediaType mediaType;

            try {

                mediaType =
                        MediaType.parseMediaType(contentType);

            } catch (Exception e) {

                mediaType =
                        MediaType.APPLICATION_OCTET_STREAM;
            }

            String fileName =
                    document.getFileName();

            if (fileName == null ||
                    fileName.trim().isEmpty()) {

                fileName = "document.pdf";
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/contract/documents/delete/{id}")
    public String deleteDocument(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            Document document =
                    documentService.getDocumentById(id);

            if (document == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Document not found."
                );

                return "redirect:/contract/contracts";
            }

            Long contractId =
                    document.getContract().getId();

            documentService.deleteDocument(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Document deleted successfully."
            );

            return "redirect:/contract/documents/" + contractId;

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to delete document: " + e.getMessage()
            );

            return "redirect:/contract/contracts";
        }
    }

    @GetMapping("/details/{id}")
    public String documentDetails(
            @PathVariable Long id,
            Model model) {

        Document document =
                documentService.getDocumentById(id);

        model.addAttribute("document", document);

        return "Document/document-view";
    }

    private Path resolveFilePath(String storedPath) {

        String normalizedPath =
                storedPath.trim().replace("\\", "/");

        while (normalizedPath.startsWith("/")) {
            normalizedPath =
                    normalizedPath.substring(1);
        }

        Path relativePath =
                Paths.get(normalizedPath);

        Path workingDirectory =
                Paths.get(
                        System.getProperty("user.dir")
                ).toAbsolutePath().normalize();

        Path resolvedPath =
                workingDirectory
                        .resolve(relativePath)
                        .normalize();

        System.out.println(
                "WORKING DIRECTORY = " + workingDirectory
        );

        System.out.println(
                "RELATIVE FILE PATH = " + relativePath
        );

        return resolvedPath;
    }
}