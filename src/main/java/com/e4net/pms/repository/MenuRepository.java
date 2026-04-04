package com.e4net.pms.repository;

import com.e4net.pms.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    /** 사업 전체 메뉴 (menuCode 오름차순 = 트리 순서 유지) */
    List<Menu> findByProject_IdOrderByMenuCode(Long projectId);

    /** 루트 메뉴 목록 */
    List<Menu> findByProject_IdAndParentIdIsNullOrderByMenuCode(Long projectId);

    /** 특정 부모의 자식 메뉴 목록 */
    List<Menu> findByParentIdOrderByMenuCode(Long parentId);

    /** 자식이 있는지 확인 (삭제 전 체크) */
    boolean existsByParentId(Long parentId);

    /** 엑셀 업로드 upsert용 — 메뉴코드 기준 */
    Optional<Menu> findByProject_IdAndMenuCode(Long projectId, String menuCode);
}
