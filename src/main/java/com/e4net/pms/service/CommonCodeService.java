package com.e4net.pms.service;

import com.e4net.pms.entity.CommonCode;
import com.e4net.pms.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;

    /** 그룹코드로 사용중인 코드 목록 조회 (정렬 순서) */
    public List<CommonCode> getByGroup(String groupCode) {
        return commonCodeRepository.findByGroupCodeAndUseYnOrderBySortOrder(groupCode, "Y");
    }
}
