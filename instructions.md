# 프로젝트 규칙: 공공기관 정보화 사업관리 시스템 (PMS)

## 1. 기본 원칙
- 모든 코드는 유지보수가 용이하도록 MVC 패턴을 엄격히 준수한다.
- 디자인은 '미니멀리즘'과 '공공기관 표준 UI'를 지향한다. (깨끗한 화이트/블루톤)
- 중복 코드를 지양하고, 공통 모듈(CSS, JS, 유틸리티)을 우선 활용한다.

## 2. 기술 스택 (Tech Stack)
- Backend: Spring Boot 3.4.x, Java 25
- Frontend: HTML5, CSS3, JavaScript (ES6+), Thymeleaf
- DB: MySQL 8.0 / DATABASE 명: `e4net_pms`
- Persistence: Spring Data JPA (Hibernate) + Specification 패턴
- Build: Maven
- 비밀번호 암호화: `spring-security-crypto` (BCryptPasswordEncoder) — Spring Security 필터 체인 없이 단독 사용

## 3. UI/UX 및 디자인 규칙 (중요!)

### 레이아웃 방식 — 반드시 확인
이 프로젝트에는 **두 가지** 레이아웃 방식이 공존한다. 새 페이지 작성 전에 기존 화면의 방식을 따른다.

| 방식 | 파일 | 사용법 |
|------|------|--------|
| **fragments replace (주력)** | `layout/fragments.html` | `th:replace="~{layout/fragments :: header}"` / `th:replace="~{layout/fragments :: sidebar('activePage키')}"` |
| Thymeleaf Layout Dialect | `layout/base.html` | `layout:decorate="~{layout/base}"` + `layout:fragment="content"` |

> 대부분의 화면(manpower, requirement 등)은 **fragments replace 방식**을 사용한다.

### 공통 색상
- Primary (Header/강조): `#2c3e50` (다크 블루)
- Accent (버튼/링크): `#2980b9` (블루)
- Success: `#27ae60` / Danger: `#e74c3c` / Background: `#f8f9fa` / Sidebar: `#1a252f`

### 업무 화면 구성 원칙 (중요!)
모든 업무 화면은 **목록 / 등록 / 상세** 3개 템플릿으로 구성한다.

| 파일 | URL 패턴 | 설명 |
|------|----------|------|
| `list.html` | `GET /{resource}` | 검색 + 페이징 목록. 마지막 열에 **[수정] [삭제]** 버튼 필수. [수정] → `/{id}/edit`, [삭제] → 공유 모달 경유 POST |
| `form.html` | `GET /{resource}/new` | 신규 등록 전용 폼 |
| `detail.html` | `GET /{resource}/{id}` | **조회 + 수정 병행** — 기본은 읽기 전용 표시, [수정] 버튼 클릭 시 같은 페이지에서 편집 폼으로 전환 |

#### list.html 수정/삭제 버튼 패턴
```html
<!-- 테이블 마지막 열 헤더 -->
<th>관리</th>

<!-- 테이블 행 마지막 열 -->
<td>
    <button class="btn-sm-edit"
            th:onclick="|location.href='@{/{res}/{id}/edit(id=${r.id})}'|">수정</button>
    <button class="btn-sm-del"
            th:onclick="|openDeleteModal(${r.id}, '[[${r.표시필드}]]')|">삭제</button>
</td>

<!-- 페이지 하단 공유 삭제 모달 (1개) -->
<div id="deleteModal" class="modal-overlay">
    <div class="modal modal-sm">
        <div class="modal-header red">
            <span class="modal-title">&#128465; 삭제 확인</span>
            <button class="modal-close" onclick="closeDeleteModal()">&times;</button>
        </div>
        <div class="modal-body">
            <p><strong id="deleteTargetName"></strong> 을(를) 삭제하시겠습니까?</p>
            <p style="margin-top:8px;color:#718096;font-size:12px;">삭제된 데이터는 복구할 수 없습니다.</p>
        </div>
        <div class="modal-footer">
            <form id="deleteForm" method="post">
                <button type="submit" class="btn btn-delete">삭제</button>
            </form>
            <button type="button" class="btn btn-cancel" onclick="closeDeleteModal()">취소</button>
        </div>
    </div>
</div>
<script>
    function openDeleteModal(id, name) {
        document.getElementById('deleteTargetName').textContent = name;
        document.getElementById('deleteForm').action = '/{resource}/' + id + '/delete';
        document.getElementById('deleteModal').classList.add('open');
    }
    function closeDeleteModal() { document.getElementById('deleteModal').classList.remove('open'); }
    document.getElementById('deleteModal').addEventListener('click', function(e) {
        if (e.target === this) closeDeleteModal();
    });
</script>
```
> **버튼 클래스:** `btn-sm-edit` (파란 텍스트), `btn-sm-del` (빨간 텍스트) — common.css 정의됨.

#### detail.html 조회/수정 전환 패턴
```html
<!-- 조회 섹션 (기본 표시) -->
<div id="viewSection"> ... detail-grid ... </div>

<!-- 수정 섹션 (기본 숨김) -->
<div id="editSection" style="display:none;">
  <form id="editForm" th:action="@{/{resource}/{id}/edit(id=${dto.id})}" method="post" th:object="${dto}">
    ... form-grid-2col ...
  </form>
</div>

<!-- 버튼: 조회 모드 -->
<div id="viewButtons">
  <button onclick="switchToEdit()">수정</button>
  <button onclick="openDeleteModal()">삭제</button>
  <a href="/{resource}">목록</a>
</div>

<!-- 버튼: 수정 모드 -->
<div id="editButtons" style="display:none;">
  <button type="submit" form="editForm">저장</button>
  <button onclick="switchToView()">취소</button>
</div>
```
```javascript
function switchToEdit() {
    document.getElementById('viewSection').style.display = 'none';
    document.getElementById('editSection').style.display = 'block';
    document.getElementById('viewButtons').style.display = 'none';
    document.getElementById('editButtons').style.display = 'flex';
}
function switchToView() {
    document.getElementById('viewSection').style.display = 'block';
    document.getElementById('editSection').style.display = 'none';
    document.getElementById('viewButtons').style.display = 'flex';
    document.getElementById('editButtons').style.display = 'none';
}
// 유효성 오류 시 수정 모드 자동 유지
(function() {
    var hasErrors = /*[[${#fields.hasErrors('*')}]]*/ false;
    if (hasErrors) switchToEdit();
})();
```

### 기타 UI 규칙
- **폼 구조:** 기본 2열 배치 (`form-grid-2col`). 전체 너비 항목은 `style="grid-column: 1 / -1;"`.
- **필수 항목:** 레이블 옆에 `<span class="req">※</span>` 표시. 서버(Bean Validation) + 클라이언트(JS) 양쪽 검증.
- **목록 페이지:** 검색 폼 + 페이징 처리된 테이블. 테이블 상단에 검색 바 배치.
- **Badge 상태 표시:** `<span class="badge" th:classappend="'badge-' + ${r.status}" th:text="${r.status}">` 패턴 사용.

## 4. 데이터베이스 및 코딩 컨벤션

### 공통 컬럼 (모든 테이블 필수)
```sql
reg_dt  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
reg_id  VARCHAR(50)                                  COMMENT '등록자',
upd_dt  DATETIME           ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
upd_id  VARCHAR(50)                                  COMMENT '수정자'
```
> **주의:** `reg_dt`는 반드시 `NOT NULL DEFAULT CURRENT_TIMESTAMP`를 포함해야 한다. 없으면 JPA INSERT 시 500 오류 발생.
> 기존 테이블에 누락된 경우 아래 SQL로 수정:
> ```sql
> ALTER TABLE {테이블명} MODIFY reg_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시';
> ```

### 명명 규칙
- DB 컬럼: 스네이크 케이스 (snake_case)
- Java 코드: 카멜 케이스 (camelCase)
- 모든 컨트롤러 메서드와 비즈니스 로직 상단에 **한글 주석** 필수

## 5. 인증 및 세션 규칙 (중요!)

### 로그인 흐름
```
POST /login → 사번(employeeNo) + 비밀번호 검증 (BCrypt)
  → session.setAttribute("loginUser", user)
  → redirect:/project-select
  
POST /project-select → 선택한 프로젝트 ID로 Project 조회
  → session.setAttribute("selectedProject", project)
  → redirect:/home
```

### 세션 속성
| 속성명 | 타입 | 설명 |
|--------|------|------|
| `loginUser` | `User` 엔티티 | 로그인한 사용자 |
| `selectedProject` | `Project` 엔티티 | 현재 선택된 사업 |

### 메뉴별 세션 체크 규칙
| 메뉴 영역 | 체크 메서드 | 조건 |
|-----------|------------|------|
| **관리자** (사용자/프로젝트 관리) | `isNotLoggedIn()` | `loginUser == null` |
| **사업관리 / 사업수행** 모든 메뉴 | `isNotReady()` | `loginUser == null \|\| selectedProject == null` |

```java
// 사업관리/사업수행 컨트롤러 공통 패턴
private boolean isNotReady(HttpSession session) {
    return session.getAttribute("loginUser") == null
        || session.getAttribute("selectedProject") == null;
}
private Project getSelectedProject(HttpSession session) {
    return (Project) session.getAttribute("selectedProject");
}
```

### 사업 자동 필터 패턴 (사업관리/사업수행 목록 공통)
- 검색 DTO에 `projectId` 필드 포함 (사업명 검색 조건 별도 불필요)
- 목록 컨트롤러 list() 메서드에서 세션 프로젝트 ID 강제 적용:
  ```java
  search.setProjectId(getSelectedProject(session).getId());
  ```
- 등록/수정 폼에서 사업명은 `<select>` 드롭다운 **금지** → hidden input + 텍스트 표시:
  ```html
  <input type="hidden" th:field="*{projectId}">
  <!-- 모델 속성이 아닌 세션 직접 접근 — ${selectedProject}는 렌더링 안 됨 -->
  <span th:text="${session.selectedProject != null ? session.selectedProject.projectName : ''}"
        style="font-weight:bold; color:#2c3e50;"></span>
  ```
- 목록 페이지 타이틀에 선택된 사업명 표시:
  ```html
  <span th:if="${selectedProject != null}"
        style="font-size:13px;font-weight:normal;color:#2980b9;margin-left:10px;"
        th:text="'[ ' + ${selectedProject.projectName} + ' ]'"></span>
  ```

## 6. JPA Specification 패턴

### 검색 DTO 구조 (사업관리/사업수행 공통)
```java
private Long   projectId;   // 세션 기반 자동 필터 (사업명 검색 아님)
private String ...;         // 나머지 검색 조건
```

### Spec 클래스 공통 패턴
```java
// count 쿼리가 아닐 때만 fetch join (N+1 방지)
if (query != null && !Long.class.equals(query.getResultType())) {
    root.fetch("project", JoinType.LEFT);
    query.distinct(true);
}
// projectId 필터
if (dto.getProjectId() != null) {
    Join<Object, Object> project = root.join("project", JoinType.LEFT);
    predicates.add(cb.equal(project.get("id"), dto.getProjectId()));
}
```

## 7. 파일 구조 및 패키지 구성
```
src/main/
  java/com/e4net/pms/
    controller/   ← @Controller, @RestController
    service/      ← @Service, 비즈니스 로직
    repository/   ← @Repository, JPA + XxxSpec.java
    entity/       ← @Entity, DB 매핑
    dto/          ← 폼 DTO (XxxDto), 검색 DTO (XxxSearchDto)
  resources/
    static/
      css/common.css   ← 공통 CSS (전역 스타일)
      js/common.js     ← 공통 JS
    templates/
      layout/
        fragments.html ← header, sidebar 프래그먼트 (주력)
        base.html      ← Thymeleaf Layout Dialect 마스터
      index.html          ← 로그인 페이지
      project-select.html ← 프로젝트 선택 페이지 (로그인 직후)
      project/         ← 관리자 > 사업 관리 (list.html / form.html)
      manpower/        ← 사업관리 > 인력관리
      requirement/     ← 사업관리 > 범위관리 > 요구사항관리
      wbs/             ← 사업관리 > 범위관리 > 사업일정(WBS)
      regular-report/  ← 사업관리 > 보고관리 > 정기보고
      weekly-report/   ← 사업관리 > 보고관리 > 주간보고
      monthly-report/  ← 사업관리 > 보고관리 > 월간보고
      meeting-report/  ← 사업관리 > 보고관리 > 회의록
      notice/          ← 커뮤니티 > 공지사항 (list.html / form.html / detail.html)
      archive/         ← 커뮤니티 > 자료실 (list.html / form.html / detail.html)
      screen-list/     ← 사업수행 > 설계 > 화면목록 (list.html / form.html / detail.html)
      menu-structure/  ← 사업수행 > 분석 > 메뉴구조 (list.html — jsTree 분할패널)
uploads/             ← 첨부파일 저장소 (app.upload.dir, .gitignore 제외 권장)
  regular-report/{projectId}/{uuid}.확장자  ← 정기보고
  weekly-report/{projectId}/{uuid}.확장자   ← 주간보고
  monthly-report/{projectId}/{uuid}.확장자  ← 월간보고
  meeting-report/{projectId}/{uuid}.확장자  ← 회의록
  community/{communityType}/{entityId}/{uuid}.확장자  ← 공지사항/자료실
  screen-list/{projectId}/{uuid}.확장자     ← 화면목록 첨부파일
```

## 8. activePage 키 목록 (사이드바 메뉴 활성화)

`fragments.html`의 `sidebar(activePage)` 프래그먼트에 전달하는 키 값. prefix 일치 시 상위 메뉴 자동 펼침.

| 키 값 | 메뉴 경로 | URL |
|-------|---------|-----|
| `home` | 사업 Home | `/home` |
| `biz-standard-output` | 사업관리 > 표준관리 > 산출물 관리 | `/deliverable` |
| `biz-manpower-input` | 사업관리 > 인력관리 > 투입인력 관리 | `/manpower` |
| `biz-scope-requirement` | 사업관리 > 범위관리 > 요구사항 관리 | `/requirement` |
| `biz-scope-wbs` | 사업관리 > 범위관리 > 사업일정(WBS) | `/wbs` |
| `biz-report-regular` | 사업관리 > 보고관리 > 정기보고 | `/regular-report` |
| `biz-report-weekly` | 사업관리 > 보고관리 > 주간보고 | `/weekly-report` |
| `biz-report-monthly` | 사업관리 > 보고관리 > 월간보고 | `/monthly-report` |
| `biz-report-meeting` | 사업관리 > 보고관리 > 회의록 | `/meeting-report` |
| `biz-risk-risk` | 사업관리 > 위험관리 > 위험관리 | (미구현) |
| `biz-risk-issue` | 사업관리 > 위험관리 > 이슈관리 | (미구현) |
| `exec-analysis-flow` | 사업수행 > 분석 > 업무흐름 | (미구현) |
| `exec-analysis-menu` | 사업수행 > 분석 > 메뉴구조 | `/menu-structure` |
| `exec-design-interface` | 사업수행 > 설계 > 인터페이스 목록 | (미구현) |
| `exec-design-screen` | 사업수행 > 설계 > 화면목록 | `/screen-list` |
| `exec-design-program` | 사업수행 > 설계 > 프로그램목록 | (미구현) |
| `exec-impl-unit` | 사업수행 > 구현 > 단위테스트 목록 | (미구현) |
| `exec-test-integration` | 사업수행 > 시험 > 통합테스트 목록 | (미구현) |
| `community-notice-list` | 커뮤니티 > 공지사항 | `/notice` |
| `community-archive-list` | 커뮤니티 > 자료실 | `/archive` |
| `admin-project-list` | 관리자 > 사업 관리 > 사업 목록 | `/project/list` |
| `admin-user-list` | 관리자 > 사용자 관리 > 사용자 목록 | `/admin/users` |
| `admin-role-list` | 관리자 > 권한 관리 > 권한 목록 | (미구현) |

## 9. 세션 사용자 정보 활용 패턴

### 컨트롤러 공통 헬퍼 메서드
사업관리/사업수행 컨트롤러에는 아래 두 헬퍼를 함께 선언한다.

```java
/** 로그인 사용자 이름 (작성자 기본값용) */
private String getLoginUserName(HttpSession session) {
    User user = (User) session.getAttribute("loginUser");
    return user != null ? user.getName() : "";
}

/** 로그인 사용자 사번 (reg_id / upd_id 저장용) */
private String getLoginUserId(HttpSession session) {
    User user = (User) session.getAttribute("loginUser");
    return user != null ? user.getEmployeeNo() : "";
}
```

### 등록 폼 작성자 기본값 자동 입력
```java
@GetMapping("/new")
public String createForm(HttpSession session, Model model) {
    ...
    dto.setWriter(getLoginUserName(session));   // 작성자 기본값
    model.addAttribute("report", dto);
    ...
}
```

### reg_id / upd_id 서비스 파라미터 패턴
서비스 `save()` / `update()`에 `String userId` 파라미터를 추가해 엔티티에 세팅한다.

```java
// 서비스
public XxxEntity save(XxxDto dto, String userId) {
    XxxEntity entity = new XxxEntity();
    mapDtoToEntity(dto, entity);
    entity.setRegId(userId);   // 등록 시 reg_id + upd_id 모두 세팅
    entity.setUpdId(userId);
    return repository.save(entity);
}

public XxxEntity update(Long id, XxxDto dto, String userId) {
    XxxEntity entity = findById(id);
    mapDtoToEntity(dto, entity);
    entity.setUpdId(userId);   // 수정 시 upd_id만 갱신
    return repository.save(entity);
}

// 컨트롤러
service.save(dto, getLoginUserId(session));
service.update(id, dto, getLoginUserId(session));
```

> **엔티티 주의:** `reg_id` 컬럼은 `updatable = false`로 선언해야 수정 시 덮어쓰이지 않는다.
> ```java
> @Column(name = "reg_id", length = 50, updatable = false)
> private String regId;
> ```

### reg_dt / upd_dt 자동 관리 (JPA Auditing)
`@EnableJpaAuditing` (PmsApplication) + `@EntityListeners(AuditingEntityListener.class)` 조합으로 자동 처리. 별도 코드 불필요.

```java
@CreatedDate
@Column(name = "reg_dt", updatable = false)   // 또는 created_at
private LocalDateTime regDt;

@LastModifiedDate
@Column(name = "upd_dt")                       // 또는 updated_at
private LocalDateTime updDt;
```

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

## 12. 예외 처리 패턴

### 컨트롤러 레벨 예외 처리 (존재하지 않는 ID 접근 등)
```java
// 서비스에서 throw new IllegalArgumentException("...") 발생 시 500 대신 목록으로 리다이렉트
@ExceptionHandler(IllegalArgumentException.class)
public String handleNotFound(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    return "redirect:/{resource}";
}
```

### 목록 페이지 에러 메시지 표시
```html
<div class="alert alert-success" th:if="${successMessage}" th:text="${successMessage}"></div>
<div class="alert alert-danger"  th:if="${errorMessage}"   th:text="${errorMessage}"></div>
```

## 13. DB 컬럼 변경 주의사항

- `spring.jpa.hibernate.ddl-auto=update` 설정은 **신규 컬럼 추가만** 자동 처리한다.
- **컬럼 삭제/수정은 자동 반영되지 않으므로** 엔티티에서 필드 제거 후 반드시 수동 실행:
  ```sql
  ALTER TABLE {테이블명}
    DROP COLUMN column1,
    DROP COLUMN column2;
  -- MySQL 5.x: IF EXISTS 미지원, 컬럼 존재 여부 먼저 확인
  ```
- `DEFAULT CURRENT_TIMESTAMP` 등 DB 기본값은 ddl-auto가 생성하지 않으므로 `reg_dt`는 수동 DDL 확인 필수.

## 14. 신규 페이지 개발 체크리스트

### 화면 구성
- [ ] 템플릿 3종 생성: `list.html` / `form.html` (등록+수정) / `detail.html` (조회+수정 병행)
- [ ] `list.html`: 마지막 열 `관리` — `btn-sm-edit`(수정), `btn-sm-del`(삭제) 버튼 + 공유 삭제 모달
- [ ] `list.html`: 성공/오류 Flash 메시지 표시 (`alert-success` / `alert-danger`)
- [ ] `detail.html`: viewSection/editSection 분리, switchToEdit()/switchToView() JS 전환 패턴 적용
- [ ] `detail.html`: 유효성 오류 시 수정 모드 자동 유지 (`hasErrors` 체크 스크립트 포함)
- [ ] `detail.html`: 삭제 확인 모달 포함
- [ ] `fragments.html` sidebar에 신규 메뉴 링크 추가 (activePage 키 목록 §8 참조)

### 컨트롤러 URL 매핑
- [ ] `GET /{resource}` → 목록
- [ ] `GET /{resource}/new` + `POST /{resource}` → 등록
- [ ] `GET /{resource}/{id}` → 상세 (`detail.html` 반환)
- [ ] `GET /{resource}/{id}/edit` + `POST /{resource}/{id}/edit` → 수정
- [ ] `POST /{resource}/{id}/delete` → 삭제 처리 후 `redirect:/{resource}`
- [ ] `@ExceptionHandler(IllegalArgumentException.class)` 추가 (존재하지 않는 ID → 500 방지)

### 단일 파일 업로드 기능 포함 시 추가 항목 (레거시 패턴 — 신규는 §16 다중파일 패턴 사용)
- [ ] `application.properties`에 multipart 설정 확인 (`app.upload.dir`, `max-file-size`)
- [ ] 폼에 `enctype="multipart/form-data"` 추가
- [ ] 컨트롤러 등록/수정에 `@RequestParam MultipartFile` 파라미터 추가
- [ ] 서비스에 `handleFileUpload()` / `deleteAttachedFile()` 구현
- [ ] 다운로드 엔드포인트 `GET /{resource}/{id}/download` 추가
- [ ] 삭제 시 물리 파일도 함께 삭제 (`deleteById()` 전 `deleteAttachedFile()` 호출)
- [ ] DTO에 `attachFileName` / `attachFilePath` 필드 추가

### 다중 파일 업로드 기능 포함 시 추가 항목 (§16 attach_file 공통 테이블 패턴)
- [ ] `attach_file` 테이블 존재 확인 (ddl-auto=update로 자동 생성되나 `reg_dt DEFAULT` 수동 확인)
- [ ] `AttachFile` 엔티티, `AttachFileRepository`, `AttachFileDto` 재사용 (신규 생성 불필요)
- [ ] 서비스에 `private static final String ENTITY_TYPE = "XXX"` 상수 선언
- [ ] 서비스 `addAttachments()` 구현: `List<MultipartFile>` 순회 → 파일 저장 → `AttachFile` 엔티티 저장
- [ ] 서비스 `toDto()`에서 `attachFileRepository.findByEntityTypeAndEntityId...` 로 첨부파일 로드
- [ ] 서비스 `delete()`: 물리 파일 삭제 → `deleteByEntityTypeAndEntityId` → `deleteById` 순서 필수
- [ ] 컨트롤러 등록/수정에 `@RequestParam(value="attachFiles", required=false) List<MultipartFile>` 추가
- [ ] 컨트롤러에 다운로드 `GET /{id}/attachment/{attId}/download` 엔드포인트 추가
- [ ] 컨트롤러에 개별 삭제 `POST /{id}/attachment/{attId}/delete` 엔드포인트 추가
- [ ] 폼에 `enctype="multipart/form-data"` 추가
- [ ] HTML attach-zone 컴포넌트 삽입 (§16 템플릿 참조)
- [ ] HTML 레이블 id: `pendingList_label` (등록폼) / `editPendingList_label` (수정 섹션) — 오타 금지
- [ ] `<script>initAttachUpload('fileInputId', 'pendingListId', 'formId')</script>` 호출
- [ ] DTO에 `List<AttachFileDto> attachments = new ArrayList<>()` 필드 추가

### 사용자 정보 연동
- [ ] 컨트롤러에 `getLoginUserId()` / `getLoginUserName()` 헬퍼 메서드 선언
- [ ] 서비스 `save()` / `update()`에 `String userId` 파라미터 추가
- [ ] 서비스 `save()`: `entity.setRegId(userId)` + `entity.setUpdId(userId)` 모두 세팅
- [ ] 서비스 `update()`: `entity.setUpdId(userId)` 만 갱신
- [ ] 엔티티 `regId` 컬럼: `@Column(updatable = false)` 선언 확인
- [ ] 등록 폼(`createForm()`)에서 작성자 기본값: `dto.setWriter(getLoginUserName(session))`

### 공유 테이블 기능 추가 시
- [ ] 검색 DTO에 `allowedTypes: List<String>` 필드 추가
- [ ] Spec 클래스에 `allowedTypes` IN 조건 처리 추가 (§11 참조)
- [ ] 각 컨트롤러 `list()`에서 `search.setAllowedTypes(...)` 강제 주입
- [ ] 등록/수정 시 `dto.setReportType("...")` 고정 처리

### 엑셀 다운로드/업로드 기능 포함 시 (§17 ExcelUtil 공통 모듈 참조)
- [ ] `pom.xml`에 `poi-ooxml 5.3.0` 의존성 확인
- [ ] Repository에 전체 조회 메서드 추가: `findAllByProject_IdOrderByIdAsc(Long projectId)`
- [ ] Repository에 upsert 매칭 메서드 추가: `findByProject_IdAndXxxId(Long, String)`
- [ ] Service에 `findAllByProject(Long projectId)` 메서드 추가
- [ ] Service에 `upsertFromExcel(List<String[]> rows, Long projectId, String userId)` 추가
  - 식별키(ID 컬럼)로 기존 데이터 조회 → 있으면 UPDATE, 없으면 INSERT
  - 반환: `int[] { 신규등록, 수정, 건너뜀 }`
- [ ] Controller에 `GET /{resource}/excel/download` 엔드포인트 추가
- [ ] Controller에 `POST /{resource}/excel/upload` 엔드포인트 추가
- [ ] 목록 HTML에 "엑셀 다운로드" `<a>` 링크 + "엑셀 업로드" 버튼 추가
- [ ] 목록 HTML에 업로드 모달 추가 (`enctype="multipart/form-data"`, `name="excelFile"`)
- [ ] 목록 HTML에 `alert-danger` 에러 메시지 표시 추가 (빈 파일 등 업로드 오류)
- [ ] 다운로드 파일명 패턴: `{사업명}_{기능명}_{오늘날짜}.xlsx`

### DB 및 공통
- [ ] DB 테이블에 `reg_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`, `reg_id`, `upd_dt ON UPDATE CURRENT_TIMESTAMP`, `upd_id` 컬럼 포함 (DDL 직접 확인 — JPA ddl-auto가 DEFAULT 값을 생성하지 않으므로 반드시 수동 검증)
- [ ] 컬럼 삭제 시 `ALTER TABLE DROP COLUMN` 수동 실행 (ddl-auto=update는 자동 삭제 안 함)
- [ ] Controller 메서드 한글 주석 작성
- [ ] 레이아웃 방식: `fragments replace` 방식 사용 (신규 업무 화면 기본)
- [ ] 메뉴 종류에 따라 `isNotReady()` 또는 `isNotLoggedIn()` 세션 체크 적용
- [ ] 사업관리/사업수행: 검색 DTO에 `projectId` 추가 + Spec 클래스 projectId 필터 구현
- [ ] 사업관리/사업수행: 목록 컨트롤러에서 `search.setProjectId(selectedProject.getId())` 적용
- [ ] 등록/수정 폼: 사업명 select 대신 hidden input + 텍스트 표시
- [ ] 목록 타이틀에 `[ 사업명 ]` 표시
- [ ] 필수 항목 ※ 표시 및 Bean Validation 적용
- [ ] 목록 페이지: 검색 + 페이징 적용 (페이지네이션 링크에 검색 파라미터 유지)

## 15. Spring Boot 서버 기동 방법
```bash
# 프로젝트 루트에서 실행
mvn spring-boot:run

# 포트 충돌 시 기존 프로세스 종료 후 재기동 (Windows)
netstat -ano | grep :8080       # PID 확인
taskkill /PID <PID> /F
mvn spring-boot:run

# 스테일 클래스 파일로 인한 오류 발생 시
mvn clean spring-boot:run
```

### application.properties 필수 인코딩 설정
```properties
# POST 폼 한글 인코딩 (필수 — 없으면 한글 깨짐)
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

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

    /** 여러 엔티티의 첨부파일 일괄 조회 (엑셀 업로드 delete-all 패턴용) */
    List<AttachFile> findByEntityTypeAndEntityIdIn(String entityType, List<Long> entityIds);

    /** 여러 엔티티의 첨부파일 일괄 삭제 */
    void deleteByEntityTypeAndEntityIdIn(String entityType, List<Long> entityIds);
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

### 엑셀 업로드 — delete-all + insert 패턴 (WBS/이슈/위험 등 순서 의존 데이터)
기존 upsert 대신 전체 삭제 후 재삽입하는 패턴.
사용 시점: 행 순서(sortOrder)가 의미 있거나 모든 데이터를 교체해야 할 때.

1. 첨부파일이 있는 경우: `findByEntityTypeAndEntityIdIn` → 물리 파일 삭제 → `deleteByEntityTypeAndEntityIdIn`
2. 전체 엔티티 삭제: `repository.deleteAll(repository.findAll...)`
3. 엑셀 행 순서대로 신규 INSERT (`sortOrder = 1`부터 순번 부여)
4. 반환: `int[]{ inserted, 0, skipped }` (updated 항상 0)

## 18. 테스트 계정 관리 및 curl 통합 테스트 패턴

### BCrypt 해시 생성 (테스트 계정 DB 삽입용)
```bash
# Maven 테스트 클래스로 해시 생성
cat > src/test/java/com/e4net/pms/HashGeneratorTest.java << 'EOF'
package com.e4net.pms;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashGeneratorTest {
    @Test void generateHash() {
        System.out.println(new BCryptPasswordEncoder().encode("원하는비밀번호"));
    }
}
EOF
mvn test -Dtest=HashGeneratorTest 2>&1 | grep '^\$2a'
```

```bash
# spring-security-crypto jar로 직접 생성 (Maven 빌드 불필요)
SPRING_JAR=$(find ~/.m2 -name "spring-security-crypto-*.jar" | head -1)
COMMONS_LOG=$(find ~/.m2 -name "commons-logging-*.jar" | head -1)

cat > /tmp/GenHash.java << 'EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class GenHash {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode(args[0]));
    }
}
EOF

javac -cp "$SPRING_JAR" /tmp/GenHash.java -d /tmp
java -cp "/tmp:$SPRING_JAR:$COMMONS_LOG" GenHash "비밀번호"
```

### 테스트 계정 DB 삽입 (users 테이블 스키마 기준)
```sql
INSERT INTO users (employee_no, name, email, password)
VALUES ('devtest', '개발테스트', 'dev@test.com', '{bcrypt_hash}')
ON DUPLICATE KEY UPDATE password = '{bcrypt_hash}';
```
> `users` 테이블에 `role` 컬럼 없음 — INSERT 시 포함하면 오류

### curl 세션 기반 통합 테스트 패턴
```bash
# 1) 로그인 (session.txt에 쿠키 저장)
curl -s -c session.txt -b session.txt \
  -X POST http://localhost:8080/login \
  -d "username=devtest&password=test1234" \
  -D headers.txt -o /dev/null
grep "Location" headers.txt   # project-select 로 리다이렉트 되면 로그인 성공

# 2) 프로젝트 선택
curl -s -c session.txt -b session.txt \
  -X POST http://localhost:8080/project-select \
  -d "projectId=1" -o /dev/null

# 3) 기능 테스트
curl -s -c session.txt -b session.txt http://localhost:8080/{resource} -w "HTTP: %{http_code}"

# 엑셀 다운로드
curl -s -c session.txt -b session.txt \
  http://localhost:8080/{resource}/excel/download \
  -o result.xlsx -w "HTTP: %{http_code}, 크기: %{size_download} bytes"

# 엑셀 업로드
curl -s -c session.txt -b session.txt \
  -X POST http://localhost:8080/{resource}/excel/upload \
  -F "excelFile=@result.xlsx" -D - -o /dev/null | grep Location
```

> **중요:** curl 세션 요청 시 `-c session.txt -b session.txt` **둘 다** 지정해야 쿠키가 읽히고 쓰임.
> `-c`만 지정하면 쿠키를 저장하지만 전송하지 않음 → 세션 인식 불가

### 엑셀 파일 유효성 빠른 확인
```bash
file result.xlsx          # "Microsoft Excel 2007+" 확인
unzip -l result.xlsx      # 내부 구조 확인
unzip -p result.xlsx xl/sharedStrings.xml   # 셀 텍스트 내용 확인
```

## 19. 커뮤니티(공지사항/자료실) 패턴 — 로그인 전용, 사업 무관

### 설계 원칙
- 단일 `community` 테이블을 `community_type` 컬럼으로 구분 (§11 공유 테이블 패턴 응용)
- 커뮤니티는 **사업(Project)에 귀속되지 않음** → `selectedProject` 세션 불필요
- 세션 체크: `loginUser`만 확인 (`isNotLogin()`), `isNotReady()` 사용 금지
- 컨트롤러 2개(`NoticeController`, `ArchiveController`) → 서비스 1개(`CommunityService`) 공유
- 각 컨트롤러에서 `COMMUNITY_TYPE` 상수로 타입 고정 후 서비스에 전달

### Entity — Community.java
```java
@Entity
@Table(name = "community")
public class Community {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "community_type", length = 20, nullable = false)
    private String communityType;   // "공지사항" / "자료실"

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "writer", length = 100)
    private String writer;

    @Column(name = "post_date")
    private LocalDate postDate;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // reg_dt, reg_id, upd_dt, upd_id (공통 감사 컬럼)
}
```

### Repository / Spec
```java
// CommunityRepository: JpaRepository + JpaSpecificationExecutor
// CommunitySpec: communityType 동등 비교 + title/writer LIKE 조건
```

### 컨트롤러 패턴 (공지사항 예시 — 자료실은 URL/TYPE만 다름)
```java
@Controller
@RequestMapping("/notice")
public class NoticeController {
    private static final String COMMUNITY_TYPE = "공지사항";
    private static final String ACTIVE_PAGE    = "community-notice-list";

    // 세션 체크: loginUser만 확인
    private boolean isNotLogin(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }

    @GetMapping
    public String list(...) {
        if (isNotLogin(session)) return "redirect:/login";
        search.setCommunityType(COMMUNITY_TYPE);   // 타입 고정
        ...
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("community") CommunityDto dto, ...) {
        dto.setCommunityType(COMMUNITY_TYPE);   // 저장 전 타입 주입
        communityService.save(dto, attachFiles, getLoginUserId(session));
        ...
    }
}
```

### 서비스 — CommunityService.java
- `ENTITY_TYPE = "COMMUNITY"`
- 업로드 경로: `uploads/community/{communityType}/{entityId}/`
- `AttachFileRepository` 동일하게 재사용 (`findByEntityTypeAndEntityIdOrderByRegDtAsc` 등)

### DTO
```java
public class CommunityDto {
    private Long   id;
    private String communityType;   // 컨트롤러에서 주입, 폼 필드 아님
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    private String writer;
    private LocalDate postDate;
    private String content;
    private List<AttachFileDto> attachments;
}
```

### activePage 키 (fragments.html에서 링크 연결됨)
| 키 값 | URL |
|-------|-----|
| `community-notice-list` | `/notice` |
| `community-archive-list` | `/archive` |

### 체크리스트
- [ ] `isNotLogin(session)` 사용 (isNotReady 금지 — selectedProject 없음)
- [ ] 컨트롤러에서 `dto.setCommunityType(COMMUNITY_TYPE)` 반드시 호출 (폼 바인딩 없음)
- [ ] 작성자 기본값: `dto.setWriter(getLoginUserName(session))`
- [ ] 폼에 `enctype="multipart/form-data"` + `initAttachUpload()` 호출
- [ ] fragments.html sidebar에 href 연결 확인

## 20. AJAX/JSON 기반 컨트롤러 패턴 (트리·인터랙티브 UI)

### 사용 시점
- 페이지 전환 없이 부분 갱신이 필요한 화면 (트리뷰, 분할 패널 등)
- 서버 응답을 JSON으로 받아 JavaScript에서 DOM 직접 조작
- 대표 예: `메뉴구조` (`MenuController`)

### 컨트롤러 패턴
```java
// 메인 페이지 (HTML 반환) — 일반 MVC
@GetMapping
public String list(HttpSession session, Model model) { ... }

// AJAX 엔드포인트 — @ResponseBody로 JSON 반환
@GetMapping("/tree")
@ResponseBody
public List<Map<String, Object>> getTreeData(HttpSession session) { ... }

@GetMapping("/{id}/detail")
@ResponseBody
public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id, ...) { ... }

// AJAX POST — @RequestBody로 JSON 수신
@PostMapping
@ResponseBody
public ResponseEntity<Map<String, Object>> create(@RequestBody XxxDto dto, ...) { ... }

@PostMapping("/{id}/edit")
@ResponseBody
public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                   @RequestBody XxxDto dto, ...) { ... }

@PostMapping("/{id}/delete")
@ResponseBody
public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, ...) { ... }
```

### 응답 구조 (성공/실패 통일)
```java
// 성공
return ResponseEntity.ok(Map.of("success", true, "id", saved.getId(), "menuCode", "M010000"));

// 실패
return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
```

### JavaScript fetch 패턴
```javascript
// POST JSON
fetch('/menu-structure', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ menuName: 'name', contextPath: '/path', useYn: 'Y' })
})
.then(r => r.json())
.then(res => {
    if (!res.success) { alert('오류: ' + res.message); return; }
    // 성공 처리
});

// GET JSON
fetch('/menu-structure/1/detail')
    .then(r => r.json())
    .then(detail => fillForm(detail));
```

### 주의사항
- `@RequestBody` 수신 시 curl 테스트에서 한글 포함 JSON은 인코딩 오류 발생 → 브라우저에서 테스트
- `@ExceptionHandler`도 `@ResponseBody` 추가 필요 (HTML 대신 JSON 반환)
- CSRF: 기본 Spring Security 설정 하에서 JSON POST는 쿠키 기반 세션으로 인증되므로 별도 토큰 불필요 (테스트 확인)

## 21. jsTree 기반 트리뷰 분할 패널 패턴

### 사용 시점
계층형(트리) 데이터를 좌측 트리 + 우측 상세 패널 형태로 관리하는 화면.
대표 예: `메뉴구조` (`menu-structure/list.html`)

### CDN 로드 (jQuery 필수)
```html
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.3.16/themes/default/style.min.css">
<!-- ... body 하단 ... -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.3.16/jstree.min.js"></script>
```

### jsTree 초기화 (서버 URL 방식)
```javascript
$('#menuTree').jstree({
    core: {
        data: { url: '/menu-structure/tree', dataType: 'json' },
        check_callback: true   // create_node / rename_node / delete_node 허용
    },
    plugins: ['search', 'wholerow'],
    search: { case_insensitive: true, show_only_matches: true }
});
```

### 서버 응답 포맷 (jsTree flat list)
```java
// 루트 노드: parent = "#"
// 자식 노드: parent = String.valueOf(parentId)
Map<String, Object> node = new LinkedHashMap<>();
node.put("id",     String.valueOf(m.getId()));
node.put("parent", m.getParentId() == null ? "#" : String.valueOf(m.getParentId()));
node.put("text",   m.getMenuName() + " (" + m.getMenuCode() + ")");
node.put("state",  Map.of("opened", m.getDepth() <= 2));
node.put("icon",   m.getDepth() < 3 ? "jstree-folder" : "jstree-file");
node.put("data",   Map.of("menuCode", m.getMenuCode(), "depth", m.getDepth(), ...));
```

### 주요 이벤트 및 메서드
```javascript
// 노드 선택 이벤트
$('#tree').on('select_node.jstree', function(e, data) {
    const node = data.node;
    const nodeData = node.data;   // node.data 에 서버 응답 data 객체
});

// 트리 조작 (check_callback: true 필요)
$('#tree').jstree('rename_node', nodeId, '새 이름');
$('#tree').jstree('delete_node', nodeId);
$('#tree').jstree(true).refresh();   // 서버에서 재로드

// 새로고침 후 특정 노드 선택
$('#tree').one('refresh.jstree', function() {
    $(this).jstree('select_node', String(newId));
});

// 검색
$('#tree').jstree('search', searchText);
```

### 레이아웃 CSS 핵심
```css
.menu-wrap {
    display: flex;
    gap: 14px;
    height: calc(100vh - 190px);   /* 브라우저 높이 기준 */
}
.tree-panel  { width: 340px; flex-shrink: 0; display: flex; flex-direction: column; }
.detail-panel { flex: 1; display: flex; flex-direction: column; }
#menuTree { flex: 1; overflow-y: auto; }
```

### 메뉴코드 자동 생성 규칙
```
포맷: M + Depth1(2자리) + Depth2(2자리) + Depth3(2자리) = 총 7자
예:   M010000 > M010100 > M010101

Depth 1: M{seq}0000          seq = 기존 루트 최대순번 + 1
Depth 2: M{parent[1-2]}{seq}00  seq = 형제 최대순번 + 1
Depth 3: M{parent[1-4]}{seq}    seq = 형제 최대순번 + 1
```

### 체크리스트
- [ ] jsTree CDN (jQuery 3.7.1 + jsTree 3.3.16) `<head>`/body 하단에 로드
- [ ] `check_callback: true` 설정 (create/rename/delete 허용)
- [ ] 서버 `/tree` 엔드포인트: flat list, id/parent/text/state/data 포함
- [ ] 서버 `/{id}/detail` 엔드포인트: 선택 노드 상세 JSON 반환
- [ ] POST 엔드포인트: `@RequestBody XxxDto` + `@ResponseBody Map` 반환
- [ ] 트리 조작 후 `refresh()` 또는 `rename_node()` / `delete_node()`로 UI 즉시 반영
- [ ] 분할 패널 레이아웃: `flex` + `calc(100vh - Npx)` 고정 높이
- [ ] 좌측 툴바 버튼 활성화 로직: 선택 노드 depth에 따라 enable/disable
