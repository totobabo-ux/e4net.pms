package com.e4net.pms.repository;

import com.e4net.pms.dto.InterfaceListSearchDto;
import com.e4net.pms.entity.InterfaceList;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InterfaceListSpec {

    public static Specification<InterfaceList> search(InterfaceListSearchDto dto) {
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

            // 연계구분 검색
            if (hasText(dto.getLinkType())) {
                predicates.add(cb.equal(root.get("linkType"), dto.getLinkType()));
            }

            // 인터페이스방식 검색
            if (hasText(dto.getInterfaceMethod())) {
                predicates.add(cb.equal(root.get("interfaceMethod"), dto.getInterfaceMethod()));
            }

            // 인터페이스명 검색 (like)
            if (hasText(dto.getInterfaceName())) {
                predicates.add(cb.like(root.get("interfaceName"), "%" + dto.getInterfaceName() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
