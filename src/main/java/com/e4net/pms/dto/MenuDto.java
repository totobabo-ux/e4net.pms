package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MenuDto {

    private Long id;
    private Long projectId;

    // ── 트리 구조 ──────────────────────────────────────────────
    private Long parentId;
    private String parentName;      // 상위 메뉴명 (화면 표시용)
    private Integer depth;
    private Integer sortOrder;

    // ── 메뉴 정보 ──────────────────────────────────────────────
    private String menuCode;
    private String menuName;
    private String contextPath;

    // ── 상태 ──────────────────────────────────────────────────
    private String fixedYn;
    private String useYn;
}
