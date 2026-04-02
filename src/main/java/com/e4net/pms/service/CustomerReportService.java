package com.e4net.pms.service;

import com.e4net.pms.dto.CustomerReportDto;
import com.e4net.pms.dto.CustomerReportSearchDto;
import com.e4net.pms.entity.CustomerReport;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.CustomerReportRepository;
import com.e4net.pms.repository.CustomerReportSpec;
import com.e4net.pms.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerReportService {

    private final CustomerReportRepository customerReportRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 목록 조회 (페이징) */
    public Page<CustomerReport> search(CustomerReportSearchDto dto, @NonNull Pageable pageable) {
        return customerReportRepository.findAll(CustomerReportSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull CustomerReport findById(@NonNull Long id) {
        return customerReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("보고서를 찾을 수 없습니다. id=" + id));
    }

    /** 등록 */
    @Transactional
    public CustomerReport save(CustomerReportDto dto, MultipartFile file) throws IOException {
        CustomerReport entity = new CustomerReport();
        mapDtoToEntity(dto, entity);
        handleFileUpload(entity, file, dto.getProjectId());
        return customerReportRepository.save(entity);
    }

    /** 수정 */
    @Transactional
    public CustomerReport update(@NonNull Long id, CustomerReportDto dto, MultipartFile file) throws IOException {
        CustomerReport entity = findById(id);
        mapDtoToEntity(dto, entity);
        handleFileUpload(entity, file, dto.getProjectId());
        return customerReportRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        CustomerReport entity = findById(id);
        deleteAttachedFile(entity);
        customerReportRepository.deleteById(id);
    }

    /** Entity → DTO */
    public CustomerReportDto toDto(CustomerReport entity) {
        CustomerReportDto dto = new CustomerReportDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setReportType(entity.getReportType());
        dto.setReportName(entity.getReportName());
        dto.setReportDate(entity.getReportDate() != null ? entity.getReportDate().toString() : null);
        dto.setReportContent(entity.getReportContent());
        dto.setWriter(entity.getWriter());
        dto.setAttachFileName(entity.getAttachFileName());
        dto.setAttachFilePath(entity.getAttachFilePath());
        return dto;
    }

    /** DTO → Entity */
    @SuppressWarnings("null")
    private void mapDtoToEntity(CustomerReportDto dto, CustomerReport entity) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
        entity.setReportType(dto.getReportType());
        entity.setReportName(dto.getReportName());
        entity.setReportDate(dto.getReportDate() != null && !dto.getReportDate().isBlank()
                ? LocalDate.parse(dto.getReportDate()) : null);
        entity.setReportContent(dto.getReportContent());
        entity.setWriter(dto.getWriter());
    }

    /** 파일 업로드 처리 (새 파일이 있을 때만) */
    private void handleFileUpload(CustomerReport entity, MultipartFile file, Long projectId) throws IOException {
        if (file == null || file.isEmpty()) return;

        // 기존 파일 삭제
        deleteAttachedFile(entity);

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadDir, "customer-report", String.valueOf(projectId));
        Files.createDirectories(dir);
        Path target = dir.resolve(storedName);
        Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        entity.setAttachFileName(originalName);
        entity.setAttachFilePath(target.toAbsolutePath().toString());
    }

    /** 첨부파일 물리 삭제 */
    private void deleteAttachedFile(CustomerReport entity) {
        if (entity.getAttachFilePath() == null) return;
        try {
            Files.deleteIfExists(Paths.get(entity.getAttachFilePath()));
        } catch (IOException ignored) {
        }
    }

    /** 다운로드용 파일 경로 조회 */
    public Path getFilePath(@NonNull Long id) {
        CustomerReport entity = findById(id);
        if (entity.getAttachFilePath() == null) {
            throw new IllegalStateException("첨부파일이 없습니다.");
        }
        return Paths.get(entity.getAttachFilePath());
    }
}
