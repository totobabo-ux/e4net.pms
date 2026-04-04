package com.e4net.pms.controller;

import com.e4net.pms.dto.UserSearchDto;
import com.e4net.pms.entity.User;
import com.e4net.pms.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 세션 체크 공통 */
    private boolean isNotLoggedIn(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    /** 사용자 목록 (검색 + 페이징) */
    @GetMapping
    public String list(UserSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session,
                       Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";

        Page<User> userPage = userRepository.searchUsers(
                search.getEmployeeNo(),
                search.getName(),
                search.getCompany(),
                PageRequest.of(page, 15, Sort.by("id").descending())
        );

        model.addAttribute("page", userPage);
        model.addAttribute("search", search);
        return "admin/user-list";
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           HttpSession session,
                           Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + id));

        model.addAttribute("user", user);
        return "admin/user-form";
    }

    /** 수정 처리 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) String company,
                         @RequestParam(required = false) String department,
                         @RequestParam(required = false) String position,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String newPassword,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (isNotLoggedIn(session)) return "redirect:/";

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + id));

        // 기본 정보 수정
        user.setName(name);
        user.setCompany(company);
        user.setDepartment(department);
        user.setPosition(position);
        user.setPhone(phone);
        user.setEmail(email);

        // 비밀번호는 값이 있을 때만 변경
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        user.setUpdId(getLoginUserId(session));
        userRepository.save(user);
        ra.addFlashAttribute("successMessage", "사용자 정보가 수정되었습니다.");
        return "redirect:/admin/users";
    }

    /** 내 정보 수정 폼 */
    @GetMapping("/my-profile")
    public String myProfileForm(HttpSession session, Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";

        User loginUser = (User) session.getAttribute("loginUser");
        User user = userRepository.findById(loginUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        model.addAttribute("user", user);
        return "my-profile";
    }

    /** 내 정보 수정 처리 */
    @PostMapping("/my-profile")
    public String myProfileUpdate(@RequestParam String name,
                                  @RequestParam(required = false) String company,
                                  @RequestParam(required = false) String department,
                                  @RequestParam(required = false) String position,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) String email,
                                  @RequestParam(required = false) String newPassword,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        if (isNotLoggedIn(session)) return "redirect:/";

        User loginUser = (User) session.getAttribute("loginUser");
        User user = userRepository.findById(loginUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setName(name);
        user.setCompany(company);
        user.setDepartment(department);
        user.setPosition(position);
        user.setPhone(phone);
        user.setEmail(email);

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        user.setUpdId(loginUser.getEmployeeNo());
        User saved = userRepository.save(user);

        // 세션의 사용자 정보도 갱신
        session.setAttribute("loginUser", saved);

        ra.addFlashAttribute("successMessage", "내 정보가 수정되었습니다.");
        return "redirect:/home";
    }

    /** 사용자 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (isNotLoggedIn(session)) return "redirect:/";

        userRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "사용자가 삭제되었습니다.");
        return "redirect:/admin/users";
    }
}
