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
