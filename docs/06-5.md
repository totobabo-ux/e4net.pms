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
