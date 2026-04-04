package com.e4net.pms.repository;

import com.e4net.pms.entity.ReqTraceability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReqTraceabilityRepository extends JpaRepository<ReqTraceability, Long> {

    /** 요구사항 + 타입별 연관 목록 조회 */
    List<ReqTraceability> findByProjectIdAndReqIdAndTargetType(
            Long projectId, Long reqId, String targetType);

    /** 요구사항 + 타입별 연관 전체 삭제 (저장 시 replace) */
    void deleteByProjectIdAndReqIdAndTargetType(
            Long projectId, Long reqId, String targetType);
}
