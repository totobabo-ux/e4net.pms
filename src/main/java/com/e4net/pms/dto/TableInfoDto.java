package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 테이블 기본 정보 DTO
 */
@Getter
@Setter
public class TableInfoDto {
    /** 테이블명 */
    private String tableName;
    /** 테이블 코멘트(설명) */
    private String comment;
    /** 예상 행 수 */
    private Long tableRows;
    /** 스토리지 엔진 */
    private String engine;
}
