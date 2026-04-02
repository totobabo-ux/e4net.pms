package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통 첨부파일 테이블
 * entity_type + entity_id 조합으로 여러 엔티티에서 공유 사용
 * 예) entity_type = "CUSTOMER_REPORT", entity_id = customer_report.id
 */
@Entity
@Table(name = "attach_file",
       indexes = @Index(name = "idx_attach_file_entity", columnList = "entity_type, entity_id"))
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AttachFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 연결 대상 식별 ──────────────────────────────────────────────
    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;     // 예: "CUSTOMER_REPORT"

    @Column(name = "entity_id", nullable = false)
    private Long entityId;         // 연결 대상의 PK

    // ── 파일 정보 ──────────────────────────────────────────────────
    @Column(name = "file_name", length = 255)
    private String fileName;       // 원본 파일명 (표시용)

    @Column(name = "file_path", length = 500)
    private String filePath;       // 저장 경로 (절대경로)

    @Column(name = "file_size")
    private Long fileSize;         // 파일 크기 (bytes)

    // ── 공통 감사 컬럼 ─────────────────────────────────────────────
    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_id", length = 50, updatable = false)
    private String regId;
}
