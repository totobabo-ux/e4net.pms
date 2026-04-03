package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 테이블 컬럼 상세 정보 DTO
 */
@Getter
@Setter
public class ColumnInfoDto {
    /** 컬럼 순서 */
    private int ordinalPosition;
    /** 컬럼명 */
    private String columnName;
    /** 데이터 타입 */
    private String columnType;
    /** NULL 허용 여부 */
    private String isNullable;
    /** 기본값 */
    private String columnDefault;
    /** 키 종류 (PRI/UNI/MUL) */
    private String columnKey;
    /** EXTRA (auto_increment 등) */
    private String extra;
    /** 컬럼 설명 (Comment) */
    private String columnComment;
}
