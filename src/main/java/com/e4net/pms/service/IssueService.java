package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.IssueDto;
import com.e4net.pms.dto.IssueSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.Issue;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.IssueRepository;
import com.e4net.pms.repository.IssueSpec;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
public class IssueService {

    // attach_file 테이블의 entity_type 값
    private static final String ENTITY_TYPE = "ISSUE";

    private final IssueRepository issueRepository;
    private final AttachFileRepository attachFileRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 목록 조회 (페이징) */
    public Page<Issue> search(IssueSearchDto dto, @NonNull Pageable pageable) {
        return issueRepository.findAll(IssueSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull Issue findById(@NonNull Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다. id=" + id));
    }

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public Issue save(IssueDto dto, List<MultipartFile> files, String userId) throws IOException {
        Issue entity = new Issue();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        Issue saved = issueRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public Issue update(@NonNull Long id, IssueDto dto, List<MultipartFile> files, String userId) throws IOException {
        Issue entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return issueRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments = attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        issueRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
    }

    /** Entity → DTO */
    public IssueDto toDto(Issue entity) {
        IssueDto dto = new IssueDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setIssueNo(entity.getIssueNo());
        dto.setIssueName(entity.getIssueName());
        dto.setRaiser(entity.getRaiser());
        dto.setRaisedDate(entity.getRaisedDate() != null ? entity.getRaisedDate().toString() : null);
        dto.setIssueContent(entity.getIssueContent());
        dto.setActionPlanDate(entity.getActionPlanDate() != null ? entity.getActionPlanDate().toString() : null);
        dto.setActionPlanContent(entity.getActionPlanContent());
        dto.setActionStatus(entity.getActionStatus());
        dto.setActionDate(entity.getActionDate() != null ? entity.getActionDate().toString() : null);
        dto.setActionContent(entity.getActionContent());
        dto.setNote(entity.getNote());

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

    /** 사업 전체 이슈 조회 (엑셀 다운로드용) */
    public List<Issue> findAllByProject(Long projectId) {
        return issueRepository.findAllByProject_IdOrderByIdAsc(projectId);
    }

    /**
     * 엑셀 워크북 생성 (다운로드용)
     * 컬럼 순서: 관리번호(0) 이슈명(1) 제기자(2) 제기일자(3) 조치상태(4) 조치계획일자(5) 조치일자(6) 이슈내용(7) 조치계획내용(8) 조치내용(9) 비고(10)
     */
    public XSSFWorkbook createExcelWorkbook(Long projectId) {
        List<Issue> list = findAllByProject(projectId);
        String[] headers = { "관리번호", "이슈명", "제기자", "제기일자", "조치상태", "조치계획일자", "조치일자", "이슈내용", "조치계획내용", "조치내용", "비고" };
        List<Object[]> rows = list.stream().map(r -> new Object[]{
            r.getIssueNo(), r.getIssueName(), r.getRaiser(),
            r.getRaisedDate() != null ? r.getRaisedDate().toString() : null,
            r.getActionStatus(),
            r.getActionPlanDate() != null ? r.getActionPlanDate().toString() : null,
            r.getActionDate() != null ? r.getActionDate().toString() : null,
            r.getIssueContent(), r.getActionPlanContent(), r.getActionContent(), r.getNote()
        }).toList();
        return ExcelUtil.createWorkbook("이슈목록", headers, rows);
    }

    /**
     * 엑셀 업로드 — upsert 처리
     * 관리번호 기준으로 현재 사업 내 기존 데이터 수정, 없으면 신규 등록
     * 컬럼 순서: 관리번호(0) 이슈명(1) 제기자(2) 제기일자(3) 조치상태(4) 조치계획일자(5) 조치일자(6) 이슈내용(7) 조치계획내용(8) 조치내용(9) 비고(10)
     *
     * @return int[] { 신규등록 수, 수정 수, 건너뜀 수 }
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String issueNoVal   = getCell(cells, 0);
            String issueNameVal = getCell(cells, 1);
            if (issueNoVal.isBlank() || issueNameVal.isBlank()) {
                skipped++;
                continue;
            }

            Issue entity;
            boolean isNew;
            var existing = issueRepository.findFirstByProject_IdAndIssueNo(projectId, issueNoVal);
            if (existing.isPresent()) {
                entity = existing.get();
                isNew  = false;
            } else {
                entity = new Issue();
                @SuppressWarnings("null")
                Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
                entity.setProject(project);
                entity.setRegId(userId);
                isNew = true;
            }

            entity.setIssueNo(issueNoVal);
            entity.setIssueName(issueNameVal);
            entity.setRaiser(getCell(cells, 2));
            parseDate(getCell(cells, 3), entity::setRaisedDate);
            String actionStatus = getCell(cells, 4);
            entity.setActionStatus(actionStatus.isBlank() ? "미조치" : actionStatus);
            parseDate(getCell(cells, 5), entity::setActionPlanDate);
            parseDate(getCell(cells, 6), entity::setActionDate);
            entity.setIssueContent(getCell(cells, 7));
            entity.setActionPlanContent(getCell(cells, 8));
            entity.setActionContent(getCell(cells, 9));
            entity.setNote(getCell(cells, 10));
            entity.setUpdId(userId);

            issueRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    // ── private ───────────────────────────────────────────────────

    /** DTO → Entity */
    @SuppressWarnings("null")
    private void mapDtoToEntity(IssueDto dto, Issue entity) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
        entity.setIssueNo(dto.getIssueNo());
        entity.setIssueName(dto.getIssueName());
        entity.setRaiser(dto.getRaiser());
        entity.setRaisedDate(parseLocalDate(dto.getRaisedDate()));
        entity.setIssueContent(dto.getIssueContent());
        entity.setActionPlanDate(parseLocalDate(dto.getActionPlanDate()));
        entity.setActionPlanContent(dto.getActionPlanContent());
        entity.setActionStatus(dto.getActionStatus() != null ? dto.getActionStatus() : "미조치");
        entity.setActionDate(parseLocalDate(dto.getActionDate()));
        entity.setActionContent(dto.getActionContent());
        entity.setNote(dto.getNote());
    }

    /** 복수 파일 추가 업로드 */
    private void addAttachments(Issue entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, "issue", String.valueOf(projectId));
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

    /** 날짜 문자열 → LocalDate (파싱 실패 시 null) */
    private LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try { return LocalDate.parse(dateStr); } catch (Exception e) { return null; }
    }

    /** 날짜 문자열 → 엔티티 setter (Consumer 패턴) */
    private void parseDate(String dateStr, java.util.function.Consumer<LocalDate> setter) {
        if (!dateStr.isBlank()) {
            try { setter.accept(LocalDate.parse(dateStr)); } catch (Exception ignored) {}
        }
    }

    /** 셀 값 안전 추출 */
    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }

    /** 파일 크기 표시 변환 */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
