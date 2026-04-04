package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class IntegrationTestDto {

    private Long id;

    @NotNull(message = "사업을 선택해주세요.")
    private Long   projectId;
    private String projectName;

    private String category;              // 분류 (공통코드 INTEGRATION_TEST_CATEGORY)
    private String integrationTestId;     // 통합테스트ID

    @NotBlank(message = "통합테스트명을 입력해주세요.")
    private String integrationTestName;   // 통합테스트명

    private String description;           // 통합테스트 설명
    private String note;                  // 비고

    private List<AttachFileDto> attachments;
}
