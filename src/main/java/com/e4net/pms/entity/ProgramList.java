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
@Table(name = "program_list")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProgramList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 프로그램 정보 ─────────────────────────────────────────
    @Column(name = "system_name", length = 100)
    private String systemName;          // 시스템구분

    @Column(name = "program_id", length = 50)
    private String programId;           // 프로그램ID (예: PGM-CMN-LGN-001)

    @Column(name = "program_name", nullable = false, length = 200)
    private String programName;         // 프로그램명 (예: ID/PW 로그인)

    @Column(name = "class_name", length = 200)
    private String className;           // 클래스명 (예: FrontLoginService)

    @Column(name = "class_path", length = 500)
    private String classPath;           // 클래스경로 (예: /11.business/.../FrontLoginService.java)

    @Column(name = "program_type", length = 50)
    private String programType;         // 프로그램구분 (공통코드 PROGRAM_TYPE)

    @Column(name = "difficulty", length = 20)
    private String difficulty;          // 개발난이도 (공통코드 PROGRAM_DIFFICULTY)

    @Column(name = "note", length = 1000)
    private String note;                // 비고

    // ── 공통 감사 컬럼 ──────────────────────────────────────
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
