package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.BusinessFlowDto;
import com.e4net.pms.dto.BusinessFlowSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.BusinessFlow;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.BusinessFlowRepository;
import com.e4net.pms.repository.BusinessFlowSpec;
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
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessFlowService {

    private static final String ENTITY_TYPE = "BUSINESS_FLOW";

    private final BusinessFlowRepository businessFlowRepository;
    private final AttachFileRepository   attachFileRepository;
    private final ProjectRepository      projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 목록 조회 (페이징) */
    public Page<BusinessFlow> search(BusinessFlowSearchDto dto, @NonNull Pageable pageable) {
        return businessFlowRepository.findAll(BusinessFlowSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull BusinessFlow findById(@NonNull Long id) {
        return businessFlowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("업무흐름을 찾을 수 없습니다. id=" + id));
    }

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public BusinessFlow save(BusinessFlowDto dto, List<MultipartFile> files, String userId) throws IOException {
        BusinessFlow entity = new BusinessFlow();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        BusinessFlow saved = businessFlowRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public BusinessFlow update(@NonNull Long id, BusinessFlowDto dto, List<MultipartFile> files, String userId) throws IOException {
        BusinessFlow entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return businessFlowRepository.save(entity);
    }

    /** 삭제 (물리 파일 + DB 레코드) */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments =
                attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        businessFlowRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
    }

    /** Entity → DTO */
    public BusinessFlowDto toDto(BusinessFlow entity) {
        BusinessFlowDto dto = new BusinessFlowDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setSystemCategory(entity.getSystemCategory());
        dto.setBizCategory(entity.getBizCategory());
        dto.setProcessId(entity.getProcessId());
        dto.setProcessName(entity.getProcessName());

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

    /** 사업 전체 목록 (엑셀 다운로드용) */
    public List<BusinessFlow> findAllByProject(Long projectId) {
        return businessFlowRepository.findAllByProject_IdOrderBySystemCategoryAscBizCategoryAscProcessIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리 (프로세스ID 기준)
     * 컬럼 순서: 시스템구분(0) 업무구분(1) 프로세스ID(2) 프로세스명(3)
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String processNameVal = getCell(cells, 3);
            if (processNameVal.isBlank()) {
                skipped++;
                continue;
            }

            String processIdVal = getCell(cells, 2);

            BusinessFlow entity;
            boolean isNew;
            if (!processIdVal.isBlank()) {
                var existing = businessFlowRepository.findByProject_IdAndProcessId(projectId, processIdVal);
                if (existing.isPresent()) {
                    entity = existing.get();
                    isNew  = false;
                } else {
                    entity = new BusinessFlow();
                    setProject(entity, projectId);
                    entity.setRegId(userId);
                    isNew = true;
                }
            } else {
                entity = new BusinessFlow();
                setProject(entity, projectId);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setSystemCategory(getCell(cells, 0));
            entity.setBizCategory(getCell(cells, 1));
            entity.setProcessId(processIdVal);
            entity.setProcessName(processNameVal);
            entity.setUpdId(userId);

            businessFlowRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    /** 다운로드용 파일 경로 조회 */
    public Path getAttachmentFilePath(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        if (attachment.getFilePath() == null) {
            throw new IllegalStateException("첨부파일 경로가 없습니다.");
        }
        return Paths.get(attachment.getFilePath());
    }

    // ── private ─────────────────────────────────────────────────

    private void mapDtoToEntity(BusinessFlowDto dto, BusinessFlow entity) {
        setProject(entity, dto.getProjectId());
        entity.setSystemCategory(dto.getSystemCategory());
        entity.setBizCategory(dto.getBizCategory());
        entity.setProcessId(dto.getProcessId());
        entity.setProcessName(dto.getProcessName());
    }

    @SuppressWarnings("null")
    private void setProject(BusinessFlow entity, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
    }

    private void addAttachments(BusinessFlow entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            // 업무흐름 전용 폴더: uploads/business-flow/{projectId}/
            Path dir = Paths.get(uploadDir, "business-flow", String.valueOf(projectId));
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

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

    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }

    private void deletePhysicalFile(String filePath) {
        if (filePath == null) return;
        try { Files.deleteIfExists(Paths.get(filePath)); } catch (IOException ignored) {}
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
