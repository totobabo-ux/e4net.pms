## 6. JPA Specification 패턴

### 검색 DTO 구조 (사업관리/사업수행 공통)
```java
private Long   projectId;   // 세션 기반 자동 필터 (사업명 검색 아님)
private String ...;         // 나머지 검색 조건
```

### Spec 클래스 공통 패턴
```java
// count 쿼리가 아닐 때만 fetch join (N+1 방지)
if (query != null && !Long.class.equals(query.getResultType())) {
    root.fetch("project", JoinType.LEFT);
    query.distinct(true);
}
// projectId 필터
if (dto.getProjectId() != null) {
    Join<Object, Object> project = root.join("project", JoinType.LEFT);
    predicates.add(cb.equal(project.get("id"), dto.getProjectId()));
}
```
