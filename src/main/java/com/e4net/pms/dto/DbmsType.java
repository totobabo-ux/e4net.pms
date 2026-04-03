package com.e4net.pms.dto;

/**
 * 지원 DBMS 종류 — 접속 URL 및 쿼리 방언(dialect) 정의
 */
public enum DbmsType {

    MYSQL    ("MySQL",      3306, "com.mysql.cj.jdbc.Driver"),
    MARIADB  ("MariaDB",    3306, "org.mariadb.jdbc.Driver"),
    ORACLE   ("Oracle",     1521, "oracle.jdbc.OracleDriver"),
    SQLSERVER("SQL Server", 1433, "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    TIBERO   ("Tibero",     8629, "com.tmax.tibero.jdbc.TbDriver");

    private final String displayName;
    private final int defaultPort;
    private final String driverClass;

    DbmsType(String displayName, int defaultPort, String driverClass) {
        this.displayName = displayName;
        this.defaultPort  = defaultPort;
        this.driverClass  = driverClass;
    }

    public String displayName()  { return displayName; }
    public int    defaultPort()  { return defaultPort;  }
    public String driverClass()  { return driverClass;  }

    /** Oracle 계열 (Oracle / Tibero) 여부 */
    public boolean isOracleFamily() { return this == ORACLE || this == TIBERO; }
    /** MySQL 계열 (MySQL / MariaDB) 여부 */
    public boolean isMysqlFamily()  { return this == MYSQL  || this == MARIADB; }

    /**
     * JDBC 접속 URL 생성
     * @param host     호스트
     * @param port     포트
     * @param database MySQL/MariaDB/SQL Server: DB명, Oracle/Tibero: 서비스명(SID)
     */
    public String buildUrl(String host, int port, String database) {
        return switch (this) {
            case MYSQL -> String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&connectTimeout=5000",
                host, port, database);
            case MARIADB -> String.format(
                "jdbc:mariadb://%s:%d/%s?connectTimeout=5000&characterEncoding=UTF-8",
                host, port, database);
            case ORACLE -> String.format(
                "jdbc:oracle:thin:@//%s:%d/%s",
                host, port, database);
            case TIBERO -> String.format(
                "jdbc:tibero:thin:@%s:%d:%s",
                host, port, database);
            case SQLSERVER -> String.format(
                "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true;loginTimeout=5",
                host, port, database);
        };
    }

    /** 문자열로부터 DbmsType 파싱 (대소문자 무시) */
    public static DbmsType from(String name) {
        if (name == null || name.isBlank()) return MYSQL;
        return switch (name.trim().toUpperCase().replace(" ", "")) {
            case "MARIADB"           -> MARIADB;
            case "ORACLE"            -> ORACLE;
            case "SQLSERVER", "MSSQL" -> SQLSERVER;
            case "TIBERO"            -> TIBERO;
            default                  -> MYSQL;
        };
    }
}
