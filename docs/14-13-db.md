## 13. DB 컬럼 변경 주의사항

- `spring.jpa.hibernate.ddl-auto=update` 설정은 **신규 컬럼 추가만** 자동 처리한다.
- **컬럼 삭제/수정은 자동 반영되지 않으므로** 엔티티에서 필드 제거 후 반드시 수동 실행:
  ```sql
  ALTER TABLE {테이블명}
    DROP COLUMN column1,
    DROP COLUMN column2;
  -- MySQL 5.x: IF EXISTS 미지원, 컬럼 존재 여부 먼저 확인
  ```
- `DEFAULT CURRENT_TIMESTAMP` 등 DB 기본값은 ddl-auto가 생성하지 않으므로 `reg_dt`는 수동 DDL 확인 필수.
