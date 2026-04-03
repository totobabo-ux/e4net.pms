package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "requirement")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Requirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;             // ※ 사업명

    // ── 요구사항 정보 ──────────────────────────────────────────
    @Column(name = "req_code", length = 50)
    private String reqCode;              // 요구사항 코드

    @Column(name = "title", nullable = false, length = 300)
    private String title;                // ※ 제목

    @Column(name = "category", length = 20)
    private String category;             // 분류 (기능/비기능)

    @Column(name = "priority", length = 10)
    private String priority = "중";      // 우선순위 (상/중/하)

    @Column(name = "status", length = 20)
    private String status = "등록";      // 상태 (등록/분석중/개발중/완료/보류)

    @Column(name = "requestor", length = 100)
    private String requestor;            // 요청자

    @Column(name = "description", length = 2000)
    private String description;          // 내용

    @Column(name = "note", length = 500)
    private String note;                 // 비고

    @Column(name = "source_type", length = 20)
    private String sourceType;           // 요구사항 출처 선택 (제안요청서/회의록/기타)

    @Column(name = "source_content", length = 300)
    private String sourceContent;        // 요구사항 출처 내용

    @Column(name = "acceptance", length = 20)
    private String acceptance = "협의중"; // 수용여부 (협의중/수용/제외)

    // ── 공통 감사 컬럼 ─────────────────────────────────────────
    @Column(name = "reg_id", length = 50, updatable = false)
    private String regId;                // 등록자 ID

    @Column(name = "upd_id", length = 50)
    private String updId;                // 수정자 ID

    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;         // 등록일시

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;         // 수정일시
}
