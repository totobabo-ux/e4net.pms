package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 메뉴구조 엔티티
 * 사업수행 > 분석 > 메뉴구조 산출물 관리
 * 메뉴코드 체계: M + Depth1(2자리) + Depth2(2자리) + Depth3(2자리)
 * 예: M010000 > M010100 > M010101
 */
@Entity
@Table(name = "menu_structure")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 트리 구조 ──────────────────────────────────────────────
    @Column(name = "parent_id")
    private Long parentId;          // null = 루트 메뉴

    @Column(name = "depth", nullable = false)
    private Integer depth;          // 1 / 2 / 3

    @Column(name = "sort_order")
    private Integer sortOrder;      // 형제 노드 내 순서

    // ── 메뉴 정보 ──────────────────────────────────────────────
    @Column(name = "menu_code", length = 10, nullable = false)
    private String menuCode;        // 예: M010101

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;        // 메뉴명

    @Column(name = "context_path", length = 200)
    private String contextPath;     // URL 경로 (예: /screen-list)

    // ── 상태 ──────────────────────────────────────────────────
    @Column(name = "fixed_yn", length = 1, nullable = false)
    private String fixedYn = "N";   // 고정 메뉴 여부 (Y=삭제불가)

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn = "Y";     // 사용 여부

    // ── 공통 감사 컬럼 ──────────────────────────────────────────
    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_id", length = 50)
    private String regId;

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    @Column(name = "upd_id", length = 50)
    private String updId;
}
