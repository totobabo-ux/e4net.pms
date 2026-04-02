package com.e4net.pms.repository;

import com.e4net.pms.entity.Deliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DeliverableRepository extends JpaRepository<Deliverable, Long>,
        JpaSpecificationExecutor<Deliverable> {

    /** 사업 ID 기준 전체 조회 (엑셀 다운로드용) */
    List<Deliverable> findAllByProject_IdOrderByIdAsc(Long projectId);

    /** 사업 ID + 산출물ID 로 단건 조회 (엑셀 업로드 upsert용) */
    Optional<Deliverable> findByProject_IdAndDeliverableId(Long projectId, String deliverableId);
}
