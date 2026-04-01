package com.e4net.pms.controller;

import com.e4net.pms.dto.ManpowerDto;
import com.e4net.pms.dto.ManpowerSearchDto;
import com.e4net.pms.entity.ProjectManpower;
import com.e4net.pms.service.ManpowerService;
import com.e4net.pms.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manpower")
@RequiredArgsConstructor
public class ManpowerController {

    private final ManpowerService manpowerService;
    private final ProjectService projectService;

    /** 목록 */
    @GetMapping
    public String list(@ModelAttribute("search") ManpowerSearchDto search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProjectManpower> result = manpowerService.search(search, pageable);
        model.addAttribute("page", result);
        model.addAttribute("manpowerList", result.getContent());
        return "manpower/list";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("manpower", new ManpowerDto());
        model.addAttribute("mode", "create");
        addFormAttributes(model);
        return "manpower/form";
    }

    /** 등록 처리 */
    @PostMapping
    public String create(@Valid @ModelAttribute("manpower") ManpowerDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            addFormAttributes(model);
            return "manpower/form";
        }
        manpowerService.save(dto);
        redirectAttributes.addFlashAttribute("successMessage", "인력이 등록되었습니다.");
        return "redirect:/manpower";
    }

    /** 상세 */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ProjectManpower entity = manpowerService.findById(id);
        model.addAttribute("manpower", manpowerService.toDto(entity));
        model.addAttribute("gradeCodes", manpowerService.getGradeCodes());
        model.addAttribute("inputTypeCodes", manpowerService.getInputTypeCodes());
        return "manpower/detail";
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ProjectManpower entity = manpowerService.findById(id);
        model.addAttribute("manpower", manpowerService.toDto(entity));
        model.addAttribute("mode", "edit");
        addFormAttributes(model);
        return "manpower/form";
    }

    /** 수정 처리 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("manpower") ManpowerDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            addFormAttributes(model);
            return "manpower/form";
        }
        manpowerService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "인력 정보가 수정되었습니다.");
        return "redirect:/manpower/" + id;
    }

    /** 삭제 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        manpowerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "인력 정보가 삭제되었습니다.");
        return "redirect:/manpower";
    }

    private void addFormAttributes(Model model) {
        model.addAttribute("projects", projectService.findAll());
        model.addAttribute("gradeCodes", manpowerService.getGradeCodes());
        model.addAttribute("inputTypeCodes", manpowerService.getInputTypeCodes());
    }
}
