package com.e4net.pms.repository;

import com.e4net.pms.entity.Wbs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WbsRepository extends JpaRepository<Wbs, Long> {

    List<Wbs> findByProjectIdOrderBySortOrderAscIdAsc(Long projectId);
}
