package com.e4net.pms.service;

import com.e4net.pms.dto.AppMenuDto;
import com.e4net.pms.entity.AppMenu;
import com.e4net.pms.repository.AppMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppMenuService {

    private final AppMenuRepository appMenuRepository;

    // ── 조회 ─────────────────────────────────────────────────

    /**
     * jsTree 포맷 트리 데이터 반환
     */
    public List<Map<String, Object>> getTreeData() {
        List<AppMenu> all = appMenuRepository.findAllByOrderByMenuCode();
        List<Map<String, Object>> result = new ArrayList<>();

        for (AppMenu m : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", String.valueOf(m.getId()));
            node.put("parent", m.getParentId() == null ? "#" : String.valueOf(m.getParentId()));
            node.put("text", m.getMenuName() + " (" + m.getMenuCode() + ")");

            Map<String, Object> state = new HashMap<>();
            state.put("opened", m.getDepth() <= 2);
            node.put("state", state);

            node.put("icon", m.getDepth() < 3 ? "jstree-folder" : "jstree-file");

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("menuCode",    m.getMenuCode());
            data.put("menuName",    m.getMenuName());
            data.put("contextPath", m.getContextPath() != null ? m.getContextPath() : "");
            data.put("depth",       m.getDepth());
            data.put("fixedYn",     m.getFixedYn());
            data.put("useYn",       m.getUseYn());
            data.put("parentId",    m.getParentId());
            node.put("data", data);

            result.add(node);
        }
        return result;
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull AppMenu findById(@NonNull Long id) {
        return appMenuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다. id=" + id));
    }

    /** Entity → DTO */
    public AppMenuDto toDto(AppMenu entity) {
        AppMenuDto dto = new AppMenuDto();
        dto.setId(entity.getId());
        dto.setParentId(entity.getParentId());
        dto.setMenuCode(entity.getMenuCode());
        dto.setMenuName(entity.getMenuName());
        dto.setContextPath(entity.getContextPath());
        dto.setIcon(entity.getIcon());
        dto.setDepth(entity.getDepth());
        dto.setSortOrder(entity.getSortOrder());
        dto.setFixedYn(entity.getFixedYn());
        dto.setUseYn(entity.getUseYn());

        if (entity.getParentId() != null) {
            appMenuRepository.findById(entity.getParentId())
                    .ifPresent(p -> dto.setParentName(p.getMenuName() + " (" + p.getMenuCode() + ")"));
        }
        return dto;
    }

    /** 엑셀 다운로드용 전체 목록 */
    public List<AppMenu> findAll() {
        return appMenuRepository.findAllByOrderByMenuCode();
    }

    // ── 쓰기 ─────────────────────────────────────────────────

    /** 메뉴 추가 */
    @Transactional
    public AppMenu create(AppMenuDto dto, String userId) {
        int depth = calcDepth(dto.getParentId());
        if (depth > 3) {
            throw new IllegalArgumentException("최대 3 Depth까지만 추가할 수 있습니다.");
        }

        AppMenu menu = new AppMenu();
        menu.setParentId(dto.getParentId());
        menu.setDepth(depth);
        menu.setMenuCode(generateMenuCode(dto.getParentId()));
        menu.setMenuName(dto.getMenuName() != null && !dto.getMenuName().isBlank()
                ? dto.getMenuName() : "New");
        menu.setContextPath(dto.getContextPath());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(countSiblings(dto.getParentId()) + 1);
        menu.setFixedYn("N");
        menu.setUseYn(dto.getUseYn() != null ? dto.getUseYn() : "Y");
        menu.setRegId(userId);
        menu.setUpdId(userId);
        return appMenuRepository.save(menu);
    }

    /** 메뉴 수정 */
    @Transactional
    public AppMenu update(@NonNull Long id, AppMenuDto dto, String userId) {
        AppMenu menu = findById(id);
        menu.setMenuName(dto.getMenuName());
        menu.setContextPath(dto.getContextPath());
        menu.setIcon(dto.getIcon());
        menu.setUseYn(dto.getUseYn() != null ? dto.getUseYn() : "Y");
        menu.setUpdId(userId);
        return appMenuRepository.save(menu);
    }

    /** 메뉴 삭제 */
    @Transactional
    public void delete(@NonNull Long id) {
        AppMenu menu = findById(id);
        if ("Y".equals(menu.getFixedYn())) {
            throw new IllegalArgumentException("고정 메뉴는 삭제할 수 없습니다.");
        }
        if (appMenuRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("하위 메뉴가 있는 경우 삭제할 수 없습니다.");
        }
        appMenuRepository.deleteById(id);
    }

    /**
     * 엑셀 업로드 — upsert 처리
     * 컬럼 순서: 메뉴코드(0) Depth(1) 상위메뉴코드(2) 메뉴명(3) Context Path(4) 사용여부(5)
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, String userId) {
        int inserted = 0, updated = 0, skipped = 0;

        for (String[] cells : rows) {
            String menuCodeVal = getCell(cells, 0);
            String menuNameVal = getCell(cells, 3);
            if (menuCodeVal.isBlank() || menuNameVal.isBlank()) {
                skipped++;
                continue;
            }

            String contextPathVal = getCell(cells, 4);
            String useYnRaw       = getCell(cells, 5);
            String useYnVal       = "미사용".equals(useYnRaw) ? "N" : "Y";

            var existing = appMenuRepository.findByMenuCode(menuCodeVal);
            if (existing.isPresent()) {
                AppMenu menu = existing.get();
                menu.setMenuName(menuNameVal);
                menu.setContextPath(contextPathVal.isBlank() ? null : contextPathVal);
                menu.setUseYn(useYnVal);
                menu.setUpdId(userId);
                appMenuRepository.save(menu);
                updated++;
            } else {
                String parentCodeVal = getCell(cells, 2);
                String depthStr      = getCell(cells, 1);

                int depth = 1;
                try { depth = Integer.parseInt(depthStr); } catch (NumberFormatException ignored) {}
                depth = Math.max(1, Math.min(3, depth));

                Long parentId = null;
                if (!parentCodeVal.isBlank() && !"-".equals(parentCodeVal)) {
                    parentId = appMenuRepository.findByMenuCode(parentCodeVal)
                            .map(AppMenu::getId)
                            .orElse(null);
                }

                AppMenu menu = new AppMenu();
                menu.setMenuCode(menuCodeVal);
                menu.setMenuName(menuNameVal);
                menu.setContextPath(contextPathVal.isBlank() ? null : contextPathVal);
                menu.setDepth(depth);
                menu.setParentId(parentId);
                menu.setSortOrder(countSiblings(parentId) + 1);
                menu.setFixedYn("N");
                menu.setUseYn(useYnVal);
                menu.setRegId(userId);
                menu.setUpdId(userId);
                appMenuRepository.save(menu);
                inserted++;
            }
        }
        return new int[]{ inserted, updated, skipped };
    }

    // ── private ──────────────────────────────────────────────

    private int calcDepth(Long parentId) {
        if (parentId == null) return 1;
        AppMenu parent = appMenuRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("상위 메뉴를 찾을 수 없습니다."));
        return parent.getDepth() + 1;
    }

    private int countSiblings(Long parentId) {
        if (parentId == null) {
            return appMenuRepository.findByParentIdIsNullOrderByMenuCode().size();
        }
        return appMenuRepository.findByParentIdOrderByMenuCode(parentId).size();
    }

    private String generateMenuCode(Long parentId) {
        if (parentId == null) {
            List<AppMenu> roots = appMenuRepository.findByParentIdIsNullOrderByMenuCode();
            int maxSeq = roots.stream()
                    .mapToInt(m -> parseSegment(m.getMenuCode(), 1, 3))
                    .max().orElse(0);
            return String.format("M%02d0000", maxSeq + 1);
        }

        AppMenu parent = appMenuRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("상위 메뉴를 찾을 수 없습니다."));
        List<AppMenu> siblings = appMenuRepository.findByParentIdOrderByMenuCode(parentId);

        if (parent.getDepth() == 1) {
            String prefix = parent.getMenuCode().substring(0, 3);
            int maxSeq = siblings.stream()
                    .mapToInt(m -> parseSegment(m.getMenuCode(), 3, 5))
                    .max().orElse(0);
            return String.format("%s%02d00", prefix, maxSeq + 1);
        } else if (parent.getDepth() == 2) {
            String prefix = parent.getMenuCode().substring(0, 5);
            int maxSeq = siblings.stream()
                    .mapToInt(m -> parseSegment(m.getMenuCode(), 5, 7))
                    .max().orElse(0);
            return String.format("%s%02d", prefix, maxSeq + 1);
        }

        throw new IllegalArgumentException("최대 3 Depth까지만 추가할 수 있습니다.");
    }

    private int parseSegment(String code, int from, int to) {
        try { return Integer.parseInt(code.substring(from, to)); }
        catch (Exception e) { return 0; }
    }

    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }
}
