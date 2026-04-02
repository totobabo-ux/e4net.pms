package com.e4net.pms.service;

import com.e4net.pms.dto.WbsDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.Wbs;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.WbsRepository;
import lombok.RequiredArgsConstructor;
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
    public List<WbsDto> batchSave(Long projectId, List<WbsDto> dtos) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

        for (int i = 0; i < dtos.size(); i++) {
            WbsDto dto = dtos.get(i);
            dto.setSortOrder(i + 1);

            Wbs entity;
            if (dto.getId() != null) {
                entity = wbsRepository.findById(dto.getId()).orElse(new Wbs());
            } else {
                entity = new Wbs();
            }
            mapDtoToEntity(dto, entity, project);
            wbsRepository.save(entity);
        }
        return findByProjectId(projectId);
    }

    /** 삭제 */
    @Transactional
    public void delete(Long id) {
        wbsRepository.deleteById(id);
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
