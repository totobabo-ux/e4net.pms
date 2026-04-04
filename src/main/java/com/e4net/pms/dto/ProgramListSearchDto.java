package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProgramListSearchDto {
    private Long   projectId;    // 세션 기반 자동 필터
    private String systemName;   // 시스템구분
    private String programType;  // 프로그램구분
    private String programName;  // 프로그램명
}
