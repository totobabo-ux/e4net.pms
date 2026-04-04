package com.e4net.pms.service;

import com.e4net.pms.dto.TraceTargetItem;
import com.e4net.pms.entity.ReqTraceability;
import com.e4net.pms.repository.BusinessFlowRepository;
import com.e4net.pms.repository.MenuRepository;
import com.e4net.pms.repository.ReqTraceabilityRepository;
import com.e4net.pms.repository.ScreenListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReqTraceabilityService {

    private final ReqTraceabilityRepository traceRepository;
    private final BusinessFlowRepository    businessFlowRepository;
    private final MenuRepository            menuRepository;
    private final ScreenListRepository      screenListRepository;

    /**
     * 탭 타입별 전체 대상 목록 + 현재 요구사항과 연결 여부 반환
     * 미구현 탭(INTERFACE/PROGRAM/UNIT_TEST/INTEGRATION_TEST)은 빈 목록 반환
     */
    public List<TraceTargetItem> getTabItems(Long projectId, Long reqId, String targetType) {
        Set<Long> mappedIds = traceRepository
                .findByProjectIdAndReqIdAndTargetType(projectId, reqId, targetType)
                .stream()
                .map(ReqTraceability::getTargetId)
                .collect(Collectors.toSet());

        return switch (targetType) {
            case "BUSINESS_FLOW" ->
                businessFlowRepository
                    .findByProject_IdOrderBySystemCategoryAscProcessIdAsc(projectId)
                    .stream()
                    .map(e -> new TraceTargetItem(
                            e.getId(),
                            e.getProcessId(),
                            e.getSystemCategory() + " > " + e.getProcessName(),
                            mappedIds.contains(e.getId())))
                    .toList();

            case "MENU" ->
                menuRepository
                    .findByProject_IdOrderByMenuCode(projectId)
                    .stream()
                    .map(e -> new TraceTargetItem(
                            e.getId(),
                            e.getMenuCode(),
                            e.getMenuName(),
                            mappedIds.contains(e.getId())))
                    .toList();

            case "SCREEN" ->
                screenListRepository
                    .findByProject_IdOrderByMenuLevel1AscMenuLevel2AscScreenNameAsc(projectId)
                    .stream()
                    .map(e -> new TraceTargetItem(
                            e.getId(),
                            e.getUrl() != null ? e.getUrl() : "-",
                            e.getScreenName(),
                            mappedIds.contains(e.getId())))
                    .toList();

            // 미구현 탭 — 빈 목록 반환
            default -> List.of();
        };
    }

    /**
     * 연관관계 저장 (기존 전체 삭제 후 새로 삽입 — replace 방식)
     */
    @Transactional
    public void save(Long projectId, Long reqId, String targetType,
                     List<Long> targetIds, String userId) {
        traceRepository.deleteByProjectIdAndReqIdAndTargetType(projectId, reqId, targetType);
        for (Long targetId : targetIds) {
            ReqTraceability entity = new ReqTraceability();
            entity.setProjectId(projectId);
            entity.setReqId(reqId);
            entity.setTargetType(targetType);
            entity.setTargetId(targetId);
            entity.setRegId(userId);
            traceRepository.save(entity);
        }
    }
}
