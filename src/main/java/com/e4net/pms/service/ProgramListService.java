package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.ProgramListDto;
import com.e4net.pms.dto.ProgramListSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.ProgramList;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.ProgramListRepository;
import com.e4net.pms.repository.ProgramListSpec;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramListService {

    private static final String ENTITY_TYPE = "PROGRAM_LIST";
    private static final String UPLOAD_FOLDER = "program-list";

    private final ProgramListRepository programListRepository;
    private final AttachFileRepository attachFileRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 목록 조회 (페이징) */
    public Page<ProgramList> search(ProgramListSearchDto dto, @NonNull Pageable pageable) {
        return programListRepository.findAll(ProgramListSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull ProgramList findById(@NonNull Long id) {
        return programListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("프로그램을 찾을 수 없습니다. id=" + id));
    }

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public ProgramList save(ProgramListDto dto, List<MultipartFile> files, String userId) throws IOException {
        ProgramList entity = new ProgramList();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        ProgramList saved = programListRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public ProgramList update(@NonNull Long id, ProgramListDto dto, List<MultipartFile> files, String userId) throws IOException {
        ProgramList entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return programListRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments = attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        programListRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
    }

    /** Entity → DTO */
    public ProgramListDto toDto(ProgramList entity) {
        ProgramListDto dto = new ProgramListDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setSystemName(entity.getSystemName());
        dto.setProgramId(entity.getProgramId());
        dto.setProgramName(entity.getProgramName());
        dto.setClassName(entity.getClassName());
        dto.setClassPath(entity.getClassPath());
        dto.setProgramType(entity.getProgramType());
        dto.setDifficulty(entity.getDifficulty());
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

    /** 사업 전체 프로그램 조회 (엑셀 다운로드용, 페이징 없음) */
    public List<ProgramList> findAllByProject(Long projectId) {
        return programListRepository.findAllByProject_IdOrderByIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리
     * 프로그램ID 기준으로 현재 사업 내 기존 데이터 수정, 없으면 신규 등록
     * 컬럼 순서: 시스템구분(0) 프로그램ID(1) 프로그램명(2) 클래스명(3) 클래스경로(4) 프로그램구분(5) 개발난이도(6) 비고(7)
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String programIdVal  = getCell(cells, 1);
            String programNameVal = getCell(cells, 2);
            if (programIdVal.isBlank() || programNameVal.isBlank()) {
                skipped++;
                continue;
            }

            ProgramList entity;
            boolean isNew;
            var existing = programListRepository.findByProject_IdAndProgramId(projectId, programIdVal);
            if (existing.isPresent()) {
                entity = existing.get();
                isNew  = false;
            } else {
                entity = new ProgramList();
                @SuppressWarnings("null")
                Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
                entity.setProject(project);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setSystemName(getCell(cells, 0));
            entity.setProgramId(programIdVal);
            entity.setProgramName(programNameVal);
            entity.setClassName(getCell(cells, 3));
            entity.setClassPath(getCell(cells, 4));
            entity.setProgramType(getCell(cells, 5));
            entity.setDifficulty(getCell(cells, 6));
            entity.setNote(getCell(cells, 7));
            entity.setUpdId(userId);

            programListRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    // ── private ───────────────────────────────────────────────────

    /** DTO → Entity */
    @SuppressWarnings("null")
    private void mapDtoToEntity(ProgramListDto dto, ProgramList entity) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
        entity.setSystemName(dto.getSystemName());
        entity.setProgramId(dto.getProgramId());
        entity.setProgramName(dto.getProgramName());
        entity.setClassName(dto.getClassName());
        entity.setClassPath(dto.getClassPath());
        entity.setProgramType(dto.getProgramType());
        entity.setDifficulty(dto.getDifficulty());
        entity.setNote(dto.getNote());
    }

    /** 복수 파일 추가 업로드 */
    private void addAttachments(ProgramList entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
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

    /** 셀 값 안전 추출 */
    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }
}
