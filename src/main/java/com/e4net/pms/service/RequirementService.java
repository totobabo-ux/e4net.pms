package com.e4net.pms.service;

import com.e4net.pms.dto.RequirementDto;
import com.e4net.pms.dto.RequirementSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.Requirement;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.RequirementRepository;
import com.e4net.pms.repository.RequirementSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;

    /** 목록 조회 (페이징) */
    public Page<Requirement> search(RequirementSearchDto dto, @NonNull Pageable pageable) {
        return requirementRepository.findAll(RequirementSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull Requirement findById(@NonNull Long id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("요구사항을 찾을 수 없습니다. id=" + id));
    }

    /** 등록 */
    @Transactional
    public Requirement save(RequirementDto dto, String userId) {
        Requirement entity = new Requirement();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        return requirementRepository.save(entity);
    }

    /** 수정 */
    @Transactional
    public Requirement update(@NonNull Long id, RequirementDto dto, String userId) {
        Requirement entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        return requirementRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        requirementRepository.deleteById(id);
    }

    /** Entity → DTO */
    public RequirementDto toDto(Requirement entity) {
        RequirementDto dto = new RequirementDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setReqCode(entity.getReqCode());
        dto.setTitle(entity.getTitle());
        dto.setCategory(entity.getCategory());
        dto.setPriority(entity.getPriority());
        dto.setStatus(entity.getStatus());
        dto.setRequestor(entity.getRequestor());
        dto.setDescription(entity.getDescription());
        dto.setNote(entity.getNote());
        dto.setSourceType(entity.getSourceType());
        dto.setSourceContent(entity.getSourceContent());
        dto.setAcceptance(entity.getAcceptance());
        return dto;
    }

    /** 사업 전체 요구사항 조회 (엑셀 다운로드용, 페이징 없음) */
    public List<Requirement> findAllByProject(Long projectId) {
        return requirementRepository.findAllByProject_IdOrderByIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리
     * 요구사항코드 기준으로 현재 사업 내 기존 데이터 수정, 없으면 신규 등록
     * 컬럼 순서: 요구사항코드(0) 제목(1) 분류(2) 우선순위(3) 상태(4) 요청자(5) 수용여부(6) 출처유형(7) 출처내용(8) 설명(9) 비고(10)
     *
     * @return int[] { 신규등록 수, 수정 수, 건너뜀 수 }
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String reqCodeVal = getCell(cells, 0);
            String titleVal   = getCell(cells, 1);
            if (reqCodeVal.isBlank() || titleVal.isBlank()) {
                skipped++;
                continue;
            }

            Requirement entity;
            boolean isNew;
            var existing = requirementRepository.findByProject_IdAndReqCode(projectId, reqCodeVal);
            if (existing.isPresent()) {
                entity = existing.get();
                isNew  = false;
            } else {
                entity = new Requirement();
                @SuppressWarnings("null")
                Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
                entity.setProject(project);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setReqCode(reqCodeVal);
            entity.setTitle(titleVal);
            entity.setCategory(getCell(cells, 2));
            String priority = getCell(cells, 3);
            entity.setPriority(priority.isBlank() ? "중" : priority);
            String status = getCell(cells, 4);
            entity.setStatus(status.isBlank() ? "등록" : status);
            entity.setRequestor(getCell(cells, 5));
            String acceptance = getCell(cells, 6);
            entity.setAcceptance(acceptance.isBlank() ? "협의중" : acceptance);
            entity.setSourceType(getCell(cells, 7));
            entity.setSourceContent(getCell(cells, 8));
            entity.setDescription(getCell(cells, 9));
            entity.setNote(getCell(cells, 10));
            entity.setUpdId(userId);

            requirementRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    /** 셀 값 안전 추출 */
    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }

    /** DTO → Entity */
    @SuppressWarnings("null")
    private void mapDtoToEntity(RequirementDto dto, Requirement entity) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

        entity.setProject(project);
        entity.setReqCode(dto.getReqCode());
        entity.setTitle(dto.getTitle());
        entity.setCategory(dto.getCategory());
        entity.setPriority(dto.getPriority() != null ? dto.getPriority() : "중");
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "등록");
        entity.setRequestor(dto.getRequestor());
        entity.setDescription(dto.getDescription());
        entity.setNote(dto.getNote());
        entity.setSourceType(dto.getSourceType());
        entity.setSourceContent(dto.getSourceContent());
        entity.setAcceptance(dto.getAcceptance() != null ? dto.getAcceptance() : "협의중");
    }
}
