package com.e4net.pms.controller;

import com.e4net.pms.dto.UnitTestDto;
import com.e4net.pms.dto.UnitTestSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.UnitTest;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.UnitTestService;
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
@RequestMapping("/unit-test")
@RequiredArgsConstructor
public class UnitTestController {

    private final UnitTestService unitTestService;

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
    public String list(@ModelAttribute("search") UnitTestSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        search.setProjectId(getSelectedProject(session).getId());
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "category", "unitTestId"));
        Page<UnitTest> result = unitTestService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "unit-test/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        Project selectedProject = getSelectedProject(session);
        UnitTestDto dto = new UnitTestDto();
        dto.setProjectId(selectedProject.getId());

        model.addAttribute("unitTest", dto);
        model.addAttribute("mode", "create");
        model.addAttribute("selectedProject", selectedProject);
        return "unit-test/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@ModelAttribute("unitTest") UnitTestDto dto,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        unitTestService.save(dto, attachFiles, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "단위테스트가 등록되었습니다.");
        return "redirect:/unit-test";
    }

    /** 상세 */
    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        UnitTest entity = unitTestService.findById(id);
        model.addAttribute("unitTest", unitTestService.toDto(entity));
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "unit-test/detail";
    }

    /** 수정 폼 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        UnitTest entity = unitTestService.findById(id);
        model.addAttribute("unitTest", unitTestService.toDto(entity));
        model.addAttribute("mode", "edit");
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "unit-test/form";
    }

    /** 수정 처리 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("unitTest") UnitTestDto dto,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        unitTestService.update(id, dto, attachFiles, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "단위테스트가 수정되었습니다.");
        return "redirect:/unit-test/" + id;
    }

    /** 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        unitTestService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "단위테스트가 삭제되었습니다.");
        return "redirect:/unit-test";
    }

    /** 첨부파일 개별 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/attachment/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long id,
                                   @PathVariable Long attachmentId,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        unitTestService.deleteAttachment(attachmentId);
        redirectAttributes.addFlashAttribute("successMessage", "첨부파일이 삭제되었습니다.");
        return "redirect:/unit-test/" + id;
    }

    /** 첨부파일 다운로드 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/attachment/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @PathVariable Long attachmentId,
                                             HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            AttachFile attachment = unitTestService.findAttachmentById(attachmentId);
            Path filePath = unitTestService.getAttachmentFilePath(attachmentId);
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
        List<UnitTest> list = unitTestService.findAllByProject(project.getId());

        String[] headers = { "분류", "단위테스트ID", "단위테스트명", "단위테스트 설명", "테스트 담당자", "비고" };
        List<Object[]> rows = list.stream().map(t -> new Object[]{
            t.getCategory(), t.getUnitTestId(), t.getUnitTestName(),
            t.getDescription(), t.getTester(), t.getNote()
        }).collect(Collectors.toList());

        XSSFWorkbook wb = ExcelUtil.createWorkbook("단위테스트목록", headers, rows);
        String fileName = project.getProjectName() + "_단위테스트목록_" + LocalDate.now() + ".xlsx";
        ExcelUtil.writeToResponse(wb, fileName, response);
    }

    /** 엑셀 업로드 */
    @PostMapping("/excel/upload")
    public String excelUpload(@RequestParam("excelFile") MultipartFile file,
                              HttpSession session, RedirectAttributes ra) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
            return "redirect:/unit-test";
        }

        List<String[]> rows = ExcelUtil.parseRows(file, 1);
        Long projectId = getSelectedProject(session).getId();
        int[] result = unitTestService.upsertFromExcel(rows, projectId, getLoginUserId(session));

        ra.addFlashAttribute("successMessage",
            String.format("엑셀 업로드 완료 — 신규: %d건, 수정: %d건, 건너뜀: %d건",
                result[0], result[1], result[2]));
        return "redirect:/unit-test";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/unit-test";
    }
}
