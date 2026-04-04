package com.e4net.pms.repository;

import com.e4net.pms.entity.ProgramList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProgramListRepository extends JpaRepository<ProgramList, Long>,
        JpaSpecificationExecutor<ProgramList> {

    /** 엑셀 다운로드용 전체 조회 */
    List<ProgramList> findAllByProject_IdOrderByIdAsc(Long projectId);

    /** 엑셀 업로드 upsert용 — 프로그램ID 기준 */
    Optional<ProgramList> findByProject_IdAndProgramId(Long projectId, String programId);
}
