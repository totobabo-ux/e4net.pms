package com.e4net.pms.repository;

import com.e4net.pms.entity.Risk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RiskRepository extends JpaRepository<Risk, Long>,
        JpaSpecificationExecutor<Risk> {

    /** 사업 ID 기준 전체 조회 (엑셀 다운로드용) */
    List<Risk> findAllByProject_IdOrderByIdAsc(Long projectId);

    /** 홈 대시보드 — 종료 제외 최근 3건 */
    List<Risk> findTop3ByProject_IdAndStatusNotOrderByIdentifiedDateDesc(Long projectId, String status);

    /** 사업 ID + 위험코드로 단건 조회 (엑셀 업로드 upsert용, 중복 시 첫 번째 반환) */
    Optional<Risk> findFirstByProject_IdAndRiskCode(Long projectId, String riskCode);
}
