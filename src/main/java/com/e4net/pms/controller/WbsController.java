package com.e4net.pms.controller;

import com.e4net.pms.dto.WbsDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.WbsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/wbs")
@RequiredArgsConstructor
public class WbsController {

    private final WbsService wbsService;

    /** 세션 준비 여부 체크 */
    private boolean isNotReady(HttpSession session) {
        return session.getAttribute("loginUser") == null
            || session.getAttribute("selectedProject") == null;
    }

    /** 세션에서 선택된 사업 조회 */
    private Project getSelectedProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    /** WBS 목록 화면 */
    @GetMapping
    public String list(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        Project selectedProject = getSelectedProject(session);
        List<WbsDto> wbsList = wbsService.findByProjectId(selectedProject.getId());

        model.addAttribute("wbsList", wbsList);
        model.addAttribute("selectedProject", selectedProject);
        return "wbs/list";
    }

    /** 전체 저장 (AJAX JSON) */
    @PostMapping("/batch-save")
    @ResponseBody
    public ResponseEntity<List<WbsDto>> batchSave(
            @RequestBody List<WbsDto> dtos,
            HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();

        Project selectedProject = getSelectedProject(session);
        List<WbsDto> updated = wbsService.batchSave(selectedProject.getId(), dtos, getLoginUserId(session));
        return ResponseEntity.ok(updated);
    }

    /** 행 삭제 (AJAX) */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        wbsService.delete(id);
        return ResponseEntity.ok().build();
    }
}
