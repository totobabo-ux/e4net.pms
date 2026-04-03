package com.e4net.pms.repository;

import com.e4net.pms.dto.ScreenListSearchDto;
import com.e4net.pms.entity.ScreenList;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ScreenListSpec {

    public static Specification<ScreenList> search(ScreenListSearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // count 쿼리가 아닐 때만 fetch join (N+1 방지)
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("project", JoinType.LEFT);
                query.distinct(true);
            }

            // 사업 ID (세션 기반 자동 필터 — 필수)
            if (dto.getProjectId() != null) {
                Join<Object, Object> project = root.join("project", JoinType.LEFT);
                predicates.add(cb.equal(project.get("id"), dto.getProjectId()));
            }

            // 메뉴Level1 검색
            if (hasText(dto.getMenuLevel1())) {
                predicates.add(cb.equal(root.get("menuLevel1"), dto.getMenuLevel1()));
            }

            // 분류 검색
            if (hasText(dto.getCategory())) {
                predicates.add(cb.equal(root.get("category"), dto.getCategory()));
            }

            // 화면명 검색 (like)
            if (hasText(dto.getScreenName())) {
                predicates.add(cb.like(root.get("screenName"), "%" + dto.getScreenName() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
