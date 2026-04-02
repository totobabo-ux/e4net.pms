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
