package com.e4net.pms.controller;

import com.e4net.pms.dto.ProjectDto;
import com.e4net.pms.dto.ProjectSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** 목록 + 검색 */
    @GetMapping
    public String list(@ModelAttribute("search") ProjectSearchDto search, Model model) {
        // 진행상태 기본값 (처음 진입 시)
        if (search.getStatusList() == null) {
            search.setStatusList(Arrays.asList("영업", "진행", "완료", "종료"));
        }
        List<Project> projects = projectService.search(search);
        model.addAttribute("projects", projects);
        model.addAttribute("totalCount", projects.size());
        return "projects";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("project", new ProjectDto());
        model.addAttribute("mode", "create");
        return "project-form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@Valid @ModelAttribute("project") ProjectDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "project-form";
        }
        projectService.save(dto);
        redirectAttributes.addFlashAttribute("successMessage", "프로젝트가 등록되었습니다.");
        return "redirect:/projects";
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        model.addAttribute("project", projectService.toDto(project));
        model.addAttribute("mode", "edit");
        return "project-form";
    }

    /** 수정 처리 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("project") ProjectDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "project-form";
        }
        projectService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "프로젝트가 수정되었습니다.");
        return "redirect:/projects";
    }

    /** 삭제 처리 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projectService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "프로젝트가 삭제되었습니다.");
        return "redirect:/projects";
    }
}
