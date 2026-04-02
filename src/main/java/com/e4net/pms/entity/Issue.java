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
@Table(name = "issue")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 이슈 기본 정보 ─────────────────────────────────────────────
    @Column(name = "issue_no", length = 50)
    private String issueNo;               // 관리번호

    @Column(name = "issue_name", length = 200, nullable = false)
    private String issueName;             // 이슈명

    @Column(name = "raiser", length = 100)
    private String raiser;                // 제기자

    @Column(name = "raised_date")
    private LocalDate raisedDate;         // 제기일자

    @Column(name = "issue_content", columnDefinition = "TEXT")
    private String issueContent;          // 이슈내용

    // ── 조치 계획 ──────────────────────────────────────────────────
    @Column(name = "action_plan_date")
    private LocalDate actionPlanDate;     // 조치계획일자

    @Column(name = "action_plan_content", columnDefinition = "TEXT")
    private String actionPlanContent;     // 조치계획내용

    // ── 조치 결과 ──────────────────────────────────────────────────
    @Column(name = "action_status", length = 50)
    private String actionStatus;          // 조치상태: 미조치/조치중/조치완료/보류

    @Column(name = "action_date")
    private LocalDate actionDate;         // 조치일자

    @Column(name = "action_content", columnDefinition = "TEXT")
    private String actionContent;         // 조치내용

    @Column(name = "note", length = 500)
    private String note;                  // 비고

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
