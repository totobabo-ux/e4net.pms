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
 * 시스템 메뉴 엔티티 — 좌측 사이드바 메뉴 DB 관리
 * 관리자 > 시스템 관리 > 메뉴 관리
 */
@Entity
@Table(name = "menu")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AppMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 트리 구조 ──────────────────────────────────────────────
    @Column(name = "parent_id")
    private Long parentId;          // null = 루트

    @Column(name = "depth", nullable = false)
    private Integer depth;          // 1 / 2 / 3

    @Column(name = "sort_order")
    private Integer sortOrder;

    // ── 메뉴 정보 ──────────────────────────────────────────────
    @Column(name = "menu_code", length = 10, nullable = false)
    private String menuCode;

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;

    @Column(name = "context_path", length = 200)
    private String contextPath;

    @Column(name = "icon", length = 30)
    private String icon;              // 아이콘 HTML 엔티티 (예: &#128203;)

    // ── 상태 ──────────────────────────────────────────────────
    @Column(name = "fixed_yn", length = 1, nullable = false)
    private String fixedYn = "N";

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn = "Y";

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
