## 17. 엑셀 다운로드/업로드 공통 모듈 — ExcelUtil

### 설계 원칙
- 엑셀 파일 생성/파싱 로직은 `ExcelUtil` (static 유틸)에 집중한다. 서비스/컨트롤러에 POI 코드 직접 작성 금지.
- 다운로드: `ExcelUtil.createWorkbook()` → `ExcelUtil.writeToResponse()`
- 업로드: `ExcelUtil.parseRows(file, 1)` → 서비스 `upsertFromExcel()` 전달
- upsert 키: 각 엔티티의 **비즈니스 ID** (PK가 아닌 사업 내 고유 식별자, 예: `deliverableId`)로 매칭
- 업로드 결과는 Flash 메시지로 표시: `"신규: N건, 수정: M건, 건너뜀: K건"`

### pom.xml 의존성
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.3.0</version>
</dependency>
```

### ExcelUtil 핵심 API (src/main/java/com/e4net/pms/util/ExcelUtil.java)
```java
// 스타일 적용 워크북 생성 (헤더: #2C3E50 배경, 흰색 볼드, 짝수행: #EAF4FB)
public static XSSFWorkbook createWorkbook(String sheetName, String[] headers, List<Object[]> rows)

// HTTP 다운로드 응답으로 전송 (Content-Disposition 헤더 포함, UTF-8 인코딩)
public static void writeToResponse(XSSFWorkbook wb, String fileName, HttpServletResponse response) throws IOException

// 업로드 엑셀 파싱 (headerRows행 제외, 완전 빈 행 자동 제거, .xlsx/.xls 모두 지원)
public static List<String[]> parseRows(MultipartFile file, int headerRows) throws IOException
```

### 컨트롤러 패턴
```java
/** 엑셀 다운로드 */
@GetMapping("/excel/download")
public void excelDownload(HttpSession session, HttpServletResponse response) throws IOException {
    if (isNotReady(session)) { response.sendRedirect("/project-select"); return; }

    Project project = getSelectedProject(session);
    List<XxxEntity> list = xxxService.findAllByProject(project.getId());

    String[] headers = { "컬럼1", "컬럼2", ..., "식별키", "항목명", ... };
    List<Object[]> rows = list.stream().map(e -> new Object[]{
        e.getField1(), e.getField2(), ...
    }).collect(Collectors.toList());

    XSSFWorkbook wb = ExcelUtil.createWorkbook("시트명", headers, rows);
    String fileName = project.getProjectName() + "_기능명_" + LocalDate.now() + ".xlsx";
    ExcelUtil.writeToResponse(wb, fileName, response);
}

/** 엑셀 업로드 */
@PostMapping("/excel/upload")
public String excelUpload(@RequestParam("excelFile") MultipartFile file,
                          HttpSession session, RedirectAttributes ra) throws IOException {
    if (isNotReady(session)) return "redirect:/project-select";
    if (file.isEmpty()) {
        ra.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
        return "redirect:/{resource}";
    }
    List<String[]> rows = ExcelUtil.parseRows(file, 1);
    int[] result = xxxService.upsertFromExcel(rows, getSelectedProject(session).getId(), getLoginUserId(session));
    ra.addFlashAttribute("successMessage",
        String.format("엑셀 업로드 완료 — 신규: %d건, 수정: %d건, 건너뜀: %d건", result[0], result[1], result[2]));
    return "redirect:/{resource}";
}
```

### 서비스 upsert 패턴
```java
@Transactional
public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
    // 컬럼 순서는 다운로드 헤더 배열과 1:1 일치해야 함
    int inserted = 0, updated = 0, skipped = 0;
    for (String[] cells : rows) {
        String bizId = getCell(cells, 4);   // 비즈니스 식별키 컬럼 인덱스
        String name  = getCell(cells, 5);   // 필수값 컬럼
        if (bizId.isBlank() || name.isBlank()) { skipped++; continue; }

        var existing = repository.findByProject_IdAndBizId(projectId, bizId);
        XxxEntity entity;
        boolean isNew;
        if (existing.isPresent()) {
            entity = existing.get(); isNew = false;
        } else {
            entity = new XxxEntity();
            entity.setProject(projectRepository.findById(projectId).orElseThrow(...));
            entity.setRegId(userId); isNew = true;
        }
        // 각 컬럼 인덱스에 맞춰 setter 호출
        entity.setField0(getCell(cells, 0));
        entity.setField1(getCell(cells, 1));
        // ...
        entity.setUpdId(userId);
        repository.save(entity);
        if (isNew) inserted++; else updated++;
    }
    return new int[]{ inserted, updated, skipped };
}

// 안전한 셀 값 추출 헬퍼
private String getCell(String[] cells, int idx) {
    return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
}
```

### Repository 추가 메서드
```java
// 전체 조회 (다운로드용 — 페이징 없음)
List<XxxEntity> findAllByProject_IdOrderByIdAsc(Long projectId);

// upsert 매칭 (비즈니스 ID 기준)
Optional<XxxEntity> findByProject_IdAndBizId(Long projectId, String bizId);
```

### HTML 버튼 및 업로드 모달
```html
<!-- 목록 page-header btn-group에 추가 -->
<a class="btn btn-secondary" th:href="@{/{res}/excel/download}">&#11015; 엑셀 다운로드</a>
<button type="button" class="btn btn-secondary" onclick="openUploadModal()">&#11014; 엑셀 업로드</button>

<!-- 메시지 표시 (errorMessage 추가 필수) -->
<div class="alert alert-success" th:if="${successMessage}" th:text="${successMessage}"></div>
<div class="alert alert-danger"  th:if="${errorMessage}"   th:text="${errorMessage}"></div>

<!-- 업로드 모달 (삭제 모달 아래) -->
<div id="uploadModal" class="modal-overlay">
    <div class="modal modal-sm">
        <div class="modal-header">
            <span class="modal-title">&#11014; 엑셀 업로드</span>
            <button class="modal-close" onclick="closeUploadModal()">&times;</button>
        </div>
        <div class="modal-body">
            <p style="margin-bottom:12px; color:#4a5568; font-size:13px;">
                다운로드한 엑셀 파일의 데이터를 수정 후 업로드하세요.<br>
                <strong style="color:#2980b9;">식별키 컬럼</strong> 기준으로 기존 데이터는 수정, 신규 데이터는 등록됩니다.
            </p>
            <form id="uploadForm" th:action="@{/{res}/excel/upload}" method="post" enctype="multipart/form-data">
                <input type="file" name="excelFile" accept=".xlsx,.xls" class="form-control">
            </form>
        </div>
        <div class="modal-footer">
            <button type="submit" form="uploadForm" class="btn btn-primary">업로드</button>
            <button type="button" class="btn btn-cancel" onclick="closeUploadModal()">취소</button>
        </div>
    </div>
</div>

<script>
    function openUploadModal()  { document.getElementById('uploadModal').classList.add('open'); }
    function closeUploadModal() { document.getElementById('uploadModal').classList.remove('open'); }
    document.getElementById('uploadModal').addEventListener('click', function(e) {
        if (e.target === this) closeUploadModal();
    });
</script>
```

### ExcelUtil 엑셀 스타일 규격
| 항목 | 값 |
|------|-----|
| 헤더 배경색 | `#2C3E50` (프로젝트 primary 색상) |
| 헤더 글자색 | `#FFFFFF` (흰색, 볼드) |
| 짝수 행 배경 | `#EAF4FB` (연한 파랑) |
| 홀수 행 배경 | 흰색 |
| 테두리 | 모든 셀 THIN |
| 헤더 높이 | 22pt |
| 열 너비 | 자동(autoSizeColumn) + 여백 512 단위, 최소 3000 / 최대 14000 |

### 주의사항
- 다운로드 헤더 배열 순서 = 업로드 파싱 셀 인덱스 → **반드시 동기화** 유지
- upsert 키가 되는 비즈니스 ID 컬럼은 헤더에 명확히 표시하고, 업무 설명에 "이 컬럼 기준으로 수정/신규 구분" 안내 필수
- `빈 파일` 업로드 시 `file.isEmpty()` 체크 후 에러 메시지 처리 필수
- Controller import 필수: `XSSFWorkbook`, `ExcelUtil`, `HttpServletResponse`, `MultipartFile`, `LocalDate`, `Collectors`

### ExcelUtil.getCellStringValue — NUMERIC 날짜 직렬 번호 파싱 (중요!)
엑셀의 날짜 셀이 숫자 직렬(예: 46076)로 저장될 때 `DateUtil`로 변환해야 한다.
```java
// ExcelUtil.getCellStringValue NUMERIC 케이스
case NUMERIC -> {
    double d = cell.getNumericCellValue();
    boolean looksLikeDate = DateUtil.isCellDateFormatted(cell)
            || (d >= 36526 && d < 73050 && d == Math.floor(d));
    if (looksLikeDate) {
        try {
            yield DateUtil.getLocalDateTime(d, false).toLocalDate().toString();
        } catch (Exception ignored) {}
    }
    yield (d == Math.floor(d) && !Double.isInfinite(d))
        ? String.valueOf((long) d) : String.valueOf(d);
}
```

### 엑셀 업로드 — 모듈별 전략 정리

| 모듈 | 전략 | 식별키 | 이유 |
|------|------|--------|------|
| 위험관리 | **upsert** | `riskCode` | 행 순서 무관, 부분 수정 가능 |
| 이슈관리 | **upsert** | `issueNo` | 행 순서 무관, 부분 수정 가능 |
| 요구사항 | **upsert** | `reqCode` | 행 순서 무관, 부분 수정 가능 |
| 공통코드 | **upsert** | `groupCode + code` | 그룹-코드 복합키 |
| 산출물 | **upsert** | `deliverableId` | 행 순서 무관 |
| WBS | **delete-all + insert** | 없음 (순서 중요) | sortOrder 의존, 전체 교체 |

### 엑셀 업로드 — delete-all + insert 패턴 (WBS — 순서 의존 데이터)
전체 삭제 후 재삽입. **업로드 전 경고 모달 필수** — 되돌릴 수 없는 파괴적 작업.

1. 첨부파일이 있는 경우: `findByEntityTypeAndEntityIdIn` → 물리 파일 삭제 → `deleteByEntityTypeAndEntityIdIn`
2. 전체 엔티티 삭제: `repository.deleteAll(repository.findAll...)`
3. 엑셀 행 순서대로 신규 INSERT (`sortOrder = 1`부터 순번 부여)
4. 반환: `int[]{ inserted, 0, skipped }` (updated 항상 0)

### WBS 업로드 경고 모달 패턴 (파괴적 replace 전 확인)
```html
<!-- 경고 모달 (먼저 표시) -->
<div id="uploadWarnModal" class="modal-overlay">
    <div class="modal modal-sm">
        <div class="modal-header red">
            <span class="modal-title">&#9888; 업로드 주의</span>
            <button class="modal-close" onclick="document.getElementById('uploadWarnModal').classList.remove('open')">&times;</button>
        </div>
        <div class="modal-body">
            <p>입력된 전체 사업일정이 제거된 후 업로드된 엑셀 정보로 대체됩니다.</p>
            <p style="margin-top:8px; color:#e74c3c; font-size:12px;">이 작업은 되돌릴 수 없습니다.</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-delete" onclick="confirmUpload()">계속</button>
            <button type="button" class="btn btn-cancel" onclick="document.getElementById('uploadWarnModal').classList.remove('open')">취소</button>
        </div>
    </div>
</div>

<!-- 파일 선택 모달 (경고 확인 후 표시) -->
<div id="uploadModal" class="modal-overlay"> ... </div>

<script>
function openUploadModal() {
    document.getElementById('uploadWarnModal').classList.add('open');
}
function confirmUpload() {
    document.getElementById('uploadWarnModal').classList.remove('open');
    document.getElementById('uploadModal').classList.add('open');
}
function closeUploadModal() {
    document.getElementById('uploadModal').classList.remove('open');
    document.getElementById('uploadForm').reset();
}
</script>
```
