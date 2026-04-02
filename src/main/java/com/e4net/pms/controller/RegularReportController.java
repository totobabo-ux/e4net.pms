package com.e4net.pms.controller;

import com.e4net.pms.dto.CustomerReportDto;
import com.e4net.pms.dto.CustomerReportSearchDto;
import com.e4net.pms.entity.CustomerReport;
import com.e4net.pms.entity.Project;
import com.e4net.pms.service.CustomerReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/regular-report")
@RequiredArgsConstructor
public class RegularReportController {

    private final CustomerReportService customerReportService;

    // 정기보고 보고구분 목록
    private static final List<String> REGULAR_TYPES = List.of("착수보고", "중간보고", "완료보고", "기타보고");

    /** 세션 준비 여부 체크 */
    private boolean isNotReady(HttpSession session) {
        return session.getAttribute("loginUser") == null
            || session.getAttribute("selectedProject") == null;
    }

    private Project getSelectedProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    /** 목록 */
    @GetMapping
    public String list(@ModelAttribute("search") CustomerReportSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        search.setProjectId(getSelectedProject(session).getId());

        // reportType 이 정기보고 유형이 아니면 무시 (보안)
        if (search.getReportType() != null && !REGULAR_TYPES.contains(search.getReportType())) {
            search.setReportType(null);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<CustomerReport> result = customerReportService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("reportList", result.getContent());
        model.addAttribute("selectedProject", getSelectedProject(session));
        model.addAttribute("regularTypes", REGULAR_TYPES);
        return "regular-report/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        Project selectedProject = getSelectedProject(session);
        CustomerReportDto dto = new CustomerReportDto();
        dto.setProjectId(selectedProject.getId());
        model.addAttribute("report", dto);
        model.addAttribute("mode", "create");
        model.addAttribute("selectedProject", selectedProject);
        model.addAttribute("regularTypes", REGULAR_TYPES);
        return "regular-report/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@ModelAttribute("report") CustomerReportDto dto,
                         @RequestParam(value = "attachFile", required = false) MultipartFile attachFile,
                         HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        customerReportService.save(dto, attachFile);
        redirectAttributes.addFlashAttribute("successMessage", "정기보고가 등록되었습니다.");
        return "redirect:/regular-report";
    }

    /** 상세 */
    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        CustomerReport entity = customerReportService.findById(id);
        model.addAttribute("report", customerReportService.toDto(entity));
        model.addAttribute("selectedProject", getSelectedProject(session));
        model.addAttribute("regularTypes", REGULAR_TYPES);
        return "regular-report/detail";
    }

    /** 수정 폼 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        CustomerReport entity = customerReportService.findById(id);
        model.addAttribute("report", customerReportService.toDto(entity));
        model.addAttribute("mode", "edit");
        model.addAttribute("selectedProject", getSelectedProject(session));
        model.addAttribute("regularTypes", REGULAR_TYPES);
        return "regular-report/form";
    }

    /** 수정 처리 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("report") CustomerReportDto dto,
                         @RequestParam(value = "attachFile", required = false) MultipartFile attachFile,
                         HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        customerReportService.update(id, dto, attachFile);
        redirectAttributes.addFlashAttribute("successMessage", "정기보고가 수정되었습니다.");
        return "redirect:/regular-report/" + id;
    }

    /** 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        customerReportService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "정기보고가 삭제되었습니다.");
        return "redirect:/regular-report";
    }

    /** 존재하지 않는 리소스 접근 예외 처리 */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/regular-report";
    }

    /** 파일 다운로드 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            Path filePath = customerReportService.getFilePath(id);
            CustomerReport entity = customerReportService.findById(id);
            Resource resource = new PathResource(filePath);
            if (!resource.exists()) return ResponseEntity.notFound().build();

            String originalName = entity.getAttachFileName() != null ? entity.getAttachFileName() : "download";
            String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
