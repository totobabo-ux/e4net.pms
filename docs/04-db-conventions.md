## 4. 데이터베이스 및 코딩 컨벤션

### 공통 컬럼 (모든 테이블 필수)
```sql
reg_dt  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
reg_id  VARCHAR(50)                                  COMMENT '등록자',
upd_dt  DATETIME           ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
upd_id  VARCHAR(50)                                  COMMENT '수정자'
```
> **주의:** `reg_dt`는 반드시 `NOT NULL DEFAULT CURRENT_TIMESTAMP`를 포함해야 한다. 없으면 JPA INSERT 시 500 오류 발생.
> 기존 테이블에 누락된 경우 아래 SQL로 수정:
> ```sql
> ALTER TABLE {테이블명} MODIFY reg_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시';
> ```

### 명명 규칙
- DB 컬럼: 스네이크 케이스 (snake_case)
- Java 코드: 카멜 케이스 (camelCase)
- 모든 컨트롤러 메서드와 비즈니스 로직 상단에 **한글 주석** 필수
