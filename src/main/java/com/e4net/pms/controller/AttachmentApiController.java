package com.e4net.pms.controller;

import com.e4net.pms.entity.AttachFile;
import com.e4net.pms.repository.AttachFileRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 첨부파일 목록 조회 REST API
 * 목록 화면에서 여러 엔티티의 첨부파일을 일괄 조회할 때 사용
 */
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentApiController {

    private final AttachFileRepository attachFileRepository;

    /**
     * 복수 엔티티의 첨부파일 목록 일괄 조회
     *
     * @param entityType 엔티티 타입 (COMMUNITY, ISSUE, RISK, REQUIREMENT, CUSTOMER_REPORT 등)
     * @param ids        엔티티 ID 목록 (콤마 구분)
     * @return Map<엔티티ID, List<{id, fileName}>>
     */
    @GetMapping
    public Map<Long, List<Map<String, Object>>> getAttachments(
            @RequestParam String entityType,
            @RequestParam String ids,
            HttpSession session) {

        if (session.getAttribute("loginUser") == null) return Collections.emptyMap();

        // ID 파싱
        List<Long> idList;
        try {
            idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return Collections.emptyMap();
        }
        if (idList.isEmpty()) return Collections.emptyMap();

        // 일괄 조회 후 entityId 기준 그룹핑
        List<AttachFile> files = attachFileRepository.findByEntityTypeAndEntityIdIn(entityType, idList);

        return files.stream().collect(Collectors.groupingBy(
                AttachFile::getEntityId,
                Collectors.mapping(f -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", f.getId());
                    m.put("fileName", f.getFileName() != null ? f.getFileName() : "파일");
                    m.put("fileSize", f.getFileSize());
                    return m;
                }, Collectors.toList())
        ));
    }
}
