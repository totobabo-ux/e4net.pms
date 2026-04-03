-- ============================================================
-- e4net PMS - created_at/updated_at → reg_dt/upd_dt 이관
-- 실행 방법:
--   mysql --default-character-set=utf8mb4 -u root -p e4net_pms < migrate_to_reg_dt.sql
-- ============================================================

SET NAMES 'utf8mb4';
SET CHARACTER SET utf8mb4;
USE e4net_pms;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. project
--    reg_dt/upd_dt 이미 존재 → created_at 값으로 덮어쓰기 후 삭제
-- ============================================================
UPDATE project
   SET reg_dt = IFNULL(created_at, reg_dt),
       upd_dt = IFNULL(updated_at, upd_dt);

ALTER TABLE project
  DROP COLUMN created_at,
  DROP COLUMN updated_at;

SELECT CONCAT('project 완료: ', COUNT(*), '건') AS result FROM project;

-- ============================================================
-- 2. users
--    reg_dt/upd_dt 이미 존재 → created_at 값으로 덮어쓰기 후 삭제
-- ============================================================
UPDATE users
   SET reg_dt = IFNULL(created_at, reg_dt),
       upd_dt = IFNULL(updated_at, upd_dt);

ALTER TABLE users
  DROP COLUMN created_at,
  DROP COLUMN updated_at;

SELECT CONCAT('users 완료: ', COUNT(*), '건') AS result FROM users;

-- ============================================================
-- 3. common_code
--    created_at/updated_at 없음 → 삭제 불필요, reg_dt/upd_dt 이미 존재
-- ============================================================
SELECT CONCAT('common_code 완료: ', COUNT(*), '건 (이관 불필요)') AS result FROM common_code;

-- ============================================================
-- 4. project_manpower
--    reg_dt/upd_dt 이미 존재 → created_at 값으로 덮어쓰기 후 삭제
-- ============================================================
UPDATE project_manpower
   SET reg_dt = IFNULL(created_at, reg_dt),
       upd_dt = IFNULL(updated_at, upd_dt);

ALTER TABLE project_manpower
  DROP COLUMN created_at,
  DROP COLUMN updated_at;

SELECT CONCAT('project_manpower 완료: ', COUNT(*), '건') AS result FROM project_manpower;

-- ============================================================
-- 5. requirement
--    reg_dt/upd_dt 이미 존재 → created_at 값으로 덮어쓰기 후 삭제
-- ============================================================
UPDATE requirement
   SET reg_dt = IFNULL(created_at, reg_dt),
       upd_dt = IFNULL(updated_at, upd_dt);

ALTER TABLE requirement
  DROP COLUMN created_at,
  DROP COLUMN updated_at;

SELECT CONCAT('requirement 완료: ', COUNT(*), '건') AS result FROM requirement;

-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
SELECT '모든 이관 완료' AS result;
