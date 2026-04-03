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

// AJAX 데이터 엔드포인트 — @ResponseBody로 JSON 반환
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

### @ExceptionHandler — JSON 반환으로 변경
```java
@ExceptionHandler(IllegalArgumentException.class)
@ResponseBody   // ← HTML 대신 JSON 반환
public ResponseEntity<Map<String, Object>> handleError(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
}
```

### 주의사항
- `@RequestBody` 수신 시 curl 테스트에서 한글 포함 JSON은 `Invalid UTF-8 start byte` 오류 발생
  → curl 테스트는 ASCII로 진행, 한글 포함 기능은 브라우저에서 확인
- 폼 기반(`@ModelAttribute`) 컨트롤러와 달리 CSRF 토큰을 별도로 포함할 필요 없음 (쿠키 세션 인증)
- `@SuppressWarnings("null")` — `@PathVariable Long id`를 `@NonNull Long` 파라미터에 전달 시 IDE 경고 발생 → 메서드 레벨에 선언으로 해결
