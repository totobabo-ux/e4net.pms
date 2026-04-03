package com.e4net.pms.service;

import com.e4net.pms.dto.WbsDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.Wbs;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.WbsRepository;
import com.e4net.pms.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WbsService {

    private final WbsRepository wbsRepository;
    private final ProjectRepository projectRepository;

    /** 사업별 WBS 목록 조회 */
    public List<WbsDto> findByProjectId(Long projectId) {
        return wbsRepository.findByProjectIdOrderBySortOrderAscIdAsc(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** 전체 저장 (생성/수정 일괄 처리) */
    @Transactional
    @SuppressWarnings("null")
    public List<WbsDto> batchSave(Long projectId, List<WbsDto> dtos, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

        for (int i = 0; i < dtos.size(); i++) {
            WbsDto dto = dtos.get(i);
            dto.setSortOrder(i + 1);

            Wbs entity;
            boolean isNew;
            if (dto.getId() != null) {
                entity = wbsRepository.findById(dto.getId()).orElse(new Wbs());
                isNew = entity.getId() == null;
            } else {
                entity = new Wbs();
                isNew = true;
            }
            mapDtoToEntity(dto, entity, project);
            if (isNew) entity.setRegId(userId);
            entity.setUpdId(userId);
            wbsRepository.save(entity);
        }
        return findByProjectId(projectId);
    }

    /** 삭제 */
    @Transactional
    public void delete(Long id) {
        wbsRepository.deleteById(id);
    }

    /**
     * 엑셀 워크북 생성 (다운로드용) — COLS 순서와 동일
     * TASK ID(0) TASK명(1) 산출물(2) 담당자(3) 계획%(4) 실적%(5)
     * 계획시작일(6) 계획종료일(7) 계획기간(8) 계획진척률(9)
     * 실제시작일(10) 실제종료일(11) 실제기간(12) 실제진척율(13) 상태(14)
     */
    public XSSFWorkbook createExcelWorkbook(Long projectId) {
        List<WbsDto> list = findByProjectId(projectId);
        String[] headers = {
            "TASK ID", "TASK명", "산출물", "담당자", "계획%", "실적%",
            "계획시작일", "계획종료일", "계획기간", "계획진척률",
            "실제시작일", "실제종료일", "실제기간", "실제진척율", "상태"
        };
        List<Object[]> rows = list.stream().map(r -> new Object[]{
            r.getTaskId(), r.getTaskName(), r.getDeliverable(), r.getAssignee(),
            r.getPlanProgress(), r.getActualProgress(),
            r.getPlanStartDate(), r.getPlanEndDate(), r.getPlanDuration(), r.getPlanRate(),
            r.getActualStartDate(), r.getActualEndDate(), r.getActualDuration(), r.getActualRate(),
            r.getStatus()
        }).toList();
        return ExcelUtil.createWorkbook("사업일정(WBS)", headers, rows);
    }

    /**
     * 엑셀 업로드 — 기존 데이터 전체 삭제 후 재등록
     * TASK ID(0) TASK명(1) 산출물(2) 담당자(3) 계획%(4) 실적%(5)
     * 계획시작일(6) 계획종료일(7) 계획기간(8, read-only skip) 계획진척률(9)
     * 실제시작일(10) 실제종료일(11) 실제기간(12, computed skip) 실제진척율(13) 상태(14)
     *
     * @return int[] { 신규등록 수, 0(수정없음), 건너뜀 수 }
     */
    @Transactional
    @SuppressWarnings("null")
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

        // 기존 데이터 전체 삭제
        wbsRepository.deleteAll(wbsRepository.findByProjectIdOrderBySortOrderAscIdAsc(projectId));

        int inserted = 0, skipped = 0, sortOrder = 0;

        for (String[] cells : rows) {
            String taskIdVal   = getCell(cells, 0);
            String taskNameVal = getCell(cells, 1);
            if (taskNameVal.isBlank()) { skipped++; continue; }

            Wbs entity = new Wbs();
            entity.setProject(project);
            entity.setRegId(userId);
            entity.setTaskId(taskIdVal.isBlank() ? null : taskIdVal);
            entity.setTaskName(taskNameVal);
            entity.setDeliverable(getCell(cells, 2));
            entity.setAssignee(getCell(cells, 3));
            entity.setPlanProgress(parseInteger(getCell(cells, 4)));
            entity.setActualProgress(parseInteger(getCell(cells, 5)));
            entity.setPlanStartDate(parseDate(getCell(cells, 6)));
            entity.setPlanEndDate(parseDate(getCell(cells, 7)));
            // col 8: planDuration (read-only) — skip
            entity.setPlanRate(parseInteger(getCell(cells, 9)));
            entity.setActualStartDate(parseDate(getCell(cells, 10)));
            entity.setActualEndDate(parseDate(getCell(cells, 11)));
            // col 12: actualDuration (computed) — skip
            entity.setActualRate(parseInteger(getCell(cells, 13)));
            String status = getCell(cells, 14);
            entity.setStatus(status.isBlank() ? "미착수" : status);
            entity.setUpdId(userId);
            entity.setSortOrder(++sortOrder);

            wbsRepository.save(entity);
            inserted++;
        }
        return new int[]{ inserted, 0, skipped };
    }

    /** 셀 값 안전 추출 */
    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }

    /** 문자열 → Integer (파싱 실패 시 null) */
    private Integer parseInteger(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    /** Entity → DTO */
    private WbsDto toDto(Wbs e) {
        WbsDto dto = new WbsDto();
        dto.setId(e.getId());
        dto.setProjectId(e.getProject().getId());
        dto.setTaskId(e.getTaskId());
        dto.setTaskName(e.getTaskName());
        dto.setDeliverable(e.getDeliverable());
        dto.setAssignee(e.getAssignee());
        dto.setPlanProgress(e.getPlanProgress());
        dto.setActualProgress(e.getActualProgress());
        dto.setPlanStartDate(e.getPlanStartDate() != null ? e.getPlanStartDate().toString() : null);
        dto.setPlanEndDate(e.getPlanEndDate() != null ? e.getPlanEndDate().toString() : null);
        dto.setPlanDuration(e.getPlanDuration());
        dto.setPlanRate(e.getPlanRate());
        dto.setActualStartDate(e.getActualStartDate() != null ? e.getActualStartDate().toString() : null);
        dto.setActualEndDate(e.getActualEndDate() != null ? e.getActualEndDate().toString() : null);
        if (e.getActualStartDate() != null && e.getActualEndDate() != null
                && !e.getActualEndDate().isBefore(e.getActualStartDate())) {
            dto.setActualDuration((int) e.getActualStartDate().until(e.getActualEndDate(),
                    java.time.temporal.ChronoUnit.DAYS) + 1);
        }
        dto.setActualRate(e.getActualRate());
        dto.setStatus(e.getStatus());
        dto.setSortOrder(e.getSortOrder());
        return dto;
    }

    /** DTO → Entity 매핑 */
    private void mapDtoToEntity(WbsDto dto, Wbs entity, Project project) {
        entity.setProject(project);
        entity.setTaskId(dto.getTaskId());
        entity.setTaskName(dto.getTaskName());
        entity.setDeliverable(dto.getDeliverable());
        entity.setAssignee(dto.getAssignee());
        entity.setPlanProgress(dto.getPlanProgress());
        entity.setActualProgress(dto.getActualProgress());
        entity.setPlanStartDate(parseDate(dto.getPlanStartDate()));
        entity.setPlanEndDate(parseDate(dto.getPlanEndDate()));
        entity.setPlanDuration(dto.getPlanDuration());
        entity.setPlanRate(dto.getPlanRate());
        entity.setActualStartDate(parseDate(dto.getActualStartDate()));
        entity.setActualEndDate(parseDate(dto.getActualEndDate()));
        entity.setActualRate(dto.getActualRate());
        entity.setStatus(dto.getStatus());
        entity.setSortOrder(dto.getSortOrder());
    }

    private LocalDate parseDate(String s) {
        return (s != null && !s.isBlank()) ? LocalDate.parse(s) : null;
    }
}
