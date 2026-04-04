package com.e4net.pms.repository;

import com.e4net.pms.entity.IntegrationTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface IntegrationTestRepository extends JpaRepository<IntegrationTest, Long>,
        JpaSpecificationExecutor<IntegrationTest> {

    /** 사업별 전체 조회 (엑셀 다운로드용) */
    List<IntegrationTest> findAllByProject_IdOrderByCategoryAscIntegrationTestIdAsc(Long projectId);

    /** 사업별 통합테스트ID 기준 단건 조회 (엑셀 업로드 upsert용) */
    Optional<IntegrationTest> findByProject_IdAndIntegrationTestId(Long projectId, String integrationTestId);
}
