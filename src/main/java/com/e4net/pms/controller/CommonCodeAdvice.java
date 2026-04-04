package com.e4net.pms.controller;

import com.e4net.pms.entity.AppMenu;
import com.e4net.pms.entity.CommonCode;
import com.e4net.pms.entity.User;
import com.e4net.pms.repository.AppMenuRepository;
import com.e4net.pms.service.CommonCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 공통코드 전역 주입 — 모든 뷰에서 ${codes['GROUP_CODE']} 로 접근 가능
 */
@ControllerAdvice
@RequiredArgsConstructor
public class CommonCodeAdvice {

    private final CommonCodeService  commonCodeService;
    private final AppMenuRepository  appMenuRepository;

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
        "SCREEN_CATEGORY",     // 화면목록 화면유형
        "PROGRAM_TYPE",        // 프로그램 구분
        "PROGRAM_DIFFICULTY",  // 개발난이도
        "INTERFACE_LINK_TYPE", // 인터페이스 연계구분
        "INTERFACE_METHOD",    // 인터페이스방식
        "INTERFACE_CYCLE",     // 인터페이스 발생주기
        "UNIT_TEST_CATEGORY",          // 단위테스트 분류
        "INTEGRATION_TEST_CATEGORY",  // 통합테스트 분류
        "ROLE_CODE"                    // 권한 코드
    );

    private static final List<String> PRIVILEGED_ROLES = List.of("ROLE_ADMIN", "ROLE_PM", "ROLE_PL");

    /** 현재 요청 URI 전역 주입 — 사이드바 active 감지용 */
    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /** 관리자/PM/PL 여부 전역 주입 — 뷰에서 ${isPrivileged} 로 접근 가능 */
    @ModelAttribute("isPrivileged")
    public boolean isPrivileged(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null && user.getRole() != null && PRIVILEGED_ROLES.contains(user.getRole());
    }

    /** 사이드바 메뉴 전역 주입 — 비권한자는 관리자(M04xxxx) 섹션 제외 */
    @ModelAttribute("sidebarMenus")
    public List<AppMenu> sidebarMenus(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        boolean privileged = user != null && user.getRole() != null && PRIVILEGED_ROLES.contains(user.getRole());

        List<AppMenu> all = appMenuRepository.findAllByOrderByMenuCode().stream()
                .filter(m -> "Y".equals(m.getUseYn()))
                .toList();

        if (privileged) return all;

        // 비권한자: menuCode M04xxxx (관리자 섹션) 전체 제외
        Set<Long> excludedIds = all.stream()
                .filter(m -> m.getMenuCode() != null && m.getMenuCode().startsWith("M04"))
                .map(AppMenu::getId)
                .collect(Collectors.toSet());

        return all.stream().filter(m -> !excludedIds.contains(m.getId())).toList();
    }

    @ModelAttribute("codes")
    public Map<String, List<CommonCode>> commonCodes() {
        Map<String, List<CommonCode>> map = new LinkedHashMap<>();
        for (String group : CODE_GROUPS) {
            map.put(group, commonCodeService.getByGroup(group));
        }
        return map;
    }
}
