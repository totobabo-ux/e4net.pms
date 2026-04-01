package com.e4net.pms.repository;

import com.e4net.pms.entity.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    List<CommonCode> findByGroupCodeAndUseYnOrderBySortOrder(String groupCode, String useYn);
}
