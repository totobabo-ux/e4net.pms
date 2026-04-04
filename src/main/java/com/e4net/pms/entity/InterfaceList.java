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
 * 인터페이스목록 엔티티
 * 사업수행 > 설계 > 인터페이스 목록
 */
@Entity
@Table(name = "interface_list")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class InterfaceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 사업 연결 ──────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── 인터페이스 정보 ────────────────────────────────────────
    @Column(name = "interface_id", length = 50)
    private String interfaceId;         // 인터페이스ID (예: SEPP_0001)

    @Column(name = "interface_name", nullable = false, length = 300)
    private String interfaceName;       // 인터페이스명 ※ 필수

    @Column(name = "link_type", length = 20)
    private String linkType;            // 연계 구분 (자기관/타기관)

    @Column(name = "source_system", length = 300)
    private String sourceSystem;        // 송신 시스템(Source)

    @Column(name = "target_system", length = 300)
    private String targetSystem;        // 수신 시스템(Target)

    @Column(name = "interface_method", length = 50)
    private String interfaceMethod;     // 인터페이스방식 (공동이용API/REST API 등)

    @Column(name = "occurrence_cycle", length = 30)
    private String occurrenceCycle;     // 발생주기 (실시간/배치 등)

    @Column(name = "note", length = 500)
    private String note;                // 비고

    // ── 공통 감사 컬럼 ──────────────────────────────────────────
    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_id", length = 50)
    private String regId;

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    @Column(name = "upd_id", length = 50)
    private String updId;
}
