package com.e4net.pms.controller;

import com.e4net.pms.dto.CommunityDto;
import com.e4net.pms.dto.CommunitySearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.Community;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.CommunityService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private static final String COMMUNITY_TYPE = "공지사항";
    private static final String ACTIVE_PAGE    = "community-notice-list";

    private final CommunityService communityService;

    private boolean isNotLogin(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    private String getLoginUserName(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getName() : "";
    }

    private com.e4net.pms.entity.Project getSelectedProject(HttpSession session) {
        return (com.e4net.pms.entity.Project) session.getAttribute("selectedProject");
    }

    /** 목록 */
    @GetMapping
    public String list(@ModelAttribute("search") CommunitySearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session, Model model) {
        if (isNotLogin(session)) return "redirect:/login";
        search.setCommunityType(COMMUNITY_TYPE);
        // 선택된 사업이 있으면 해당 사업 게시글만 조회
        com.e4net.pms.entity.Project selectedProject = getSelectedProject(session);
        if (selectedProject != null) search.setProjectId(selectedProject.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Community> result = communityService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("communityList", result.getContent());
        model.addAttribute("selectedProject", selectedProject);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "notice/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotLogin(session)) return "redirect:/login";
        com.e4net.pms.entity.Project selectedProject = getSelectedProject(session);
        CommunityDto dto = new CommunityDto();
        dto.setCommunityType(COMMUNITY_TYPE);
        dto.setWriter(getLoginUserName(session));
        dto.setPostDate(LocalDate.now());
        if (selectedProject != null) dto.setProjectId(selectedProject.getId());
        model.addAttribute("community", dto);
        model.addAttribute("mode", "create");
        model.addAttribute("selectedProject", selectedProject);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "notice/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@Valid @ModelAttribute("community") CommunityDto dto,
                         BindingResult result,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) throws IOException {
        if (isNotLogin(session)) return "redirect:/login";
        dto.setCommunityType(COMMUNITY_TYPE);
        if (dto.getProjectId() == null) {
            com.e4net.pms.entity.Project p = getSelectedProject(session);
            if (p != null) dto.setProjectId(p.getId());
        }
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("activePage", ACTIVE_PAGE);
            return "notice/form";
        }
        communityService.save(dto, attachFiles, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "공지사항이 등록되었습니다.");
        return "redirect:/notice";
    }

    /** 상세 */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotLogin(session)) return "redirect:/login";
        Community entity = communityService.findById(id);
        model.addAttribute("community", communityService.toDto(entity));
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "notice/detail";
    }

    /** 수정 처리 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("community") CommunityDto dto,
                         BindingResult result,
                         @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) throws IOException {
        if (isNotLogin(session)) return "redirect:/login";
        dto.setCommunityType(COMMUNITY_TYPE);
        if (result.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("activePage", ACTIVE_PAGE);
            return "notice/detail";
        }
        communityService.update(id, dto, attachFiles, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "공지사항이 수정되었습니다.");
        return "redirect:/notice/" + id;
    }

    /** 삭제 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotLogin(session)) return "redirect:/login";
        communityService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "공지사항이 삭제되었습니다.");
        return "redirect:/notice";
    }

    /** 첨부파일 개별 삭제 */
    @PostMapping("/{id}/attachment/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long id,
                                   @PathVariable Long attachmentId,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotLogin(session)) return "redirect:/login";
        communityService.deleteAttachment(attachmentId);
        redirectAttributes.addFlashAttribute("successMessage", "첨부파일이 삭제되었습니다.");
        return "redirect:/notice/" + id;
    }

    /** 첨부파일 다운로드 */
    @GetMapping("/{id}/attachment/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @PathVariable Long attachmentId,
                                             HttpSession session) {
        if (isNotLogin(session)) return ResponseEntity.status(403).build();
        try {
            AttachFile attachment = communityService.findAttachmentById(attachmentId);
            Path filePath = communityService.getAttachmentFilePath(attachmentId);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/notice";
    }
}
