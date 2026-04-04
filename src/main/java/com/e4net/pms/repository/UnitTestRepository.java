package com.e4net.pms.repository;

import com.e4net.pms.entity.UnitTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UnitTestRepository extends JpaRepository<UnitTest, Long>,
        JpaSpecificationExecutor<UnitTest> {

    /** 사업별 전체 조회 (엑셀 다운로드용) */
    List<UnitTest> findAllByProject_IdOrderByCategoryAscUnitTestIdAsc(Long projectId);

    /** 사업별 단위테스트ID 기준 단건 조회 (엑셀 업로드 upsert용) */
    Optional<UnitTest> findByProject_IdAndUnitTestId(Long projectId, String unitTestId);
}
