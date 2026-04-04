package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class InterfaceListDto {

    private Long id;

    // ── 사업 ──────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long   projectId;
    private String projectName;

    // ── 인터페이스 정보 ────────────────────────────────────────
    private String interfaceId;         // 인터페이스ID

    @NotBlank(message = "인터페이스명을 입력해주세요.")
    private String interfaceName;       // 인터페이스명 ※ 필수

    private String linkType;            // 연계 구분
    private String sourceSystem;        // 송신 시스템(Source)
    private String targetSystem;        // 수신 시스템(Target)
    private String interfaceMethod;     // 인터페이스방식
    private String occurrenceCycle;     // 발생주기
    private String note;                // 비고

    // ── 첨부파일 ──────────────────────────────────────────────
    private List<AttachFileDto> attachments = new ArrayList<>();
}
