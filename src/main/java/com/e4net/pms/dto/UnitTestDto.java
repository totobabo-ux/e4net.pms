package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class UnitTestDto {

    private Long id;

    @NotNull(message = "사업을 선택해주세요.")
    private Long   projectId;
    private String projectName;

    private String category;      // 분류 (공통코드 UNIT_TEST_CATEGORY)
    private String unitTestId;    // 단위테스트ID

    @NotBlank(message = "단위테스트명을 입력해주세요.")
    private String unitTestName;  // 단위테스트명

    private String description;   // 단위테스트 설명
    private String tester;        // 테스트 담당자
    private String note;          // 비고

    private List<AttachFileDto> attachments;
}
