package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class WbsDto {

    private Long id;
    private Long projectId;
    private String taskId;
    private String taskName;
    private String deliverable;
    private String assignee;
    private Integer planProgress;       // 계획(%)
    private Integer actualProgress;     // 실적(%)
    private String planStartDate;       // yyyy-MM-dd
    private String planEndDate;         // yyyy-MM-dd
    private Integer planDuration;       // 계획기간(일)
    private Integer planRate;           // 계획진척률(%)
    private String actualStartDate;     // yyyy-MM-dd
    private String actualEndDate;       // yyyy-MM-dd
    private Integer actualDuration;     // 실제기간(일, 서버에서 계산)
    private Integer actualRate;         // 실제진척율(%)
    private String status;
    private Integer sortOrder;
}
