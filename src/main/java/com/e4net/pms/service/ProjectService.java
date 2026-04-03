package com.e4net.pms.service;

import com.e4net.pms.dto.ProjectDto;
import com.e4net.pms.dto.ProjectSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.ProjectSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
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
    @SuppressWarnings("null")
    public @NonNull Project findById(@NonNull Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다. id=" + id));
    }

    /** 등록 */
    @Transactional
    public Project save(ProjectDto dto, String userId) {
        Project project = new Project();
        mapDtoToEntity(dto, project);
        project.setRegId(userId);
        project.setUpdId(userId);
        return projectRepository.save(project);
    }

    /** 수정 */
    @Transactional
    public Project update(@NonNull Long id, ProjectDto dto, String userId) {
        Project project = findById(id);
        mapDtoToEntity(dto, project);
        project.setUpdId(userId);
        return projectRepository.save(project);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        projectRepository.deleteById(id);
    }

    /** DTO → Entity 매핑 */
    private void mapDtoToEntity(ProjectDto dto, Project project) {
        project.setProjectName(dto.getProjectName());
        project.setCategory(dto.getCategory());
        project.setCompany(dto.getCompany());
        project.setOrderer(dto.getOrderer());
        project.setContractor(dto.getContractor());
        project.setContractStart(dto.getContractStart());
        project.setContractEnd(dto.getContractEnd());
        project.setPm(dto.getPm());
        project.setContractAmount(dto.getContractAmount());
    }

    /** Entity → DTO 변환 */
    public ProjectDto toDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setProjectName(project.getProjectName());
        dto.setCategory(project.getCategory());
        dto.setCompany(project.getCompany());
        dto.setOrderer(project.getOrderer());
        dto.setContractor(project.getContractor());
        dto.setContractStart(project.getContractStart());
        dto.setContractEnd(project.getContractEnd());
        dto.setPm(project.getPm());
        dto.setContractAmount(project.getContractAmount());
        return dto;
    }
}
