package com.e4net.pms.controller;

import com.e4net.pms.dto.BusinessFlowDto;
import com.e4net.pms.dto.BusinessFlowSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.BusinessFlow;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.BusinessFlowService;
import com.e4net.pms.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/business-flow")
@RequiredArgsConstructor
public class BusinessFlowController {

    private final BusinessFlowService businessFlowService;

    /** 세션 준비 여부 체크 */
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

    /** 목록 */
    @GetMapping
    public String list(@ModelAttribute("search") BusinessFlowSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        search.setProjectId(getSelectedProject(session).getId());
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "systemCategory", "bizCategory", "processId"));
        Page<BusinessFlow> result = businessFlowService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "business-flow/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        Project selectedProject = getSelectedProject(session);
        BusinessFlowDto dto = new BusinessFlowDto();
        dto.setProjectId(selectedProject.getId());

        model.addAttribute("flow", dto);
        model.addAttribute("selectedProject", selectedProject);
        return "business-flow/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@ModelAttribute("flow") BusinessFlowDto dto,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        businessFlowService.save(dto, attachFiles, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "업무흐름이 등록되었습니다.");
        return "redirect:/business-flow";
    }

    /** 상세 */
    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        BusinessFlow entity = businessFlowService.findById(id);
        model.addAttribute("flow", businessFlowService.toDto(entity));
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "business-flow/detail";
    }

    /** 수정 처리 (detail 페이지 내 인라인 수정) */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("flow") BusinessFlowDto dto,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        businessFlowService.update(id, dto, attachFiles, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "업무흐름이 수정되었습니다.");
        return "redirect:/business-flow/" + id;
    }

    /** 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        businessFlowService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "업무흐름이 삭제되었습니다.");
        return "redirect:/business-flow";
    }

    /** 첨부파일 개별 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/attachment/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long id,
                                   @PathVariable Long attachmentId,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        businessFlowService.deleteAttachment(attachmentId);
        redirectAttributes.addFlashAttribute("successMessage", "첨부파일이 삭제되었습니다.");
        return "redirect:/business-flow/" + id;
    }

    /** 첨부파일 다운로드 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/attachment/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @PathVariable Long attachmentId,
                                             HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            AttachFile attachment = businessFlowService.findAttachmentById(attachmentId);
            Path filePath = businessFlowService.getAttachmentFilePath(attachmentId);
            Resource resource = new PathResource(filePath);
            if (!resource.exists()) return ResponseEntity.notFound().build();
            String originalName = attachment.getFileName() != null ? attachment.getFileName() : "download";
            String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 엑셀 다운로드 */
    @GetMapping("/excel/download")
    public void excelDownload(HttpSession session, HttpServletResponse response) throws IOException {
        if (isNotReady(session)) { response.sendRedirect("/project-select"); return; }

        Project project = getSelectedProject(session);
        List<BusinessFlow> list = businessFlowService.findAllByProject(project.getId());

        String[] headers = { "시스템구분", "업무구분", "프로세스ID", "프로세스명" };
        List<Object[]> rows = list.stream().map(f -> new Object[]{
            f.getSystemCategory(), f.getBizCategory(), f.getProcessId(), f.getProcessName()
        }).collect(Collectors.toList());

        XSSFWorkbook wb = ExcelUtil.createWorkbook("업무흐름", headers, rows);
        String fileName = project.getProjectName() + "_업무흐름_" + LocalDate.now() + ".xlsx";
        ExcelUtil.writeToResponse(wb, fileName, response);
    }

    /** 엑셀 업로드 */
    @PostMapping("/excel/upload")
    public String excelUpload(@RequestParam("excelFile") MultipartFile file,
                              HttpSession session, RedirectAttributes ra) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
            return "redirect:/business-flow";
        }

        List<String[]> rows = ExcelUtil.parseRows(file, 1);
        Long projectId = getSelectedProject(session).getId();
        int[] result = businessFlowService.upsertFromExcel(rows, projectId, getLoginUserId(session));

        ra.addFlashAttribute("successMessage",
            String.format("엑셀 업로드 완료 — 신규: %d건, 수정: %d건, 건너뜀: %d건",
                result[0], result[1], result[2]));
        return "redirect:/business-flow";
    }

    /** 예외 처리 */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/business-flow";
    }
}
