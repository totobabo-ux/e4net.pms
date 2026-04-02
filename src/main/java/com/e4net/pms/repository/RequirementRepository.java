package com.e4net.pms.repository;

import com.e4net.pms.entity.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RequirementRepository extends JpaRepository<Requirement, Long>,
        JpaSpecificationExecutor<Requirement> {

    /** 사업 ID 기준 전체 조회 (엑셀 다운로드용) */
    List<Requirement> findAllByProject_IdOrderByIdAsc(Long projectId);

    /** 사업 ID + 요구사항코드로 단건 조회 (엑셀 업로드 upsert용) */
    Optional<Requirement> findByProject_IdAndReqCode(Long projectId, String reqCode);
}
