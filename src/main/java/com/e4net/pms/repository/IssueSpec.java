package com.e4net.pms.repository;

import com.e4net.pms.dto.IssueSearchDto;
import com.e4net.pms.entity.Issue;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IssueSpec {

    public static Specification<Issue> search(IssueSearchDto dto) {
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

            // 이슈명 검색
            if (hasText(dto.getIssueName())) {
                predicates.add(cb.like(root.get("issueName"), "%" + dto.getIssueName() + "%"));
            }

            // 제기자 검색
            if (hasText(dto.getRaiser())) {
                predicates.add(cb.like(root.get("raiser"), "%" + dto.getRaiser() + "%"));
            }

            // 조치상태 검색
            if (hasText(dto.getActionStatus())) {
                predicates.add(cb.equal(root.get("actionStatus"), dto.getActionStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
