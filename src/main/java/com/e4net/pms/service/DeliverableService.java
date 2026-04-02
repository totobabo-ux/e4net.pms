package com.e4net.pms.service;

import com.e4net.pms.dto.DeliverableDto;
import com.e4net.pms.dto.DeliverableSearchDto;
import com.e4net.pms.entity.Deliverable;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.DeliverableRepository;
import com.e4net.pms.repository.DeliverableSpec;
import com.e4net.pms.repository.ProjectRepository;
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
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final ProjectRepository projectRepository;

    /** 목록 조회 (페이징) */
    public Page<Deliverable> search(DeliverableSearchDto dto, @NonNull Pageable pageable) {
        return deliverableRepository.findAll(DeliverableSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull Deliverable findById(@NonNull Long id) {
        return deliverableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("산출물을 찾을 수 없습니다. id=" + id));
    }

    /** 등록 */
    @Transactional
    public Deliverable save(DeliverableDto dto, String userId) {
        Deliverable entity = new Deliverable();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        return deliverableRepository.save(entity);
    }

    /** 수정 */
    @Transactional
    public Deliverable update(@NonNull Long id, DeliverableDto dto, String userId) {
        Deliverable entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        return deliverableRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        deliverableRepository.deleteById(id);
    }

    /** Entity → DTO 변환 */
    public DeliverableDto toDto(Deliverable entity) {
        DeliverableDto dto = new DeliverableDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setDeliverableType(entity.getDeliverableType());
        dto.setCategory1(entity.getCategory1());
        dto.setCategory2(entity.getCategory2());
        dto.setCode(entity.getCode());
        dto.setDeliverableId(entity.getDeliverableId());
        dto.setName(entity.getName());
        dto.setWrittenYn(entity.getWrittenYn());
        dto.setStage(entity.getStage());
        dto.setWriter(entity.getWriter());
        dto.setNote(entity.getNote());
        return dto;
    }

    /** 사업 전체 산출물 조회 (엑셀 다운로드용, 페이징 없음) */
    public List<Deliverable> findAllByProject(Long projectId) {
        return deliverableRepository.findAllByProject_IdOrderByIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리
     * 산출물ID 기준으로 현재 사업 내 기존 데이터 수정, 없으면 신규 등록
     *
     * @param rows      ExcelUtil.parseRows() 결과 (헤더 제외)
     * @param projectId 세션 사업 ID
     * @param userId    로그인 사용자 사번
     * @return int[] { 신규등록 수, 수정 수, 건너뜀 수 }
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        // 컬럼 순서: 산출물구분(0) 분류1(1) 분류2(2) 코드(3) 산출물ID(4) 산출물명(5) 작성여부(6) 단계(7) 작성자(8) 비고(9)
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String deliverableIdVal = getCell(cells, 4);
            String nameVal          = getCell(cells, 5);
            if (deliverableIdVal.isBlank() || nameVal.isBlank()) {
                skipped++;
                continue;
            }

            Deliverable entity;
            boolean isNew;
            var existing = deliverableRepository.findByProject_IdAndDeliverableId(projectId, deliverableIdVal);
            if (existing.isPresent()) {
                entity = existing.get();
                isNew  = false;
            } else {
                entity = new Deliverable();
                @SuppressWarnings("null")
                Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
                entity.setProject(project);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setDeliverableType(getCell(cells, 0));
            entity.setCategory1(getCell(cells, 1));
            entity.setCategory2(getCell(cells, 2));
            entity.setCode(getCell(cells, 3));
            entity.setDeliverableId(deliverableIdVal);
            entity.setName(nameVal);
            entity.setWrittenYn(getCell(cells, 6));
            String stage = getCell(cells, 7);
            entity.setStage(stage.isBlank() ? "미도래" : stage);
            entity.setWriter(getCell(cells, 8));
            entity.setNote(getCell(cells, 9));
            entity.setUpdId(userId);

            deliverableRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    /** 셀 값 안전 추출 */
    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }

    /** DTO → Entity 매핑 */
    @SuppressWarnings("null")
    private void mapDtoToEntity(DeliverableDto dto, Deliverable entity) {
        // 사업 조회 및 설정
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

        entity.setProject(project);
        entity.setDeliverableType(dto.getDeliverableType());
        entity.setCategory1(dto.getCategory1());
        entity.setCategory2(dto.getCategory2());
        entity.setCode(dto.getCode());
        entity.setDeliverableId(dto.getDeliverableId());
        entity.setName(dto.getName());
        entity.setWrittenYn(dto.getWrittenYn());
        entity.setStage(dto.getStage() != null ? dto.getStage() : "미도래");
        entity.setWriter(dto.getWriter());
        entity.setNote(dto.getNote());
    }
}
