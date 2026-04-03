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
 * 화면목록 엔티티
 * 사업수행 > 설계 > 화면목록 산출물 관리
 */
@Entity
@Table(name = "screen_list")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ScreenList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 메뉴 구조 ──────────────────────────────────────────────
    @Column(name = "menu_level1", length = 50)
    private String menuLevel1;          // 메뉴Level1 (예: 사업관리)

    @Column(name = "menu_level2", length = 50)
    private String menuLevel2;          // 메뉴Level2 (예: 표준관리)

    @Column(name = "menu_level3", length = 50)
    private String menuLevel3;          // 메뉴Level3 (예: 산출물 관리)

    // ── 화면 정보 ──────────────────────────────────────────────
    @Column(name = "category", length = 20)
    private String category;            // 분류 (목록/등록/상세/수정/조회)

    @Column(name = "screen_name", nullable = false, length = 200)
    private String screenName;          // 화면명 ※ 필수

    @Column(name = "screen_desc", length = 500)
    private String screenDesc;          // 화면설명

    @Column(name = "url", length = 300)
    private String url;                 // URL

    @Column(name = "template_file", length = 300)
    private String templateFile;        // 템플릿 파일 경로

    @Column(name = "note", length = 500)
    private String note;                // 비고

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
