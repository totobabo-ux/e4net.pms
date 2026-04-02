package com.e4net.pms.repository;

import com.e4net.pms.entity.Deliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliverableRepository extends JpaRepository<Deliverable, Long>,
        JpaSpecificationExecutor<Deliverable> {
}
