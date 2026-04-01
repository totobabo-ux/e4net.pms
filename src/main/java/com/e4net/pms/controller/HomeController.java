package com.e4net.pms.controller;

import com.e4net.pms.entity.Project;
import com.e4net.pms.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProjectService projectService;

    /** 로그인 페이지 */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** 대시보드 (사업 Home) */
    @GetMapping("/home")
    public String home(Model model) {
        List<Project> allProjects = projectService.findAll();

        long total = allProjects.size();
        long inProgress = allProjects.stream().filter(p -> "진행".equals(p.getStatus())).count();
        long completed = allProjects.stream().filter(p -> "완료".equals(p.getStatus())).count();
        long sales = allProjects.stream().filter(p -> "영업".equals(p.getStatus())).count();

        // 최근 5건
        List<Project> recent = allProjects.stream().limit(5).toList();

        model.addAttribute("totalProjects", total);
        model.addAttribute("inProgressProjects", inProgress);
        model.addAttribute("completedProjects", completed);
        model.addAttribute("salesProjects", sales);
        model.addAttribute("recentProjects", recent);

        return "home";
    }
}
