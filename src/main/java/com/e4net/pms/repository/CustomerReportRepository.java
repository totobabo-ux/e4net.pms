package com.e4net.pms.repository;

import com.e4net.pms.entity.CustomerReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerReportRepository extends JpaRepository<CustomerReport, Long>,
        JpaSpecificationExecutor<CustomerReport> {
}
