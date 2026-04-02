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
    public Requirement save(RequirementDto dto) {
        Requirement entity = new Requirement();
        mapDtoToEntity(dto, entity);
        return requirementRepository.save(entity);
    }

    /** 수정 */
    @Transactional
    public Requirement update(@NonNull Long id, RequirementDto dto) {
        Requirement entity = findById(id);
        mapDtoToEntity(dto, entity);
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
