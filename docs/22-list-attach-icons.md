## 22. 목록 화면 첨부파일 아이콘 패턴

### 설계 원칙
- 목록 테이블에서 첨부파일 유무를 아이콘 1개로 표시 (파일이 없으면 아무것도 표시 안 함)
- 파일이 2개 이상이면 아이콘 우상단에 빨간 배지로 수량 표시
- 아이콘 클릭 시 공통 모달에서 파일 목록 확인 및 다운로드
- 인프라는 `common.js` + `common.css`에 통합 — 각 list.html에 data 속성만 추가하면 자동 동작

### 구성 요소

#### 1. REST API — AttachmentApiController
```
GET /api/attachments?entityType=ISSUE&ids=1,2,3,4
응답: Map<Long(entityId), List<{id, fileName, fileSize}>>
```
- 미로그인 시 빈 맵 반환
- 쉼표 구분 ID 파싱, 일괄 조회 후 entityId 기준 그룹핑

#### 2. HTML — list.html 수정 방법 (3곳)
```html
<!-- ① table 태그에 data 속성 추가 -->
<table data-entity-type="ISSUE" data-base-url="/issue">

<!-- ② thead에 첨부 컬럼 헤더 추가 (관리 th 앞) -->
<th style="width:80px;">첨부</th>

<!-- ③ tbody 각 행에 attach-icons-cell td 추가 (관리 td 앞) -->
<td class="td-center attach-icons-cell" th:data-entity-id="${r.id}"></td>

<!-- ④ empty-row colspan 1 증가 -->
<td colspan="N+1">...</td>
```

#### 3. entityType 값 목록
| 모듈 | entityType | base-url |
|------|-----------|----------|
| 이슈관리 | `ISSUE` | `/issue` |
| 위험관리 | `RISK` | `/risk` |
| 요구사항 | `REQUIREMENT` | `/requirement` |
| 정기보고 | `CUSTOMER_REPORT` | `/regular-report` |
| 주간보고 | `CUSTOMER_REPORT` | `/weekly-report` |
| 월간보고 | `CUSTOMER_REPORT` | `/monthly-report` |
| 회의록 | `CUSTOMER_REPORT` | `/meeting-report` |
| 공지사항 | `COMMUNITY` | `/notice` |
| 자료실 | `COMMUNITY` | `/archive` |

> **주의:** 보고관리 4종은 entityType이 모두 `CUSTOMER_REPORT`로 동일하지만 `base-url`이 달라 다운로드 URL이 정확히 생성됨.

#### 4. common.js — loadAttachIcons() 동작
```javascript
// DOMContentLoaded 시 자동 실행 (IIFE)
// 1. 공통 첨부 모달(#attachModal) body에 동적 생성
// 2. table[data-entity-type] 찾아 attach-icons-cell 수집
// 3. /api/attachments?entityType=X&ids=1,2,3 일괄 fetch
// 4. 파일 있는 셀에만 .btn-attach-icon 버튼 렌더링
//    - 파일 2개 이상: .attach-badge 배지 표시
//    - 클릭 시 openAttachModal(files, baseUrl, entityId) 호출
```

전역 함수:
- `openAttachModal(files, baseUrl, entityId)` — 모달에 파일 목록 채우고 open
- `closeAttachModal()` — 모달 닫기

#### 5. common.css — 관련 클래스
| 클래스 | 용도 |
|--------|------|
| `.btn-attach-icon` | 아이콘 버튼 (📄) |
| `.attach-badge` | 파일 수 배지 (빨간 원) |
| `.attach-file-list` | 모달 내 파일 ul |
| `.attach-file-item` | 모달 내 파일 li (flex 행) |
| `.attach-file-icon` | 모달 내 파일 아이콘 |
| `.attach-file-name` | 모달 내 파일명 링크 (클릭 → 다운로드) |
| `.attach-file-size` | 모달 내 파일 크기 |

### 다운로드 URL 패턴
모달에서 파일명 클릭 시:
```
{baseUrl}/{entityId}/attachment/{fileId}/download
예: /issue/42/attachment/7/download
```
이 URL은 각 모듈 컨트롤러의 `@GetMapping("/{id}/attachment/{attId}/download")` 엔드포인트로 연결됨 (§16 참조).

### 새 모듈에 적용 시 체크리스트
- [ ] 해당 모듈 컨트롤러에 `/attachment/{attId}/download` 엔드포인트 존재 확인 (§16)
- [ ] `AttachFileRepository.findByEntityTypeAndEntityIdIn()` 존재 확인 (§16)
- [ ] list.html에 table data 속성, 첨부 th, attach-icons-cell td, colspan 수정
- [ ] entityType 문자열이 서비스의 `ENTITY_TYPE` 상수와 일치하는지 확인
