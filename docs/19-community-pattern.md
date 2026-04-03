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
