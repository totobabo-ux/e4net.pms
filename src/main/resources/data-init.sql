-- ============================================================
-- e4net PMS 초기 데이터 (수동 실행용)
-- ============================================================

-- ── 공통 코드: 급수 (GRADE) ──────────────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn) VALUES
('GRADE', 'GRADE_SPECIAL',     '특급기술자', 1, 'Y'),
('GRADE', 'GRADE_HIGH',        '고급기술자', 2, 'Y'),
('GRADE', 'GRADE_MIDDLE',      '중급기술자', 3, 'Y'),
('GRADE', 'GRADE_LOW',         '초급기술자', 4, 'Y'),
('GRADE', 'GRADE_ASSIST',      '보조원',     5, 'Y')
ON DUPLICATE KEY UPDATE code_name = VALUES(code_name);

-- ── 공통 코드: 투입구분 (INPUT_TYPE) ─────────────────────
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn) VALUES
('INPUT_TYPE', 'INPUT_RESIDENT',    '상주',   1, 'Y'),
('INPUT_TYPE', 'INPUT_NON_RESIDENT','비상주', 2, 'Y'),
('INPUT_TYPE', 'INPUT_SUPPORT',     '지원',   3, 'Y'),
('INPUT_TYPE', 'INPUT_PART',        '파트타임', 4, 'Y')
ON DUPLICATE KEY UPDATE code_name = VALUES(code_name);

-- ── 샘플 사용자 (테스트용) ────────────────────────────────
INSERT INTO users (employee_no, name, company, department, position, phone, email) VALUES
('E001', '김철수', '(주)이포넷', '개발1팀',   '수석연구원', '010-1111-2222', 'kim@e4net.com'),
('E002', '이영희', '(주)이포넷', '기획팀',     '선임연구원', '010-3333-4444', 'lee@e4net.com'),
('E003', '박민준', '(주)이포넷', '개발2팀',   '연구원',     '010-5555-6666', 'park@e4net.com'),
('E004', '최수진', '(주)파트너스', '개발팀', '수석연구원', '010-7777-8888', 'choi@partner.com'),
('E005', '정대현', '(주)파트너스', 'PM팀',   '책임연구원', '010-9999-0000', 'jung@partner.com')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 공통 코드: 권한 코드 (ROLE_CODE)
INSERT INTO common_code (group_code, code, code_name, sort_order, use_yn) VALUES
('ROLE_CODE', 'ROLE_ADMIN', '관리자',     1, 'Y'),
('ROLE_CODE', 'ROLE_PM',    'PM',         2, 'Y'),
('ROLE_CODE', 'ROLE_PL',    'PL',         3, 'Y'),
('ROLE_CODE', 'ROLE_USER',  '일반사용자', 4, 'Y')
ON DUPLICATE KEY UPDATE code_name = VALUES(code_name);
