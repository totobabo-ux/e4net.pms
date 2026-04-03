package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.ScreenListDto;
import com.e4net.pms.dto.ScreenListSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.ScreenList;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.ScreenListRepository;
import com.e4net.pms.repository.ScreenListSpec;
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
public class ScreenListService {

    private static final String ENTITY_TYPE = "SCREEN_LIST";

    private final ScreenListRepository screenListRepository;
    private final AttachFileRepository attachFileRepository;
    private final ProjectRepository    projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 목록 조회 (페이징) */
    public Page<ScreenList> search(ScreenListSearchDto dto, @NonNull Pageable pageable) {
        return screenListRepository.findAll(ScreenListSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull ScreenList findById(@NonNull Long id) {
        return screenListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("화면목록을 찾을 수 없습니다. id=" + id));
    }

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public ScreenList save(ScreenListDto dto, List<MultipartFile> files, String userId) throws IOException {
        ScreenList entity = new ScreenList();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        ScreenList saved = screenListRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public ScreenList update(@NonNull Long id, ScreenListDto dto, List<MultipartFile> files, String userId) throws IOException {
        ScreenList entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return screenListRepository.save(entity);
    }

    /** 삭제 (물리 파일 + DB 레코드) */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments =
                attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        screenListRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
    }

    /** Entity → DTO */
    public ScreenListDto toDto(ScreenList entity) {
        ScreenListDto dto = new ScreenListDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setMenuLevel1(entity.getMenuLevel1());
        dto.setMenuLevel2(entity.getMenuLevel2());
        dto.setMenuLevel3(entity.getMenuLevel3());
        dto.setCategory(entity.getCategory());
        dto.setScreenName(entity.getScreenName());
        dto.setScreenDesc(entity.getScreenDesc());
        dto.setUrl(entity.getUrl());
        dto.setTemplateFile(entity.getTemplateFile());
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

    /** 다운로드용 파일 경로 조회 */
    public Path getAttachmentFilePath(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        if (attachment.getFilePath() == null) {
            throw new IllegalStateException("첨부파일 경로가 없습니다.");
        }
        return Paths.get(attachment.getFilePath());
    }

    /** 사업 전체 화면목록 조회 (엑셀 다운로드용) */
    public List<ScreenList> findAllByProject(Long projectId) {
        return screenListRepository.findAllByProject_IdOrderByIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리
     * 컬럼 순서: 메뉴Level1(0) 메뉴Level2(1) 메뉴Level3(2) 분류(3)
     *            화면명(4) 화면설명(5) URL(6) 템플릿파일(7) 비고(8)
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String screenNameVal = getCell(cells, 4);
            if (screenNameVal.isBlank()) {
                skipped++;
                continue;
            }

            String urlVal = getCell(cells, 6);

            // URL 기준으로 기존 데이터 탐색, 없으면 신규
            ScreenList entity;
            boolean isNew;
            if (!urlVal.isBlank()) {
                var existing = screenListRepository.findByProject_IdAndUrl(projectId, urlVal);
                if (existing.isPresent()) {
                    entity = existing.get();
                    isNew  = false;
                } else {
                    entity = new ScreenList();
                    setProject(entity, projectId);
                    entity.setRegId(userId);
                    isNew = true;
                }
            } else {
                entity = new ScreenList();
                setProject(entity, projectId);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setMenuLevel1(getCell(cells, 0));
            entity.setMenuLevel2(getCell(cells, 1));
            entity.setMenuLevel3(getCell(cells, 2));
            entity.setCategory(getCell(cells, 3));
            entity.setScreenName(screenNameVal);
            entity.setScreenDesc(getCell(cells, 5));
            entity.setUrl(urlVal);
            entity.setTemplateFile(getCell(cells, 7));
            entity.setNote(getCell(cells, 8));
            entity.setUpdId(userId);

            screenListRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    // ── private ─────────────────────────────────────────────────

    private void mapDtoToEntity(ScreenListDto dto, ScreenList entity) {
        setProject(entity, dto.getProjectId());
        entity.setMenuLevel1(dto.getMenuLevel1());
        entity.setMenuLevel2(dto.getMenuLevel2());
        entity.setMenuLevel3(dto.getMenuLevel3());
        entity.setCategory(dto.getCategory());
        entity.setScreenName(dto.getScreenName());
        entity.setScreenDesc(dto.getScreenDesc());
        entity.setUrl(dto.getUrl());
        entity.setTemplateFile(dto.getTemplateFile());
        entity.setNote(dto.getNote());
    }

    @SuppressWarnings("null")
    private void setProject(ScreenList entity, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
    }

    private void addAttachments(ScreenList entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, "screen-list", String.valueOf(projectId));
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
