-- ============================================================
-- e4net PMS - 테이블/컬럼 한글 코멘트 업데이트
-- 실행 방법 (한글 깨짐 방지):
--   mysql --default-character-set=utf8mb4 -u root -p e4net_pms < comment_update.sql
-- ============================================================

SET NAMES 'utf8mb4';
SET CHARACTER SET utf8mb4;
USE e4net_pms;

-- FK 제약 임시 비활성화 (ALTER TABLE 중 참조 오류 방지)
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. project (사업 정보)
-- ============================================================
ALTER TABLE project COMMENT = '사업(프로젝트) 정보';

ALTER TABLE project
  MODIFY COLUMN id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '사업 ID (PK)',
  MODIFY COLUMN project_name     VARCHAR(200) NOT NULL               COMMENT '사업명',
  MODIFY COLUMN category         VARCHAR(20)                         COMMENT '사업구분 (SI/SM/서비스 등)',
  MODIFY COLUMN company          VARCHAR(100)                        COMMENT '수행회사',
  MODIFY COLUMN orderer          VARCHAR(100)                        COMMENT '발주처',
  MODIFY COLUMN contractor       VARCHAR(100)                        COMMENT '계약처',
  MODIFY COLUMN contract_start   DATE                                COMMENT '계약시작일',
  MODIFY COLUMN contract_end     DATE                                COMMENT '계약종료일',
  MODIFY COLUMN pm               VARCHAR(50)                         COMMENT 'PM 성명',
  MODIFY COLUMN contract_amount  BIGINT                              COMMENT '계약금액 (원)',
  MODIFY COLUMN created_at       DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN updated_at       DATETIME                            COMMENT '수정일시';


-- ============================================================
-- 2. users (사용자 정보)
-- ============================================================
ALTER TABLE users COMMENT = '시스템 사용자 정보';

ALTER TABLE users
  MODIFY COLUMN id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '사용자 ID (PK)',
  MODIFY COLUMN employee_no  VARCHAR(50)  NOT NULL               COMMENT '사번 (로그인 ID, UNIQUE)',
  MODIFY COLUMN password     VARCHAR(255) NOT NULL               COMMENT '비밀번호 (BCrypt 해시)',
  MODIFY COLUMN name         VARCHAR(50)  NOT NULL               COMMENT '성명',
  MODIFY COLUMN company      VARCHAR(100)                        COMMENT '소속회사',
  MODIFY COLUMN department   VARCHAR(100)                        COMMENT '소속부서',
  MODIFY COLUMN position     VARCHAR(50)                         COMMENT '직위',
  MODIFY COLUMN phone        VARCHAR(20)                         COMMENT '연락처',
  MODIFY COLUMN email        VARCHAR(100)                        COMMENT '이메일',
  MODIFY COLUMN created_at   DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN updated_at   DATETIME                            COMMENT '수정일시';


-- ============================================================
-- 3. common_code (공통코드)
-- ============================================================
ALTER TABLE common_code COMMENT = '공통코드 (그룹코드 + 코드값 관리)';

ALTER TABLE common_code
  MODIFY COLUMN id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '코드 ID (PK)',
  MODIFY COLUMN group_code VARCHAR(50)  NOT NULL               COMMENT '코드그룹 (GRADE / INPUT_TYPE 등)',
  MODIFY COLUMN code       VARCHAR(50)  NOT NULL               COMMENT '코드값',
  MODIFY COLUMN code_name  VARCHAR(100) NOT NULL               COMMENT '코드명 (화면 표시)',
  MODIFY COLUMN sort_order INT          DEFAULT 0              COMMENT '정렬순서',
  MODIFY COLUMN use_yn     VARCHAR(1)   DEFAULT 'Y'            COMMENT '사용여부 (Y/N)';


-- ============================================================
-- 4. project_manpower (사업 투입인력)
--    FK: FKh92da0oiebiqumo8vxrionwwc (project_id → project.id)
--        FKqji8btyu95paq34e3jsi3el6r (user_id   → users.id)
-- ============================================================
ALTER TABLE project_manpower DROP FOREIGN KEY FKh92da0oiebiqumo8vxrionwwc;
ALTER TABLE project_manpower DROP FOREIGN KEY FKqji8btyu95paq34e3jsi3el6r;

ALTER TABLE project_manpower COMMENT = '사업별 투입인력 정보';

ALTER TABLE project_manpower
  MODIFY COLUMN id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '투입인력 ID (PK)',
  MODIFY COLUMN project_id       BIGINT       NOT NULL               COMMENT '사업 ID (FK → project.id)',
  MODIFY COLUMN user_id          BIGINT       NOT NULL               COMMENT '사용자 ID (FK → users.id)',
  MODIFY COLUMN company          VARCHAR(100)                        COMMENT '소속회사',
  MODIFY COLUMN department       VARCHAR(100)                        COMMENT '소속부서',
  MODIFY COLUMN phone            VARCHAR(20)                         COMMENT '연락처',
  MODIFY COLUMN role             VARCHAR(100) NOT NULL               COMMENT '역할 (PM / 개발자 등)',
  MODIFY COLUMN position         VARCHAR(50)                         COMMENT '직위',
  MODIFY COLUMN grade_code       VARCHAR(20)  NOT NULL               COMMENT '급수 코드 (공통코드 GRADE 참조)',
  MODIFY COLUMN input_type_code  VARCHAR(20)  NOT NULL               COMMENT '투입구분 코드 (공통코드 INPUT_TYPE 참조)',
  MODIFY COLUMN input_start_date DATE         NOT NULL               COMMENT '투입시작일',
  MODIFY COLUMN input_end_date   DATE         NOT NULL               COMMENT '투입종료일',
  MODIFY COLUMN input_mm         DOUBLE                              COMMENT '투입공수 (MM)',
  MODIFY COLUMN status           VARCHAR(20)  DEFAULT '투입중'       COMMENT '투입상태 (투입중/투입종료/대기)',
  MODIFY COLUMN note             VARCHAR(500)                        COMMENT '비고',
  MODIFY COLUMN created_at       DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN updated_at       DATETIME                            COMMENT '수정일시';

ALTER TABLE project_manpower
  ADD CONSTRAINT FKh92da0oiebiqumo8vxrionwwc FOREIGN KEY (project_id) REFERENCES project(id),
  ADD CONSTRAINT FKqji8btyu95paq34e3jsi3el6r FOREIGN KEY (user_id)    REFERENCES users(id);


-- ============================================================
-- 5. requirement (요구사항)
--    FK: FK90i86ya3vioykp05h351pda0j (project_id → project.id)
-- ============================================================
ALTER TABLE requirement DROP FOREIGN KEY FK90i86ya3vioykp05h351pda0j;

ALTER TABLE requirement COMMENT = '사업별 요구사항 관리';

ALTER TABLE requirement
  MODIFY COLUMN id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '요구사항 ID (PK)',
  MODIFY COLUMN project_id     BIGINT        NOT NULL               COMMENT '사업 ID (FK → project.id)',
  MODIFY COLUMN req_code       VARCHAR(50)                          COMMENT '요구사항 코드 (예: REQ-001)',
  MODIFY COLUMN title          VARCHAR(300)  NOT NULL               COMMENT '요구사항 제목',
  MODIFY COLUMN category       VARCHAR(20)                          COMMENT '분류 (기능/비기능)',
  MODIFY COLUMN priority       VARCHAR(10)   DEFAULT '중'           COMMENT '우선순위 (상/중/하)',
  MODIFY COLUMN status         VARCHAR(20)   DEFAULT '등록'         COMMENT '상태 (등록/분석중/개발중/완료/보류)',
  MODIFY COLUMN requestor      VARCHAR(100)                         COMMENT '요청자',
  MODIFY COLUMN description    VARCHAR(2000)                        COMMENT '요구사항 상세내용',
  MODIFY COLUMN note           VARCHAR(500)                         COMMENT '비고',
  MODIFY COLUMN source_type    VARCHAR(20)                          COMMENT '출처구분 (제안요청서/회의록/기타)',
  MODIFY COLUMN source_content VARCHAR(300)                         COMMENT '출처내용',
  MODIFY COLUMN acceptance     VARCHAR(20)   DEFAULT '협의중'       COMMENT '수용여부 (협의중/수용/제외)',
  MODIFY COLUMN reg_id         VARCHAR(50)                          COMMENT '등록자 ID',
  MODIFY COLUMN upd_id         VARCHAR(50)                          COMMENT '수정자 ID',
  MODIFY COLUMN created_at     DATETIME                             COMMENT '등록일시',
  MODIFY COLUMN updated_at     DATETIME                             COMMENT '수정일시';

ALTER TABLE requirement
  ADD CONSTRAINT FK90i86ya3vioykp05h351pda0j FOREIGN KEY (project_id) REFERENCES project(id);


-- ============================================================
-- 6. attach_file (첨부파일)
-- ============================================================
ALTER TABLE attach_file COMMENT = '공통 첨부파일 (entity_type + entity_id 로 대상 구분)';

ALTER TABLE attach_file
  MODIFY COLUMN id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '첨부파일 ID (PK)',
  MODIFY COLUMN entity_type VARCHAR(50)  NOT NULL               COMMENT '연결 대상 유형 (REQUIREMENT / CUSTOMER_REPORT 등)',
  MODIFY COLUMN entity_id   BIGINT       NOT NULL               COMMENT '연결 대상 PK',
  MODIFY COLUMN file_name   VARCHAR(255)                        COMMENT '원본 파일명 (표시용)',
  MODIFY COLUMN file_path   VARCHAR(500)                        COMMENT '서버 저장 경로 (절대경로)',
  MODIFY COLUMN file_size   BIGINT                              COMMENT '파일 크기 (bytes)',
  MODIFY COLUMN reg_dt      DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN reg_id      VARCHAR(50)                         COMMENT '등록자 ID';


-- ============================================================
-- 7. wbs (사업일정 / WBS)
--    FK: FKm0go6cciyi9203prqhl9pur19 (project_id → project.id)
-- ============================================================
ALTER TABLE wbs DROP FOREIGN KEY FKm0go6cciyi9203prqhl9pur19;

ALTER TABLE wbs COMMENT = '사업일정 관리 (WBS)';

ALTER TABLE wbs
  MODIFY COLUMN id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'WBS ID (PK)',
  MODIFY COLUMN project_id        BIGINT       NOT NULL               COMMENT '사업 ID (FK → project.id)',
  MODIFY COLUMN task_id           VARCHAR(50)                         COMMENT 'TASK ID',
  MODIFY COLUMN task_name         VARCHAR(300)                        COMMENT 'TASK명',
  MODIFY COLUMN deliverable       VARCHAR(300)                        COMMENT '산출물',
  MODIFY COLUMN assignee          VARCHAR(100)                        COMMENT '담당자',
  MODIFY COLUMN plan_progress     INT                                 COMMENT '계획 진행률 (%)',
  MODIFY COLUMN actual_progress   INT                                 COMMENT '실적 진행률 (%)',
  MODIFY COLUMN plan_start_date   DATE                                COMMENT '계획 시작일',
  MODIFY COLUMN plan_end_date     DATE                                COMMENT '계획 종료일',
  MODIFY COLUMN plan_duration     INT                                 COMMENT '계획 기간 (일)',
  MODIFY COLUMN plan_rate         INT                                 COMMENT '계획 진척률 (%)',
  MODIFY COLUMN actual_start_date DATE                                COMMENT '실제 시작일',
  MODIFY COLUMN actual_end_date   DATE                                COMMENT '실제 종료일',
  MODIFY COLUMN actual_rate       INT                                 COMMENT '실제 진척률 (%)',
  MODIFY COLUMN status            VARCHAR(20)                         COMMENT '상태',
  MODIFY COLUMN sort_order        INT                                 COMMENT '정렬순서',
  MODIFY COLUMN reg_dt            DATETIME     NOT NULL               COMMENT '등록일시',
  MODIFY COLUMN reg_id            VARCHAR(50)                         COMMENT '등록자 ID',
  MODIFY COLUMN upd_dt            DATETIME                            COMMENT '수정일시',
  MODIFY COLUMN upd_id            VARCHAR(50)                         COMMENT '수정자 ID';

ALTER TABLE wbs
  ADD CONSTRAINT FKm0go6cciyi9203prqhl9pur19 FOREIGN KEY (project_id) REFERENCES project(id);


-- ============================================================
-- 8. risk (위험관리)
--    FK: FKnn522gtyxlr4nkcttf240kmw7 (project_id → project.id)
-- ============================================================
ALTER TABLE risk DROP FOREIGN KEY FKnn522gtyxlr4nkcttf240kmw7;

ALTER TABLE risk COMMENT = '사업별 위험 관리';

ALTER TABLE risk
  MODIFY COLUMN id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '위험 ID (PK)',
  MODIFY COLUMN project_id        BIGINT       NOT NULL               COMMENT '사업 ID (FK → project.id)',
  MODIFY COLUMN risk_code         VARCHAR(50)                         COMMENT '위험코드 (예: RISK-001)',
  MODIFY COLUMN risk_name         VARCHAR(200) NOT NULL               COMMENT '위험명',
  MODIFY COLUMN risk_type         VARCHAR(50)                         COMMENT '위험유형 (기술/일정/비용/인력/외부)',
  MODIFY COLUMN identified_date   DATE                                COMMENT '위험 식별일자',
  MODIFY COLUMN description       TEXT                                COMMENT '위험 설명',
  MODIFY COLUMN probability       VARCHAR(50)                         COMMENT '발생가능성 (낮음/보통/높음)',
  MODIFY COLUMN impact            VARCHAR(50)                         COMMENT '영향도 (적음/보통/심각/매우심각)',
  MODIFY COLUMN risk_level        VARCHAR(50)                         COMMENT '위험등급 (LOW/MODERATE/HIGH 등)',
  MODIFY COLUMN response_strategy VARCHAR(100)                        COMMENT '대응전략 (회피/전가/완화/수용)',
  MODIFY COLUMN response_plan     TEXT                                COMMENT '대응계획',
  MODIFY COLUMN owner             VARCHAR(200)                        COMMENT '담당자',
  MODIFY COLUMN activity_result   TEXT                                COMMENT '활동결과',
  MODIFY COLUMN status            VARCHAR(50)                         COMMENT '위험상태 (진행중/해결/종료)',
  MODIFY COLUMN reg_dt            DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN reg_id            VARCHAR(50)                         COMMENT '등록자 ID',
  MODIFY COLUMN upd_dt            DATETIME                            COMMENT '수정일시',
  MODIFY COLUMN upd_id            VARCHAR(50)                         COMMENT '수정자 ID';

ALTER TABLE risk
  ADD CONSTRAINT FKnn522gtyxlr4nkcttf240kmw7 FOREIGN KEY (project_id) REFERENCES project(id);


-- ============================================================
-- 9. issue (이슈관리)
--    FK: FKcombytcpeogaqi2012phvvvhy (project_id → project.id)
-- ============================================================
ALTER TABLE issue DROP FOREIGN KEY FKcombytcpeogaqi2012phvvvhy;

ALTER TABLE issue COMMENT = '사업별 이슈 관리';

ALTER TABLE issue
  MODIFY COLUMN id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '이슈 ID (PK)',
  MODIFY COLUMN project_id          BIGINT       NOT NULL               COMMENT '사업 ID (FK → project.id)',
  MODIFY COLUMN issue_no            VARCHAR(50)                         COMMENT '이슈 관리번호 (예: ISS-001)',
  MODIFY COLUMN issue_name          VARCHAR(200) NOT NULL               COMMENT '이슈명',
  MODIFY COLUMN raiser              VARCHAR(100)                        COMMENT '이슈 제기자',
  MODIFY COLUMN raised_date         DATE                                COMMENT '이슈 제기일자',
  MODIFY COLUMN issue_content       TEXT                                COMMENT '이슈 내용',
  MODIFY COLUMN action_plan_date    DATE                                COMMENT '조치계획 일자',
  MODIFY COLUMN action_plan_content TEXT                                COMMENT '조치계획 내용',
  MODIFY COLUMN action_status       VARCHAR(50)                         COMMENT '조치상태 (미조치/조치중/조치완료/보류)',
  MODIFY COLUMN action_date         DATE                                COMMENT '조치 완료일자',
  MODIFY COLUMN action_content      TEXT                                COMMENT '조치내용',
  MODIFY COLUMN note                VARCHAR(500)                        COMMENT '비고',
  MODIFY COLUMN reg_dt              DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN reg_id              VARCHAR(50)                         COMMENT '등록자 ID',
  MODIFY COLUMN upd_dt              DATETIME                            COMMENT '수정일시',
  MODIFY COLUMN upd_id              VARCHAR(50)                         COMMENT '수정자 ID';

ALTER TABLE issue
  ADD CONSTRAINT FKcombytcpeogaqi2012phvvvhy FOREIGN KEY (project_id) REFERENCES project(id);


-- ============================================================
-- 10. deliverable (산출물)
--     FK: FKrexwpmbblx397fd05mty71klm (project_id → project.id)
-- ============================================================
ALTER TABLE deliverable DROP FOREIGN KEY FKrexwpmbblx397fd05mty71klm;

ALTER TABLE deliverable COMMENT = '사업별 산출물 관리';

ALTER TABLE deliverable
  MODIFY COLUMN id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '산출물 ID (PK)',
  MODIFY COLUMN project_id       BIGINT       NOT NULL               COMMENT '사업 ID (FK → project.id)',
  MODIFY COLUMN deliverable_type VARCHAR(20)                         COMMENT '산출물 구분 (관리산출물/개발산출물)',
  MODIFY COLUMN category1        VARCHAR(50)                         COMMENT '분류1 (대분류)',
  MODIFY COLUMN category2        VARCHAR(50)                         COMMENT '분류2 (중분류)',
  MODIFY COLUMN code             VARCHAR(50)                         COMMENT '산출물 코드',
  MODIFY COLUMN deliverable_id   VARCHAR(50)                         COMMENT '산출물 ID',
  MODIFY COLUMN name             VARCHAR(200) NOT NULL               COMMENT '산출물명',
  MODIFY COLUMN written_yn       VARCHAR(10)                         COMMENT '작성여부 (Y/N)',
  MODIFY COLUMN stage            VARCHAR(20)  DEFAULT '미도래'       COMMENT '단계 (미도래/진행중/완료)',
  MODIFY COLUMN writer           VARCHAR(100)                        COMMENT '작성자',
  MODIFY COLUMN note             VARCHAR(500)                        COMMENT '비고',
  MODIFY COLUMN reg_dt           DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN reg_id           VARCHAR(50)                         COMMENT '등록자 ID',
  MODIFY COLUMN upd_dt           DATETIME                            COMMENT '수정일시',
  MODIFY COLUMN upd_id           VARCHAR(50)                         COMMENT '수정자 ID';

ALTER TABLE deliverable
  ADD CONSTRAINT FKrexwpmbblx397fd05mty71klm FOREIGN KEY (project_id) REFERENCES project(id);


-- ============================================================
-- 11. customer_report (고객 보고)
--     FK: FK3bvatpwcbh4wt2v800ok802k4 (project_id → project.id)
-- ============================================================
ALTER TABLE customer_report DROP FOREIGN KEY FK3bvatpwcbh4wt2v800ok802k4;

ALTER TABLE customer_report COMMENT = '사업별 고객 보고 (정기/주간/월간/회의록 등)';

ALTER TABLE customer_report
  MODIFY COLUMN id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '보고 ID (PK)',
  MODIFY COLUMN project_id     BIGINT       NOT NULL               COMMENT '사업 ID (FK → project.id)',
  MODIFY COLUMN report_type    VARCHAR(20)                         COMMENT '보고구분 (정기보고/주간보고/월간보고/회의록)',
  MODIFY COLUMN report_name    VARCHAR(300)                        COMMENT '보고서명',
  MODIFY COLUMN report_date    DATE                                COMMENT '보고일자',
  MODIFY COLUMN report_content TEXT                                COMMENT '보고내용',
  MODIFY COLUMN writer         VARCHAR(100)                        COMMENT '작성자',
  MODIFY COLUMN reg_dt         DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN reg_id         VARCHAR(50)                         COMMENT '등록자 ID',
  MODIFY COLUMN upd_dt         DATETIME                            COMMENT '수정일시',
  MODIFY COLUMN upd_id         VARCHAR(50)                         COMMENT '수정자 ID';

ALTER TABLE customer_report
  ADD CONSTRAINT FK3bvatpwcbh4wt2v800ok802k4 FOREIGN KEY (project_id) REFERENCES project(id);


-- ============================================================
-- 12. community (공지사항 / 자료실)
-- ============================================================
ALTER TABLE community COMMENT = '공지사항 및 자료실 게시물';

ALTER TABLE community
  MODIFY COLUMN id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '게시물 ID (PK)',
  MODIFY COLUMN community_type VARCHAR(20)  NOT NULL               COMMENT '구분 (공지사항/자료실)',
  MODIFY COLUMN title          VARCHAR(500) NOT NULL               COMMENT '제목',
  MODIFY COLUMN writer         VARCHAR(100)                        COMMENT '작성자',
  MODIFY COLUMN post_date      DATE                                COMMENT '게시일자',
  MODIFY COLUMN content        TEXT                                COMMENT '본문 내용',
  MODIFY COLUMN reg_dt         DATETIME                            COMMENT '등록일시',
  MODIFY COLUMN reg_id         VARCHAR(50)                         COMMENT '등록자 ID',
  MODIFY COLUMN upd_dt         DATETIME                            COMMENT '수정일시',
  MODIFY COLUMN upd_id         VARCHAR(50)                         COMMENT '수정자 ID';


-- FK 재활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 완료 확인
SELECT '모든 테이블 한글 코멘트 업데이트 완료' AS result;
