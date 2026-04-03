package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 데이터베이스 접속 정보 요청 DTO
 */
@Getter
@Setter
public class DbConnectRequest {

    /** DBMS 종류 (MYSQL / MARIADB / ORACLE / SQLSERVER / TIBERO) */
    private String dbmsType = "MYSQL";

    /** 호스트 */
    private String host;

    /** 포트 */
    private int port;

    /**
     * 데이터베이스명 / 서비스명(Oracle·Tibero의 경우 서비스명 또는 SID)
     */
    private String database;

    /**
     * 스키마 (Oracle·Tibero 전용 — 비어 있으면 username 을 스키마로 사용)
     */
    private String schema;

    /** 사용자명 */
    private String username;

    /** 비밀번호 */
    private String password;

    /** 실제 쿼리에 사용할 스키마/DB명 반환 */
    public String effectiveSchema() {
        DbmsType type = DbmsType.from(dbmsType);
        if (type.isOracleFamily()) {
            return (schema != null && !schema.isBlank()) ? schema.trim() : username;
        }
        return database;
    }
}
