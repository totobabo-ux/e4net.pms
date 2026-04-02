## 11. 공유 테이블 + 다중 컨트롤러 패턴

### 개요
하나의 DB 테이블을 여러 기능(컨트롤러)이 공유할 때 사용. 대표 예: `customer_report` 테이블을 정기보고/주간보고/월간보고/회의록 4개 컨트롤러가 공유.

### 검색 DTO — `allowedTypes` IN 조건 강제 적용
```java
public class CustomerReportSearchDto {
    private Long         projectId;
    private String       reportType;    // 사용자 입력 검색 조건 (단일)
    private List<String> allowedTypes;  // 컨트롤러가 강제 주입하는 IN 조건
    private String       reportName;
    private String       writer;
}
```

### Spec 클래스 — allowedTypes IN 절 처리
```java
// allowedTypes가 있으면 IN 조건 강제 (목록별 타입 필터링)
if (dto.getAllowedTypes() != null && !dto.getAllowedTypes().isEmpty()) {
    predicates.add(root.get("reportType").in(dto.getAllowedTypes()));
}
// 단일 reportType 검색 조건 (사용자 입력)
if (dto.getReportType() != null && !dto.getReportType().isBlank()) {
    predicates.add(cb.equal(root.get("reportType"), dto.getReportType()));
}
```

### 컨트롤러 — 각 기능별 타입 강제 주입
```java
// 정기보고 컨트롤러
private static final List<String> REGULAR_TYPES = List.of("착수보고","중간보고","완료보고","기타보고");

@GetMapping
public String list(@ModelAttribute("search") CustomerReportSearchDto search, ...) {
    search.setProjectId(getSelectedProject(session).getId());
    search.setAllowedTypes(REGULAR_TYPES);  // ← IN 조건 강제
    ...
}

// 주간/월간/회의록 컨트롤러 (단일 타입)
search.setAllowedTypes(List.of("주간보고"));  // 해당 타입만 조회
dto.setReportType("주간보고");               // 등록/수정 시 타입 고정
```

### 주의사항
- `setReportType()`을 `list()`에서 사용하면 검색 조건과 혼동 → `setAllowedTypes()` 사용 권장
- 공유 테이블이라도 업로드 폴더는 `resolveUploadFolder(reportType)` 으로 기능별 분리 (§10 참조)
