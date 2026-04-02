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
    public Deliverable save(DeliverableDto dto) {
        Deliverable entity = new Deliverable();
        mapDtoToEntity(dto, entity);
        return deliverableRepository.save(entity);
    }

    /** 수정 */
    @Transactional
    public Deliverable update(@NonNull Long id, DeliverableDto dto) {
        Deliverable entity = findById(id);
        mapDtoToEntity(dto, entity);
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
