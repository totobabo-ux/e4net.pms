package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.UnitTestDto;
import com.e4net.pms.dto.UnitTestSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.UnitTest;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.UnitTestRepository;
import com.e4net.pms.repository.UnitTestSpec;
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
public class UnitTestService {

    private static final String ENTITY_TYPE   = "UNIT_TEST";
    private static final String UPLOAD_FOLDER = "unit-test";

    private final UnitTestRepository   unitTestRepository;
    private final AttachFileRepository attachFileRepository;
    private final ProjectRepository    projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 목록 조회 (페이징) */
    public Page<UnitTest> search(UnitTestSearchDto dto, @NonNull Pageable pageable) {
        return unitTestRepository.findAll(UnitTestSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull UnitTest findById(@NonNull Long id) {
        return unitTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("단위테스트를 찾을 수 없습니다. id=" + id));
    }

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public UnitTest save(UnitTestDto dto, List<MultipartFile> files, String userId) throws IOException {
        UnitTest entity = new UnitTest();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        UnitTest saved = unitTestRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public UnitTest update(@NonNull Long id, UnitTestDto dto, List<MultipartFile> files, String userId) throws IOException {
        UnitTest entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return unitTestRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments =
                attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        unitTestRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
    }

    /** Entity → DTO */
    public UnitTestDto toDto(UnitTest entity) {
        UnitTestDto dto = new UnitTestDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setCategory(entity.getCategory());
        dto.setUnitTestId(entity.getUnitTestId());
        dto.setUnitTestName(entity.getUnitTestName());
        dto.setDescription(entity.getDescription());
        dto.setTester(entity.getTester());
        dto.setNote(entity.getNote());

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

    /** 사업 전체 조회 (엑셀 다운로드용) */
    public List<UnitTest> findAllByProject(Long projectId) {
        return unitTestRepository.findAllByProject_IdOrderByCategoryAscUnitTestIdAsc(projectId);
    }

    /** 다운로드용 파일 경로 조회 */
    public Path getAttachmentFilePath(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        if (attachment.getFilePath() == null) {
            throw new IllegalStateException("첨부파일 경로가 없습니다.");
        }
        return Paths.get(attachment.getFilePath());
    }

    /**
     * 엑셀 업로드 — upsert 처리 (단위테스트ID 기준)
     * 컬럼 순서: 분류(0) 단위테스트ID(1) 단위테스트명(2) 설명(3) 테스트담당자(4) 비고(5)
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String unitTestNameVal = getCell(cells, 2);
            if (unitTestNameVal.isBlank()) {
                skipped++;
                continue;
            }

            String unitTestIdVal = getCell(cells, 1);

            UnitTest entity;
            boolean isNew;
            if (!unitTestIdVal.isBlank()) {
                var existing = unitTestRepository.findByProject_IdAndUnitTestId(projectId, unitTestIdVal);
                if (existing.isPresent()) {
                    entity = existing.get();
                    isNew  = false;
                } else {
                    entity = new UnitTest();
                    setProject(entity, projectId);
                    entity.setRegId(userId);
                    isNew = true;
                }
            } else {
                entity = new UnitTest();
                setProject(entity, projectId);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setCategory(getCell(cells, 0));
            entity.setUnitTestId(unitTestIdVal);
            entity.setUnitTestName(unitTestNameVal);
            entity.setDescription(getCell(cells, 3));
            entity.setTester(getCell(cells, 4));
            entity.setNote(getCell(cells, 5));
            entity.setUpdId(userId);

            unitTestRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    // ── private ─────────────────────────────────────────────────

    @SuppressWarnings("null")
    private void mapDtoToEntity(UnitTestDto dto, UnitTest entity) {
        setProject(entity, dto.getProjectId());
        entity.setCategory(dto.getCategory());
        entity.setUnitTestId(dto.getUnitTestId());
        entity.setUnitTestName(dto.getUnitTestName());
        entity.setDescription(dto.getDescription());
        entity.setTester(dto.getTester());
        entity.setNote(dto.getNote());
    }

    @SuppressWarnings("null")
    private void setProject(UnitTest entity, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
    }

    private void addAttachments(UnitTest entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, UPLOAD_FOLDER, String.valueOf(projectId));
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

    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }
}
