package com.e4net.pms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 요구사항 추적 - 탭별 대상 항목 DTO
 * (업무흐름 / 메뉴구조 / 화면목록 등)
 */
@Getter
@AllArgsConstructor
public class TraceTargetItem {
    private Long id;
    private String code;   // 프로세스ID / 메뉴코드 / URL 등
    private String name;   // 프로세스명 / 메뉴명 / 화면명 등
    private boolean mapped; // 현재 요구사항에 연결 여부
}
