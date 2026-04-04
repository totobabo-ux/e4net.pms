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
@Table(name = "community")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연관 (nullable — 전체 공지는 project 없이 등록 가능) ────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // ── 구분 ─────────────────────────────────────────────────────
    @Column(name = "community_type", length = 20, nullable = false)
    private String communityType;   // 공지사항 / 자료실

    // ── 기본 정보 ─────────────────────────────────────────────────
    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "writer", length = 100)
    private String writer;

    @Column(name = "post_date")
    private LocalDate postDate;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

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
