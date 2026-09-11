
package com.legalcontract.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.legalcontract.entity.Contract;
import com.legalcontract.entity.User;
import com.legalcontract.repository.ContractRepository;
import com.legalcontract.repository.UserRepository;

@Service
@Transactional
public class ContractService {

    private final ContractRepository contractRepo;
    private final UserRepository userRepo;

    public ContractService(
            ContractRepository contractRepo,
            UserRepository userRepo) {

        this.contractRepo = contractRepo;
        this.userRepo = userRepo;
    }

    public Contract createContract(Contract contract) {

        if (contract == null) {
            throw new RuntimeException("Contract data cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();

        contract.setCreatedAt(now);
        contract.setUpdatedAt(now);

        contract.setStatus(normalizeStatus(contract.getStatus()));

        User currentUser = getCurrentUser();

        if (currentUser != null) {
            contract.setCreatedBy(currentUser);
        }

        return contractRepo.save(contract);
    }

    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {

        return contractRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Contract getContractById(Long id) {

        return contractRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Contract not found with id: " + id
                        )
                );
    }

    public Contract updateContract(
            Long id,
            Contract contract) {

        Contract existingContract =
                getContractById(id);

        if (contract.getContractNumber() != null) {
            existingContract.setContractNumber(
                    contract.getContractNumber()
            );
        }

        if (contract.getTitle() != null) {
            existingContract.setTitle(
                    contract.getTitle()
            );
        }

        if (contract.getDescription() != null) {
            existingContract.setDescription(
                    contract.getDescription()
            );
        }

        if (contract.getContractType() != null) {
            existingContract.setContractType(
                    contract.getContractType()
            );
        }

        if (contract.getStatus() != null) {
            existingContract.setStatus(
                    normalizeStatus(contract.getStatus())
            );
        }

        if (contract.getStartDate() != null) {
            existingContract.setStartDate(
                    contract.getStartDate()
            );
        }

        if (contract.getEndDate() != null) {
            existingContract.setEndDate(
                    contract.getEndDate()
            );
        }

        existingContract.setUpdatedAt(
                LocalDateTime.now()
        );

        return contractRepo.save(existingContract);
    }

    public void deleteContract(Long id) {

        Contract existingContract =
                getContractById(id);

        contractRepo.delete(existingContract);
    }

    private String normalizeStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            return "DRAFT";
        }

        String value = status.trim().toUpperCase();

        switch (value) {

            case "ACTIVE":
                return "ACTIVE";

            case "DRAFT":
                return "DRAFT";

            case "EXPIRED":
                return "EXPIRED";

            case "TERMINATED":
                return "TERMINATED";

            default:
                return value;
        }
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getPrincipal())) {

            return null;
        }

        String username =
                authentication.getName();

        return userRepo.findByUsername(username)
                .orElse(null);
    }
}
