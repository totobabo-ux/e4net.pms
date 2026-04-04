package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.InterfaceListDto;
import com.e4net.pms.dto.InterfaceListSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.InterfaceList;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.InterfaceListRepository;
import com.e4net.pms.repository.InterfaceListSpec;
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
public class InterfaceListService {

    private static final String ENTITY_TYPE   = "INTERFACE_LIST";
    private static final String UPLOAD_FOLDER = "interface-list";

    private final InterfaceListRepository interfaceListRepository;
    private final AttachFileRepository    attachFileRepository;
    private final ProjectRepository       projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ── 조회 ─────────────────────────────────────────────────

    /** 목록 조회 (페이징) */
    public Page<InterfaceList> search(InterfaceListSearchDto dto, @NonNull Pageable pageable) {
        return interfaceListRepository.findAll(InterfaceListSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull InterfaceList findById(@NonNull Long id) {
        return interfaceListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("인터페이스목록을 찾을 수 없습니다. id=" + id));
    }

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    // ── 쓰기 ─────────────────────────────────────────────────

    /** 등록 */
    @Transactional
    public InterfaceList save(InterfaceListDto dto, List<MultipartFile> files, String userId) throws IOException {
        InterfaceList entity = new InterfaceList();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        InterfaceList saved = interfaceListRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public InterfaceList update(@NonNull Long id, InterfaceListDto dto, List<MultipartFile> files, String userId) throws IOException {
        InterfaceList entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return interfaceListRepository.save(entity);
    }

    /** 삭제 (첨부파일 물리 삭제 포함) */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments =
                attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        interfaceListRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
    }

    // ── 변환 ─────────────────────────────────────────────────

    /** Entity → DTO */
    public InterfaceListDto toDto(InterfaceList entity) {
        InterfaceListDto dto = new InterfaceListDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setInterfaceId(entity.getInterfaceId());
        dto.setInterfaceName(entity.getInterfaceName());
        dto.setLinkType(entity.getLinkType());
        dto.setSourceSystem(entity.getSourceSystem());
        dto.setTargetSystem(entity.getTargetSystem());
        dto.setInterfaceMethod(entity.getInterfaceMethod());
        dto.setOccurrenceCycle(entity.getOccurrenceCycle());
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

    /** 사업 전체 목록 (엑셀 다운로드용) */
    public List<InterfaceList> findAllByProject(Long projectId) {
        return interfaceListRepository.findAllByProject_IdOrderByInterfaceIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리 (인터페이스ID 기준)
     * 컬럼 순서: 인터페이스ID(0) 인터페이스명(1) 연계구분(2) 송신시스템(3)
     *            수신시스템(4) 인터페이스방식(5) 발생주기(6) 비고(7)
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String interfaceNameVal = getCell(cells, 1);
            if (interfaceNameVal.isBlank()) {
                skipped++;
                continue;
            }

            String interfaceIdVal = getCell(cells, 0);

            InterfaceList entity;
            boolean isNew;
            if (!interfaceIdVal.isBlank()) {
                var existing = interfaceListRepository.findByProject_IdAndInterfaceId(projectId, interfaceIdVal);
                if (existing.isPresent()) {
                    entity = existing.get();
                    isNew  = false;
                } else {
                    entity = new InterfaceList();
                    setProject(entity, projectId);
                    entity.setRegId(userId);
                    isNew = true;
                }
            } else {
                entity = new InterfaceList();
                setProject(entity, projectId);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setInterfaceId(interfaceIdVal);
            entity.setInterfaceName(interfaceNameVal);
            entity.setLinkType(getCell(cells, 2));
            entity.setSourceSystem(getCell(cells, 3));
            entity.setTargetSystem(getCell(cells, 4));
            entity.setInterfaceMethod(getCell(cells, 5));
            entity.setOccurrenceCycle(getCell(cells, 6));
            entity.setNote(getCell(cells, 7));
            entity.setUpdId(userId);

            interfaceListRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    // ── private ──────────────────────────────────────────────

    private void mapDtoToEntity(InterfaceListDto dto, InterfaceList entity) {
        setProject(entity, dto.getProjectId());
        entity.setInterfaceId(dto.getInterfaceId());
        entity.setInterfaceName(dto.getInterfaceName());
        entity.setLinkType(dto.getLinkType());
        entity.setSourceSystem(dto.getSourceSystem());
        entity.setTargetSystem(dto.getTargetSystem());
        entity.setInterfaceMethod(dto.getInterfaceMethod());
        entity.setOccurrenceCycle(dto.getOccurrenceCycle());
        entity.setNote(dto.getNote());
    }

    @SuppressWarnings("null")
    private void setProject(InterfaceList entity, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
    }

    private void addAttachments(InterfaceList entity, List<MultipartFile> files,
                                Long projectId, String userId) throws IOException {
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
