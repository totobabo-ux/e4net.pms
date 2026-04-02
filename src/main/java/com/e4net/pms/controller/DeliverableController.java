package com.e4net.pms.controller;

import com.e4net.pms.dto.DeliverableDto;
import com.e4net.pms.dto.DeliverableSearchDto;
import com.e4net.pms.entity.Deliverable;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.User;
import com.e4net.pms.service.DeliverableService;
import com.e4net.pms.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/deliverable")
@RequiredArgsConstructor
public class DeliverableController {

    private final DeliverableService deliverableService;

    /** 세션 준비 여부 체크 (로그인 + 프로젝트 선택) */
    private boolean isNotReady(HttpSession session) {
        return session.getAttribute("loginUser") == null
            || session.getAttribute("selectedProject") == null;
    }

    /** 세션에서 선택된 사업 조회 */
    private Project getSelectedProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    private String getLoginUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getEmployeeNo() : "";
    }

    /** 목록 — 선택된 사업으로 자동 필터 */
    @GetMapping
    public String list(@ModelAttribute("search") DeliverableSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       HttpSession session,
                       Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        // 세션 사업 ID 강제 적용
        search.setProjectId(getSelectedProject(session).getId());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Deliverable> result = deliverableService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("deliverableList", result.getContent());
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "deliverable/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";

        Project selectedProject = getSelectedProject(session);
        DeliverableDto dto = new DeliverableDto();
        dto.setProjectId(selectedProject.getId()); // 사업 자동 설정

        model.addAttribute("deliverable", dto);
        model.addAttribute("mode", "create");
        model.addAttribute("selectedProject", selectedProject);
        return "deliverable/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@Valid @ModelAttribute("deliverable") DeliverableDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("selectedProject", getSelectedProject(session));
            return "deliverable/form";
        }
        deliverableService.save(dto, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "산출물이 등록되었습니다.");
        return "redirect:/deliverable";
    }

    /** 상세 */
    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        Deliverable entity = deliverableService.findById(id);
        model.addAttribute("deliverable", deliverableService.toDto(entity));
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "deliverable/detail";
    }

    /** 수정 폼 */
    @SuppressWarnings("null")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        Deliverable entity = deliverableService.findById(id);
        model.addAttribute("deliverable", deliverableService.toDto(entity));
        model.addAttribute("mode", "edit");
        model.addAttribute("selectedProject", getSelectedProject(session));
        return "deliverable/form";
    }

    /** 수정 처리 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("deliverable") DeliverableDto dto,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (isNotReady(session)) return "redirect:/project-select";
        if (result.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("selectedProject", getSelectedProject(session));
            return "deliverable/detail";
        }
        deliverableService.update(id, dto, getLoginUserId(session));
        redirectAttributes.addFlashAttribute("successMessage", "산출물이 수정되었습니다.");
        return "redirect:/deliverable/" + id;
    }

    /** 삭제 */
    @SuppressWarnings("null")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotReady(session)) return "redirect:/project-select";
        deliverableService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "산출물이 삭제되었습니다.");
        return "redirect:/deliverable";
    }

    /** 엑셀 다운로드 — 현재 사업의 산출물 전체 */
    @GetMapping("/excel/download")
    public void excelDownload(HttpSession session, HttpServletResponse response) throws IOException {
        if (isNotReady(session)) { response.sendRedirect("/project-select"); return; }

        Project project = getSelectedProject(session);
        List<Deliverable> list = deliverableService.findAllByProject(project.getId());

        String[] headers = { "산출물구분", "분류1", "분류2", "코드", "산출물ID", "산출물명", "작성여부", "단계", "작성자", "비고" };
        List<Object[]> rows = list.stream().map(d -> new Object[]{
            d.getDeliverableType(), d.getCategory1(), d.getCategory2(), d.getCode(),
            d.getDeliverableId(), d.getName(), d.getWrittenYn(), d.getStage(),
            d.getWriter(), d.getNote()
        }).collect(Collectors.toList());

        XSSFWorkbook wb = ExcelUtil.createWorkbook("산출물목록", headers, rows);
        String fileName = project.getProjectName() + "_산출물목록_" + LocalDate.now() + ".xlsx";
        ExcelUtil.writeToResponse(wb, fileName, response);
    }

    /** 엑셀 업로드 — 산출물ID 기준 upsert */
    @PostMapping("/excel/upload")
    public String excelUpload(@RequestParam("excelFile") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes ra) throws IOException {
        if (isNotReady(session)) return "redirect:/project-select";
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
            return "redirect:/deliverable";
        }

        List<String[]> rows = ExcelUtil.parseRows(file, 1);
        Long projectId = getSelectedProject(session).getId();
        int[] result = deliverableService.upsertFromExcel(rows, projectId, getLoginUserId(session));

        ra.addFlashAttribute("successMessage",
            String.format("엑셀 업로드 완료 — 신규: %d건, 수정: %d건, 건너뜀: %d건",
                result[0], result[1], result[2]));
        return "redirect:/deliverable";
    }
}
