package com.e4net.pms.controller;

import com.e4net.pms.entity.CommonCode;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.repository.CommunityRepository;
import com.e4net.pms.repository.IssueRepository;
import com.e4net.pms.repository.RiskRepository;
import com.e4net.pms.repository.UserRepository;
import com.e4net.pms.service.CommonCodeService;
import com.e4net.pms.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProjectService      projectService;
    private final UserRepository      userRepository;
    private final IssueRepository     issueRepository;
    private final RiskRepository      riskRepository;
    private final CommunityRepository communityRepository;
    private final CommonCodeService   commonCodeService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 로그인 페이지 */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** 로그인 처리 → 프로젝트 선택 화면으로 이동 */
    @PostMapping("/login")
    public String login(@RequestParam("username") String employeeNo,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes ra) {

        Optional<User> userOpt = userRepository.findByEmployeeNo(employeeNo);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            ra.addFlashAttribute("errorMessage", "사번 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/";
        }

        User loginUser = userOpt.get();
        session.setAttribute("loginUser", loginUser);

        // role 코드명 세션 저장 (헤더 표시용)
        String roleName = (loginUser.getRole() == null) ? null :
                commonCodeService.getByGroup("ROLE_CODE").stream()
                        .filter(c -> loginUser.getRole().equals(c.getCode()))
                        .map(CommonCode::getCodeName)
                        .findFirst().orElse(null);
        session.setAttribute("loginUserRoleName", roleName);

        return "redirect:/project-select";
    }

    /** 로그아웃 */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("successMessage", "로그아웃되었습니다.");
        return "redirect:/";
    }

    /** 프로젝트 선택 화면 */
    @GetMapping("/project-select")
    public String projectSelect(HttpSession session, Model model) {
        if (session.getAttribute("loginUser") == null) return "redirect:/";

        List<Project> projects = projectService.findAll();
        model.addAttribute("projects", projects);
        return "project-select";
    }

    /** 프로젝트 선택 처리 */
    @PostMapping("/project-select")
    public String projectSelectSubmit(@RequestParam("projectId") Long projectId,
                                      HttpSession session) {
        if (session.getAttribute("loginUser") == null) return "redirect:/";

        Project project = projectService.findById(projectId);
        session.setAttribute("selectedProject", project);
        return "redirect:/home";
    }

    /** 대시보드 (사업 Home) — 로그인 + 프로젝트 선택 필수 */
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        if (session.getAttribute("loginUser") == null) return "redirect:/";
        if (session.getAttribute("selectedProject") == null) return "redirect:/project-select";

        Project project = (Project) session.getAttribute("selectedProject");
        Long projectId  = project.getId();

        // 공지사항 최근 5건
        model.addAttribute("notices",
            communityRepository.findTop5ByCommunityTypeOrderByPostDateDescIdDesc("공지사항"));

        // 자료실 최근 5건
        model.addAttribute("archives",
            communityRepository.findTop5ByCommunityTypeOrderByPostDateDescIdDesc("자료실"));

        // 이슈 현황 — 조치완료 제외 최근 3건
        model.addAttribute("openIssues",
            issueRepository.findTop3ByProject_IdAndActionStatusNotOrderByRaisedDateDesc(projectId, "조치완료"));

        // 위험 현황 — 종료 제외 최근 3건
        model.addAttribute("openRisks",
            riskRepository.findTop3ByProject_IdAndStatusNotOrderByIdentifiedDateDesc(projectId, "종료"));

        model.addAttribute("selectedProject", project);
        return "home";
    }
}
