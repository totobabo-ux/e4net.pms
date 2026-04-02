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
@Table(name = "deliverable")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;              // ※ 사업명

    // ── 산출물 정보 ──────────────────────────────────────────
    @Column(name = "deliverable_type", length = 20)
    private String deliverableType;       // 산출물 구분 (관리산출물/개발산출물)

    @Column(name = "category1", length = 50)
    private String category1;             // 분류1

    @Column(name = "category2", length = 50)
    private String category2;             // 분류2

    @Column(name = "code", length = 50)
    private String code;                  // 코드

    @Column(name = "deliverable_id", length = 50)
    private String deliverableId;         // 산출물ID

    @Column(name = "name", nullable = false, length = 200)
    private String name;                  // ※ 산출물명

    @Column(name = "written_yn", length = 10)
    private String writtenYn;             // 작성여부

    @Column(name = "stage", length = 20)
    private String stage = "미도래";       // 단계

    @Column(name = "writer", length = 100)
    private String writer;                // 작성자

    @Column(name = "note", length = 500)
    private String note;                  // 비고

    // ── 공통 감사 컬럼 ──────────────────────────────────────
    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;          // 등록일시

    @Column(name = "reg_id", length = 50)
    private String regId;                 // 등록자 ID

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;          // 수정일시

    @Column(name = "upd_id", length = 50)
    private String updId;                 // 수정자 ID
}
