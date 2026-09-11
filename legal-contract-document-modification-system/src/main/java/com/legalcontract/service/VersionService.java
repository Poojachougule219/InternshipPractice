package com.legalcontract.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.legalcontract.entity.Document;
import com.legalcontract.entity.User;
import com.legalcontract.entity.Version;
import com.legalcontract.repository.DocumentRepository;
import com.legalcontract.repository.UserRepository;
import com.legalcontract.repository.VersionRepository;

@Service
public class VersionService {

    @Autowired
    private VersionRepository versionRepo;

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private UserRepository userRepo;


    // CREATE VERSION
    public Version createVersion(Version version) {

        // Validate Document
        if (version.getDocument() == null ||
            version.getDocument().getId() == null) {

            throw new RuntimeException("Document ID is required");
        }

        Long documentId = version.getDocument().getId();

        Document document = documentRepo.findById(documentId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Document not found with id : " + documentId
                    ));

        version.setDocument(document);


        // Validate Created By User
        if (version.getCreatedBy() == null ||
            version.getCreatedBy().getId() == null) {

            throw new RuntimeException("Created By User ID is required");
        }

        Long userId = version.getCreatedBy().getId();

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "User not found with id : " + userId
                    ));

        version.setCreatedBy(user);


        return versionRepo.save(version);
    }


    // GET ALL VERSIONS
    public List<Version> getAllVersions() {
        return versionRepo.findAll();
    }


    // GET VERSION BY ID
    public Version getVersionById(Long id) {

        return versionRepo.findById(id)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Version not found with id : " + id
                    ));
    }


    // UPDATE VERSION BY ID
    public Version updateVersion(Long id, Version version) {

        Version existingVersion = getVersionById(id);

        // Validate Document
        if (version.getDocument() == null ||
            version.getDocument().getId() == null) {

            throw new RuntimeException("Document ID is required");
        }

        Document document = documentRepo.findById(
                version.getDocument().getId()
        ).orElseThrow(() ->
            new RuntimeException(
                "Document not found with id : "
                + version.getDocument().getId()
            )
        );

        existingVersion.setDocument(document);


        existingVersion.setVersionNumber(
                version.getVersionNumber()
        );

        existingVersion.setFilePath(
                version.getFilePath()
        );

        existingVersion.setChangeDescription(
                version.getChangeDescription()
        );


        // Validate Created By
        if (version.getCreatedBy() == null ||
            version.getCreatedBy().getId() == null) {

            throw new RuntimeException("Created By User ID is required");
        }

        User user = userRepo.findById(
                version.getCreatedBy().getId()
        ).orElseThrow(() ->
            new RuntimeException(
                "User not found with id : "
                + version.getCreatedBy().getId()
            )
        );

        existingVersion.setCreatedBy(user);

        existingVersion.setStatus(version.getStatus());

        return versionRepo.save(existingVersion);
    }


    // DELETE VERSION
    public void deleteVersion(Long id) {

        Version existingVersion = getVersionById(id);

        versionRepo.delete(existingVersion);
    }
}