package com.e4net.pms.repository;

import com.e4net.pms.entity.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    /** 그룹코드 + 사용여부로 조회 (정렬순서) — 기존 메서드 */
    List<CommonCode> findByGroupCodeAndUseYnOrderBySortOrder(String groupCode, String useYn);

    /** 전체 목록 — 그룹코드/정렬순서 오름차순 (관리 화면 전체 조회용) */
    List<CommonCode> findAllByOrderByGroupCodeAscSortOrderAscIdAsc();

    /** 그룹별 전체 조회 (use_yn 무관, 관리 화면용) */
    List<CommonCode> findByGroupCodeOrderBySortOrderAscIdAsc(String groupCode);

    /** 그룹코드 + 코드로 단건 조회 (엑셀 upsert용) */
    Optional<CommonCode> findByGroupCodeAndCode(String groupCode, String code);

    /** 중복 없는 그룹코드 목록 (사이드바용) */
    @Query("SELECT DISTINCT c.groupCode FROM CommonCode c ORDER BY c.groupCode")
    List<String> findDistinctGroupCodes();
}
