package com.e4net.pms.repository;

import com.e4net.pms.dto.RequirementSearchDto;
import com.e4net.pms.entity.Requirement;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RequirementSpec {

    public static Specification<Requirement> search(RequirementSearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // count 쿼리가 아닐 때만 fetch join (N+1 방지)
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("project", JoinType.LEFT);
                query.distinct(true);
            }

            // 선택된 사업 ID (세션 기반 자동 필터)
            if (dto.getProjectId() != null) {
                Join<Object, Object> project = root.join("project", JoinType.LEFT);
                predicates.add(cb.equal(project.get("id"), dto.getProjectId()));
            }

            // 제목 검색
            if (hasText(dto.getTitle())) {
                predicates.add(cb.like(root.get("title"), "%" + dto.getTitle() + "%"));
            }

            // 분류 검색
            if (hasText(dto.getCategory())) {
                predicates.add(cb.equal(root.get("category"), dto.getCategory()));
            }

            // 상태 검색
            if (hasText(dto.getStatus())) {
                predicates.add(cb.equal(root.get("status"), dto.getStatus()));
            }

            // 우선순위 검색
            if (hasText(dto.getPriority())) {
                predicates.add(cb.equal(root.get("priority"), dto.getPriority()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
