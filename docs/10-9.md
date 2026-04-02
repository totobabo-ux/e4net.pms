## 9. 세션 사용자 정보 활용 패턴

### 컨트롤러 공통 헬퍼 메서드
사업관리/사업수행 컨트롤러에는 아래 두 헬퍼를 함께 선언한다.

```java
/** 로그인 사용자 이름 (작성자 기본값용) */
private String getLoginUserName(HttpSession session) {
    User user = (User) session.getAttribute("loginUser");
    return user != null ? user.getName() : "";
}

/** 로그인 사용자 사번 (reg_id / upd_id 저장용) */
private String getLoginUserId(HttpSession session) {
    User user = (User) session.getAttribute("loginUser");
    return user != null ? user.getEmployeeNo() : "";
}
```

### 등록 폼 작성자 기본값 자동 입력
```java
@GetMapping("/new")
public String createForm(HttpSession session, Model model) {
    ...
    dto.setWriter(getLoginUserName(session));   // 작성자 기본값
    model.addAttribute("report", dto);
    ...
}
```

### reg_id / upd_id 서비스 파라미터 패턴
서비스 `save()` / `update()`에 `String userId` 파라미터를 추가해 엔티티에 세팅한다.

```java
// 서비스
public XxxEntity save(XxxDto dto, String userId) {
    XxxEntity entity = new XxxEntity();
    mapDtoToEntity(dto, entity);
    entity.setRegId(userId);   // 등록 시 reg_id + upd_id 모두 세팅
    entity.setUpdId(userId);
    return repository.save(entity);
}

public XxxEntity update(Long id, XxxDto dto, String userId) {
    XxxEntity entity = findById(id);
    mapDtoToEntity(dto, entity);
    entity.setUpdId(userId);   // 수정 시 upd_id만 갱신
    return repository.save(entity);
}

// 컨트롤러
service.save(dto, getLoginUserId(session));
service.update(id, dto, getLoginUserId(session));
```

> **엔티티 주의:** `reg_id` 컬럼은 `updatable = false`로 선언해야 수정 시 덮어쓰이지 않는다.
> ```java
> @Column(name = "reg_id", length = 50, updatable = false)
> private String regId;
> ```

### reg_dt / upd_dt 자동 관리 (JPA Auditing)
`@EnableJpaAuditing` (PmsApplication) + `@EntityListeners(AuditingEntityListener.class)` 조합으로 자동 처리. 별도 코드 불필요.

```java
@CreatedDate
@Column(name = "reg_dt", updatable = false)   // 또는 created_at
private LocalDateTime regDt;

@LastModifiedDate
@Column(name = "upd_dt")                       // 또는 updated_at
private LocalDateTime updDt;
```
