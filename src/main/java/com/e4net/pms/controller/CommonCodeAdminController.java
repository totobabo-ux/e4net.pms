package com.e4net.pms.controller;

import com.e4net.pms.entity.CommonCode;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.CommonCodeService;
import com.e4net.pms.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/common-code")
@RequiredArgsConstructor
public class CommonCodeAdminController {

    private final CommonCodeService commonCodeService;

    /** 세션 로그인 체크 */
    private boolean isNotLoggedIn(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    /** 공통코드 관리 메인 화면 */
    @GetMapping
    public String list(HttpSession session, Model model) {
        if (isNotLoggedIn(session)) return "redirect:/login";

        List<String> groupCodes = commonCodeService.findDistinctGroupCodes();
        List<CommonCode> allCodes = commonCodeService.findAll();

        model.addAttribute("groupCodes", groupCodes);
        model.addAttribute("allCodes", allCodes);
        return "admin/system/common-code";
    }

    /**
     * 단건 저장 (신규/수정) — AJAX JSON
     * Request body: { id, groupCode, code, codeName, sortOrder, useYn }
     */
    @PostMapping(value = "/save", produces = "application/json")
    @ResponseBody
    public Map<String, Object> save(@RequestBody CommonCode req, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (isNotLoggedIn(session)) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }
        try {
            String userId = getLoginUserId(session);
            CommonCode saved;
            if (req.getId() != null) {
                saved = commonCodeService.update(req.getId(), req, userId);
            } else {
                saved = commonCodeService.saveNew(req, userId);
            }
            result.put("success", true);
            result.put("id", saved.getId());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage() != null ? e.getMessage() : "저장 실패");
        }
        return result;
    }

    /** 삭제 — AJAX */
    @PostMapping(value = "/delete/{id}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (isNotLoggedIn(session)) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }
        try {
            commonCodeService.delete(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage() != null ? e.getMessage() : "삭제 실패");
        }
        return result;
    }

    /** 엑셀 다운로드 — 전체 공통코드 */
    @GetMapping("/excel/download")
    public void excelDownload(HttpSession session, HttpServletResponse response) throws IOException {
        if (isNotLoggedIn(session)) { response.sendRedirect("/login"); return; }

        List<CommonCode> list = commonCodeService.findAll();
        String[] headers = { "그룹코드", "코드", "코드명", "정렬순서", "사용여부" };
        List<Object[]> rows = list.stream().map(c -> new Object[]{
            c.getGroupCode(), c.getCode(), c.getCodeName(), c.getSortOrder(), c.getUseYn()
        }).collect(Collectors.toList());

        XSSFWorkbook wb = ExcelUtil.createWorkbook("공통코드목록", headers, rows);
        String fileName = "공통코드_" + LocalDate.now() + ".xlsx";
        ExcelUtil.writeToResponse(wb, fileName, response);
    }

    /** 엑셀 업로드 — 그룹코드+코드 기준 upsert */
    @PostMapping("/excel/upload")
    public String excelUpload(@RequestParam("excelFile") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes ra) throws IOException {
        if (isNotLoggedIn(session)) return "redirect:/login";
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
            return "redirect:/admin/common-code";
        }

        List<String[]> rows = ExcelUtil.parseRows(file, 1);
        int[] result = commonCodeService.upsertFromExcel(rows, getLoginUserId(session));

        ra.addFlashAttribute("successMessage",
            String.format("엑셀 업로드 완료 — 신규: %d건, 수정: %d건, 건너뜀: %d건",
                result[0], result[1], result[2]));
        return "redirect:/admin/common-code";
    }
}
