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
@Table(name = "project_manpower")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProjectManpower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;            // ※ 사업명

    // ── 참여자 연결 ────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // ※ 참여자

    // ── 소속 정보 ──────────────────────────────────────────────
    @Column(name = "company", length = 100)
    private String company;             // 소속회사 (user 기본값, 수정 가능)

    @Column(name = "department", length = 100)
    private String department;          // 소속부서

    @Column(name = "phone", length = 20)
    private String phone;               // 연락처 (user 기본값, 자동채움)

    // ── 역할/등급 ──────────────────────────────────────────────
    @Column(name = "role", nullable = false, length = 100)
    private String role;                // ※ 역할

    @Column(name = "position", length = 50)
    private String position;            // 직위

    @Column(name = "grade_code", nullable = false, length = 20)
    private String gradeCode;           // ※ 급수 코드 (GRADE)

    // ── 투입 정보 ──────────────────────────────────────────────
    @Column(name = "input_type_code", nullable = false, length = 20)
    private String inputTypeCode;       // ※ 투입구분 코드 (INPUT_TYPE)

    @Column(name = "input_start_date", nullable = false)
    private LocalDate inputStartDate;   // ※ 투입시작일

    @Column(name = "input_end_date", nullable = false)
    private LocalDate inputEndDate;     // ※ 투입종료일

    @Column(name = "input_mm")
    private Double inputMm;             // 투입MM

    // ── 상태/비고 ──────────────────────────────────────────────
    @Column(name = "status", length = 20)
    private String status = "투입중";   // 상태 (투입중/투입종료/대기)

    @Column(name = "note", length = 500)
    private String note;                // 비고

    // ── 공통 감사 컬럼 ─────────────────────────────────────────
    @Column(name = "reg_id", length = 50, updatable = false)
    private String regId;               // 등록자 ID

    @Column(name = "upd_id", length = 50)
    private String updId;               // 수정자 ID

    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;        // 등록일시

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;        // 수정일시
}
