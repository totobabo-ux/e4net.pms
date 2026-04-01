package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor
public class ProjectDto {

    private Long id;

    private String projectCode;

    @NotBlank(message = "프로젝트명은 필수입니다.")
    private String projectName;

    private String category;
    private String subCategory;
    private String company;
    private Boolean isPublic = true;
    private String orderer;
    private String contractor;
    private LocalDate contractStart;
    private LocalDate contractEnd;
    private String pm;
    private Long contractAmount;
    private Double preInputMm;
    private Double totalMm;
    private Integer headcount;
    private String status;
}
