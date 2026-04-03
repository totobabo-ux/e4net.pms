package com.e4net.pms.controller;

import com.e4net.pms.entity.CommonCode;
import com.e4net.pms.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 공통코드 전역 주입 — 모든 뷰에서 ${codes['GROUP_CODE']} 로 접근 가능
 */
@ControllerAdvice
@RequiredArgsConstructor
public class CommonCodeAdvice {

    private final CommonCodeService commonCodeService;

    private static final List<String> CODE_GROUPS = List.of(
        "GRADE",               // 인력 급수
        "INPUT_TYPE",          // 인력 투입구분
        "MANPOWER_STATUS",     // 인력 상태
        "PROJECT_CATEGORY",    // 사업 유형
        "ISSUE_ACTION_STATUS", // 이슈 조치상태
        "DELIVERABLE_TYPE",    // 산출물 구분
        "DELIVERABLE_WRITE_YN",// 산출물 작성여부
        "DELIVERABLE_STAGE",   // 산출물 단계
        "RISK_TYPE",           // 위험 유형
        "RISK_PROBABILITY",    // 위험 발생가능성
        "RISK_IMPACT",         // 위험 영향도
        "RISK_LEVEL",          // 위험 등급
        "RISK_STATUS",         // 위험 상태
        "RISK_STRATEGY",       // 위험 대응전략
        "REQ_SOURCE",          // 요구사항 출처
        "REQ_TYPE",            // 요구사항 분류
        "REQ_PRIORITY",        // 요구사항 우선순위
        "REQ_STATUS",          // 요구사항 상태
        "REQ_AGREEMENT",       // 요구사항 고객합의
        "SCREEN_MENU_LEVEL1",  // 화면목록 1차메뉴
        "SCREEN_CATEGORY"      // 화면목록 화면유형
    );

    @ModelAttribute("codes")
    public Map<String, List<CommonCode>> commonCodes() {
        Map<String, List<CommonCode>> map = new LinkedHashMap<>();
        for (String group : CODE_GROUPS) {
            map.put(group, commonCodeService.getByGroup(group));
        }
        return map;
    }
}
