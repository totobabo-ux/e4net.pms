package com.e4net.pms.repository;

import com.e4net.pms.dto.CustomerReportSearchDto;
import com.e4net.pms.entity.CustomerReport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomerReportSpec {

    public static Specification<CustomerReport> search(CustomerReportSearchDto dto) {
        return (root, query, cb) -> {
            // N+1 방지 fetch join
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("project");
            }

            List<Predicate> predicates = new ArrayList<>();

            if (dto.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), dto.getProjectId()));
            }
            if (dto.getReportType() != null && !dto.getReportType().isBlank()) {
                predicates.add(cb.equal(root.get("reportType"), dto.getReportType()));
            }
            if (dto.getReportName() != null && !dto.getReportName().isBlank()) {
                predicates.add(cb.like(root.get("reportName"), "%" + dto.getReportName() + "%"));
            }
            if (dto.getWriter() != null && !dto.getWriter().isBlank()) {
                predicates.add(cb.like(root.get("writer"), "%" + dto.getWriter() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
