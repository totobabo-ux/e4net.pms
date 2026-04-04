package com.e4net.pms.controller;

import com.e4net.pms.entity.CommonCode;
import com.e4net.pms.entity.User;
import com.e4net.pms.repository.UserRepository;
import com.e4net.pms.service.CommonCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleManagementController {

    private final UserRepository userRepository;
    private final CommonCodeService commonCodeService;

    /** 세션 체크 공통 */
    private boolean isNotLoggedIn(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    /** 권한 관리 화면 */
    @GetMapping
    public String roleManagement(HttpSession session, Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";

        List<CommonCode> roleCodes = commonCodeService.getByGroup("ROLE_CODE");
        model.addAttribute("roleCodes", roleCodes);
        return "admin/role-management";
    }

    /** 특정 권한의 사용자 목록 (AJAX) */
    @GetMapping("/users")
    @ResponseBody
    public List<User> getUsersByRole(@RequestParam String roleCode,
                                     HttpSession session) {
        if (isNotLoggedIn(session)) return List.of();
        return userRepository.findByRoleOrderByNameAsc(roleCode);
    }

    /** 전체 사용자 목록 (AJAX - 권한 없는 사용자 포함) */
    @GetMapping("/all-users")
    @ResponseBody
    public List<User> getAllUsers(HttpSession session) {
        if (isNotLoggedIn(session)) return List.of();
        return userRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    String na = a.getName() == null ? "" : a.getName();
                    String nb = b.getName() == null ? "" : b.getName();
                    return na.compareTo(nb);
                })
                .toList();
    }

    /** 사용자 권한 저장 (AJAX) */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveRoles(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        if (isNotLoggedIn(session))
            return ResponseEntity.status(401).body(Map.of("result", "로그인이 필요합니다."));

        String roleCode = (String) payload.get("roleCode");
        @SuppressWarnings("unchecked")
        List<Integer> userIds = (List<Integer>) payload.get("userIds");

        if (roleCode == null || roleCode.isBlank())
            return ResponseEntity.badRequest().body(Map.of("result", "권한코드가 없습니다."));

        // 기존 해당 권한 사용자들의 권한 초기화 (다른 권한으로 이동한 경우 처리)
        List<User> previousUsers = userRepository.findByRoleOrderByNameAsc(roleCode);
        for (User u : previousUsers) {
            u.setRole(null);
            userRepository.save(u);
        }

        // 선택된 사용자들에게 권한 부여
        if (userIds != null) {
            for (Integer uid : userIds) {
                userRepository.findById(uid.longValue()).ifPresent(u -> {
                    u.setRole(roleCode);
                    userRepository.save(u);
                });
            }
        }

        return ResponseEntity.ok(Map.of("result", "저장되었습니다."));
    }

    /** 권한 전체 매트릭스 일괄 저장 (AJAX) */
    @PostMapping("/save-all")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveAll(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        if (isNotLoggedIn(session))
            return ResponseEntity.status(401).body(Map.of("result", "로그인이 필요합니다."));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> userRoles = (List<Map<String, Object>>) payload.get("userRoles");
        if (userRoles == null)
            return ResponseEntity.badRequest().body(Map.of("result", "데이터가 없습니다."));

        for (Map<String, Object> item : userRoles) {
            Integer userId = (Integer) item.get("userId");
            String roleCode = (String) item.get("roleCode");   // null 허용

            if (userId == null) continue;
            userRepository.findById(userId.longValue()).ifPresent(u -> {
                u.setRole(roleCode);  // null 이면 권한 해제
                userRepository.save(u);
            });
        }

        return ResponseEntity.ok(Map.of("result", "저장되었습니다."));
    }
}
