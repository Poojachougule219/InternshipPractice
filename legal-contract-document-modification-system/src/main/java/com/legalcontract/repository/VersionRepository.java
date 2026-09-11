package com.legalcontract.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.legalcontract.entity.Version;

public interface VersionRepository extends JpaRepository <Version, Long>{

}
