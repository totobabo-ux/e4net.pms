package com.e4net.pms.repository;

import com.e4net.pms.entity.BusinessFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BusinessFlowRepository extends JpaRepository<BusinessFlow, Long>,
        JpaSpecificationExecutor<BusinessFlow> {

    /** 사업별 업무흐름 목록 (추적성 탭용) */
    List<BusinessFlow> findByProject_IdOrderBySystemCategoryAscProcessIdAsc(Long projectId);

    /** 사업별 전체 조회 (엑셀 다운로드용) */
    List<BusinessFlow> findAllByProject_IdOrderBySystemCategoryAscBizCategoryAscProcessIdAsc(Long projectId);

    /** 사업별 프로세스ID 기준 단건 조회 (엑셀 업로드 upsert용) */
    java.util.Optional<BusinessFlow> findByProject_IdAndProcessId(Long projectId, String processId);
}
