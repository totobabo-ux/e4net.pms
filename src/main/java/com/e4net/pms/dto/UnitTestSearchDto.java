package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class UnitTestSearchDto {
    private Long   projectId;
    private String category;      // 분류
    private String unitTestName;  // 단위테스트명 (like)
}
