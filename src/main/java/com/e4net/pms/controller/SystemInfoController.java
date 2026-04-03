package com.e4net.pms.controller;

import com.e4net.pms.dto.ColumnInfoDto;
import com.e4net.pms.dto.DbConnectRequest;
import com.e4net.pms.dto.TableInfoDto;
import com.e4net.pms.service.SystemInfoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 시스템 정보 컨트롤러 — 관리자 > 시스템 정보 > 데이터베이스
 */
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class SystemInfoController {

    private final SystemInfoService systemInfoService;

    private boolean isNotLoggedIn(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    /** 데이터베이스 화면 */
    @GetMapping("/database")
    public String database(HttpSession session, Model model) {
        if (isNotLoggedIn(session)) return "redirect:/";
        model.addAttribute("activePage", "exec-design-table");
        return "admin/system/database";
    }

    /** DB 연결 및 테이블 목록 조회 (AJAX) */
    @PostMapping("/database/connect")
    @ResponseBody
    public ResponseEntity<?> connect(@RequestBody DbConnectRequest req, HttpSession session) {
        if (isNotLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            List<TableInfoDto> tables = systemInfoService.getTables(req);
            // 세션에 접속 정보 저장 (엑셀 다운로드 시 재사용)
            session.setAttribute("dbConnectInfo", req);
            return ResponseEntity.ok(tables);
        } catch (SQLException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "DB 연결 실패: " + e.getMessage()));
        }
    }

    /** 특정 테이블의 컬럼 정보 조회 (AJAX — 모달용) */
    @PostMapping("/database/columns")
    @ResponseBody
    public ResponseEntity<?> columns(@RequestBody Map<String, String> body, HttpSession session) {
        if (isNotLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        DbConnectRequest req = (DbConnectRequest) session.getAttribute("dbConnectInfo");
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "먼저 데이터베이스에 연결하세요."));
        }
        String tableName = body.get("tableName");
        if (tableName == null || tableName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "테이블명이 없습니다."));
        }
        try {
            List<ColumnInfoDto> columns = systemInfoService.getColumns(req, tableName);
            return ResponseEntity.ok(columns);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "조회 실패: " + e.getMessage()));
        }
    }

    /** 테이블 목록 엑셀 다운로드 */
    @GetMapping("/database/download-list")
    public void downloadList(HttpSession session, HttpServletResponse response) throws Exception {
        if (isNotLoggedIn(session)) {
            response.sendRedirect("/");
            return;
        }
        DbConnectRequest req = (DbConnectRequest) session.getAttribute("dbConnectInfo");
        if (req == null) {
            response.sendError(400, "먼저 데이터베이스에 연결하세요.");
            return;
        }
        systemInfoService.downloadTableList(req, response);
    }

    /** 테이블설계서 엑셀 다운로드 */
    @GetMapping("/database/download")
    public void download(HttpSession session, HttpServletResponse response) throws Exception {
        if (isNotLoggedIn(session)) {
            response.sendRedirect("/");
            return;
        }
        DbConnectRequest req = (DbConnectRequest) session.getAttribute("dbConnectInfo");
        if (req == null) {
            response.sendError(400, "먼저 데이터베이스에 연결하세요.");
            return;
        }
        systemInfoService.downloadTableDesign(req, response);
    }
}
