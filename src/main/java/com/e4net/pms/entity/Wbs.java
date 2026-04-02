package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wbs")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor
public class Wbs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "task_id", length = 50)
    private String taskId;              // TASK ID

    @Column(name = "task_name", length = 300)
    private String taskName;            // TASK명

    @Column(name = "deliverable", length = 300)
    private String deliverable;         // 산출물

    @Column(name = "assignee", length = 100)
    private String assignee;            // 담당자

    @Column(name = "plan_progress")
    private Integer planProgress;       // 계획(%)

    @Column(name = "actual_progress")
    private Integer actualProgress;     // 실적(%)

    @Column(name = "plan_start_date")
    private LocalDate planStartDate;    // 계획시작일

    @Column(name = "plan_end_date")
    private LocalDate planEndDate;      // 계획종료일

    @Column(name = "plan_duration")
    private Integer planDuration;       // 계획기간(일)

    @Column(name = "plan_rate")
    private Integer planRate;           // 계획진척률(%)

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;  // 실제시작일

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;    // 실제종료일

    @Column(name = "actual_rate")
    private Integer actualRate;         // 실제진척율(%)

    @Column(name = "status", length = 20)
    private String status;              // 상태

    @Column(name = "sort_order")
    private Integer sortOrder;          // 정렬순서

    @CreatedDate
    @Column(name = "reg_dt", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_id", length = 50)
    private String regId;

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    @Column(name = "upd_id", length = 50)
    private String updId;
}
