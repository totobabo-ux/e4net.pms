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
@Table(name = "project")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;

    @Column(name = "category", length = 20)
    private String category;        // SL / SM / 서비스

    @Column(name = "company", length = 100)
    private String company;         // 회사 (직접 입력)

    @Column(name = "orderer", length = 100)
    private String orderer;         // 발주처

    @Column(name = "contractor", length = 100)
    private String contractor;      // 계약처

    @Column(name = "contract_start")
    private LocalDate contractStart;

    @Column(name = "contract_end")
    private LocalDate contractEnd;

    @Column(name = "pm", length = 50)
    private String pm;

    @Column(name = "contract_amount")
    private Long contractAmount;    // 계약금액

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
