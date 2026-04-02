package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.CustomerReportDto;
import com.e4net.pms.dto.CustomerReportSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.CustomerReport;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.AttachFileRepository;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerReportService {

    // attach_file 테이블의 entity_type 값
    private static final String ENTITY_TYPE = "CUSTOMER_REPORT";

    private final CustomerReportRepository customerReportRepository;
    private final AttachFileRepository attachFileRepository;
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

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public CustomerReport save(CustomerReportDto dto, List<MultipartFile> files, String userId) throws IOException {
        CustomerReport entity = new CustomerReport();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        CustomerReport saved = customerReportRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public CustomerReport update(@NonNull Long id, CustomerReportDto dto, List<MultipartFile> files, String userId) throws IOException {
        CustomerReport entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return customerReportRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        // 존재 여부 확인
        findById(id);
        // 물리 파일 전체 삭제 후 DB 레코드 삭제
        List<AttachFile> attachments = attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        customerReportRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
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

        // 공통 첨부파일 테이블에서 조회
        List<AttachFileDto> attachmentDtos = attachFileRepository
                .findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, entity.getId())
                .stream()
                .map(this::toAttachFileDto)
                .toList();
        dto.setAttachments(attachmentDtos);

        return dto;
    }

    /** AttachFile Entity → DTO */
    public AttachFileDto toAttachFileDto(AttachFile attachment) {
        AttachFileDto dto = new AttachFileDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileSize(attachment.getFileSize());
        dto.setFileSizeDisplay(formatFileSize(attachment.getFileSize()));
        return dto;
    }

    /** 다운로드용 파일 경로 조회 */
    public Path getAttachmentFilePath(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        if (attachment.getFilePath() == null) {
            throw new IllegalStateException("첨부파일 경로가 없습니다.");
        }
        return Paths.get(attachment.getFilePath());
    }

    // ── private ───────────────────────────────────────────────────

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

    /** 복수 파일 추가 업로드 (기존 파일 유지, 새 파일만 추가) */
    private void addAttachments(CustomerReport entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, resolveUploadFolder(entity.getReportType()), String.valueOf(projectId));
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            AttachFile attachment = new AttachFile();
            attachment.setEntityType(ENTITY_TYPE);
            attachment.setEntityId(entity.getId());
            attachment.setFileName(originalName);
            attachment.setFilePath(target.toAbsolutePath().toString());
            attachment.setFileSize(file.getSize());
            attachment.setRegId(userId);
            attachFileRepository.save(attachment);
        }
    }

    /** reportType → 업로드 하위 폴더명 매핑 */
    private String resolveUploadFolder(String reportType) {
        if ("주간보고".equals(reportType)) return "weekly-report";
        if ("월간보고".equals(reportType)) return "monthly-report";
        if ("회의록".equals(reportType))  return "meeting-report";
        return "regular-report";
    }

    /** 물리 파일 삭제 */
    private void deletePhysicalFile(String filePath) {
        if (filePath == null) return;
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ignored) {
        }
    }

    /** 파일 크기 표시 변환 (bytes → KB/MB) */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
