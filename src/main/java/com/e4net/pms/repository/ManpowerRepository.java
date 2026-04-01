package com.e4net.pms.repository;

import com.e4net.pms.entity.ProjectManpower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ManpowerRepository extends JpaRepository<ProjectManpower, Long>,
        JpaSpecificationExecutor<ProjectManpower> {
}
