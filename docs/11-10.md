## 10. 파일 업로드 패턴 (기능별 폴더 분리)

### application.properties 설정
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
app.upload.dir=uploads   # 프로젝트 루트 기준 상대경로
```

### 폼 (HTML)
```html
<!-- enctype 필수, method="post" -->
<form id="reportForm" th:action="..." method="post" enctype="multipart/form-data" th:object="${dto}">
    <!-- 기존 첨부파일 표시 -->
    <span th:if="${dto.attachFileName != null}" class="file-info">
        현재: &#128206; <span th:text="${dto.attachFileName}"></span>
        <span style="color:#718096;">(새 파일 선택 시 교체됩니다)</span>
    </span>
    <!-- 파일 입력 — th:field 사용 불가, name 직접 지정 -->
    <input type="file" name="attachFile" class="form-control">
</form>
```

### 컨트롤러
```java
// 등록
@PostMapping
public String create(@ModelAttribute("dto") XxxDto dto,
                     @RequestParam(value = "attachFile", required = false) MultipartFile attachFile,
                     HttpSession session, RedirectAttributes ra) throws IOException { ... }

// 수정
@PostMapping("/{id}/edit")
public String update(@PathVariable Long id, @ModelAttribute("dto") XxxDto dto,
                     @RequestParam(value = "attachFile", required = false) MultipartFile attachFile,
                     ...) throws IOException { ... }

// 다운로드
@GetMapping("/{id}/download")
public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) {
    Path filePath = service.getFilePath(id);
    Resource resource = new PathResource(filePath);
    if (!resource.exists()) return ResponseEntity.notFound().build();
    String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
}
```

### 서비스 파일 처리 패턴
업로드 폴더는 기능(보고 유형)별로 분리한다. 같은 테이블을 공유하더라도 폴더는 별도 관리.

```java
@Value("${app.upload.dir:uploads}")
private String uploadDir;

/** reportType → 업로드 하위 폴더명 매핑 (보고관리 공유 테이블 예시) */
private String resolveUploadFolder(String reportType) {
    if ("주간보고".equals(reportType)) return "weekly-report";
    if ("월간보고".equals(reportType)) return "monthly-report";
    if ("회의록".equals(reportType))  return "meeting-report";
    return "regular-report";   // 착수보고/중간보고/완료보고/기타보고
}

private void handleFileUpload(XxxEntity entity, MultipartFile file, Long projectId) throws IOException {
    if (file == null || file.isEmpty()) return;
    deleteAttachedFile(entity);  // 기존 파일 삭제

    String originalName = file.getOriginalFilename();
    String ext = (originalName != null && originalName.contains("."))
                 ? originalName.substring(originalName.lastIndexOf(".")) : "";
    String storedName = UUID.randomUUID() + ext;

    // 기능별 폴더 분리: uploads/{feature}/{projectId}/
    Path dir = Paths.get(uploadDir, resolveUploadFolder(entity.getReportType()), String.valueOf(projectId));
    Files.createDirectories(dir);
    Files.copy(file.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);

    entity.setAttachFileName(originalName);
    entity.setAttachFilePath(dir.resolve(storedName).toAbsolutePath().toString());
}

private void deleteAttachedFile(XxxEntity entity) {
    if (entity.getAttachFilePath() == null) return;
    try { Files.deleteIfExists(Paths.get(entity.getAttachFilePath())); } catch (IOException ignored) {}
}
```
> **삭제 시 주의:** `deleteById()` 호출 전 반드시 `deleteAttachedFile()` 먼저 호출하여 물리 파일 정리.

### DTO 필드
```java
private String attachFileName;  // 원본 파일명 (표시용)
private String attachFilePath;  // 저장 경로 (읽기 전용, 서비스에서 채움)
```
