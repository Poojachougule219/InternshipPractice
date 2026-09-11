package com.legalcontract.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.legalcontract.entity.Document;
import com.legalcontract.entity.Version;
import com.legalcontract.repository.DocumentRepository;
import com.legalcontract.repository.VersionRepository;

@Controller
@RequestMapping("/contract/versions")
public class VersionPageController {


@Autowired
private VersionRepository versionRepository;

@Autowired
private DocumentRepository documentRepository;

@GetMapping("/document/{documentId}")
public String versionHistory(
        @PathVariable Long documentId,
        Model model) {

    Document document = documentRepository.findById(documentId).orElse(null);

    if (document == null) {
        return "redirect:/contract/contracts";
    }

    List<Version> versions = versionRepository.findAll()
            .stream()
            .filter(version ->
                    version.getDocument() != null &&
                    version.getDocument().getId() != null &&
                    version.getDocument().getId().equals(documentId))
            .sorted(Comparator.comparing(
                    Version::getVersionNumber,
                    Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

    model.addAttribute("document", document);
    model.addAttribute("contract", document.getContract());
    model.addAttribute("versions", versions);

    return "Version/version-history";
}

@GetMapping("/view/{id}")
public String viewVersion(
        @PathVariable Long id,
        Model model) {

    Version version = versionRepository.findById(id).orElse(null);

    if (version == null) {
        return "redirect:/contract/contracts";
    }

    model.addAttribute("version", version);
    model.addAttribute("document", version.getDocument());

    if (version.getDocument() != null) {
        model.addAttribute("contract", version.getDocument().getContract());
    }

    return "Version/version-view";
}


}
