package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공통 첨부파일 DTO
 * attach_file 테이블 조회 결과를 화면에 전달하는 공통 DTO
 */
@Getter @Setter @NoArgsConstructor
public class AttachFileDto {

    private Long   id;
    private String fileName;        // 원본 파일명
    private Long   fileSize;        // 파일 크기 (bytes)
    private String fileSizeDisplay; // 표시용 크기 (예: "1.2 MB")
}
