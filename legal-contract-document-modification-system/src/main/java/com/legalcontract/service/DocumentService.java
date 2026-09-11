package com.legalcontract.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.User;
import com.legalcontract.repository.DocumentRepository;
import com.legalcontract.repository.UserRepository;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final ContractService contractService;

    private final UserRepository userRepository;

    /*
     * Uploaded files are stored outside the database.
     *
     * Folder:
     * uploads/contracts/
     *
     * The folder is automatically created when
     * the first document is uploaded.
     */
    private final Path uploadDirectory =
            Paths.get("uploads", "contracts");

    public DocumentService(
            DocumentRepository documentRepository,
            ContractService contractService,
            UserRepository userRepository) {

        this.documentRepository = documentRepository;
        this.contractService = contractService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // UPLOAD DOCUMENT
    // =========================================================

    public Document uploadDocument(
            Long contractId,
            MultipartFile file,
            String documentName) throws IOException {

        // -----------------------------------------------------
        // Validate file
        // -----------------------------------------------------

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Please select a PDF document."
            );
        }

        // -----------------------------------------------------
        // Validate document name
        // -----------------------------------------------------

        if (documentName == null
                || documentName.trim().isEmpty()) {

            throw new RuntimeException(
                    "Please enter document name."
            );
        }

        // -----------------------------------------------------
        // Validate contract ID
        // -----------------------------------------------------

        if (contractId == null) {

            throw new RuntimeException(
                    "Contract ID is required."
            );
        }

        // -----------------------------------------------------
        // Get contract
        // -----------------------------------------------------

        Contract contract =
                contractService.getContractById(contractId);

        if (contract == null) {

            throw new RuntimeException(
                    "Contract not found with id: " + contractId
            );
        }

        // -----------------------------------------------------
        // ONLY ONE CURRENT PDF PER CONTRACT
        //
        // Historical documents with status ACTIVE
        // do not block a new upload.
        // Only the current UPLOADED document blocks it.
        // -----------------------------------------------------

        if (documentRepository.existsByContractIdAndStatus(
                contractId,
                "UPLOADED")) {

            throw new RuntimeException(
                    "A PDF document is already uploaded for this contract."
            );
        }

        // -----------------------------------------------------
        // Validate file name
        // -----------------------------------------------------

        String originalFileName =
                file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.trim().isEmpty()) {

            throw new RuntimeException(
                    "Invalid file name."
            );
        }

        // -----------------------------------------------------
        // Remove path information
        // -----------------------------------------------------

        originalFileName =
                Paths.get(originalFileName)
                        .getFileName()
                        .toString();

        // -----------------------------------------------------
        // Allow ONLY PDF
        // -----------------------------------------------------

        String lowerFileName =
                originalFileName.toLowerCase();

        if (!lowerFileName.endsWith(".pdf")) {

            throw new RuntimeException(
                    "Only PDF files are allowed."
            );
        }

        // -----------------------------------------------------
        // Maximum file size: 10 MB
        // -----------------------------------------------------

        long maxFileSize =
                10L * 1024L * 1024L;

        if (file.getSize() > maxFileSize) {

            throw new RuntimeException(
                    "File size must not exceed 10 MB."
            );
        }

        // -----------------------------------------------------
        // Create upload directory
        // -----------------------------------------------------

        Files.createDirectories(
                uploadDirectory
        );

        // -----------------------------------------------------
        // PDF extension
        // -----------------------------------------------------

        String extension = ".pdf";

        // -----------------------------------------------------
        // Generate unique stored filename
        // -----------------------------------------------------

        String storedFileName =
                UUID.randomUUID().toString()
                        + extension;

        Path targetPath =
                uploadDirectory.resolve(
                        storedFileName
                );

        // -----------------------------------------------------
        // Save physical file
        // -----------------------------------------------------

        Files.copy(
                file.getInputStream(),
                targetPath
        );

        try {

            // =================================================
            // CREATE NEW DOCUMENT
            // =================================================

            Document document =
                    new Document();

            document.setContract(
                    contract
            );

            document.setDocumentName(
                    documentName.trim()
            );

            document.setFileName(
                    originalFileName
            );

            document.setFilePath(
                    targetPath.toString()
            );

            // -------------------------------------------------
            // Store MIME type
            // -------------------------------------------------

            String contentType =
                    file.getContentType();

            if (contentType == null
                    || contentType.trim().isEmpty()
                    || !"application/pdf".equalsIgnoreCase(contentType)) {

                contentType = "application/pdf";
            }

            document.setFileType(
                    contentType
            );

            // -------------------------------------------------
            // Store file size
            // -------------------------------------------------

            document.setFileSize(
                    file.getSize()
            );

            // -------------------------------------------------
            // Document status
            // -------------------------------------------------

            document.setStatus(
                    "UPLOADED"
            );

            // -------------------------------------------------
            // Upload date/time
            // -------------------------------------------------

            document.setUploadedAt(
                    LocalDateTime.now()
            );

            // -------------------------------------------------
            // Get currently logged-in user
            // -------------------------------------------------

            User currentUser =
                    getCurrentUser();

            if (currentUser != null) {

                document.setUploadedBy(
                        currentUser
                );
            }

            // =================================================
            // SAVE DOCUMENT
            // =================================================

            return documentRepository.save(
                    document
            );

        } catch (Exception e) {

            /*
             * If saving the database record fails,
             * remove the newly uploaded physical file.
             */

            try {

                Files.deleteIfExists(
                        targetPath
                );

            } catch (IOException ignored) {

                // Ignore cleanup exception
            }

            throw e;
        }
    }

    // =========================================================
    // GET ALL DOCUMENTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Document> getAllDocuments() {

        return documentRepository.findAll();
    }

    // =========================================================
    // GET DOCUMENTS BY CONTRACT
    // =========================================================

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByContract(
            Long contractId) {

        return documentRepository
                .findByContractIdOrderByUploadedAtDesc(
                        contractId
                );
    }

    // =========================================================
    // GET DOCUMENT BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {

        return documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Document not found with id: "
                                        + id
                        )
                );
    }

    // =========================================================
    // DELETE DOCUMENT
    // =========================================================

    public void deleteDocument(Long id) {

        Document document =
                getDocumentById(id);

        /*
         * Delete physical file.
         */

        if (document.getFilePath() != null
                && !document.getFilePath()
                        .trim()
                        .isEmpty()) {

            try {

                Path filePath =
                        Paths.get(
                                document.getFilePath()
                        );

                Files.deleteIfExists(
                        filePath
                );

            } catch (IOException e) {

                System.err.println(
                        "Unable to delete physical file: "
                                + e.getMessage()
                );
            }
        }

        /*
         * Delete database record.
         */

        documentRepository.delete(
                document
        );
    }

    // =========================================================
    // GET CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getPrincipal()
                )) {

            return null;
        }

        String username =
                authentication.getName();

        return userRepository
                .findByUsername(username)
                .orElse(null);
    }

}