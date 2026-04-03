package com.e4net.pms.controller;

import com.e4net.pms.dto.MenuDto;
import com.e4net.pms.entity.Menu;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.MenuService;
import com.e4net.pms.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/menu-structure")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    private boolean isNotReady(HttpSession session) {
        return session.getAttribute("loginUser") == null
                || session.getAttribute("selectedProject") == null;
    }

    private Project getSelectedProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    // ── 화면 ─────────────────────────────────────────────────

    /** 메인 페이지 */
    @GetMapping
    public String list(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "menu-structure/list";
    }

    // ── AJAX ─────────────────────────────────────────────────

    /** jsTree 트리 데이터 */
    @GetMapping("/tree")
    @ResponseBody
    public List<Map<String, Object>> getTreeData(HttpSession session) {
        if (isNotReady(session)) return List.of();
        return menuService.getTreeData(getSelectedProject(session).getId());
    }

    /** 메뉴 상세 조회 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id,
                                                          HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            MenuDto dto = menuService.toDto(menuService.findById(id));
            Map<String, Object> res = new HashMap<>();
            res.put("id",          dto.getId());
            res.put("parentId",    dto.getParentId());
            res.put("parentName",  dto.getParentName() != null ? dto.getParentName() : "없음 (루트)");
            res.put("menuCode",    dto.getMenuCode());
            res.put("menuName",    dto.getMenuName());
            res.put("contextPath", dto.getContextPath() != null ? dto.getContextPath() : "");
            res.put("depth",       dto.getDepth());
            res.put("fixedYn",     dto.getFixedYn());
            res.put("useYn",       dto.getUseYn());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 메뉴 추가 */
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@RequestBody MenuDto dto,
                                                       HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            dto.setProjectId(getSelectedProject(session).getId());
            Menu saved = menuService.create(dto, getLoginUserId(session));
            MenuDto result = menuService.toDto(saved);
            Map<String, Object> res = new HashMap<>();
            res.put("success",  true);
            res.put("id",       result.getId());
            res.put("menuCode", result.getMenuCode());
            res.put("menuName", result.getMenuName());
            res.put("depth",    result.getDepth());
            res.put("parentId", result.getParentId());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 메뉴 수정 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                       @RequestBody MenuDto dto,
                                                       HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            menuService.update(id, dto, getLoginUserId(session));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 메뉴 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id,
                                                       HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            menuService.delete(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── 엑셀 ─────────────────────────────────────────────────

    /**
     * 메뉴구조도 엑셀 다운로드
     * 컬럼: 메뉴코드, Depth, 상위메뉴코드, 메뉴명(들여쓰기), Context Path, 사용여부
     */
    @GetMapping("/excel/download")
    public void excelDownload(HttpSession session, HttpServletResponse response) throws IOException {
        if (isNotReady(session)) { response.sendRedirect("/project-select"); return; }

        Project project = getSelectedProject(session);
        List<Menu> menus = menuService.findAllByProject(project.getId());

        // 부모코드 빠른 조회용 Map
        Map<Long, String> codeMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Menu::getMenuCode));

        String[] headers = { "메뉴코드", "Depth", "상위메뉴코드", "메뉴명", "Context Path", "사용여부" };
        List<Object[]> rows = menus.stream().map(m -> new Object[]{
            m.getMenuCode(),
            m.getDepth(),
            m.getParentId() != null ? codeMap.getOrDefault(m.getParentId(), "-") : "-",
            "  ".repeat(m.getDepth() - 1) + m.getMenuName(),   // 들여쓰기로 계층 표현
            m.getContextPath() != null ? m.getContextPath() : "",
            "Y".equals(m.getUseYn()) ? "사용" : "미사용"
        }).collect(Collectors.toList());

        XSSFWorkbook wb = ExcelUtil.createWorkbook("메뉴구조", headers, rows);
        String fileName = project.getProjectName() + "_메뉴구조_" + LocalDate.now() + ".xlsx";
        ExcelUtil.writeToResponse(wb, fileName, response);
    }

    /** 예외 처리 */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
