package com.e4net.pms.repository;

import com.e4net.pms.entity.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RequirementRepository extends JpaRepository<Requirement, Long>,
        JpaSpecificationExecutor<Requirement> {
}
