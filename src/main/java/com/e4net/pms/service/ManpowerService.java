package com.e4net.pms.service;

import com.e4net.pms.dto.ManpowerDto;
import com.e4net.pms.dto.ManpowerSearchDto;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.ProjectManpower;
import com.e4net.pms.entity.User;
import com.e4net.pms.repository.ManpowerRepository;
import com.e4net.pms.repository.ManpowerSpec;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManpowerService {

    private final ManpowerRepository manpowerRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /** 목록 조회 (페이징) */
    public Page<ProjectManpower> search(ManpowerSearchDto dto, @NonNull Pageable pageable) {
        return manpowerRepository.findAll(ManpowerSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull ProjectManpower findById(@NonNull Long id) {
        return manpowerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("인력 정보를 찾을 수 없습니다. id=" + id));
    }

    /** 등록 */
    @Transactional
    public ProjectManpower save(ManpowerDto dto, String userId) {
        ProjectManpower entity = new ProjectManpower();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        return manpowerRepository.save(entity);
    }

    /** 수정 */
    @Transactional
    public ProjectManpower update(@NonNull Long id, ManpowerDto dto, String userId) {
        ProjectManpower entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        return manpowerRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        manpowerRepository.deleteById(id);
    }

    /** Entity → DTO */
    public ManpowerDto toDto(ProjectManpower entity) {
        ManpowerDto dto = new ManpowerDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setUserId(entity.getUser().getId());
        dto.setUserName(entity.getUser().getName());
        dto.setCompany(entity.getCompany());
        dto.setDepartment(entity.getDepartment());
        dto.setPhone(entity.getPhone());
        dto.setRole(entity.getRole());
        dto.setPosition(entity.getPosition());
        dto.setGradeCode(entity.getGradeCode());
        dto.setInputTypeCode(entity.getInputTypeCode());
        dto.setInputStartDate(entity.getInputStartDate());
        dto.setInputEndDate(entity.getInputEndDate());
        dto.setInputMm(entity.getInputMm());
        dto.setStatus(entity.getStatus());
        dto.setNote(entity.getNote());
        return dto;
    }

    /** DTO → Entity */
    @SuppressWarnings("null")
    private void mapDtoToEntity(ManpowerDto dto, ProjectManpower entity) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));

        entity.setProject(project);
        entity.setUser(user);
        entity.setCompany(dto.getCompany());
        entity.setDepartment(dto.getDepartment());
        entity.setPhone(dto.getPhone());
        entity.setRole(dto.getRole());
        entity.setPosition(dto.getPosition());
        entity.setGradeCode(dto.getGradeCode());
        entity.setInputTypeCode(dto.getInputTypeCode());
        entity.setInputStartDate(dto.getInputStartDate());
        entity.setInputEndDate(dto.getInputEndDate());
        entity.setInputMm(dto.getInputMm());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "투입중");
        entity.setNote(dto.getNote());
    }
}
