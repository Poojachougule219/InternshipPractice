package com.legalcontract.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.legalcontract.entity.Clause;
import com.legalcontract.entity.Contract;
import com.legalcontract.service.ClauseService;
import com.legalcontract.service.ContractService;
import com.legalcontract.service.DocumentService;

@Controller
public class ClausePageController {

    @Autowired
    private ClauseService clauseService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private DocumentService documentService;

    // =========================================================
    // 1. VIEW ALL CLAUSES OF A CONTRACT
    // URL: /contract/clauses/{contractId}
    // =========================================================

    @GetMapping("/contract/clauses/{contractId}")
    public String clauseList(
            @PathVariable Long contractId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            Contract contract =
                    contractService.getContractById(contractId);

            if (contract == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Contract not found."
                );

                return "redirect:/contract/contracts";
            }

            model.addAttribute(
                    "contract",
                    contract
            );

            model.addAttribute(
                    "clauses",
                    clauseService.getClausesByContract(
                            contractId
                    )
            );

            return "Clause/clause-list";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to load clauses: "
                            + e.getMessage()
            );

            return "redirect:/contract/contracts";
        }
    }

    // =========================================================
    // 2. OPEN ADD CLAUSE PAGE
    // URL: /contract/clauses/add/{contractId}
    // =========================================================

    @GetMapping("/contract/clauses/add/{contractId}")
    public String addClausePage(
            @PathVariable Long contractId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            Contract contract =
                    contractService.getContractById(contractId);

            if (contract == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Contract not found."
                );

                return "redirect:/contract/contracts";
            }

            Clause clause =
                    new Clause();

            model.addAttribute(
                    "clause",
                    clause
            );

            model.addAttribute(
                    "contract",
                    contract
            );

            model.addAttribute(
                    "documents",
                    documentService.getDocumentsByContract(
                            contractId
                    )
            );

            return "Clause/clause-add";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to open add clause page: "
                            + e.getMessage()
            );

            return "redirect:/contract/contracts";
        }
    }

    // =========================================================
    // 3. SAVE NEW CLAUSE
    // URL: /contract/clauses/save
    // =========================================================

    @PostMapping("/contract/clauses/save")
    public String saveClause(
            @RequestParam Long contractId,
            @RequestParam(required = false) Long documentId,
            Clause clause,
            RedirectAttributes redirectAttributes) {

        try {

            clauseService.createClause(
                    clause,
                    contractId,
                    documentId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Clause created successfully."
            );

            return "redirect:/contract/clauses/"
                    + contractId;

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to create clause: "
                            + e.getMessage()
            );

            return "redirect:/contract/clauses/add/"
                    + contractId;
        }
    }

    // =========================================================
    // 4. VIEW SINGLE CLAUSE
    // URL: /contract/clauses/view/{id}
    // =========================================================

    @GetMapping("/contract/clauses/view/{id}")
    public String viewClause(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            Clause clause =
                    clauseService.getClauseById(id);

            model.addAttribute(
                    "clause",
                    clause
            );

            model.addAttribute(
                    "contract",
                    clause.getContract()
            );

            return "Clause/clause-view";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to view clause: "
                            + e.getMessage()
            );

            return "redirect:/contract/contracts";
        }
    }

    // =========================================================
    // 5. OPEN EDIT CLAUSE PAGE
    // URL: /contract/clauses/edit/{id}
    // =========================================================

    @GetMapping("/contract/clauses/edit/{id}")
    public String editClausePage(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            Clause clause =
                    clauseService.getClauseById(id);

            model.addAttribute(
                    "clause",
                    clause
            );

            model.addAttribute(
                    "contract",
                    clause.getContract()
            );

            if (clause.getContract() != null) {

                model.addAttribute(
                        "documents",
                        documentService.getDocumentsByContract(
                                clause.getContract().getId()
                        )
                );
            }

            return "Clause/clause-edit";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to edit clause: "
                            + e.getMessage()
            );

            return "redirect:/contract/contracts";
        }
    }

    // =========================================================
    // 6. UPDATE CLAUSE
    // URL: /contract/clauses/update/{id}
    // =========================================================

    @PostMapping("/contract/clauses/update/{id}")
    public String updateClause(
            @PathVariable Long id,
            @RequestParam Long contractId,
            @RequestParam(required = false) Long documentId,
            Clause clause,
            RedirectAttributes redirectAttributes) {

        try {

            Clause updatedClause =
                    clauseService.updateClause(
                            id,
                            clause,
                            contractId,
                            documentId
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Clause updated successfully."
            );

            return "redirect:/contract/clauses/"
                    + updatedClause
                            .getContract()
                            .getId();

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to update clause: "
                            + e.getMessage()
            );

            return "redirect:/contract/clauses/edit/"
                    + id;
        }
    }

    // =========================================================
    // 7. DELETE CLAUSE
    // URL: /contract/clauses/delete/{id}
    // =========================================================

    @GetMapping("/contract/clauses/delete/{id}")
    public String deleteClause(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            Clause clause =
                    clauseService.getClauseById(id);

            Long contractId =
                    clause.getContract() != null
                            ? clause.getContract().getId()
                            : null;

            clauseService.deleteClause(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Clause deleted successfully."
            );

            if (contractId != null) {

                return "redirect:/contract/clauses/"
                        + contractId;
            }

            return "redirect:/contract/contracts";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to delete clause: "
                            + e.getMessage()
            );

            return "redirect:/contract/contracts";
        }
    }
}