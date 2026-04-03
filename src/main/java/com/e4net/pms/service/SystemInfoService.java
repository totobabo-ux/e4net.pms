package com.e4net.pms.service;

import com.e4net.pms.dto.ColumnInfoDto;
import com.e4net.pms.dto.DbConnectRequest;
import com.e4net.pms.dto.DbmsType;
import com.e4net.pms.dto.TableInfoDto;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 시스템 정보 서비스 — 다중 DBMS 접속, 테이블/컬럼 조회, 설계서 엑셀 생성
 */
@Service
public class SystemInfoService {

    private static final byte[] HEADER_BG  = hexToBytes("2C3E50");
    private static final byte[] ALT_ROW_BG = hexToBytes("EAF4FB");

    // ── DB 연결 ────────────────────────────────────────────────────────────────

    private Connection connect(DbConnectRequest req) throws SQLException {
        DbmsType type = DbmsType.from(req.getDbmsType());
        String url = type.buildUrl(req.getHost(), req.getPort(), req.getDatabase());
        return DriverManager.getConnection(url, req.getUsername(), req.getPassword());
    }

    // ── 테이블 목록 조회 ────────────────────────────────────────────────────────

    public List<TableInfoDto> getTables(DbConnectRequest req) throws SQLException {
        try (Connection conn = connect(req)) {
            return fetchTables(conn, req);
        }
    }

    private List<TableInfoDto> fetchTables(Connection conn, DbConnectRequest req) throws SQLException {
        DbmsType type  = DbmsType.from(req.getDbmsType());
        String schema  = req.effectiveSchema();
        List<TableInfoDto> list = new ArrayList<>();

        String sql = tableListSql(type);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TableInfoDto t = new TableInfoDto();
                    t.setTableName(rs.getString("TABLE_NAME"));
                    t.setComment(nvl(rs.getString("TABLE_COMMENT")));
                    t.setTableRows(rs.getLong("TABLE_ROWS"));
                    t.setEngine(nvl(rs.getString("ENGINE")));
                    list.add(t);
                }
            }
        }
        return list;
    }

    // ── 컬럼 목록 조회 ──────────────────────────────────────────────────────────

    public List<ColumnInfoDto> getColumns(DbConnectRequest req, String tableName) throws SQLException {
        DbmsType type = DbmsType.from(req.getDbmsType());
        String schema = req.effectiveSchema();

        try (Connection conn = connect(req)) {
            return fetchColumns(conn, type, schema, tableName);
        }
    }

    private List<ColumnInfoDto> fetchColumns(Connection conn, DbmsType type,
                                              String schema, String tableName) throws SQLException {
        List<ColumnInfoDto> list = new ArrayList<>();
        String sql = columnListSql(type);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Oracle·Tibero: param1=schema, param2=tableName
            // 나머지:         param1=schema, param2=tableName
            ps.setString(1, type.isOracleFamily() ? schema.toUpperCase() : schema);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ColumnInfoDto c = new ColumnInfoDto();
                    c.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
                    c.setColumnName(rs.getString("COLUMN_NAME"));
                    c.setColumnType(nvl(rs.getString("COLUMN_TYPE")));
                    c.setIsNullable(nvl(rs.getString("IS_NULLABLE")));   // 정규화: YES/NO
                    c.setColumnDefault(nvl(rs.getString("COLUMN_DEFAULT")));
                    c.setColumnKey(nvl(rs.getString("COLUMN_KEY")));
                    c.setExtra(nvl(rs.getString("EXTRA")));
                    c.setColumnComment(nvl(rs.getString("COLUMN_COMMENT")));
                    list.add(c);
                }
            }
        }
        return list;
    }

    // ── 테이블 목록 엑셀 다운로드 ──────────────────────────────────────────────

    public void downloadTableList(DbConnectRequest req, HttpServletResponse response)
            throws SQLException, IOException {

        try (Connection conn = connect(req);
             XSSFWorkbook wb = new XSSFWorkbook()) {

            List<TableInfoDto> tables = fetchTables(conn, req);

            XSSFCellStyle hdrStyle  = makeHeaderStyle(wb, HEADER_BG);
            XSSFCellStyle normStyle = makeNormalStyle(wb, null);
            XSSFCellStyle altStyle  = makeNormalStyle(wb, ALT_ROW_BG);

            XSSFSheet sheet = wb.createSheet("테이블 목록");
            String[] headers = {"NO", "DB명", "주제영역", "테이블ID", "테이블명", "비고"};

            Row hRow = sheet.createRow(0);
            hRow.setHeightInPoints(22);
            for (int c = 0; c < headers.length; c++) {
                Cell cell = hRow.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(hdrStyle);
            }

            for (int i = 0; i < tables.size(); i++) {
                TableInfoDto t  = tables.get(i);
                XSSFCellStyle s = (i % 2 == 0) ? normStyle : altStyle;
                Row row = sheet.createRow(i + 1);
                setCell(row, 0, i + 1,               s);
                setCell(row, 1, req.effectiveSchema(), s);
                setCell(row, 2, t.getComment(),        s);
                setCell(row, 3, t.getTableName(),      s);
                setCell(row, 4, t.getComment(),        s);
                setCell(row, 5, "",                    s);
            }
            autoSize(sheet, headers.length);

            writeResponse(wb, req.effectiveSchema() + "_테이블목록.xlsx", response);
        }
    }

    // ── 테이블설계서 엑셀 다운로드 (단일 시트, 전 컬럼) ────────────────────────

    public void downloadTableDesign(DbConnectRequest req, HttpServletResponse response)
            throws SQLException, IOException {

        DbmsType type = DbmsType.from(req.getDbmsType());

        try (Connection conn = connect(req);
             XSSFWorkbook wb = new XSSFWorkbook()) {

            List<TableInfoDto> tables = fetchTables(conn, req);

            XSSFCellStyle hdrStyle     = makeHeaderStyle(wb, HEADER_BG);
            XSSFCellStyle normStyle    = makeNormalStyle(wb, null);
            XSSFCellStyle altStyle     = makeNormalStyle(wb, ALT_ROW_BG);
            XSSFCellStyle normCtrStyle = makeNormalStyle(wb, null);
            normCtrStyle.setAlignment(HorizontalAlignment.CENTER);
            XSSFCellStyle altCtrStyle  = makeNormalStyle(wb, ALT_ROW_BG);
            altCtrStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFSheet sheet = wb.createSheet("컬럼설계서");
            String[] headers = {
                "주제영역", "테이블ID", "테이블명", "컬럼ID", "컬럼명",
                "컬럼순서", "DATA TYPE", "길이",
                "NOT NULL", "PK", "FK",
                "개인정보여부", "암호화여부", "공개/비공개여부", "비 고"
            };
            Row hRow = sheet.createRow(0);
            hRow.setHeightInPoints(24);
            for (int c = 0; c < headers.length; c++) {
                Cell cell = hRow.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(hdrStyle);
            }

            String schema = req.effectiveSchema();
            int rowIdx = 1;

            for (TableInfoDto t : tables) {
                String tableComment = t.getComment();
                List<ColumnInfoDto> cols = fetchColumns(conn, type, schema, t.getTableName());

                for (ColumnInfoDto c : cols) {
                    boolean even = (rowIdx % 2 == 0);
                    XSSFCellStyle s  = even ? normStyle    : altStyle;
                    XSSFCellStyle sC = even ? normCtrStyle : altCtrStyle;
                    Row row = sheet.createRow(rowIdx++);

                    setCell(row,  0, tableComment,          s);
                    setCell(row,  1, t.getTableName(),      s);
                    setCell(row,  2, tableComment,          s);
                    setCell(row,  3, c.getColumnName(),     s);
                    setCell(row,  4, c.getColumnComment(),  s);
                    setCell(row,  5, c.getOrdinalPosition(), sC);
                    setCell(row,  6, extractType(c.getColumnType()),   s);
                    setCell(row,  7, extractLength(c.getColumnType()), sC);
                    setCell(row,  8, "NO".equals(c.getIsNullable())   ? "Y" : "N", sC);
                    setCell(row,  9, "PRI".equals(c.getColumnKey())   ? "Y" : "N", sC);
                    setCell(row, 10, "MUL".equals(c.getColumnKey())   ? "Y" : "N", sC);
                    setCell(row, 11, "N",    sC);
                    setCell(row, 12, "N",    sC);
                    setCell(row, 13, "공개", sC);
                    setCell(row, 14, "",     s);
                }
            }
            autoSize(sheet, headers.length);

            writeResponse(wb, schema + "_테이블설계서.xlsx", response);
        }
    }

    // ── DBMS별 SQL 방언 ────────────────────────────────────────────────────────

    /**
     * 테이블 목록 조회 SQL — 파라미터: (1) schema/owner
     */
    private String tableListSql(DbmsType type) {
        if (type.isMysqlFamily()) {
            return """
                SELECT TABLE_NAME, TABLE_COMMENT, TABLE_ROWS, ENGINE
                  FROM INFORMATION_SCHEMA.TABLES
                 WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'
                 ORDER BY TABLE_NAME
                """;
        }
        if (type.isOracleFamily()) {
            return """
                SELECT t.TABLE_NAME,
                       NVL(c.COMMENTS, '')  AS TABLE_COMMENT,
                       NVL(t.NUM_ROWS, 0)   AS TABLE_ROWS,
                       ''                   AS ENGINE
                  FROM ALL_TABLES t
                  LEFT JOIN ALL_TAB_COMMENTS c
                    ON t.TABLE_NAME = c.TABLE_NAME AND t.OWNER = c.OWNER
                 WHERE t.OWNER = UPPER(?)
                 ORDER BY t.TABLE_NAME
                """;
        }
        // SQL Server
        return """
            SELECT t.TABLE_NAME,
                   ISNULL(CAST(ep.value AS NVARCHAR(MAX)), '') AS TABLE_COMMENT,
                   0   AS TABLE_ROWS,
                   ''  AS ENGINE
              FROM INFORMATION_SCHEMA.TABLES t
              LEFT JOIN sys.extended_properties ep
                ON  ep.major_id = OBJECT_ID(t.TABLE_SCHEMA + '.' + t.TABLE_NAME)
                AND ep.minor_id = 0
                AND ep.class    = 1
                AND ep.name     = 'MS_Description'
             WHERE t.TABLE_CATALOG = ? AND t.TABLE_TYPE = 'BASE TABLE'
             ORDER BY t.TABLE_NAME
            """;
    }

    /**
     * 컬럼 목록 조회 SQL — 파라미터: (1) schema/owner, (2) tableName
     * 반환 컬럼명을 통일: ORDINAL_POSITION, COLUMN_NAME, COLUMN_TYPE,
     *                     IS_NULLABLE(YES/NO), COLUMN_DEFAULT, COLUMN_KEY(PRI/MUL/UNI/''),
     *                     EXTRA, COLUMN_COMMENT
     */
    private String columnListSql(DbmsType type) {
        if (type.isMysqlFamily()) {
            return """
                SELECT ORDINAL_POSITION, COLUMN_NAME, COLUMN_TYPE,
                       IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, COLUMN_COMMENT
                  FROM INFORMATION_SCHEMA.COLUMNS
                 WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                 ORDER BY ORDINAL_POSITION
                """;
        }
        if (type.isOracleFamily()) {
            // IS_NULLABLE: Oracle NULLABLE='N' → 'NO', 'Y' → 'YES' 로 정규화
            // COLUMN_TYPE: 타입+길이 조합
            // COLUMN_KEY: PK·FK·UNI → PRI·MUL·UNI
            return """
                SELECT
                    c.COLUMN_ID  AS ORDINAL_POSITION,
                    c.COLUMN_NAME,
                    CASE
                        WHEN c.DATA_TYPE IN ('VARCHAR2','CHAR','NVARCHAR2','NCHAR','RAW')
                            THEN c.DATA_TYPE || '(' || c.DATA_LENGTH || ')'
                        WHEN c.DATA_TYPE = 'NUMBER' AND c.DATA_PRECISION IS NOT NULL
                            THEN c.DATA_TYPE || '(' || c.DATA_PRECISION
                                 || CASE WHEN c.DATA_SCALE > 0 THEN ',' || c.DATA_SCALE ELSE '' END
                                 || ')'
                        ELSE c.DATA_TYPE
                    END AS COLUMN_TYPE,
                    CASE WHEN c.NULLABLE = 'N' THEN 'NO' ELSE 'YES' END AS IS_NULLABLE,
                    c.DATA_DEFAULT AS COLUMN_DEFAULT,
                    NVL((
                        SELECT MAX(CASE con.CONSTRAINT_TYPE
                                       WHEN 'P' THEN 'PRI'
                                       WHEN 'R' THEN 'MUL'
                                       WHEN 'U' THEN 'UNI'
                                   END)
                          FROM ALL_CONS_COLUMNS cc
                          JOIN ALL_CONSTRAINTS  con
                            ON cc.CONSTRAINT_NAME = con.CONSTRAINT_NAME
                           AND cc.OWNER           = con.OWNER
                         WHERE cc.OWNER       = c.OWNER
                           AND cc.TABLE_NAME  = c.TABLE_NAME
                           AND cc.COLUMN_NAME = c.COLUMN_NAME
                           AND con.CONSTRAINT_TYPE IN ('P','R','U')
                    ), '') AS COLUMN_KEY,
                    '' AS EXTRA,
                    NVL((
                        SELECT cm.COMMENTS
                          FROM ALL_COL_COMMENTS cm
                         WHERE cm.OWNER       = c.OWNER
                           AND cm.TABLE_NAME  = c.TABLE_NAME
                           AND cm.COLUMN_NAME = c.COLUMN_NAME
                    ), '') AS COLUMN_COMMENT
                FROM ALL_TAB_COLUMNS c
                WHERE c.OWNER = UPPER(?) AND c.TABLE_NAME = ?
                ORDER BY c.COLUMN_ID
                """;
        }
        // SQL Server — COLUMN_TYPE = 타입+길이, COLUMN_COMMENT = MS_Description
        return """
            SELECT
                c.ORDINAL_POSITION,
                c.COLUMN_NAME,
                CASE
                    WHEN c.DATA_TYPE IN ('varchar','nvarchar','char','nchar','varbinary','binary')
                        THEN c.DATA_TYPE + '(' +
                             CASE WHEN c.CHARACTER_MAXIMUM_LENGTH = -1
                                  THEN 'MAX'
                                  ELSE CAST(c.CHARACTER_MAXIMUM_LENGTH AS VARCHAR)
                             END + ')'
                    WHEN c.DATA_TYPE IN ('decimal','numeric')
                        THEN c.DATA_TYPE + '(' + CAST(c.NUMERIC_PRECISION AS VARCHAR)
                             + ',' + CAST(c.NUMERIC_SCALE AS VARCHAR) + ')'
                    ELSE c.DATA_TYPE
                END AS COLUMN_TYPE,
                c.IS_NULLABLE,
                c.COLUMN_DEFAULT,
                '' AS COLUMN_KEY,
                '' AS EXTRA,
                ISNULL(CAST(ep.value AS NVARCHAR(MAX)), '') AS COLUMN_COMMENT
            FROM INFORMATION_SCHEMA.COLUMNS c
            LEFT JOIN sys.extended_properties ep
                ON  ep.major_id  = OBJECT_ID(c.TABLE_SCHEMA + '.' + c.TABLE_NAME)
                AND ep.minor_id  = COLUMNPROPERTY(
                        OBJECT_ID(c.TABLE_SCHEMA + '.' + c.TABLE_NAME),
                        c.COLUMN_NAME, 'ColumnId')
                AND ep.class = 1
                AND ep.name  = 'MS_Description'
            WHERE c.TABLE_CATALOG = ? AND c.TABLE_NAME = ?
            ORDER BY c.ORDINAL_POSITION
            """;
    }

    // ── DATA TYPE 파싱 ─────────────────────────────────────────────────────────

    /** "varchar(50)" → "varchar" */
    private String extractType(String columnType) {
        if (columnType == null) return "";
        int p = columnType.indexOf('(');
        return p < 0 ? columnType.trim() : columnType.substring(0, p).trim();
    }

    /** "varchar(50)" → "50",  "decimal(10,0)" → "10,0",  "int" → "" */
    private String extractLength(String columnType) {
        if (columnType == null) return "";
        int s = columnType.indexOf('(');
        int e = columnType.lastIndexOf(')');
        if (s < 0 || e <= s) return "";
        return columnType.substring(s + 1, e).trim();
    }

    // ── 공통 Excel 헬퍼 ────────────────────────────────────────────────────────

    private XSSFCellStyle makeHeaderStyle(XSSFWorkbook wb, byte[] bgRgb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(new XSSFColor(hexToBytes("FFFFFF"), null));
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(bgRgb, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style, BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle makeNormalStyle(XSSFWorkbook wb, byte[] bgRgb) {
        XSSFCellStyle style = wb.createCellStyle();
        if (bgRgb != null) {
            style.setFillForegroundColor(new XSSFColor(bgRgb, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style, BorderStyle.THIN);
        return style;
    }

    private void setCell(Row row, int col, Object val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(style);
        if (val == null) {
            cell.setCellValue("");
        } else if (val instanceof Number) {
            cell.setCellValue(((Number) val).doubleValue());
        } else {
            cell.setCellValue(val.toString());
        }
    }

    private void autoSize(XSSFSheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
            int w = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(Math.min(w + 512, 15000), 3000));
        }
    }

    private void setBorder(CellStyle style, BorderStyle bs) {
        style.setBorderTop(bs);
        style.setBorderBottom(bs);
        style.setBorderLeft(bs);
        style.setBorderRight(bs);
    }

    private void writeResponse(XSSFWorkbook wb, String fileName,
                               HttpServletResponse response) throws IOException {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        wb.write(response.getOutputStream());
    }

    private String nvl(String val) { return val == null ? "" : val; }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
