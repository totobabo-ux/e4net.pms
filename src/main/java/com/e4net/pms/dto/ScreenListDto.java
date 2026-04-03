package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class ScreenListDto {

    private Long id;

    // ── 사업 ──────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;
    private String projectName;

    // ── 메뉴 구조 ─────────────────────────────────────────────
    private String menuLevel1;      // 메뉴Level1
    private String menuLevel2;      // 메뉴Level2
    private String menuLevel3;      // 메뉴Level3

    // ── 화면 정보 ─────────────────────────────────────────────
    private String category;        // 분류

    @NotBlank(message = "화면명을 입력해주세요.")
    private String screenName;      // 화면명 ※ 필수

    private String screenDesc;      // 화면설명
    private String url;             // URL
    private String templateFile;    // 템플릿 파일
    private String note;            // 비고

    // ── 첨부파일 ──────────────────────────────────────────────
    private List<AttachFileDto> attachments = new ArrayList<>();
}
