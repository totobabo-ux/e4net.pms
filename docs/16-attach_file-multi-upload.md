## 16. 공통 첨부파일 테이블 패턴 — attach_file (다중 파일, 신규 기능 기본)

### 설계 원칙
- 모든 엔티티의 첨부파일은 단일 테이블 `attach_file`로 통합 관리한다.
- `entity_type`(문자열 상수) + `entity_id`(PK) 조합으로 폴리모픽 연관을 구현한다.
- 엔티티에 `@OneToMany` 관계를 두지 않는다. 연관은 서비스 레이어에서만 처리한다.
- 물리 파일 경로는 `attach_file.file_path`에 절대경로로 저장한다.

### Entity — AttachFile.java
```java
@Entity
@Table(name = "attach_file",
       indexes = @Index(name = "idx_attach_file_entity", columnList = "entity_type, entity_id"))
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AttachFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_id", length = 50, updatable = false)
    private String regId;
}
```

### Repository — AttachFileRepository.java
```java
public interface AttachFileRepository extends JpaRepository<AttachFile, Long> {
    List<AttachFile> findByEntityTypeAndEntityIdOrderByRegDtAsc(String entityType, Long entityId);
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
```

### DTO — AttachFileDto.java
```java
@Getter @Setter @NoArgsConstructor
public class AttachFileDto {
    private Long   id;
    private String fileName;
    private Long   fileSize;
    private String fileSizeDisplay;   // 예: "1.2 MB" — 서비스에서 계산
}
```
> `fileSizeDisplay` 포맷: `< 1KB → "NNN B"`, `< 1MB → "N.N KB"`, 이상 → `"N.N MB"`

### 부모 DTO — 첨부파일 목록 필드 추가
```java
// XxxDto 에 추가
private List<AttachFileDto> attachments = new ArrayList<>();
```

### 서비스 패턴
```java
// 엔티티 타입 상수 (모듈마다 고유값, 대문자 스네이크)
private static final String ENTITY_TYPE = "CUSTOMER_REPORT";   // 예시

// 첨부파일 저장 (등록/수정 공통)
private void addAttachments(XxxEntity entity, List<MultipartFile> files, String userId) throws IOException {
    if (files == null || files.isEmpty()) return;
    for (MultipartFile file : files) {
        if (file.isEmpty()) continue;
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                     ? originalName.substring(originalName.lastIndexOf(".")) : "";
        String storedName = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, resolveUploadFolder(...), String.valueOf(projectId));
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);

        AttachFile attachment = new AttachFile();
        attachment.setEntityType(ENTITY_TYPE);
        attachment.setEntityId(entity.getId());
        attachment.setFileName(originalName);
        attachment.setFilePath(dir.resolve(storedName).toAbsolutePath().toString());
        attachment.setFileSize(file.getSize());
        attachment.setRegId(userId);
        attachFileRepository.save(attachment);
    }
}

// DTO 변환 시 첨부파일 로드
private XxxDto toDto(XxxEntity entity) {
    XxxDto dto = new XxxDto();
    // ... 필드 매핑 ...
    List<AttachFileDto> attachDtos = attachFileRepository
        .findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, entity.getId())
        .stream().map(a -> {
            AttachFileDto d = new AttachFileDto();
            d.setId(a.getId());
            d.setFileName(a.getFileName());
            d.setFileSize(a.getFileSize());
            d.setFileSizeDisplay(formatFileSize(a.getFileSize()));
            return d;
        }).collect(Collectors.toList());
    dto.setAttachments(attachDtos);
    return dto;
}

// 삭제 시 첨부파일 cascade 처리 (순서 반드시 준수)
public void delete(Long id) {
    XxxEntity entity = findById(id);
    // 1) 물리 파일 삭제
    attachFileRepository.findByEntityTypeAndEntityIdOrderByRegDtAsc(ENTITY_TYPE, id)
        .forEach(a -> deletePhysicalFile(a.getFilePath()));
    // 2) DB 첨부파일 행 삭제
    attachFileRepository.deleteByEntityTypeAndEntityId(ENTITY_TYPE, id);
    // 3) 본체 엔티티 삭제
    repository.deleteById(id);
}

private void deletePhysicalFile(String filePath) {
    if (filePath == null) return;
    try { Files.deleteIfExists(Paths.get(filePath)); } catch (IOException ignored) {}
}

private String formatFileSize(Long bytes) {
    if (bytes == null || bytes == 0) return "0 B";
    if (bytes < 1024)      return bytes + " B";
    if (bytes < 1_048_576) return String.format("%.1f KB", bytes / 1024.0);
    return String.format("%.1f MB", bytes / 1_048_576.0);
}
```

### 컨트롤러 패턴
```java
// 등록
@PostMapping
public String create(@ModelAttribute XxxDto dto,
                     @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                     HttpSession session, RedirectAttributes ra) throws IOException {
    XxxEntity saved = service.save(dto, getLoginUserId(session));
    service.addAttachments(saved, attachFiles, getLoginUserId(session));
    ra.addFlashAttribute("successMessage", "등록되었습니다.");
    return "redirect:/{resource}/" + saved.getId();
}

// 수정
@PostMapping("/{id}/edit")
public String update(@PathVariable Long id, @ModelAttribute XxxDto dto,
                     @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
                     HttpSession session, RedirectAttributes ra) throws IOException {
    service.update(id, dto, getLoginUserId(session));
    service.addAttachments(service.findEntityById(id), attachFiles, getLoginUserId(session));
    ra.addFlashAttribute("successMessage", "수정되었습니다.");
    return "redirect:/{resource}/" + id;
}

// 첨부파일 다운로드
@GetMapping("/{id}/attachment/{attId}/download")
public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id,
                                                    @PathVariable Long attId,
                                                    HttpSession session) {
    if (isNotReady(session)) return ResponseEntity.status(302).build();
    AttachFile attachment = service.findAttachmentById(attId);
    Path filePath = Paths.get(attachment.getFilePath());
    Resource resource = new PathResource(filePath);
    if (!resource.exists()) return ResponseEntity.notFound().build();
    String encodedName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                                   .replace("+", "%20");
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
}

// 첨부파일 개별 삭제
@PostMapping("/{id}/attachment/{attId}/delete")
public String deleteAttachment(@PathVariable Long id, @PathVariable Long attId,
                                HttpSession session, RedirectAttributes ra) {
    if (isNotReady(session)) return "redirect:/";
    service.deleteAttachment(attId);
    ra.addFlashAttribute("successMessage", "첨부파일이 삭제되었습니다.");
    return "redirect:/{resource}/" + id;
}
```

### HTML attach-zone 컴포넌트 템플릿

#### 등록 폼 (form.html) — `pendingList` / `pendingList_label`
```html
<div class="form-cell" style="grid-column: 1 / -1; align-items:flex-start;">
    <label class="form-label">첨부파일</label>
    <div class="attach-zone">
        <!-- 기존 첨부파일 (수정 폼에만 표시) -->
        <div th:if="${not #lists.isEmpty(report.attachments)}">
            <div class="attach-section-label">기존 첨부파일</div>
            <div th:each="att : ${report.attachments}" class="attach-row attach-existing">
                <span class="attach-icon">&#128196;</span>
                <span class="attach-name" th:text="${att.fileName}"></span>
                <span class="attach-size" th:text="${att.fileSizeDisplay}"></span>
                <form th:action="@{/{res}/{id}/attachment/{attId}/delete(id=${report.id},attId=${att.id})}"
                      method="post" style="margin:0; flex-shrink:0;">
                    <button type="submit" class="btn-sm-del"
                            onclick="return confirm('이 첨부파일을 삭제하시겠습니까?')">삭제</button>
                </form>
            </div>
        </div>
        <!-- 추가할 파일 목록 (JS 렌더링) -->
        <div id="pendingList_label" class="attach-section-label" style="display:none; margin-top:8px;">추가할 파일</div>
        <div id="pendingList"></div>
        <div class="attach-add-area">
            <button type="button" class="btn-attach-add"
                    onclick="document.getElementById('attachFileInput').click()">
                &#128206; 파일 추가
            </button>
            <span class="attach-hint">여러 파일 동시 선택 가능</span>
        </div>
        <input type="file" id="attachFileInput" name="attachFiles" multiple style="display:none;">
    </div>
</div>
```
```html
<!-- 폼 하단 스크립트 -->
<script th:src="@{/js/common.js}"></script>
<script>
    initAttachUpload('attachFileInput', 'pendingList', 'reportForm');
</script>
```

#### 상세 페이지 조회 섹션 (viewSection)
```html
<div class="detail-cell" style="grid-column: 1 / -1; align-items:flex-start;">
    <span class="detail-label" style="padding-top:4px;">첨부파일</span>
    <div style="flex:1; min-width:0;">
        <div th:if="${#lists.isEmpty(report.attachments)}" class="attach-empty">첨부파일 없음</div>
        <div th:each="att : ${report.attachments}" class="attach-row attach-view">
            <span class="attach-icon">&#128196;</span>
            <a th:href="@{/{res}/{id}/attachment/{attId}/download(id=${report.id},attId=${att.id})}"
               class="attach-name td-link" th:text="${att.fileName}"></a>
            <span class="attach-size" th:text="${att.fileSizeDisplay}"></span>
            <span style="font-size:12px; color:#2980b9;">&#11015; 다운로드</span>
        </div>
    </div>
</div>
```

#### 상세 페이지 수정 섹션 (editSection) — `editPendingList` / `editPendingList_label`
- 컨테이너 id: `editPendingList`, 레이블 id: `editPendingList_label`
- `initAttachUpload('editFileInput', 'editPendingList', 'reportEditForm')` 호출

> **ID 명명 규칙 (중요!):**
> - 등록 폼: `pendingList` (컨테이너), `pendingList_label` (레이블), `attachFileInput` (input)
> - 수정 섹션: `editPendingList` (컨테이너), `editPendingList_label` (레이블), `editFileInput` (input)
> - `initAttachUpload`는 `pendingListId + '_label'` 패턴으로 레이블 id를 조합하므로 **반드시 일치**해야 함

### CSS 첨부파일 컴포넌트 클래스 (common.css 정의)
| 클래스 | 용도 |
|--------|------|
| `.attach-zone` | 첨부파일 영역 전체 컨테이너 |
| `.attach-section-label` | "기존 첨부파일" / "추가할 파일" 소제목 |
| `.attach-row` | 파일 1행 공통 레이아웃 (flex) |
| `.attach-existing` | 기존 첨부파일 행 (회색 배경) |
| `.attach-pending` | 추가 예정 파일 행 (연두 배경) |
| `.attach-view` | 조회 전용 파일 행 (파란 링크) |
| `.attach-icon` | 파일 아이콘 |
| `.attach-name` | 파일명 (텍스트/링크) |
| `.attach-size` | 파일 크기 표시 |
| `.attach-empty` | 첨부파일 없음 메시지 |
| `.attach-add-area` | "파일 추가" 버튼 영역 |
| `.btn-attach-add` | "파일 추가" 버튼 |
| `.attach-hint` | "여러 파일 동시 선택 가능" 힌트 텍스트 |

### initAttachUpload JS 함수 동작 원리 (common.js)
- `pendingFiles` 배열에 `{ file: File, name: string, size: number }` 객체로 저장 (raw File만 저장하면 브라우저에서 stale 참조 문제 발생)
- 파일 선택 후 `input.value = ''` 초기화 → 같은 파일 재선택 가능, 중복은 name+size 비교로 제외
- 폼 submit 시 `DataTransfer` API로 `pendingFiles` → `input.files` 동기화
- `DataTransfer.items.add(file)` 실패 시 `new File([blob], name)` Blob 폴백 적용 (stale File 참조 대응)
- 전역 제거 핸들러: `window['__removePending_' + pendingListId]` — 같은 페이지에 여러 attach-zone 공존 가능
