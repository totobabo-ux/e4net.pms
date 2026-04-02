package com.e4net.pms.controller;

import com.e4net.pms.dto.RequirementDto;
import com.e4net.pms.dto.RequirementSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.Requirement;
import com.e4net.pms.service.RequirementService;
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

@Controller
@RequestMapping("/requirement")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    /** 세션 준비 여부 체크 (로그인 + 프로젝트 선택) */
    private boolean isNotReady(HttpSession session) {
        return session.getAttribute("loginUser") == null
            || session.getAttribute("selectedProject") == null;
    }

    private Project getSelectedProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    /** 목록 — 선택된 사업으로 자동 필터 */
    @GetMapping
    public String list(@ModelAttribute("search") RequirementSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session,
                       Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        // 세션 사업 ID 강제 적용
        search.setProjectId(getSelectedProject(session).getId());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Requirement> result = requirementService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("requirementList", result.getContent());
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "requirement/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        Project selectedProject = getSelectedProject(session);
        RequirementDto dto = new RequirementDto();
        dto.setProjectId(selectedProject.getId()); // 사업 자동 설정

        model.addAttribute("requirement", dto);
        model.addAttribute("mode", "create");
        model.addAttribute("selectedProject", selectedProject);
        return "requirement/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@Valid @ModelAttribute("requirement") RequirementDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("selectedProject", getSelectedProject(session));
            return "requirement/form";
        }
        requirementService.save(dto);
        redirectAttributes.addFlashAttribute("successMessage", "요구사항이 등록되었습니다.");
        return "redirect:/requirement";
    }

    /** 상세 */
    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        Requirement entity = requirementService.findById(id);
        model.addAttribute("requirement", requirementService.toDto(entity));
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "requirement/detail";
    }

    /** 수정 폼 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        Requirement entity = requirementService.findById(id);
        model.addAttribute("requirement", requirementService.toDto(entity));
        model.addAttribute("mode", "edit");
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "requirement/form";
    }

    /** 수정 처리 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("requirement") RequirementDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        if (result.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("selectedProject", getSelectedProject(session));
            return "requirement/detail";
        }
        requirementService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "요구사항이 수정되었습니다.");
        return "redirect:/requirement/" + id;
    }

    /** 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        requirementService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "요구사항이 삭제되었습니다.");
        return "redirect:/requirement";
    }
}
