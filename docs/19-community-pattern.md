## 19. 커뮤니티(공지사항/자료실) 패턴

### 설계 원칙
- 단일 `community` 테이블을 `community_type` 컬럼으로 구분 (§11 공유 테이블 패턴 응용)
- 커뮤니티는 **사업(Project)에 선택적 귀속** — `project_id` FK nullable, null이면 전체 공통 글
- 세션 체크: `loginUser`만 확인 (`isNotLogin()`), `isNotReady()` 사용 금지
- 컨트롤러 2개(`NoticeController`, `ArchiveController`) → 서비스 1개(`CommunityService`) 공유
- 각 컨트롤러에서 `COMMUNITY_TYPE` 상수로 타입 고정 후 서비스에 전달
- 세션에 `selectedProject`가 있으면 해당 사업 글만 필터링

### Entity — Community.java
```java
@Entity
@Table(name = "community")
public class Community {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;   // nullable — null이면 전체 공통 글

    @Column(name = "community_type", length = 20, nullable = false)
    private String communityType;   // "공지사항" / "자료실"

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "writer", length = 100)
    private String writer;

    @Column(name = "post_date")
    private LocalDate postDate;

    @Column(name = "content", columnDefinition = "LONGTEXT")   // ← TEXT(64KB)가 아닌 LONGTEXT 필수
    private String content;

    // reg_dt, reg_id, upd_dt, upd_id (공통 감사 컬럼)
}
```
> **중요:** `content` 컬럼은 반드시 `LONGTEXT`로 선언. 웹에디터(Quill)에서 이미지를 Base64로 삽입하면 64KB를 초과하여 `TEXT`로는 500 오류 발생.
> 기존 테이블 변경: `ALTER TABLE community MODIFY content LONGTEXT;`

### Repository / Spec
```java
// CommunityRepository: JpaRepository + JpaSpecificationExecutor
// CommunitySpec: communityType 동등 비교 + title/writer LIKE 조건 + projectId 동등 비교
static Specification<Community> eqProject(Long projectId) {
    return (root, query, cb) -> projectId == null ? null
        : cb.equal(root.get("project").get("id"), projectId);
}
```

### 컨트롤러 패턴 (공지사항 예시 — 자료실은 URL/TYPE만 다름)
```java
@Controller
@RequestMapping("/notice")
public class NoticeController {
    private static final String COMMUNITY_TYPE = "공지사항";
    private static final String ACTIVE_PAGE    = "community-notice-list";

    private boolean isNotLogin(HttpSession session) {
        return session.getAttribute("loginUser") == null;
    }
    private Project getSelectedProject(HttpSession session) {
        return (Project) session.getAttribute("selectedProject");
    }

    @GetMapping
    public String list(...) {
        if (isNotLogin(session)) return "redirect:/login";
        search.setCommunityType(COMMUNITY_TYPE);
        Project p = getSelectedProject(session);
        if (p != null) search.setProjectId(p.getId());   // 사업 필터
        model.addAttribute("selectedProject", p);
        ...
    }

    @GetMapping("/new")
    public String createForm(...) {
        CommunityDto dto = new CommunityDto();
        dto.setCommunityType(COMMUNITY_TYPE);
        dto.setWriter(getLoginUserName(session));
        dto.setPostDate(LocalDate.now());   // 작성일 오늘 날짜 기본값
        Project p = getSelectedProject(session);
        if (p != null) dto.setProjectId(p.getId());
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
- `mapDtoToEntity()`: `dto.getProjectId()`로 `entity.setProject()` 처리 (ProjectRepository 의존)
- `toDto()`: `entity.getProject()`에서 `dto.setProjectId()` / `dto.setProjectName()` 채움

### DTO — CommunityDto.java
```java
public class CommunityDto {
    private Long   id;
    private Long   projectId;     // 선택된 사업 ID (nullable)
    private String projectName;   // 화면 표시용
    private String communityType; // 컨트롤러에서 주입, 폼 필드 아님
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    private String writer;
    @DateTimeFormat(pattern = "yyyy-MM-dd")   // th:field 바인딩 필수
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
- [ ] `isNotLogin(session)` 사용 (isNotReady 금지 — selectedProject는 optional)
- [ ] 컨트롤러에서 `dto.setCommunityType(COMMUNITY_TYPE)` 반드시 호출 (폼 바인딩 없음)
- [ ] 작성자 기본값: `dto.setWriter(getLoginUserName(session))`
- [ ] 작성일 기본값: `dto.setPostDate(LocalDate.now())` — 등록 폼 초기화 시
- [ ] `CommunityDto.postDate`에 `@DateTimeFormat(pattern = "yyyy-MM-dd")` 선언
- [ ] `content` 컬럼 LONGTEXT — 웹에디터 이미지 저장 시 필수
- [ ] 폼에 `enctype="multipart/form-data"` + `initAttachUpload()` 호출
- [ ] fragments.html sidebar에 href 연결 확인
