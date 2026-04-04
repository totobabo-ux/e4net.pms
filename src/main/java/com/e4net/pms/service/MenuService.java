package com.e4net.pms.service;

import com.e4net.pms.dto.MenuDto;
import com.e4net.pms.entity.Menu;
import com.e4net.pms.entity.Project;
import com.e4net.pms.repository.MenuRepository;
import com.e4net.pms.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final ProjectRepository projectRepository;

    // ── 조회 ─────────────────────────────────────────────────

    /**
     * jsTree 포맷 트리 데이터 반환
     * parent="#" 은 루트, 나머지는 부모 ID(문자열)
     */
    public List<Map<String, Object>> getTreeData(Long projectId) {
        List<Menu> all = menuRepository.findByProject_IdOrderByMenuCode(projectId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Menu m : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", String.valueOf(m.getId()));
            node.put("parent", m.getParentId() == null ? "#" : String.valueOf(m.getParentId()));
            node.put("text", m.getMenuName() + " (" + m.getMenuCode() + ")");

            // 기본 열림 상태: depth 1, 2 는 열림
            Map<String, Object> state = new HashMap<>();
            state.put("opened", m.getDepth() <= 2);
            node.put("state", state);

            // 아이콘: depth 3은 파일, 나머지는 폴더
            node.put("icon", m.getDepth() < 3 ? "jstree-folder" : "jstree-file");

            // AJAX detail에서 쓸 추가 데이터
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("menuCode", m.getMenuCode());
            data.put("menuName", m.getMenuName());
            data.put("contextPath", m.getContextPath() != null ? m.getContextPath() : "");
            data.put("depth", m.getDepth());
            data.put("fixedYn", m.getFixedYn());
            data.put("useYn", m.getUseYn());
            data.put("parentId", m.getParentId());
            node.put("data", data);

            result.add(node);
        }
        return result;
    }

    /** 단건 조회 */
    @SuppressWarnings("null")
    public @NonNull Menu findById(@NonNull Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다. id=" + id));
    }

    /** Entity → DTO */
    public MenuDto toDto(Menu entity) {
        MenuDto dto = new MenuDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setParentId(entity.getParentId());
        dto.setMenuCode(entity.getMenuCode());
        dto.setMenuName(entity.getMenuName());
        dto.setContextPath(entity.getContextPath());
        dto.setDepth(entity.getDepth());
        dto.setSortOrder(entity.getSortOrder());
        dto.setFixedYn(entity.getFixedYn());
        dto.setUseYn(entity.getUseYn());

        // 상위 메뉴명 조회
        if (entity.getParentId() != null) {
            menuRepository.findById(entity.getParentId())
                    .ifPresent(p -> dto.setParentName(p.getMenuName() + " (" + p.getMenuCode() + ")"));
        }
        return dto;
    }

    /** 엑셀 다운로드용 전체 목록 */
    public List<Menu> findAllByProject(Long projectId) {
        return menuRepository.findByProject_IdOrderByMenuCode(projectId);
    }

    /**
     * 엑셀 업로드 — upsert 처리
     * 컬럼 순서: 메뉴코드(0) Depth(1) 상위메뉴코드(2) 메뉴명(3) Context Path(4) 사용여부(5)
     * - 메뉴코드 기존 존재 → 메뉴명/ContextPath/사용여부 수정
     * - 신규 → 상위메뉴코드로 parentId 조회 후 등록 (메뉴코드 그대로 사용)
     */
    @Transactional
    public int[] upsertFromExcel(List<String[]> rows, Long projectId, String userId) {
        @SuppressWarnings("null")
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

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

            var existing = menuRepository.findByProject_IdAndMenuCode(projectId, menuCodeVal);
            if (existing.isPresent()) {
                // 수정: 메뉴명 / ContextPath / 사용여부
                Menu menu = existing.get();
                menu.setMenuName(menuNameVal);
                menu.setContextPath(contextPathVal.isBlank() ? null : contextPathVal);
                menu.setUseYn(useYnVal);
                menu.setUpdId(userId);
                menuRepository.save(menu);
                updated++;
            } else {
                // 신규 등록
                String parentCodeVal = getCell(cells, 2);
                String depthStr      = getCell(cells, 1);

                int depth = 1;
                try { depth = Integer.parseInt(depthStr); } catch (NumberFormatException ignored) {}
                depth = Math.max(1, Math.min(3, depth));

                Long parentId = null;
                if (!parentCodeVal.isBlank() && !"-".equals(parentCodeVal)) {
                    parentId = menuRepository.findByProject_IdAndMenuCode(projectId, parentCodeVal)
                            .map(Menu::getId)
                            .orElse(null);
                }

                Menu menu = new Menu();
                menu.setProject(project);
                menu.setMenuCode(menuCodeVal);
                menu.setMenuName(menuNameVal);
                menu.setContextPath(contextPathVal.isBlank() ? null : contextPathVal);
                menu.setDepth(depth);
                menu.setParentId(parentId);
                menu.setSortOrder(countSiblings(parentId, projectId) + 1);
                menu.setFixedYn("N");
                menu.setUseYn(useYnVal);
                menu.setRegId(userId);
                menu.setUpdId(userId);
                menuRepository.save(menu);
                inserted++;
            }
        }
        return new int[]{ inserted, updated, skipped };
    }

    // ── 쓰기 ─────────────────────────────────────────────────

    /** 메뉴 추가 */
    @SuppressWarnings("null")
    @Transactional
    public Menu create(MenuDto dto, String userId) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("사업을 찾을 수 없습니다."));

        int depth = calcDepth(dto.getParentId());
        if (depth > 3) {
            throw new IllegalArgumentException("최대 3 Depth까지만 추가할 수 있습니다.");
        }

        Menu menu = new Menu();
        menu.setProject(project);
        menu.setParentId(dto.getParentId());
        menu.setDepth(depth);
        menu.setMenuCode(generateMenuCode(dto.getParentId(), dto.getProjectId()));
        menu.setMenuName(dto.getMenuName() != null && !dto.getMenuName().isBlank()
                ? dto.getMenuName() : "New");
        menu.setContextPath(dto.getContextPath());
        menu.setSortOrder(countSiblings(dto.getParentId(), dto.getProjectId()) + 1);
        menu.setFixedYn("N");
        menu.setUseYn(dto.getUseYn() != null ? dto.getUseYn() : "Y");
        menu.setRegId(userId);
        menu.setUpdId(userId);
        return menuRepository.save(menu);
    }

    /** 메뉴 수정 (메뉴명 / Context Path / 사용여부만 변경 가능) */
    @Transactional
    public Menu update(@NonNull Long id, MenuDto dto, String userId) {
        Menu menu = findById(id);
        menu.setMenuName(dto.getMenuName());
        menu.setContextPath(dto.getContextPath());
        menu.setUseYn(dto.getUseYn() != null ? dto.getUseYn() : "Y");
        menu.setUpdId(userId);
        return menuRepository.save(menu);
    }

    /** 메뉴 삭제 (고정 메뉴 / 자식 있는 메뉴 불가) */
    @Transactional
    public void delete(@NonNull Long id) {
        Menu menu = findById(id);
        if ("Y".equals(menu.getFixedYn())) {
            throw new IllegalArgumentException("고정 메뉴는 삭제할 수 없습니다.");
        }
        if (menuRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("하위 메뉴가 있는 경우 삭제할 수 없습니다.");
        }
        menuRepository.deleteById(id);
    }

    // ── private ──────────────────────────────────────────────

    /** 부모 ID로 현재 depth 계산 */
    private int calcDepth(Long parentId) {
        if (parentId == null) return 1;
        Menu parent = menuRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("상위 메뉴를 찾을 수 없습니다."));
        return parent.getDepth() + 1;
    }

    /** 형제 노드 수 */
    private int countSiblings(Long parentId, Long projectId) {
        if (parentId == null) {
            return menuRepository.findByProject_IdAndParentIdIsNullOrderByMenuCode(projectId).size();
        }
        return menuRepository.findByParentIdOrderByMenuCode(parentId).size();
    }

    /**
     * 메뉴코드 자동 생성
     * Depth 1 : M + XX + 00 + 00  (예: M010000)
     * Depth 2 : M + PP + XX + 00  (예: M010100)
     * Depth 3 : M + PP + QQ + XX  (예: M010101)
     * XX = 형제 중 최대 순번 + 1
     */
    private String generateMenuCode(Long parentId, Long projectId) {
        if (parentId == null) {
            // Depth 1
            List<Menu> roots = menuRepository.findByProject_IdAndParentIdIsNullOrderByMenuCode(projectId);
            int maxSeq = roots.stream()
                    .mapToInt(m -> parseSegment(m.getMenuCode(), 1, 3))
                    .max().orElse(0);
            return String.format("M%02d0000", maxSeq + 1);
        }

        Menu parent = menuRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("상위 메뉴를 찾을 수 없습니다."));
        List<Menu> siblings = menuRepository.findByParentIdOrderByMenuCode(parentId);

        if (parent.getDepth() == 1) {
            // Depth 2: 부모 "M01" + XX + "00"
            String prefix = parent.getMenuCode().substring(0, 3); // "M01"
            int maxSeq = siblings.stream()
                    .mapToInt(m -> parseSegment(m.getMenuCode(), 3, 5))
                    .max().orElse(0);
            return String.format("%s%02d00", prefix, maxSeq + 1);
        } else if (parent.getDepth() == 2) {
            // Depth 3: 부모 "M0101" + XX
            String prefix = parent.getMenuCode().substring(0, 5); // "M0101"
            int maxSeq = siblings.stream()
                    .mapToInt(m -> parseSegment(m.getMenuCode(), 5, 7))
                    .max().orElse(0);
            return String.format("%s%02d", prefix, maxSeq + 1);
        }

        throw new IllegalArgumentException("최대 3 Depth까지만 추가할 수 있습니다.");
    }

    private int parseSegment(String code, int from, int to) {
        try {
            return Integer.parseInt(code.substring(from, to));
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCell(String[] cells, int idx) {
        return (cells != null && idx < cells.length && cells[idx] != null) ? cells[idx].trim() : "";
    }
}
