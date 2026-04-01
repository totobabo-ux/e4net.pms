package com.e4net.pms.repository;

import com.e4net.pms.dto.ProjectSearchDto;
import com.e4net.pms.entity.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpec {

    public static Specification<Project> search(ProjectSearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 프로젝트명 / 코드 검색
            if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
                String like = "%" + dto.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("projectName"), like),
                        cb.like(root.get("projectCode"), like)
                ));
            }

            // 구분
            if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
                predicates.add(cb.equal(root.get("category"), dto.getCategory()));
            }

            // 회사
            if (dto.getCompany() != null && !dto.getCompany().isBlank()) {
                predicates.add(cb.equal(root.get("company"), dto.getCompany()));
            }

            // 공개여부
            if (dto.getIsPublicStr() != null && !dto.getIsPublicStr().isBlank()) {
                predicates.add(cb.equal(root.get("isPublic"), Boolean.valueOf(dto.getIsPublicStr())));
            }

            // 발주처
            if (dto.getOrderer() != null && !dto.getOrderer().isBlank()) {
                predicates.add(cb.like(root.get("orderer"), "%" + dto.getOrderer() + "%"));
            }

            // 계약처
            if (dto.getContractor() != null && !dto.getContractor().isBlank()) {
                predicates.add(cb.like(root.get("contractor"), "%" + dto.getContractor() + "%"));
            }

            // PM
            if (dto.getPm() != null && !dto.getPm().isBlank()) {
                predicates.add(cb.like(root.get("pm"), "%" + dto.getPm() + "%"));
            }

            // 계약일자 범위
            if (dto.getContractStartFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("contractStart"), dto.getContractStartFrom()));
            }
            if (dto.getContractStartTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("contractStart"), dto.getContractStartTo()));
            }

            // 진행상태 (복수)
            if (dto.getStatusList() != null && !dto.getStatusList().isEmpty()) {
                predicates.add(root.get("status").in(dto.getStatusList()));
            }

            query.orderBy(cb.desc(root.get("id")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
