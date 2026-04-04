package com.e4net.pms.service;

import com.e4net.pms.entity.CommonCode;
import com.e4net.pms.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;

    /** 그룹코드로 사용중인 코드 목록 조회 (정렬 순서) */
    public List<CommonCode> getByGroup(String groupCode) {
        return commonCodeRepository.findByGroupCodeAndUseYnOrderBySortOrder(groupCode, "Y");
    }

    // ── 관리 기능 ────────────────────────────────────────────────

    /** 전체 목록 (그룹코드/정렬순서 정렬, 엑셀 다운로드 + 화면 초기 로딩용) */
    public List<CommonCode> findAll() {
        return commonCodeRepository.findAllByOrderByGroupCodeAscSortOrderAscIdAsc();
    }

    /** 그룹별 전체 목록 (use_yn 무관) */
    public List<CommonCode> findByGroup(String groupCode) {
        return commonCodeRepository.findByGroupCodeOrderBySortOrderAscIdAsc(groupCode);
    }

    /** 중복 없는 그룹코드 목록 */
    public List<String> findDistinctGroupCodes() {
        return commonCodeRepository.findDistinctGroupCodes();
    }

    /** 신규 등록 */
    @Transactional
    public CommonCode saveNew(CommonCode entity, String userId) {
        entity.setRegId(userId);
        entity.setUpdId(userId);
        return commonCodeRepository.save(entity);
    }

    /** 수정 */
    @Transactional
    public CommonCode update(Long id, CommonCode updated, String userId) {
        CommonCode entity = commonCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공통코드를 찾을 수 없습니다. id=" + id));
        entity.setGroupCode(updated.getGroupCode());
        entity.setCode(updated.getCode());
        entity.setCodeName(updated.getCodeName());
        entity.setSortOrder(updated.getSortOrder() != null ? updated.getSortOrder() : 0);
        entity.setUseYn(updated.getUseYn() != null ? updated.getUseYn() : "Y");
        entity.setUpdId(userId);
        return commonCodeRepository.save(entity);
    }

    /** 삭제 */
    @Transactional
    public void delete(Long id) {
        commonCodeRepository.deleteById(id);
    }

    /**
     * 엑셀 업로드 upsert — 그룹코드+코드 기준
     * 컬럼 순서: 그룹코드(0), 코드(1), 코드명(2), 정렬순서(3), 사용여부(4)
     *
     * @return int[] { 신규등록 수, 수정 수, 건너뜀 수 }
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, String userId) {
        int inserted = 0, updated = 0, skipped = 0;
        for (String[] cells : rows) {
            String groupCode = getCell(cells, 0);
            String code      = getCell(cells, 1);
            String codeName  = getCell(cells, 2);
            if (groupCode.isBlank() || code.isBlank() || codeName.isBlank()) {
                skipped++;
                continue;
            }
            var existing = commonCodeRepository.findByGroupCodeAndCode(groupCode, code);
            CommonCode entity;
            boolean isNew;
            if (existing.isPresent()) {
                entity = existing.get();
                isNew  = false;
            } else {
                entity = new CommonCode();
                entity.setRegId(userId);
                isNew = true;
            }
            entity.setGroupCode(groupCode);
            entity.setCode(code);
            entity.setCodeName(codeName);
            String sortStr = getCell(cells, 3);
            entity.setSortOrder(sortStr.isBlank() ? 0 : parseIntSafe(sortStr));
            String useYn = getCell(cells, 4);
            entity.setUseYn(useYn.isBlank() ? "Y" : useYn);
            entity.setUpdId(userId);
            commonCodeRepository.save(entity);
            if (isNew) inserted++; else updated++;
        }
        return new int[]{ inserted, updated, skipped };
    }

    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
