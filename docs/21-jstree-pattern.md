## 21. jsTree 기반 트리뷰 분할 패널 패턴

### 사용 시점
계층형(트리) 데이터를 좌측 트리 + 우측 상세 패널 형태로 관리하는 화면.
대표 예: `메뉴구조` (`menu-structure/list.html`)

### CDN 로드 순서 (jQuery 필수 — jsTree 이전에 로드)
```html
<head>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.3.16/themes/default/style.min.css">
</head>
<body>
  ...
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.3.16/jstree.min.js"></script>
  <script th:src="@{/js/common.js}"></script>
</body>
```

### jsTree 초기화 (서버 URL 방식)
```javascript
$('#menuTree').jstree({
    core: {
        data: { url: '/menu-structure/tree', dataType: 'json' },
        check_callback: true,   // create_node / rename_node / delete_node 허용
        themes: { responsive: false }
    },
    plugins: ['search', 'wholerow'],
    search: { case_insensitive: true, show_only_matches: true }
});
```

### 서버 응답 포맷 (flat list → jsTree가 트리로 조립)
```java
Map<String, Object> node = new LinkedHashMap<>();
node.put("id",     String.valueOf(m.getId()));           // 문자열 ID 필수
node.put("parent", m.getParentId() == null
                   ? "#"                                  // 루트 노드
                   : String.valueOf(m.getParentId()));    // 자식 노드
node.put("text",   m.getMenuName() + " (" + m.getMenuCode() + ")");
node.put("state",  Map.of("opened", m.getDepth() <= 2)); // 기본 펼침 여부
node.put("icon",   m.getDepth() < 3 ? "jstree-folder" : "jstree-file");
node.put("data",   Map.of(                               // 노드 추가 데이터
    "menuCode", m.getMenuCode(),
    "menuName", m.getMenuName(),
    "depth",    m.getDepth(),
    "fixedYn",  m.getFixedYn(),
    "useYn",    m.getUseYn(),
    "parentId", m.getParentId()
));
```

### 주요 이벤트
```javascript
// 노드 선택
$('#tree').on('select_node.jstree', function(e, data) {
    const node     = data.node;
    const nodeData = node.data;   // 서버에서 내려준 data 객체
    // AJAX로 상세 로드
    fetch('/menu-structure/' + node.id + '/detail').then(r => r.json()).then(fillForm);
});

// 트리 로드 완료
$('#tree').on('loaded.jstree', function() {
    $(this).jstree('open_all');
});
```

### 트리 조작 메서드 (check_callback: true 필요)
```javascript
// 노드 이름 변경 (수정 성공 후)
$('#tree').jstree('rename_node', nodeId, '새 이름 (M010000)');

// 노드 삭제 (삭제 성공 후)
$('#tree').jstree('delete_node', nodeId);

// 서버에서 전체 재로드 (등록 성공 후 — 신규 노드 표시)
$('#tree').jstree(true).refresh();

// 재로드 완료 후 특정 노드 선택
$('#tree').one('refresh.jstree', function() {
    $(this).jstree('select_node', String(newId));
});

// 검색 (show_only_matches: true → 일치 항목만 표시)
$('#tree').jstree('search', searchText);
```

### 분할 패널 레이아웃 CSS
```css
.menu-wrap {
    display: flex;
    gap: 14px;
    height: calc(100vh - 190px);   /* 190px = breadcrumb + page-header 높이 */
    min-height: 500px;
}
/* 좌측 트리 패널 — 고정 너비 */
.tree-panel {
    width: 340px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
/* 트리 영역 — 스크롤 */
#menuTree { flex: 1; overflow-y: auto; }

/* 우측 상세 패널 — 나머지 너비 */
.detail-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
.detail-panel-body { flex: 1; padding: 28px 32px; overflow-y: auto; }
```

### 메뉴코드 자동 생성 규칙
```
포맷: M + D1(2자리) + D2(2자리) + D3(2자리) = 총 7자
     M  01  00  00  → depth 1 루트 (1번째)
     M  01  01  00  → depth 2 (루트 M01의 1번째 자식)
     M  01  01  01  → depth 3 (M0101의 1번째 자식)

생성 로직:
  depth 1: 형제 menuCode.substring(1,3) 최대값 + 1 → M{seq}0000
  depth 2: parent.menuCode.substring(0,3) + {seq} + "00"
  depth 3: parent.menuCode.substring(0,5) + {seq}
```

### 체크리스트
- [ ] jsTree CDN (jQuery 3.7.1 + jsTree 3.3.16) 순서 준수
- [ ] `check_callback: true` 설정 (tree 조작 허용)
- [ ] `plugins: ['search', 'wholerow']` — 검색 + 전체 행 하이라이트
- [ ] 서버 `/tree` flat list: id/parent/text/state/icon/data 포함
- [ ] `node.id`는 반드시 **String** (jsTree 내부 요구사항)
- [ ] 루트 노드 `parent: "#"` (리터럴 "#")
- [ ] POST 엔드포인트: `@RequestBody` + `@ResponseBody` (§20 AJAX 패턴 참조)
- [ ] 등록 성공 → `refresh()` 후 `select_node(newId)`
- [ ] 수정 성공 → `rename_node(id, text)` (새로고침 없이 즉시 반영)
- [ ] 삭제 성공 → `delete_node(id)` + 우측 폼 숨김
- [ ] 분할 패널 높이 `calc(100vh - Npx)` — N값은 실제 UI 측정 후 조정
