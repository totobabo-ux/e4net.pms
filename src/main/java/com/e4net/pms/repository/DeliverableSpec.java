package com.e4net.pms.repository;

import com.e4net.pms.dto.DeliverableSearchDto;
import com.e4net.pms.entity.Deliverable;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DeliverableSpec {

    public static Specification<Deliverable> search(DeliverableSearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // count 쿼리가 아닐 때만 fetch join (N+1 방지)
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("project", JoinType.LEFT);
                query.distinct(true);
            }

            // 선택된 사업 ID (세션 기반 자동 필터 — 필수)
            if (dto.getProjectId() != null) {
                Join<Object, Object> project = root.join("project", JoinType.LEFT);
                predicates.add(cb.equal(project.get("id"), dto.getProjectId()));
            }

            // 산출물 구분 검색 (equal)
            if (hasText(dto.getDeliverableType())) {
                predicates.add(cb.equal(root.get("deliverableType"), dto.getDeliverableType()));
            }

            // 단계 검색 (equal)
            if (hasText(dto.getStage())) {
                predicates.add(cb.equal(root.get("stage"), dto.getStage()));
            }

            // 산출물명 검색 (like)
            if (hasText(dto.getName())) {
                predicates.add(cb.like(root.get("name"), "%" + dto.getName() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
