package com.e4net.pms.repository;

import com.e4net.pms.entity.Wbs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WbsRepository extends JpaRepository<Wbs, Long> {

    List<Wbs> findByProjectIdOrderBySortOrderAscIdAsc(Long projectId);

    long countByProject_Id(Long projectId);

    Optional<Wbs> findFirstByProject_IdAndTaskId(Long projectId, String taskId);
}
