package com.legalcontract.controller;

import com.legalcontract.entity.Clause;
import com.legalcontract.entity.Contract;
import com.legalcontract.entity.Document;
import com.legalcontract.entity.Modification;
import com.legalcontract.entity.User;
import com.legalcontract.repository.DocumentRepository;
import com.legalcontract.service.ClauseService;
import com.legalcontract.service.ContractService;
import com.legalcontract.service.ModificationService;
import com.legalcontract.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/contract/modifications")
public class ModificationPageController {


@Autowired
private ModificationService modificationService;

@Autowired
private ContractService contractService;

@Autowired
private ClauseService clauseService;

@Autowired
private DocumentRepository documentRepository;

@Autowired
private UserService userService;

@GetMapping
public String listModifications(Model model) {

    List<Modification> modifications =
            modificationService.getAllModifications();

    model.addAttribute("modifications", modifications);

    return "Modification/modification-list";
}

@GetMapping("/add")
public String showAddModificationForm(Model model) {

    Modification modification = new Modification();

    modification.setModificationType("CONTRACT");
    modification.setStatus("PENDING");

    List<Contract> contracts =
            contractService.getAllContracts();

    model.addAttribute("modification", modification);
    model.addAttribute("contracts", contracts);
    model.addAttribute("clauses", Collections.emptyList());
    model.addAttribute("documents", Collections.emptyList());

    return "Modification/modification-add";
}

@GetMapping("/add/{contractId}")
public String showAddModificationFormForContract(
        @PathVariable Long contractId,
        Model model) {

    Contract contract =
            contractService.getContractById(contractId);

    if (contract == null) {
        return "redirect:/contract/modifications";
    }

    List<Clause> clauses =
            clauseService.getClausesByContract(contractId);

    List<Document> documents =
            documentRepository
                    .findByContractIdOrderByUploadedAtDesc(
                            contractId);

    Modification modification =
            new Modification();

    modification.setContract(contract);
    modification.setModificationType("CONTRACT");
    modification.setStatus("PENDING");

    model.addAttribute("modification", modification);
    model.addAttribute("contract", contract);
    model.addAttribute(
            "contracts",
            contractService.getAllContracts());
    model.addAttribute("clauses", clauses);
    model.addAttribute("documents", documents);

    return "Modification/modification-add";
}

@GetMapping("/contract/{contractId}/clauses")
@ResponseBody
public List<Clause> getClausesByContract(
        @PathVariable Long contractId) {

    return clauseService.getClausesByContract(contractId);
}

@GetMapping("/contract/{contractId}/documents")
@ResponseBody
public List<Document> getDocumentsByContract(
        @PathVariable Long contractId) {

    return documentRepository
            .findByContractIdOrderByUploadedAtDesc(
                    contractId);
}

@PostMapping("/save")
public String saveModification(
        @ModelAttribute Modification modification,
        @RequestParam(required = false) Long contractId,
        @RequestParam(required = false) Long clauseId,
        @RequestParam(required = false) Long documentId,
        Authentication authentication) {

    if (contractId == null) {
        return "redirect:/contract/modifications/add";
    }

    Contract contract =
            contractService.getContractById(contractId);

    if (contract == null) {
        return "redirect:/contract/modifications/add";
    }

    modification.setContract(contract);

    if (clauseId != null) {

        Clause clause =
                clauseService.getClauseById(clauseId);

        modification.setClause(clause);

    } else {

        modification.setClause(null);
    }

    if (documentId != null) {

        Document document =
                documentRepository
                        .findById(documentId)
                        .orElse(null);

        modification.setDocument(document);

    } else {

        modification.setDocument(null);
    }

    String modificationType =
            modification.getModificationType();

    if (modificationType == null
            || modificationType.isBlank()) {

        if (modification.getClause() != null) {
            modificationType = "CLAUSE";
        } else {
            modificationType = "CONTRACT";
        }
    }

    modificationType =
            modificationType.trim().toUpperCase();

    modification.setModificationType(
            modificationType);

    if ("CONTRACT".equals(modificationType)) {
        modification.setClause(null);
    }

    modification.setStatus("PENDING");

    if (authentication == null
            || authentication.getName() == null
            || authentication.getName().isBlank()
            || "anonymousUser".equalsIgnoreCase(
                    authentication.getName())) {

        return "redirect:/login";
    }

    String username =
            authentication.getName().trim();

    User currentUser =
            userService.getUserByUsername(username);

    if (currentUser == null) {
        return "redirect:/login";
    }

    modification.setModifiedBy(currentUser);

    modificationService.createModification(
            modification);

    return "redirect:/contract/modifications";
}

@GetMapping("/view/{id}")
public String viewModification(
        @PathVariable Long id,
        Model model) {

    Modification modification =
            modificationService.getModificationById(id);

    if (modification == null) {
        return "redirect:/contract/modifications";
    }

    model.addAttribute(
            "modification",
            modification);

    return "Modification/modification-view";
}

@GetMapping("/edit/{id}")
public String editModification(
        @PathVariable Long id,
        Model model) {

    Modification modification =
            modificationService.getModificationById(id);

    if (modification == null) {
        return "redirect:/contract/modifications";
    }

    List<Contract> contracts =
            contractService.getAllContracts();

    List<Clause> clauses =
            Collections.emptyList();

    List<Document> documents =
            Collections.emptyList();

    if (modification.getContract() != null) {

        Long contractId =
                modification.getContract().getId();

        clauses =
                clauseService.getClausesByContract(
                        contractId);

        documents =
                documentRepository
                        .findByContractIdOrderByUploadedAtDesc(
                                contractId);
    }

    model.addAttribute(
            "modification",
            modification);

    model.addAttribute(
            "contracts",
            contracts);

    model.addAttribute(
            "clauses",
            clauses);

    model.addAttribute(
            "documents",
            documents);

    return "Modification/modification-edit";
}

@PostMapping("/update/{id}")
public String updateModification(
        @PathVariable Long id,
        @ModelAttribute Modification modification,
        @RequestParam(required = false) Long contractId,
        @RequestParam(required = false) Long clauseId,
        @RequestParam(required = false) Long documentId) {

    if (contractId == null) {
        return "redirect:/contract/modifications/edit/" + id;
    }

    Contract contract =
            contractService.getContractById(contractId);

    if (contract == null) {
        return "redirect:/contract/modifications/edit/" + id;
    }

    modification.setContract(contract);

    if (clauseId != null) {

        Clause clause =
                clauseService.getClauseById(clauseId);

        modification.setClause(clause);

    } else {

        modification.setClause(null);
    }

    if (documentId != null) {

        Document document =
                documentRepository
                        .findById(documentId)
                        .orElse(null);

        modification.setDocument(document);

    } else {

        modification.setDocument(null);
    }

    String modificationType =
            modification.getModificationType();

    if (modificationType == null
            || modificationType.isBlank()) {

        if (modification.getClause() != null) {
            modificationType = "CLAUSE";
        } else {
            modificationType = "CONTRACT";
        }
    }

    modificationType =
            modificationType.trim().toUpperCase();

    modification.setModificationType(
            modificationType);

    if ("CONTRACT".equals(modificationType)) {
        modification.setClause(null);
    }

    modificationService.updateModification(
            id,
            modification);

    return "redirect:/contract/modifications/view/" + id;
}

@GetMapping("/delete/{id}")
public String deleteModification(
        @PathVariable Long id) {

    modificationService.deleteModification(id);

    return "redirect:/contract/modifications";
}


}
