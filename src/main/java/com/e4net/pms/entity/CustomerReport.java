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
@Table(name = "customer_report")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CustomerReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 보고 기본 정보 ─────────────────────────────────────────────
    @Column(name = "report_type", length = 20)
    private String reportType;           // 보고구분

    @Column(name = "report_name", length = 300)
    private String reportName;           // 보고서명

    @Column(name = "report_date")
    private LocalDate reportDate;        // 보고일자

    @Column(name = "report_content", columnDefinition = "TEXT")
    private String reportContent;        // 보고내용

    @Column(name = "writer", length = 100)
    private String writer;               // 작성자

    // ── 첨부파일 ───────────────────────────────────────────────────
    @Column(name = "attach_file_name", length = 255)
    private String attachFileName;       // 원본 파일명

    @Column(name = "attach_file_path", length = 500)
    private String attachFilePath;       // 저장 경로

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
