package com.e4net.pms.controller;

import com.e4net.pms.dto.ManpowerDto;
import com.e4net.pms.dto.ManpowerSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.ProjectManpower;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.ManpowerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manpower")
@RequiredArgsConstructor
public class ManpowerController {

    private final ManpowerService manpowerService;

    /** 로그인 사용자 ID 반환 */
    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    /** 세션에서 선택된 프로젝트 반환 (없으면 null) */
    private Project getSelectedProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    /** 세션 + 프로젝트 선택 여부 체크 */
    private boolean isNotReady(HttpSession session) {
        return session.getAttribute("loginUser") == null
            || session.getAttribute("selectedProject") == null;
    }

    /** 목록 — 선택된 사업으로 자동 필터 */
    @GetMapping
    public String list(@ModelAttribute("search") ManpowerSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session,
                       Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        // 세션 사업 ID 강제 적용
        Project selectedProject = getSelectedProject(session);
        search.setProjectId(selectedProject.getId());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProjectManpower> result = manpowerService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("manpowerList", result.getContent());
        model.addAttribute("selectedProject", selectedProject);
        return "manpower/list";
    }

    /** 등록 폼 — 선택 사업 자동 선택 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        Project selectedProject = getSelectedProject(session);
        ManpowerDto dto = new ManpowerDto();
        dto.setProjectId(selectedProject.getId());       // 사업 자동 선택
        dto.setProjectName(selectedProject.getProjectName());

        model.addAttribute("manpower", dto);
        model.addAttribute("mode", "create");
        model.addAttribute("selectedProject", selectedProject);
        addFormAttributes(model, selectedProject);
        return "manpower/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@Valid @ModelAttribute("manpower") ManpowerDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        if (result.hasErrors()) {
            Project selectedProject = getSelectedProject(session);
            model.addAttribute("mode", "create");
            model.addAttribute("selectedProject", selectedProject);
            addFormAttributes(model, selectedProject);
            return "manpower/form";
        }
        manpowerService.save(dto, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "인력이 등록되었습니다.");
        return "redirect:/manpower";
    }

    /** 상세 */
    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        ProjectManpower entity = manpowerService.findById(id);
        model.addAttribute("manpower", manpowerService.toDto(entity));
        return "manpower/detail";
    }

    /** 수정 폼 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        Project selectedProject = getSelectedProject(session);
        ProjectManpower entity = manpowerService.findById(id);
        model.addAttribute("manpower", manpowerService.toDto(entity));
        model.addAttribute("mode", "edit");
        model.addAttribute("selectedProject", selectedProject);
        addFormAttributes(model, selectedProject);
        return "manpower/form";
    }

    /** 수정 처리 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("manpower") ManpowerDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        if (result.hasErrors()) {
            Project selectedProject = getSelectedProject(session);
            model.addAttribute("mode", "edit");
            model.addAttribute("selectedProject", selectedProject);
            addFormAttributes(model, selectedProject);
            return "manpower/form";
        }
        manpowerService.update(id, dto, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "인력 정보가 수정되었습니다.");
        return "redirect:/manpower/" + id;
    }

    /** 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        manpowerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "인력 정보가 삭제되었습니다.");
        return "redirect:/manpower";
    }

    /** 폼 공통 속성: 선택된 사업만 드롭다운에 표시 */
    private void addFormAttributes(Model model, Project selectedProject) {
        // 사업관리 컨텍스트에서는 선택된 프로젝트만 제공 (폼에서 변경 불가)
        // 공통코드(gradeCodes, inputTypeCodes 등)는 CommonCodeAdvice 에서 자동 주입
        model.addAttribute("projects", List.of(selectedProject));
    }
}
