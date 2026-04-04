package com.e4net.pms.controller;

import com.e4net.pms.dto.ProjectDto;
import com.e4net.pms.dto.ProjectSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    private boolean isNotLoggedIn(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    /** 목록 + 검색 — 관리자: 모든 프로젝트 조회 */
    @GetMapping
    public String list(@ModelAttribute("search") ProjectSearchDto search,
                       HttpSession session, Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";
        List<Project> projects = projectService.search(search);
        model.addAttribute("projects", projects);
        model.addAttribute("totalCount", projects.size());
        return "project/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";
        model.addAttribute("project", new ProjectDto());
        model.addAttribute("mode", "create");
        return "project/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@Valid @ModelAttribute("project") ProjectDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "project/form";
        }
        projectService.save(dto, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "프로젝트가 등록되었습니다.");
        return "redirect:/projects";
    }

    /** 수정 폼 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";
        Project project = projectService.findById(id);
        model.addAttribute("project", projectService.toDto(project));
        model.addAttribute("mode", "edit");
        return "project/form";
    }

    /** 수정 처리 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("project") ProjectDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "project/form";
        }
        projectService.update(id, dto, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "프로젝트가 수정되었습니다.");
        return "redirect:/projects";
    }


}
