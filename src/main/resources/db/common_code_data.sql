-- ============================================================
-- 공통코드 기초 데이터 INSERT
-- code = 실제 DB 저장값 (기존 데이터와 동일), code_name = 화면 표시명
-- 실행 전 기존 데이터 중복 여부 확인 후 적용
-- ============================================================

-- ── GRADE : 인력 급수 ─────────────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('GRADE', '특급',  '특급',  1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('GRADE', '고급',  '고급',  2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('GRADE', '중급',  '중급',  3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('GRADE', '초급',  '초급',  4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── INPUT_TYPE : 인력 투입구분 ────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('INPUT_TYPE', '상주',    '상주',    1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INPUT_TYPE', '비상주',  '비상주',  2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INPUT_TYPE', '혼합',    '혼합',    3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── MANPOWER_STATUS : 인력 상태 ──────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('MANPOWER_STATUS', '투입중',    '투입중',    1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('MANPOWER_STATUS', '대기',      '대기',      2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('MANPOWER_STATUS', '투입종료',  '투입종료',  3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── PROJECT_CATEGORY : 사업 유형 ─────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('PROJECT_CATEGORY', 'SL',    'SL',    1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROJECT_CATEGORY', 'SM',    'SM',    2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROJECT_CATEGORY', '서비스', '서비스', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── ISSUE_ACTION_STATUS : 이슈 조치상태 ──────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('ISSUE_ACTION_STATUS', '미조치',   '미조치',   1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('ISSUE_ACTION_STATUS', '조치중',   '조치중',   2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('ISSUE_ACTION_STATUS', '조치완료', '조치완료', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('ISSUE_ACTION_STATUS', '보류',     '보류',     4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── DELIVERABLE_TYPE : 산출물 구분 ───────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('DELIVERABLE_TYPE', '관리산출물', '관리산출물', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_TYPE', '개발산출물', '개발산출물', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── DELIVERABLE_WRITE_YN : 산출물 작성여부 ───────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('DELIVERABLE_WRITE_YN', 'Y', '작성',   1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_WRITE_YN', 'N', '미작성', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── DELIVERABLE_STAGE : 산출물 단계 ──────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('DELIVERABLE_STAGE', '미도래',  '미도래',  1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_STAGE', '미착수',  '미착수',  2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_STAGE', '수행',    '수행',    3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_STAGE', 'PL확인',  'PL확인',  4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_STAGE', 'PM확인',  'PM확인',  5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_STAGE', 'PMO확인', 'PMO확인', 6, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('DELIVERABLE_STAGE', '완료',    '완료',    7, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── RISK_TYPE : 위험 유형 ─────────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('RISK_TYPE', '기술', '기술', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_TYPE', '일정', '일정', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_TYPE', '비용', '비용', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_TYPE', '인력', '인력', 4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_TYPE', '외부', '외부', 5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── RISK_PROBABILITY : 위험 발생가능성 ───────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('RISK_PROBABILITY', '낮음', '낮음', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_PROBABILITY', '보통', '보통', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_PROBABILITY', '높음', '높음', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── RISK_IMPACT : 위험 영향도 ─────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('RISK_IMPACT', '적음',    '적음',    1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_IMPACT', '보통',    '보통',    2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_IMPACT', '심각',    '심각',    3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_IMPACT', '매우심각', '매우심각', 4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── RISK_LEVEL : 위험 등급 ────────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('RISK_LEVEL', 'VERY LOW',  'VERY LOW',  1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_LEVEL', 'LOW',       'LOW',       2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_LEVEL', 'MODERATE',  'MODERATE',  3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_LEVEL', 'HIGH',      'HIGH',      4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_LEVEL', 'VERY HIGH', 'VERY HIGH', 5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── RISK_STATUS : 위험 상태 ───────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('RISK_STATUS', '진행중', '진행중', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_STATUS', '해결',   '해결',   2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_STATUS', '종료',   '종료',   3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── RISK_STRATEGY : 위험 대응전략 ────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('RISK_STRATEGY', '회피', '회피', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_STRATEGY', '전가', '전가', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_STRATEGY', '완화', '완화', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('RISK_STRATEGY', '수용', '수용', 4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── REQ_SOURCE : 요구사항 출처 ───────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('REQ_SOURCE', '제안요청서', '제안요청서', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_SOURCE', '회의록',     '회의록',     2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_SOURCE', '기타',       '기타',       3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── REQ_TYPE : 요구사항 분류 ─────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('REQ_TYPE', '기능',   '기능',   1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_TYPE', '비기능', '비기능', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── REQ_PRIORITY : 요구사항 우선순위 ─────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('REQ_PRIORITY', '상', '상', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_PRIORITY', '중', '중', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_PRIORITY', '하', '하', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── REQ_STATUS : 요구사항 상태 ───────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('REQ_STATUS', '등록',   '등록',   1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_STATUS', '분석중', '분석중', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_STATUS', '개발중', '개발중', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_STATUS', '완료',   '완료',   4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_STATUS', '보류',   '보류',   5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── REQ_AGREEMENT : 요구사항 고객합의 ────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('REQ_AGREEMENT', '협의중', '협의중', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_AGREEMENT', '수용',   '수용',   2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('REQ_AGREEMENT', '제외',   '제외',   3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── SCREEN_MENU_LEVEL1 : 화면목록 1차메뉴 ────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('SCREEN_MENU_LEVEL1', '공통',    '공통',    1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_MENU_LEVEL1', '사업관리', '사업관리', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_MENU_LEVEL1', '사업수행', '사업수행', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_MENU_LEVEL1', '커뮤니티', '커뮤니티', 4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_MENU_LEVEL1', '관리자',   '관리자',   5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── PROGRAM_TYPE : 프로그램 구분 ─────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('PROGRAM_TYPE', 'Controller', 'Controller', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_TYPE', 'Service',    'Service',    2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_TYPE', 'Entity',     'Entity',     3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_TYPE', 'Repository', 'Repository', 4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_TYPE', 'DTO',        'DTO',        5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_TYPE', 'Util',       'Util',       6, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_TYPE', 'Config',     'Config',     7, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_TYPE', '기타',        '기타',        8, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── PROGRAM_DIFFICULTY : 개발난이도 ──────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('PROGRAM_DIFFICULTY', '상', '상', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_DIFFICULTY', '중', '중', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('PROGRAM_DIFFICULTY', '하', '하', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── SCREEN_CATEGORY : 화면목록 화면유형 ──────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('SCREEN_CATEGORY', '목록', '목록', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_CATEGORY', '등록', '등록', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_CATEGORY', '상세', '상세', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_CATEGORY', '수정', '수정', 4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('SCREEN_CATEGORY', '조회', '조회', 5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── INTERFACE_LINK_TYPE : 인터페이스 연계구분 ─────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('INTERFACE_LINK_TYPE', '자기관', '자기관', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_LINK_TYPE', '타기관', '타기관', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_LINK_TYPE', '기타',   '기타',   3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── INTERFACE_METHOD : 인터페이스방식 ────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('INTERFACE_METHOD', '공동이용API', '공동이용API', 1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_METHOD', 'REST API',   'REST API',   2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_METHOD', 'SOAP',       'SOAP',       3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_METHOD', '파일전송',    '파일전송',    4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_METHOD', 'DB Link',    'DB Link',    5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_METHOD', '기타',        '기타',        6, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());

-- ── INTERFACE_CYCLE : 인터페이스 발생주기 ────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn, reg_id, upd_id, reg_dt, upd_dt)
VALUES
  ('INTERFACE_CYCLE', '실시간',     '실시간',     1, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_CYCLE', '배치(일별)', '배치(일별)', 2, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_CYCLE', '배치(주별)', '배치(주별)', 3, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_CYCLE', '배치(월별)', '배치(월별)', 4, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_CYCLE', '수동',       '수동',       5, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW()),
  ('INTERFACE_CYCLE', '기타',       '기타',       6, 'Y', 'SYSTEM', 'SYSTEM', NOW(), NOW());
