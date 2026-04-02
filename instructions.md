# 프로젝트 규칙: 공공기관 정보화 사업관리 시스템 (PMS)

## 1. 기본 원칙
- 모든 코드는 유지보수가 용이하도록 MVC 패턴을 엄격히 준수한다.
- 디자인은 '미니멀리즘'과 '공공기관 표준 UI'를 지향한다. (깨끗한 화이트/블루톤)
- 중복 코드를 지양하고, 공통 모듈(CSS, JS, 유틸리티)을 우선 활용한다.

## 2. 기술 스택 (Tech Stack)
- Backend: Spring Boot 3.4.x, Java 21+
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
uploads/             ← 첨부파일 저장소 (app.upload.dir, .gitignore 제외 권장)
  regular-report/{projectId}/{uuid}.확장자
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
| `biz-report-weekly` | 사업관리 > 보고관리 > 주간보고 | (미구현) |
| `biz-report-monthly` | 사업관리 > 보고관리 > 월간보고 | (미구현) |
| `biz-report-meeting` | 사업관리 > 보고관리 > 회의록 | (미구현) |
| `biz-risk-risk` | 사업관리 > 위험관리 > 위험관리 | (미구현) |
| `biz-risk-issue` | 사업관리 > 위험관리 > 이슈관리 | (미구현) |
| `exec-analysis-flow` | 사업수행 > 분석 > 업무흐름 | (미구현) |
| `exec-analysis-menu` | 사업수행 > 분석 > 메뉴구조 | (미구현) |
| `exec-design-interface` | 사업수행 > 설계 > 인터페이스 목록 | (미구현) |
| `exec-design-screen` | 사업수행 > 설계 > 화면목록 | (미구현) |
| `exec-design-program` | 사업수행 > 설계 > 프로그램목록 | (미구현) |
| `exec-impl-unit` | 사업수행 > 구현 > 단위테스트 목록 | (미구현) |
| `exec-test-integration` | 사업수행 > 시험 > 통합테스트 목록 | (미구현) |
| `community-notice-list` | 커뮤니티 > 공지사항 | (미구현) |
| `community-archive-list` | 커뮤니티 > 자료실 | (미구현) |
| `admin-project-list` | 관리자 > 사업 관리 > 사업 목록 | `/projects` |
| `admin-user-list` | 관리자 > 사용자 관리 > 사용자 목록 | `/admin/users` |
| `admin-role-list` | 관리자 > 권한 관리 > 권한 목록 | (미구현) |

## 9. 파일 업로드 패턴

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
```java
@Value("${app.upload.dir:uploads}")
private String uploadDir;

private void handleFileUpload(XxxEntity entity, MultipartFile file, Long projectId) throws IOException {
    if (file == null || file.isEmpty()) return;
    deleteAttachedFile(entity);  // 기존 파일 삭제

    String originalName = file.getOriginalFilename();
    String ext = (originalName != null && originalName.contains("."))
                 ? originalName.substring(originalName.lastIndexOf(".")) : "";
    String storedName = UUID.randomUUID() + ext;

    Path dir = Paths.get(uploadDir, "{feature}", String.valueOf(projectId));
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

## 10. 예외 처리 패턴

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

## 11. DB 컬럼 변경 주의사항

- `spring.jpa.hibernate.ddl-auto=update` 설정은 **신규 컬럼 추가만** 자동 처리한다.
- **컬럼 삭제/수정은 자동 반영되지 않으므로** 엔티티에서 필드 제거 후 반드시 수동 실행:
  ```sql
  ALTER TABLE {테이블명}
    DROP COLUMN column1,
    DROP COLUMN column2;
  -- MySQL 5.x: IF EXISTS 미지원, 컬럼 존재 여부 먼저 확인
  ```
- `DEFAULT CURRENT_TIMESTAMP` 등 DB 기본값은 ddl-auto가 생성하지 않으므로 `reg_dt`는 수동 DDL 확인 필수.

## 12. 신규 페이지 개발 체크리스트

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

### 파일 업로드 기능 포함 시 추가 항목
- [ ] `application.properties`에 multipart 설정 확인 (`app.upload.dir`, `max-file-size`)
- [ ] 폼에 `enctype="multipart/form-data"` 추가
- [ ] 컨트롤러 등록/수정에 `@RequestParam MultipartFile` 파라미터 추가
- [ ] 서비스에 `handleFileUpload()` / `deleteAttachedFile()` 구현
- [ ] 다운로드 엔드포인트 `GET /{resource}/{id}/download` 추가
- [ ] 삭제 시 물리 파일도 함께 삭제 (`deleteById()` 전 `deleteAttachedFile()` 호출)
- [ ] DTO에 `attachFileName` / `attachFilePath` 필드 추가

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

## 13. Spring Boot 서버 기동 방법
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
