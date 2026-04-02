package com.e4net.pms.util;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 공통 엑셀 유틸리티 — 다운로드/업로드 공통 처리
 * 사용법:
 *   다운로드: ExcelUtil.createWorkbook(...) → ExcelUtil.writeToResponse(...)
 *   업로드:   ExcelUtil.parseRows(file, 1)  → 서비스 upsert 메서드에 전달
 */
public class ExcelUtil {

    private ExcelUtil() {}

    /** 헤더 배경색 (#2C3E50 — 프로젝트 primary 색상) */
    private static final byte[] HEADER_BG  = hexToBytes("2C3E50");
    /** 짝수 행 배경색 (연한 파랑) */
    private static final byte[] ALT_ROW_BG = hexToBytes("EAF4FB");

    /**
     * 스타일이 적용된 XSSFWorkbook 생성
     *
     * @param sheetName 시트명
     * @param headers   헤더 배열 (1행)
     * @param rows      데이터 행 목록 (각 행은 Object[])
     */
    public static XSSFWorkbook createWorkbook(String sheetName, String[] headers, List<Object[]> rows) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet(sheetName);

        // ── 헤더 스타일 ─────────────────────────────────────────
        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(new XSSFColor(hexToBytes("FFFFFF"), null));
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(HEADER_BG, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(headerStyle, BorderStyle.THIN);

        // ── 홀수 행 스타일 (흰색) ────────────────────────────────
        XSSFCellStyle normalStyle = wb.createCellStyle();
        normalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(normalStyle, BorderStyle.THIN);

        // ── 짝수 행 스타일 (연한 파랑) ────────────────────────────
        XSSFCellStyle altStyle = wb.createCellStyle();
        altStyle.setFillForegroundColor(new XSSFColor(ALT_ROW_BG, null));
        altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        altStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(altStyle, BorderStyle.THIN);

        // ── 헤더 행 생성 ─────────────────────────────────────────
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(22);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ── 데이터 행 생성 ───────────────────────────────────────
        for (int r = 0; r < rows.size(); r++) {
            Row row = sheet.createRow(r + 1);
            Object[] rowData = rows.get(r);
            XSSFCellStyle rowStyle = (r % 2 == 0) ? normalStyle : altStyle;
            for (int c = 0; c < rowData.length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellStyle(rowStyle);
                Object val = rowData[c];
                if (val == null) {
                    cell.setCellValue("");
                } else if (val instanceof Number) {
                    cell.setCellValue(((Number) val).doubleValue());
                } else {
                    cell.setCellValue(val.toString());
                }
            }
        }

        // ── 열 너비 자동 조절 (최소 3000, 최대 14000 단위) ──────────
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(Math.min(width + 512, 14000), 3000));
        }

        return wb;
    }

    /**
     * XSSFWorkbook을 HTTP 다운로드 응답으로 전송
     *
     * @param wb       워크북
     * @param fileName 다운로드 파일명 (확장자 .xlsx 포함)
     * @param response HttpServletResponse
     */
    public static void writeToResponse(XSSFWorkbook wb, String fileName,
                                       HttpServletResponse response) throws IOException {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        wb.write(response.getOutputStream());
        wb.close();
    }

    /**
     * 업로드된 엑셀 파일에서 데이터 행 파싱 (헤더 행 제외)
     *
     * @param file       업로드된 파일 (.xlsx 또는 .xls)
     * @param headerRows 건너뛸 헤더 행 수 (보통 1)
     * @return 각 행의 셀 값(String) 배열 목록 (완전 빈 행 제외)
     */
    public static List<String[]> parseRows(org.springframework.web.multipart.MultipartFile file,
                                           int headerRows) throws IOException {
        List<String[]> result = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            for (int r = headerRows; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                short lastCol = row.getLastCellNum();
                if (lastCol <= 0) continue;
                String[] cells = new String[lastCol];
                boolean hasData = false;
                for (int c = 0; c < lastCol; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String val = getCellStringValue(cell);
                    cells[c] = val;
                    if (!val.isBlank()) hasData = true;
                }
                if (hasData) result.add(cells);
            }
        }
        return result;
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d) && !Double.isInfinite(d))
                    ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) { yield String.valueOf((long) cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    private static void setBorder(CellStyle style, BorderStyle borderStyle) {
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
    }

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
