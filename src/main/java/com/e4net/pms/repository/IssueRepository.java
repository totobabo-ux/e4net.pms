package com.e4net.pms.repository;

import com.e4net.pms.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long>,
        JpaSpecificationExecutor<Issue> {

    /** 사업 ID 기준 전체 조회 (엑셀 다운로드용) */
    List<Issue> findAllByProject_IdOrderByIdAsc(Long projectId);

    /** 홈 대시보드 — 조치완료 제외 최근 3건 */
    List<Issue> findTop3ByProject_IdAndActionStatusNotOrderByRaisedDateDesc(Long projectId, String actionStatus);

    /** 사업 ID + 관리번호로 단건 조회 (엑셀 업로드 upsert용) */
    Optional<Issue> findFirstByProject_IdAndIssueNo(Long projectId, String issueNo);
}
