package com.e4net.pms.repository;

import com.e4net.pms.entity.InterfaceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface InterfaceListRepository extends JpaRepository<InterfaceList, Long>,
        JpaSpecificationExecutor<InterfaceList> {

    /** 사업 ID 기준 전체 조회 (엑셀 다운로드용) */
    List<InterfaceList> findAllByProject_IdOrderByInterfaceIdAsc(Long projectId);

    /** 사업별 인터페이스ID 기준 단건 조회 (엑셀 업로드 upsert용) */
    Optional<InterfaceList> findByProject_IdAndInterfaceId(Long projectId, String interfaceId);
}
