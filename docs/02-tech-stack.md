## 2. 기술 스택 (Tech Stack)
- Backend: Spring Boot 3.4.x, Java 25
- Frontend: HTML5, CSS3, JavaScript (ES6+), Thymeleaf
- DB: MySQL 8.0 / DATABASE 명: `e4net_pms`
- Persistence: Spring Data JPA (Hibernate) + Specification 패턴
- Build: Maven
- 비밀번호 암호화: `spring-security-crypto` (BCryptPasswordEncoder) — Spring Security 필터 체인 없이 단독 사용

### application.properties 필수 인코딩 설정
```properties
# POST 폼 한글 인코딩 (필수 — 없으면 한글 깨짐)
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```
