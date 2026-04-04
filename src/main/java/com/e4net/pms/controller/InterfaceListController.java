package com.e4net.pms.controller;

import com.e4net.pms.dto.InterfaceListDto;
import com.e4net.pms.dto.InterfaceListSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.InterfaceList;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.InterfaceListService;
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
@RequestMapping("/interface-list")
@RequiredArgsConstructor
public class InterfaceListController {

    private final InterfaceListService interfaceListService;

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

    /** 목록 */
    @GetMapping
    public String list(@ModelAttribute("search") InterfaceListSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        search.setProjectId(getSelectedProject(session).getId());
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "interfaceId"));
        Page<InterfaceList> result = interfaceListService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "interface-list/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        Project selectedProject = getSelectedProject(session);
        InterfaceListDto dto = new InterfaceListDto();
        dto.setProjectId(selectedProject.getId());

        model.addAttribute("iface", dto);
        model.addAttribute("mode", "create");
        model.addAttribute("selectedProject", selectedProject);
        return "interface-list/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@ModelAttribute("iface") InterfaceListDto dto,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session, RedirectAttributes ra) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        interfaceListService.save(dto, attachFiles, getLoginUserId(session));
        ra.addFlashAttribute("successMessage", "인터페이스목록이 등록되었습니다.");
        return "redirect:/interface-list";
    }

    /** 상세 */
    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        InterfaceList entity = interfaceListService.findById(id);
        model.addAttribute("iface", interfaceListService.toDto(entity));
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "interface-list/detail";
    }

    /** 수정 폼 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        InterfaceList entity = interfaceListService.findById(id);
        model.addAttribute("iface", interfaceListService.toDto(entity));
        model.addAttribute("mode", "edit");
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "interface-list/form";
    }

    /** 수정 처리 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("iface") InterfaceListDto dto,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session, RedirectAttributes ra) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        dto.setProjectId(getSelectedProject(session).getId());
        interfaceListService.update(id, dto, attachFiles, getLoginUserId(session));
        ra.addFlashAttribute("successMessage", "인터페이스목록이 수정되었습니다.");
        return "redirect:/interface-list/" + id;
    }

    /** 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (isNotReady(session)) return "redirect:/project-select";
        interfaceListService.delete(id);
        ra.addFlashAttribute("successMessage", "인터페이스목록이 삭제되었습니다.");
        return "redirect:/interface-list";
    }

    /** 첨부파일 개별 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/attachment/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long id,
                                   @PathVariable Long attachmentId,
                                   HttpSession session, RedirectAttributes ra) {
        if (isNotReady(session)) return "redirect:/project-select";
        interfaceListService.deleteAttachment(attachmentId);
        ra.addFlashAttribute("successMessage", "첨부파일이 삭제되었습니다.");
        return "redirect:/interface-list/" + id;
    }

    /** 첨부파일 다운로드 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/attachment/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @PathVariable Long attachmentId,
                                             HttpSession session) {
        if (isNotReady(session)) return ResponseEntity.status(403).build();
        try {
            AttachFile attachment = interfaceListService.findAttachmentById(attachmentId);
            Path filePath = interfaceListService.getAttachmentFilePath(attachmentId);
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

    // ── 엑셀 ─────────────────────────────────────────────────

    /** 엑셀 다운로드 */
    @GetMapping("/excel/download")
    public void excelDownload(HttpSession session, HttpServletResponse response) throws IOException {
        if (isNotReady(session)) { response.sendRedirect("/project-select"); return; }

        Project project = getSelectedProject(session);
        List<InterfaceList> list = interfaceListService.findAllByProject(project.getId());

        String[] headers = { "인터페이스ID", "인터페이스명", "연계구분", "송신시스템(Source)",
                              "수신시스템(Target)", "인터페이스방식", "발생주기", "비고" };
        List<Object[]> rows = list.stream().map(i -> new Object[]{
            i.getInterfaceId(), i.getInterfaceName(), i.getLinkType(),
            i.getSourceSystem(), i.getTargetSystem(),
            i.getInterfaceMethod(), i.getOccurrenceCycle(), i.getNote()
        }).collect(Collectors.toList());

        XSSFWorkbook wb = ExcelUtil.createWorkbook("인터페이스목록", headers, rows);
        String fileName = project.getProjectName() + "_인터페이스목록_" + LocalDate.now() + ".xlsx";
        ExcelUtil.writeToResponse(wb, fileName, response);
    }

    /** 엑셀 업로드 */
    @PostMapping("/excel/upload")
    public String excelUpload(@RequestParam("excelFile") MultipartFile file,
                              HttpSession session, RedirectAttributes ra) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
            return "redirect:/interface-list";
        }

        List<String[]> rows = ExcelUtil.parseRows(file, 1);
        Long projectId = getSelectedProject(session).getId();
        int[] result = interfaceListService.upsertFromExcel(rows, projectId, getLoginUserId(session));

        ra.addFlashAttribute("successMessage",
            String.format("엑셀 업로드 완료 — 신규: %d건, 수정: %d건, 건너뜀: %d건",
                result[0], result[1], result[2]));
        return "redirect:/interface-list";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleError(IllegalArgumentException e, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/interface-list";
    }
}
