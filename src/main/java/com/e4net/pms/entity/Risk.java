package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 위험 기본 정보 ─────────────────────────────────────────────
    @Column(name = "risk_code", length = 50)
    private String riskCode;              // 위험코드

    @Column(name = "risk_name", length = 200, nullable = false)
    private String riskName;              // 위험명

    @Column(name = "risk_type", length = 50)
    private String riskType;              // 위험유형: 기술/일정/비용/인력/외부

    @Column(name = "identified_date")
    private LocalDate identifiedDate;     // 식별일자

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;           // 위험설명

    // ── 위험 평가 ──────────────────────────────────────────────────
    @Column(name = "probability", length = 50)
    private String probability;           // 발생가능성: 낮음/보통/높음

    @Column(name = "impact", length = 50)
    private String impact;                // 영향도: 적음/보통/심각/매우심각

    @Column(name = "risk_level", length = 50)
    private String riskLevel;             // 위험등급: VERY LOW/LOW/MODERATE/HIGH/VERY HIGH

    // ── 대응 정보 ──────────────────────────────────────────────────
    @Column(name = "response_strategy", length = 100)
    private String responseStrategy;      // 대응전략: 회피/전가/완화/수용

    @Column(name = "response_plan", columnDefinition = "TEXT")
    private String responsePlan;          // 대응계획

    @Column(name = "owner", length = 200)
    private String owner;                 // 담당자

    // ── 활동결과 및 상태 ───────────────────────────────────────────
    @Column(name = "activity_result", columnDefinition = "TEXT")
    private String activityResult;        // 활동결과

    @Column(name = "status", length = 50)
    private String status;                // 위험상태: 진행중/해결/종료

    // ── 공통 감사 컬럼 ─────────────────────────────────────────────
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
