package com.e4net.pms.repository;

import com.e4net.pms.dto.RiskSearchDto;
import com.e4net.pms.entity.Risk;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RiskSpec {

    public static Specification<Risk> search(RiskSearchDto dto) {
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

            // 위험명 검색
            if (hasText(dto.getRiskName())) {
                predicates.add(cb.like(root.get("riskName"), "%" + dto.getRiskName() + "%"));
            }

            // 위험유형 검색
            if (hasText(dto.getRiskType())) {
                predicates.add(cb.equal(root.get("riskType"), dto.getRiskType()));
            }

            // 위험등급 검색
            if (hasText(dto.getRiskLevel())) {
                predicates.add(cb.equal(root.get("riskLevel"), dto.getRiskLevel()));
            }

            // 위험상태 검색
            if (hasText(dto.getStatus())) {
                predicates.add(cb.equal(root.get("status"), dto.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
