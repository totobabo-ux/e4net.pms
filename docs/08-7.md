## 7. 파일 구조 및 패키지 구성
```
src/main/
  java/com/e4net/pms/
    controller/   ← @Controller, @RestController
    service/      ← @Service, 비즈니스 로직
    repository/   ← @Repository, JPA + XxxSpec.java
    entity/       ← @Entity, DB 매핑
    dto/          ← 폼 DTO (XxxDto), 검색 DTO (XxxSearchDto)
  resources/
    static/
      css/common.css   ← 공통 CSS (전역 스타일)
      js/common.js     ← 공통 JS
    templates/
      layout/
        fragments.html ← header, sidebar 프래그먼트 (주력)
        base.html      ← Thymeleaf Layout Dialect 마스터
      index.html          ← 로그인 페이지
      project-select.html ← 프로젝트 선택 페이지 (로그인 직후)
      project/         ← 관리자 > 사업 관리 (list.html / form.html)
      manpower/        ← 사업관리 > 인력관리
      requirement/     ← 사업관리 > 범위관리 > 요구사항관리
      wbs/             ← 사업관리 > 범위관리 > 사업일정(WBS)
      regular-report/  ← 사업관리 > 보고관리 > 정기보고
      weekly-report/   ← 사업관리 > 보고관리 > 주간보고
      monthly-report/  ← 사업관리 > 보고관리 > 월간보고
      meeting-report/  ← 사업관리 > 보고관리 > 회의록
uploads/             ← 첨부파일 저장소 (app.upload.dir, .gitignore 제외 권장)
  regular-report/{projectId}/{uuid}.확장자  ← 정기보고
  weekly-report/{projectId}/{uuid}.확장자   ← 주간보고
  monthly-report/{projectId}/{uuid}.확장자  ← 월간보고
  meeting-report/{projectId}/{uuid}.확장자  ← 회의록
```
