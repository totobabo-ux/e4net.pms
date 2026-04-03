package com.e4net.pms.service;

import com.e4net.pms.dto.AttachFileDto;
import com.e4net.pms.dto.RiskDto;
import com.e4net.pms.dto.RiskSearchDto;
import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.entity.Project;
import com.e4net.pms.entity.Risk;
import com.e4net.pms.repository.AttachFileRepository;
import com.e4net.pms.repository.ProjectRepository;
import com.e4net.pms.repository.RiskRepository;
import com.e4net.pms.repository.RiskSpec;
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
public class RiskService {

    // attach_file 테이블의 entity_type 값
    private static final String ENTITY_TYPE = "RISK";

    private final RiskRepository riskRepository;
    private final AttachFileRepository attachFileRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 목록 조회 (페이징) */
    public Page<Risk> search(RiskSearchDto dto, @NonNull Pageable pageable) {
        return riskRepository.findAll(RiskSpec.search(dto), pageable);
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull Risk findById(@NonNull Long id) {
        return riskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위험을 찾을 수 없습니다. id=" + id));
    }

    /** 첨부파일 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AttachFile findAttachmentById(@NonNull Long attachmentId) {
        return attachFileRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + attachmentId));
    }

    /** 등록 */
    @Transactional
    public Risk save(RiskDto dto, List<MultipartFile> files, String userId) throws IOException {
        Risk entity = new Risk();
        mapDtoToEntity(dto, entity);
        entity.setRegId(userId);
        entity.setUpdId(userId);
        Risk saved = riskRepository.save(entity);
        addAttachments(saved, files, dto.getProjectId(), userId);
        return saved;
    }

    /** 수정 */
    @Transactional
    public Risk update(@NonNull Long id, RiskDto dto, List<MultipartFile> files, String userId) throws IOException {
        Risk entity = findById(id);
        mapDtoToEntity(dto, entity);
        entity.setUpdId(userId);
        addAttachments(entity, files, dto.getProjectId(), userId);
        return riskRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        findById(id);
        List<AttachFile> attachments = attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id);
        attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
        attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
        riskRepository.deleteById(id);
    }

    /** 첨부파일 개별 삭제 */
    @Transactional
    public void deleteAttachment(@NonNull Long attachmentId) {
        AttachFile attachment = findAttachmentById(attachmentId);
        deletePhysicalFile(attachment.getFilePath());
        attachFileRepository.deleteById(attachmentId);
    }

    /** Entity → DTO */
    public RiskDto toDto(Risk entity) {
        RiskDto dto = new RiskDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setProjectName(entity.getProject().getProjectName());
        dto.setRiskCode(entity.getRiskCode());
        dto.setRiskName(entity.getRiskName());
        dto.setRiskType(entity.getRiskType());
        dto.setIdentifiedDate(entity.getIdentifiedDate() != null ? entity.getIdentifiedDate().toString() : null);
        dto.setDescription(entity.getDescription());
        dto.setProbability(entity.getProbability());
        dto.setImpact(entity.getImpact());
        dto.setRiskLevel(entity.getRiskLevel());
        dto.setResponseStrategy(entity.getResponseStrategy());
        dto.setResponsePlan(entity.getResponsePlan());
        dto.setOwner(entity.getOwner());
        dto.setActivityResult(entity.getActivityResult());
        dto.setStatus(entity.getStatus());

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

    /** 사업 전체 위험 조회 (엑셀 다운로드용, 페이징 없음) */
    public List<Risk> findAllByProject(Long projectId) {
        return riskRepository.findAllByProject_IdOrderByIdAsc(projectId);
    }

    /**
     * 엑셀 업로드 — 기존 데이터 전체 삭제 후 재등록
     * 컬럼 순서: 위험코드(0) 위험명(1) 위험유형(2) 식별일자(3) 발생가능성(4) 영향도(5) 위험등급(6) 대응전략(7) 담당자(8) 위험상태(9) 대응계획(10) 활동결과(11)
     *
     * @return int[] { 신규등록 수, 0(수정없음), 건너뜀 수 }
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        @SuppressWarnings("null")
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

        // 기존 위험 전체 삭제 (첨부파일 포함)
        List<Risk> existingRisks = riskRepository.findAllByProject_IdOrderByIdAsc(projectId);
        if (!existingRisks.isEmpty()) {
            List<Long> riskIds = existingRisks.stream().map(Risk::getId).toList();
            List<AttachFile> attachments = attachFileRepository.findByEntityTypeAndEntityIdIn(ENTITY_TYPE, riskIds);
            attachments.forEach(a -> deletePhysicalFile(a.getFilePath()));
            attachFileRepository.deleteByEntityTypeAndEntityIdIn(ENTITY_TYPE, riskIds);
            riskRepository.deleteAll(existingRisks);
        }

        int inserted = 0, skipped = 0;

        for (String[] cells : rows) {
            String riskNameVal = getCell(cells, 1);
            if (riskNameVal.isBlank()) { skipped++; continue; }

            Risk entity = new Risk();
            entity.setProject(project);
            entity.setRegId(userId);
            entity.setRiskCode(getCell(cells, 0));
            entity.setRiskName(riskNameVal);
            entity.setRiskType(getCell(cells, 2));
            String identifiedDateVal = getCell(cells, 3);
            if (!identifiedDateVal.isBlank()) {
                try { entity.setIdentifiedDate(LocalDate.parse(identifiedDateVal)); } catch (Exception ignored) {}
            }
            String probability = getCell(cells, 4);
            entity.setProbability(probability.isBlank() ? "보통" : probability);
            String impact = getCell(cells, 5);
            entity.setImpact(impact.isBlank() ? "보통" : impact);
            entity.setRiskLevel(getCell(cells, 6));
            entity.setResponseStrategy(getCell(cells, 7));
            entity.setOwner(getCell(cells, 8));
            String status = getCell(cells, 9);
            entity.setStatus(status.isBlank() ? "진행중" : status);
            entity.setResponsePlan(getCell(cells, 10));
            entity.setActivityResult(getCell(cells, 11));
            entity.setUpdId(userId);

            riskRepository.save(entity);
            inserted++;
        }
        return new int[]{ inserted, 0, skipped };
    }

    /** 엑셀 워크북 생성 (다운로드용) */
    public XSSFWorkbook createExcelWorkbook(Long projectId) {
        List<Risk> list = findAllByProject(projectId);
        String[] headers = { "위험코드", "위험명", "위험유형", "식별일자", "발생가능성", "영향도", "위험등급", "대응전략", "담당자", "위험상태", "대응계획", "활동결과" };
        List<Object[]> rows = list.stream().map(r -> new Object[]{
            r.getRiskCode(), r.getRiskName(), r.getRiskType(),
            r.getIdentifiedDate() != null ? r.getIdentifiedDate().toString() : null,
            r.getProbability(), r.getImpact(), r.getRiskLevel(),
            r.getResponseStrategy(), r.getOwner(), r.getStatus(),
            r.getResponsePlan(), r.getActivityResult()
        }).toList();
        return ExcelUtil.createWorkbook("위험목록", headers, rows);
    }

    // ── private ───────────────────────────────────────────────────

    /** DTO → Entity */
    @SuppressWarnings("null")
    private void mapDtoToEntity(RiskDto dto, Risk entity) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));
        entity.setProject(project);
        entity.setRiskCode(dto.getRiskCode());
        entity.setRiskName(dto.getRiskName());
        entity.setRiskType(dto.getRiskType());
        entity.setIdentifiedDate(dto.getIdentifiedDate() != null && !dto.getIdentifiedDate().isBlank()
                ? LocalDate.parse(dto.getIdentifiedDate()) : null);
        entity.setDescription(dto.getDescription());
        entity.setProbability(dto.getProbability() != null ? dto.getProbability() : "보통");
        entity.setImpact(dto.getImpact() != null ? dto.getImpact() : "보통");
        entity.setRiskLevel(dto.getRiskLevel());
        entity.setResponseStrategy(dto.getResponseStrategy());
        entity.setResponsePlan(dto.getResponsePlan());
        entity.setOwner(dto.getOwner());
        entity.setActivityResult(dto.getActivityResult());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "진행중");
    }

    /** 복수 파일 추가 업로드 (기존 파일 유지, 새 파일만 추가) */
    private void addAttachments(Risk entity, List<MultipartFile> files, Long projectId, String userId) throws IOException {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, "risk", String.valueOf(projectId));
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

    /** 셀 값 안전 추출 */
    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }

    /** 파일 크기 표시 변환 (bytes → KB/MB) */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
