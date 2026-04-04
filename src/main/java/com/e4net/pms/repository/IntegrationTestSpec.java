package com.e4net.pms.repository;

import com.e4net.pms.dto.IntegrationTestSearchDto;
import com.e4net.pms.entity.IntegrationTest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IntegrationTestSpec {

    public static Specification<IntegrationTest> search(IntegrationTestSearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("project", JoinType.LEFT);
                query.distinct(true);
            }

            if (dto.getProjectId() != null) {
                Join<Object, Object> project = root.join("project", JoinType.LEFT);
                predicates.add(cb.equal(project.get("id"), dto.getProjectId()));
            }

            if (hasText(dto.getCategory())) {
                predicates.add(cb.equal(root.get("category"), dto.getCategory()));
            }

            if (hasText(dto.getIntegrationTestName())) {
                predicates.add(cb.like(root.get("integrationTestName"), "%" + dto.getIntegrationTestName() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
