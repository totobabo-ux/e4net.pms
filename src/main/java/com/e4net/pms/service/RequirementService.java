package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.RequirementDto;
import com.e4net.pms.dto.RequirementSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.Requirement;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.RequirementRepository;
import com.e4net.pms.repository.RequirementSpec;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequirementService {

    private static final String ENTITY_TYPE = "REQUIREMENT";

    private final RequirementRepository requirementRepository;
    private final AttachFileRepository attachFileRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public Requirement save(RequirementDto dto, List<MultipartFile> files, String userId) throws IOException {
        Requirement entity = new Requirement();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        Requirement saved = requirementRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public Requirement update(@NonNull Long id, RequirementDto dto, List<MultipartFile> files, String userId) throws IOException {
        Requirement entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return requirementRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments = attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        requirementRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
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

    /** 사업 전체 요구사항 조회 (엑셀 다운로드용, 페이징 없음) */
    public List<Requirement> findAllByProject(Long projectId) {
        return requirementRepository.findAllByProject_IdOrderByIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리
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

    // ── private ───────────────────────────────────────────────────

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

    /** 복수 파일 추가 업로드 */
    private void addAttachments(Requirement entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, "requirement", String.valueOf(projectId));
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

    /** 물리 파일 삭제 */
    private void deletePhysicalFile(String filePath) {
        if (filePath == null) return;
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ignored) {
        }
    }

    /** 파일 크기 표시 변환 */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
