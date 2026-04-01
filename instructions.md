# 프로젝트 규칙: 공공기관 정보화 사업관리 시스템 (PMS)

## 1. 기본 원칙
- 모든 코드는 유지보수가 용이하도록 MVC 패턴을 엄격히 준수한다.
- 디자인은 '미니멀리즘'과 '공공기관 표준 UI'를 지향한다. (깨끗한 화이트/블루톤)
- 중복 코드를 지양하고, 공통 모듈(CSS, JS, 유틸리티)을 우선 활용한다.

## 2. 기술 스택 (Tech Stack)
- Backend: Spring Boot 3.2 (MVC)
- Frontend: HTML5, CSS3, JavaScript (ES6+), Thymeleaf (Layout Dialect)
- DB: MySQL 8.0
- DATABASE 명: e4net_pms 하위에 table 생성
- Persistence: Spring Data JPA (Hibernate)
- Build: Maven

## 3. UI/UX 및 디자인 규칙 (중요!)
- **레이아웃:** `templates/layout/base.html`의 레이아웃을 모든 페이지에서 상속받는다.
  - 사용법: `layout:decorate="~{layout/base}"` + `th:with="activePage='키값'"`
  - 컨텐츠 영역: `<layout:fragment name="content">` 사용
  - 페이지 전용 CSS: `<layout:fragment name="extra-styles">` 사용
  - 페이지 전용 JS: `<layout:fragment name="extra-scripts">` 사용
- **폼(Form) 구조:** 입력 폼은 기본적으로 '2열 배치'를 사용한다.
- **공통 색상:**
  - Primary (Header/강조): `#2c3e50` (다크 블루)
  - Accent (버튼/링크): `#2980b9` (블루)
  - Success: `#27ae60` (그린)
  - Danger: `#e74c3c` (레드)
  - Background: `#f8f9fa` (연한 회색)
  - Sidebar: `#1a252f` (다크)
- **필수 항목:** 필수 입력 값은 레이블 옆에 빨간색 별표(※)를 표시하고, 서버/클라이언트 양쪽에서 유효성 검사를 수행한다.
- **데이터 그리드:** 모든 목록은 페이징 처리가 된 테이블 구조를 사용하며, 테이블 상단에 검색 바를 배치한다.

## 4. 데이터베이스 및 코딩 컨벤션
- **공통 컬럼:** 모든 테이블은 다음 4개 컬럼을 반드시 포함한다.
  ```sql
  reg_dt  DATETIME     NOT NULL COMMENT '등록일시',
  reg_id  VARCHAR(50)           COMMENT '등록자',
  upd_dt  DATETIME              COMMENT '수정일시',
  upd_id  VARCHAR(50)           COMMENT '수정자'
  ```
- **변수 명명:** DB는 스네이크 케이스(snake_case), 코드는 카멜 케이스(camelCase)를 사용한다.
- **주석:** 모든 컨트롤러 메서드와 비즈니스 로직 상단에 한글로 기능을 상세히 설명한다.

## 5. 파일 구조 및 패키지 구성
```
src/main/
  java/com/e4net/pms/
    controller/   ← @Controller, @RestController
    service/      ← @Service, 비즈니스 로직
    repository/   ← @Repository, JPA
    entity/       ← @Entity, DB 매핑
    dto/          ← 폼/응답 데이터 객체
  resources/
    static/
      css/common.css   ← 공통 CSS (유일한 전역 스타일)
      js/common.js     ← 공통 JS
    templates/
      layout/base.html ← 마스터 레이아웃 (모든 페이지 상속)
      fragments/       ← 재사용 HTML 조각 (sidebar 등)
      *.html           ← 각 페이지 (layout 상속 구조)
```

## 6. activePage 키 목록 (사이드바 메뉴 활성화)
| 키 값            | 메뉴 항목      |
|-----------------|--------------|
| `home`          | 사업 Home     |
| `plan-info`     | 사업정보 관리  |
| `plan-manpower` | 인력계획       |
| `plan-schedule` | 일정계획 (WBS) |
| `exec-manpower` | 인력투입실적   |
| `exec-issue`    | 이슈관리       |
| `exec-risk`     | 위험관리       |

## 7. 신규 페이지 개발 체크리스트
- [ ] Entity에 `reg_dt`, `reg_id`, `upd_dt`, `upd_id` 포함 여부 확인
- [ ] Controller 메서드 한글 주석 작성
- [ ] 레이아웃 상속 (`layout:decorate="~{layout/base}"`) 적용
- [ ] `activePage` 값 지정 (`th:with="activePage='키값'"`)
- [ ] 필수 항목 ※ 표시 및 유효성 검사
- [ ] 목록 페이지: 검색 + 페이징 적용
