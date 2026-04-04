package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 요구사항 추적 연관관계 엔티티
 * 요구사항 ↔ 업무흐름/메뉴구조/화면목록 등 연관 매핑
 */
@Entity
@Table(name = "req_traceability",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_req_trace", columnNames = {"req_id", "target_type", "target_id"}))
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ReqTraceability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사업 ID */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /** 요구사항 ID */
    @Column(name = "req_id", nullable = false)
    private Long reqId;

    /**
     * 연관 대상 유형
     * BUSINESS_FLOW / MENU / SCREEN
     * (INTERFACE / PROGRAM / UNIT_TEST / INTEGRATION_TEST 는 향후 구현)
     */
    @Column(name = "target_type", length = 30, nullable = false)
    private String targetType;

    /** 연관 대상 ID */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_id", length = 50, updatable = false)
    private String regId;
}
