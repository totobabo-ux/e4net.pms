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
 * 업무흐름 엔티티
 * 사업수행 > 분석 > 업무흐름 관리
 */
@Entity
@Table(name = "business_flow")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BusinessFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 업무흐름 정보 ──────────────────────────────────────────
    @Column(name = "system_category", length = 100)
    private String systemCategory;   // 시스템구분

    @Column(name = "biz_category", length = 100)
    private String bizCategory;      // 업무구분

    @Column(name = "process_id", length = 100)
    private String processId;        // 프로세스ID

    @Column(name = "process_name", nullable = false, length = 200)
    private String processName;      // 프로세스명 ※ 필수

    // ── 공통 감사 컬럼 ──────────────────────────────────────────
    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_id", length = 50, updatable = false)
    private String regId;

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    @Column(name = "upd_id", length = 50)
    private String updId;
}
