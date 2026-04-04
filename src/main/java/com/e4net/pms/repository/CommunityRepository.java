package com.e4net.pms.repository;

import com.e4net.pms.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityRepository extends JpaRepository<Community, Long>, JpaSpecificationExecutor<Community> {

    /** 홈 대시보드 — 유형별 최근 5건 */
    java.util.List<com.e4net.pms.entity.Community> findTop5ByCommunityTypeOrderByPostDateDescIdDesc(String communityType);
}
