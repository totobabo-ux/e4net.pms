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
