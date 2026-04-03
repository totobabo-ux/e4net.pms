package com.e4net.pms.repository;

import com.e4net.pms.entity.BusinessFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BusinessFlowRepository extends JpaRepository<BusinessFlow, Long>,
        JpaSpecificationExecutor<BusinessFlow> {
}
