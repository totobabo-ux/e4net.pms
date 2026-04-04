package com.e4net.pms.controller;

import com.e4net.pms.entity.User;
import com.e4net.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    // BCryptPasswordEncoder는 stateless이므로 매번 new로 생성해도 무방
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 회원가입 폼 */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /** 회원가입 처리 */
    @PostMapping("/register")
    public String registerSubmit(
            @ModelAttribute User user,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // 사번(회원ID) 필수 체크
        if (user.getEmployeeNo() == null || user.getEmployeeNo().isBlank()) {
            model.addAttribute("error", "사번(회원ID)은 필수 입력 항목입니다.");
            return "register";
        }

        // 사번 중복 체크
        if (userRepository.existsByEmployeeNo(user.getEmployeeNo())) {
            model.addAttribute("error", "이미 사용 중인 사번입니다.");
            return "register";
        }

        // 비밀번호 필수 체크
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            model.addAttribute("error", "비밀번호는 필수 입력 항목입니다.");
            return "register";
        }

        // 비밀번호 일치 체크
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "register";
        }

        // BCrypt 암호화 후 저장
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegId(user.getEmployeeNo());  // 자기 등록 시 본인 사번
        user.setUpdId(user.getEmployeeNo());
        user.setRole("ROLE_USER");            // 기본 권한: 일반사용자
        userRepository.save(user);

        return "redirect:/?registerSuccess=true";
    }
}
