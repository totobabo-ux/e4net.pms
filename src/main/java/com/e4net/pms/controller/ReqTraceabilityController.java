package com.e4net.pms.controller;

import com.e4net.pms.dto.RequirementSearchDto;
import com.e4net.pms.dto.TraceTargetItem;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.ReqTraceabilityService;
import com.e4net.pms.service.RequirementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/req-traceability")
@RequiredArgsConstructor
public class ReqTraceabilityController {

    private final ReqTraceabilityService traceService;
    private final RequirementService     requirementService;

    private boolean isNotReady(HttpSession session) {
        return session.getAttribute("loginUser") == null
            || session.getAttribute("selectedProject") == null;
    }

    private Project getProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    private String getUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    /** 메인 페이지 — 요구사항 목록 렌더링 */
    @GetMapping
    public String page(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session, Model model) {

        if (isNotReady(session)) return "redirect:/project-select";

        Project project = getProject(session);

        RequirementSearchDto search = new RequirementSearchDto();
        search.setProjectId(project.getId());
        search.setTitle(title.isBlank() ? null : title);
        search.setStatus(status.isBlank() ? null : status);

        model.addAttribute("reqPage",
                requirementService.search(search,
                        PageRequest.of(page, 20, Sort.by("reqCode").ascending())));
        model.addAttribute("title", title);
        model.addAttribute("status", status);
        model.addAttribute("selectedProject", project);
        return "requirement/traceability";
    }

    /** 탭 데이터 API — 대상 목록 + 연결 여부 (JSON) */
    @GetMapping("/tab-data")
    @ResponseBody
    public ResponseEntity<List<TraceTargetItem>> tabData(
            @RequestParam Long reqId,
            @RequestParam String type,
            HttpSession session) {

        if (isNotReady(session)) return ResponseEntity.status(403).build();
        Long projectId = getProject(session).getId();
        return ResponseEntity.ok(traceService.getTabItems(projectId, reqId, type));
    }

    /** 연관관계 저장 API (JSON) */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> save(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        if (isNotReady(session)) return ResponseEntity.status(403).build();
        Long projectId = getProject(session).getId();
        Long reqId     = Long.valueOf(body.get("reqId").toString());
        String type    = body.get("type").toString();

        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("targetIds");
        List<Long> targetIds = rawIds.stream().map(i -> (long) i).toList();

        traceService.save(projectId, reqId, type, targetIds, getUserId(session));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
