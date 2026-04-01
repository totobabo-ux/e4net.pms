package com.e4net.pms.service;

import com.e4net.pms.dto.ProjectDto;
import com.e4net.pms.dto.ProjectSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.ProjectSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    /** 전체 목록 조회 (최신순) */
    public List<Project> findAll() {
        return projectRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "id"));
    }

    /** 검색 조건으로 목록 조회 */
    public List<Project> search(ProjectSearchDto searchDto) {
        return projectRepository.findAll(ProjectSpec.search(searchDto));
    }

    /** ID로 단건 조회 */
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다. id=" + id));
    }

    /** 등록 */
    @Transactional
    public Project save(ProjectDto dto) {
        Project project = new Project();
        mapDtoToEntity(dto, project);
        return projectRepository.save(project);
    }

    /** 수정 */
    @Transactional
    public Project update(Long id, ProjectDto dto) {
        Project project = findById(id);
        mapDtoToEntity(dto, project);
        return projectRepository.save(project);
    }

    /** 삭제 */
    @Transactional
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    /** DTO → Entity 매핑 */
    private void mapDtoToEntity(ProjectDto dto, Project project) {
        project.setProjectCode(dto.getProjectCode());
        project.setProjectName(dto.getProjectName());
        project.setCategory(dto.getCategory());
        project.setSubCategory(dto.getSubCategory());
        project.setCompany(dto.getCompany());
        project.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true);
        project.setOrderer(dto.getOrderer());
        project.setContractor(dto.getContractor());
        project.setContractStart(dto.getContractStart());
        project.setContractEnd(dto.getContractEnd());
        project.setPm(dto.getPm());
        project.setContractAmount(dto.getContractAmount());
        project.setPreInputMm(dto.getPreInputMm());
        project.setTotalMm(dto.getTotalMm());
        project.setHeadcount(dto.getHeadcount());
        project.setStatus(dto.getStatus());
    }

    /** Entity → DTO 변환 */
    public ProjectDto toDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setProjectCode(project.getProjectCode());
        dto.setProjectName(project.getProjectName());
        dto.setCategory(project.getCategory());
        dto.setSubCategory(project.getSubCategory());
        dto.setCompany(project.getCompany());
        dto.setIsPublic(project.getIsPublic());
        dto.setOrderer(project.getOrderer());
        dto.setContractor(project.getContractor());
        dto.setContractStart(project.getContractStart());
        dto.setContractEnd(project.getContractEnd());
        dto.setPm(project.getPm());
        dto.setContractAmount(project.getContractAmount());
        dto.setPreInputMm(project.getPreInputMm());
        dto.setTotalMm(project.getTotalMm());
        dto.setHeadcount(project.getHeadcount());
        dto.setStatus(project.getStatus());
        return dto;
    }
}
