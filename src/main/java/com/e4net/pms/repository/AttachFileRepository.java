package com.e4net.pms.repository;

import com.e4net.pms.entity.AttachFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 공통 첨부파일 Repository
 * entity_type + entity_id 조합으로 특정 엔티티의 첨부파일 목록 조회/삭제
 */
public interface AttachFileRepository extends JpaRepository<AttachFile, Long> {

    /** 특정 엔티티의 첨부파일 목록 조회 (등록일 오름차순) */
    List<AttachFile> findByEntityTypeAndEntityIdOrderByRegDtAsc(String entityType, Long entityId);

    /** 특정 엔티티의 첨부파일 전체 삭제 (물리파일 삭제 후 호출) */
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
