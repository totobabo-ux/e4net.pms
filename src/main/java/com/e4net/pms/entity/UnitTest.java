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
@Table(name = "unit_test")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UnitTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 단위테스트 정보 ─────────────────────────────────────────
    @Column(name = "category", length = 50)
    private String category;          // 분류 (공통코드 UNIT_TEST_CATEGORY)

    @Column(name = "unit_test_id", length = 100)
    private String unitTestId;        // 단위테스트ID (예: index)

    @Column(name = "unit_test_name", nullable = false, length = 200)
    private String unitTestName;      // 단위테스트명 (예: 로그인)

    @Column(name = "description", length = 1000)
    private String description;       // 단위테스트 설명

    @Column(name = "tester", length = 100)
    private String tester;            // 테스트 담당자

    @Column(name = "note", length = 500)
    private String note;              // 비고

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
