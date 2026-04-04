-- ============================================================
-- E4NET PMS 사이드바 메뉴 초기 데이터 (수동 실행용)
-- 실행 순서: depth1 → depth2 → depth3
-- ============================================================

-- depth 1: 섹션 헤더
INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt) VALUES
(1, NULL, 1, 'M010000', '사업관리', NULL, '&#128203;', 'Y', 'Y', NOW(), NOW()),
(1, NULL, 2, 'M020000', '사업수행', NULL, '&#128187;', 'Y', 'Y', NOW(), NOW()),
(1, NULL, 3, 'M030000', '커뮤니티', NULL, '&#128172;', 'Y', 'Y', NOW(), NOW()),
(1, NULL, 4, 'M040000', '관리자',   NULL, '&#9881;',   'Y', 'Y', NOW(), NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), icon = VALUES(icon);

-- depth 2: 그룹 헤더 (parent_id는 depth1 id 기준)
INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 1, 'M010100', '표준관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 2, 'M010200', '인력관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 3, 'M010300', '범위관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 4, 'M010400', '위험관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 5, 'M010500', '보고관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 1, 'M020100', '분석', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 2, 'M020200', '설계', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 3, 'M020300', '구현', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 4, 'M020400', '시험', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 1, 'M030100', '공지사항', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M030000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 2, 'M030200', '자료실', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M030000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 1, 'M040100', '사업 관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 2, 'M040200', '사용자 관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 2, m.id, 3, 'M040300', '시스템 관리', NULL, NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040000'
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- depth 3: 리프 메뉴 (실제 링크)
INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M010101', '산출물 관리', '/deliverable', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010100'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M010201', '투입인력 관리', '/manpower', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010200'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M010301', '요구사항 관리', '/requirement', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010300'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 2, 'M010302', '요구사항 추적', '/req-traceability', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010300'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 3, 'M010303', '사업일정(WBS)', '/wbs', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010300'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M010401', '이슈관리', '/issue', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010400'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 2, 'M010402', '위험관리', '/risk', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010400'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M010501', '정기보고', '/regular-report', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010500'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 2, 'M010502', '주간보고', '/weekly-report', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010500'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 3, 'M010503', '월간보고', '/monthly-report', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010500'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 4, 'M010504', '회의록', '/meeting-report', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M010500'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M020101', '업무흐름', '/business-flow', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020100'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 2, 'M020102', '메뉴구조', '/menu-structure', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020100'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M020201', '화면목록', '/screen-list', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020200'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 2, 'M020202', '프로그램목록', '/program-list', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020200'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 3, 'M020203', '인터페이스 목록', '/interface-list', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020200'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 4, 'M020204', '테이블목록', '/admin/system/database', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020200'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M020301', '단위테스트 목록', '/unit-test', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020300'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M020401', '통합테스트 목록', '/integration-test', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M020400'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M030101', '공지사항 목록', '/notice', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M030100'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M030201', '자료실 목록', '/archive', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M030200'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M040101', '사업 목록', '/projects', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040100'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M040201', '사용자 목록', '/admin/users', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040200'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 1, 'M040301', '공통코드 관리', '/admin/common-code', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040300'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 2, 'M040302', '메뉴 관리', '/admin/menu', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040300'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);

INSERT INTO menu (depth, parent_id, sort_order, menu_code, menu_name, context_path, icon, fixed_yn, use_yn, reg_dt, upd_dt)
SELECT 3, m.id, 3, 'M040303', '권한 관리', '/admin/roles', NULL, 'Y', 'Y', NOW(), NOW() FROM menu m WHERE m.menu_code = 'M040300'
ON DUPLICATE KEY UPDATE context_path = VALUES(context_path);
