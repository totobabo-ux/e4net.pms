package com.e4net.pms.repository;

import com.e4net.pms.dto.CommunitySearchDto;
import com.e4net.pms.entity.Community;
import org.springframework.data.jpa.domain.Specification;

public class CommunitySpec {

    public static Specification<Community> search(CommunitySearchDto dto) {
        return Specification
            .where(eqType(dto.getCommunityType()))
            .and(likeTitle(dto.getTitle()))
            .and(likeWriter(dto.getWriter()));
    }

    private static Specification<Community> eqType(String communityType) {
        return (root, query, cb) ->
            communityType == null ? null : cb.equal(root.get("communityType"), communityType);
    }

    private static Specification<Community> likeTitle(String title) {
        return (root, query, cb) ->
            (title == null || title.isBlank()) ? null : cb.like(root.get("title"), "%" + title + "%");
    }

    private static Specification<Community> likeWriter(String writer) {
        return (root, query, cb) ->
            (writer == null || writer.isBlank()) ? null : cb.like(root.get("writer"), "%" + writer + "%");
    }
}
