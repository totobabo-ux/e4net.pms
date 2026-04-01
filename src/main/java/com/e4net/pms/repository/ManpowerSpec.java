package com.e4net.pms.repository;

import com.e4net.pms.dto.ManpowerSearchDto;
import com.e4net.pms.entity.ProjectManpower;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ManpowerSpec {

    public static Specification<ProjectManpower> search(ManpowerSearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // count 쿼리가 아닐 때만 fetch join (N+1 방지)
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("project", JoinType.LEFT);
                root.fetch("user", JoinType.LEFT);
                query.distinct(true);
            }

            // 사업명 검색
            if (hasText(dto.getProjectName())) {
                Join<Object, Object> project = root.join("project", JoinType.LEFT);
                predicates.add(cb.like(project.get("projectName"), "%" + dto.getProjectName() + "%"));
            }

            // 참여자명 검색
            if (hasText(dto.getUserName())) {
                Join<Object, Object> user = root.join("user", JoinType.LEFT);
                predicates.add(cb.like(user.get("name"), "%" + dto.getUserName() + "%"));
            }

            // 소속회사 검색
            if (hasText(dto.getCompany())) {
                predicates.add(cb.like(root.get("company"), "%" + dto.getCompany() + "%"));
            }

            // 상태 검색
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
