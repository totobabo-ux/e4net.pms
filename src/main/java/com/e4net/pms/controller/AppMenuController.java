package com.e4net.pms.controller;

import com.e4net.pms.dto.AppMenuDto;
import com.e4net.pms.entity.AppMenu;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.AppMenuService;
import com.e4net.pms.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/menu")
@RequiredArgsConstructor
public class AppMenuController {

    private final AppMenuService appMenuService;

    private boolean isNotLogin(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    // ── 화면 ─────────────────────────────────────────────────

    @GetMapping
    public String list(HttpSession session, Model model) {
        if (isNotLogin(session)) return "redirect:/login";
        return "admin/menu/list";
    }

    // ── AJAX ─────────────────────────────────────────────────

    @GetMapping("/tree")
    @ResponseBody
    public List<Map<String, Object>> getTreeData(HttpSession session) {
        if (isNotLogin(session)) return List.of();
        return appMenuService.getTreeData();
    }

    @SuppressWarnings("null")
    @GetMapping("/{id}/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id, HttpSession session) {
        if (isNotLogin(session)) return ResponseEntity.status(403).build();
        try {
            AppMenuDto dto = appMenuService.toDto(appMenuService.findById(id));
            Map<String, Object> res = new HashMap<>();
            res.put("id",          dto.getId());
            res.put("parentId",    dto.getParentId());
            res.put("parentName",  dto.getParentName() != null ? dto.getParentName() : "없음 (루트)");
            res.put("menuCode",    dto.getMenuCode());
            res.put("menuName",    dto.getMenuName());
            res.put("contextPath", dto.getContextPath() != null ? dto.getContextPath() : "");
            res.put("icon",        dto.getIcon() != null ? dto.getIcon() : "");
            res.put("depth",       dto.getDepth());
            res.put("fixedYn",     dto.getFixedYn());
            res.put("useYn",       dto.getUseYn());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@RequestBody AppMenuDto dto, HttpSession session) {
        if (isNotLogin(session)) return ResponseEntity.status(403).build();
        try {
            AppMenu saved = appMenuService.create(dto, getLoginUserId(session));
            AppMenuDto result = appMenuService.toDto(saved);
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

    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                       @RequestBody AppMenuDto dto,
                                                       HttpSession session) {
        if (isNotLogin(session)) return ResponseEntity.status(403).build();
        try {
            appMenuService.update(id, dto, getLoginUserId(session));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, HttpSession session) {
        if (isNotLogin(session)) return ResponseEntity.status(403).build();
        try {
            appMenuService.delete(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── 엑셀 ─────────────────────────────────────────────────

    @GetMapping("/excel/download")
    public void excelDownload(HttpSession session, HttpServletResponse response) throws IOException {
        if (isNotLogin(session)) { response.sendRedirect("/login"); return; }

        List<AppMenu> menus = appMenuService.findAll();
        Map<Long, String> codeMap = menus.stream()
                .collect(Collectors.toMap(AppMenu::getId, AppMenu::getMenuCode));

        String[] headers = { "메뉴코드", "Depth", "상위메뉴코드", "메뉴명", "Context Path", "사용여부" };
        List<Object[]> rows = menus.stream().map(m -> new Object[]{
            m.getMenuCode(),
            m.getDepth(),
            m.getParentId() != null ? codeMap.getOrDefault(m.getParentId(), "-") : "-",
            "  ".repeat(m.getDepth() - 1) + m.getMenuName(),
            m.getContextPath() != null ? m.getContextPath() : "",
            "Y".equals(m.getUseYn()) ? "사용" : "미사용"
        }).collect(Collectors.toList());

        XSSFWorkbook wb = ExcelUtil.createWorkbook("메뉴관리", headers, rows);
        String fileName = "e4net_pms_메뉴관리_" + LocalDate.now() + ".xlsx";
        ExcelUtil.writeToResponse(wb, fileName, response);
    }

    @PostMapping("/excel/upload")
    public String excelUpload(@RequestParam("excelFile") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes ra) throws IOException {
        if (isNotLogin(session)) return "redirect:/login";
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
            return "redirect:/admin/menu";
        }

        List<String[]> rows = ExcelUtil.parseRows(file, 1);
        int[] result = appMenuService.upsertFromExcel(rows, getLoginUserId(session));

        ra.addFlashAttribute("successMessage",
            String.format("엑셀 업로드 완료 — 신규: %d건, 수정: %d건, 건너뜀: %d건",
                result[0], result[1], result[2]));
        return "redirect:/admin/menu";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
