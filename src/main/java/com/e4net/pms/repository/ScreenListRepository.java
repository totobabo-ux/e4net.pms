package com.e4net.pms.repository;

import com.e4net.pms.entity.ScreenList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ScreenListRepository extends JpaRepository<ScreenList, Long>,
        JpaSpecificationExecutor<ScreenList> {

    /** 사업 ID 기준 전체 조회 (엑셀 다운로드용) */
    List<ScreenList> findAllByProject_IdOrderByIdAsc(Long projectId);

    /** 사업 ID + URL 기준 단건 조회 (엑셀 업로드 upsert용) */
    java.util.Optional<ScreenList> findByProject_IdAndUrl(Long projectId, String url);
}
