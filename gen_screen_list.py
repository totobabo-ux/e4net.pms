# -*- coding: utf-8 -*-
import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter

wb = openpyxl.Workbook()
ws = wb.active
ws.title = "화면목록"

headers = ["No", "메뉴Level1", "메뉴Level2", "메뉴Level3", "분류", "화면명",
           "화면설명", "URL", "템플릿 파일", "연관 프로그램", "비고"]

header_fill = PatternFill(start_color="2C3E50", end_color="2C3E50", fill_type="solid")
header_font = Font(bold=True, color="FFFFFF", size=10)
center_align = Alignment(horizontal="center", vertical="center", wrap_text=True)
left_align   = Alignment(horizontal="left",   vertical="center", wrap_text=True)
thin = Side(style="thin", color="AAAAAA")
border = Border(left=thin, right=thin, top=thin, bottom=thin)

for col_idx, h in enumerate(headers, 1):
    cell = ws.cell(row=1, column=col_idx, value=h)
    cell.font = header_font
    cell.fill = header_fill
    cell.alignment = center_align
    cell.border = border

data = [
    (1,  "공통",    "-",         "-",            "조회", "로그인",         "사용자 인증 화면. 사번/비밀번호 입력 후 로그인",                            "GET /",                           "templates/index.html",                          "HomeController",                                                                  "비로그인 접근 허용"),
    (2,  "공통",    "-",         "-",            "등록", "회원가입",       "신규 사용자 등록 화면. 사번·이름·소속·연락처 입력",                          "GET /register",                   "templates/register.html",                       "UserController",                                                                  ""),
    (3,  "공통",    "-",         "-",            "조회", "프로젝트 선택",   "로그인 후 참여 중인 사업을 선택하는 화면",                                  "GET /project-select",             "templates/project-select.html",                 "HomeController",                                                                  "로그인 후 필수 진입"),
    (4,  "공통",    "-",         "-",            "조회", "사업 Home",     "선택된 사업의 주요 현황 요약 대시보드",                                     "GET /home",                       "templates/home.html",                           "HomeController",                                                                  ""),
    (5,  "사업관리", "표준관리",   "산출물 관리",   "목록", "산출물 목록",    "사업 산출물 전체 목록 조회. 구분·단계·산출물명 검색",                          "GET /deliverable",                "templates/deliverable/list.html",               "DeliverableController\nDeliverableService\nDeliverableRepository\nDeliverableSpec",  "엑셀 다운로드·업로드"),
    (6,  "사업관리", "표준관리",   "산출물 관리",   "등록", "산출물 등록",    "신규 산출물 정보(구분·분류·코드·산출물명 등) 입력 및 저장",                     "GET /deliverable/new",            "templates/deliverable/form.html",               "DeliverableController\nDeliverableService",                                        ""),
    (7,  "사업관리", "표준관리",   "산출물 관리",   "상세", "산출물 상세",    "산출물 상세 정보 조회. 인라인 수정 전환 가능",                               "GET /deliverable/{id}",           "templates/deliverable/detail.html",             "DeliverableController\nDeliverableService",                                        ""),
    (8,  "사업관리", "표준관리",   "산출물 관리",   "수정", "산출물 수정",    "등록된 산출물 정보 수정",                                                 "GET /deliverable/{id}/edit",      "templates/deliverable/form.html",               "DeliverableController\nDeliverableService",                                        "form.html 공용"),
    (9,  "사업관리", "인력관리",   "투입인력 관리", "목록", "인력 목록",     "사업 투입 인력 목록 조회. 역할·급수·소속 검색",                               "GET /manpower",                   "templates/manpower/list.html",                  "ManpowerController\nManpowerService\nManpowerRepository\nManpowerSpec",             ""),
    (10, "사업관리", "인력관리",   "투입인력 관리", "등록", "인력 등록",     "투입 인력 정보(이름·역할·급수·투입기간 등) 등록",                             "GET /manpower/new",               "templates/manpower/form.html",                  "ManpowerController\nManpowerService",                                              ""),
    (11, "사업관리", "인력관리",   "투입인력 관리", "상세", "인력 상세",     "투입 인력 상세 정보 조회",                                                 "GET /manpower/{id}",              "templates/manpower/detail.html",                "ManpowerController\nManpowerService",                                              ""),
    (12, "사업관리", "인력관리",   "투입인력 관리", "수정", "인력 수정",     "투입 인력 정보 수정",                                                      "GET /manpower/{id}/edit",         "templates/manpower/form.html",                  "ManpowerController\nManpowerService",                                              "form.html 공용"),
    (13, "사업관리", "범위관리",   "요구사항 관리", "목록", "요구사항 목록",  "사업 요구사항 전체 목록. 유형·우선순위·상태 검색",                             "GET /requirement",                "templates/requirement/list.html",               "RequirementController\nRequirementService\nRequirementRepository\nRequirementSpec",  "엑셀 다운로드, 파일 첨부"),
    (14, "사업관리", "범위관리",   "요구사항 관리", "등록", "요구사항 등록",  "요구사항 정보(유형·우선순위·담당자 등) 및 첨부파일 등록",                        "GET /requirement/new",            "templates/requirement/form.html",               "RequirementController\nRequirementService\nAttachFileRepository",                   "다중 파일 첨부"),
    (15, "사업관리", "범위관리",   "요구사항 관리", "상세", "요구사항 상세",  "요구사항 상세 조회 및 인라인 수정, 첨부파일 다운로드",                           "GET /requirement/{id}",           "templates/requirement/detail.html",             "RequirementController\nRequirementService\nAttachFileRepository",                   "파일 다운로드"),
    (16, "사업관리", "범위관리",   "사업일정(WBS)", "목록", "WBS 목록/관리", "사업 WBS 트리 구조 조회 및 인라인 편집·일정 관리",                             "GET /wbs",                        "templates/wbs/list.html",                       "WbsController\nWbsService\nWbsRepository",                                         "인라인 편집, 엑셀 다운로드"),
    (17, "사업관리", "보고관리",   "주간보고",      "목록", "주간보고 목록",  "주간보고 목록 조회. 보고서명·보고일 검색",                                     "GET /weekly-report",              "templates/weekly-report/list.html",             "WeeklyReportController\nCustomerReportService\nCustomerReportRepository",           ""),
    (18, "사업관리", "보고관리",   "주간보고",      "등록", "주간보고 등록",  "주간보고 정보(보고서명·내용·첨부파일) 등록",                                   "GET /weekly-report/new",          "templates/weekly-report/form.html",             "WeeklyReportController\nCustomerReportService\nAttachFileRepository",               "다중 파일 첨부"),
    (19, "사업관리", "보고관리",   "주간보고",      "상세", "주간보고 상세",  "주간보고 상세 조회, 인라인 수정, 첨부파일 다운로드",                             "GET /weekly-report/{id}",         "templates/weekly-report/detail.html",           "WeeklyReportController\nCustomerReportService\nAttachFileRepository",               "파일 다운로드"),
    (20, "사업관리", "보고관리",   "주간보고",      "수정", "주간보고 수정",  "주간보고 정보 수정 및 첨부파일 추가/삭제",                                     "GET /weekly-report/{id}/edit",    "templates/weekly-report/form.html",             "WeeklyReportController\nCustomerReportService",                                    "form.html 공용"),
    (21, "사업관리", "보고관리",   "월간보고",      "목록", "월간보고 목록",  "월간보고 목록 조회. 보고서명·보고일 검색",                                     "GET /monthly-report",             "templates/monthly-report/list.html",            "MonthlyReportController\nCustomerReportService\nCustomerReportRepository",          ""),
    (22, "사업관리", "보고관리",   "월간보고",      "등록", "월간보고 등록",  "월간보고 정보(보고서명·내용·첨부파일) 등록",                                   "GET /monthly-report/new",         "templates/monthly-report/form.html",            "MonthlyReportController\nCustomerReportService\nAttachFileRepository",              "다중 파일 첨부"),
    (23, "사업관리", "보고관리",   "월간보고",      "상세", "월간보고 상세",  "월간보고 상세 조회, 인라인 수정, 첨부파일 다운로드",                             "GET /monthly-report/{id}",        "templates/monthly-report/detail.html",          "MonthlyReportController\nCustomerReportService\nAttachFileRepository",              "파일 다운로드"),
    (24, "사업관리", "보고관리",   "월간보고",      "수정", "월간보고 수정",  "월간보고 정보 수정 및 첨부파일 추가/삭제",                                     "GET /monthly-report/{id}/edit",   "templates/monthly-report/form.html",            "MonthlyReportController\nCustomerReportService",                                   "form.html 공용"),
    (25, "사업관리", "보고관리",   "정기보고",      "목록", "정기보고 목록",  "정기보고 목록 조회. 보고서명·보고일 검색",                                     "GET /regular-report",             "templates/regular-report/list.html",            "RegularReportController\nCustomerReportService\nCustomerReportRepository",          ""),
    (26, "사업관리", "보고관리",   "정기보고",      "등록", "정기보고 등록",  "정기보고 정보(보고서명·내용·첨부파일) 등록",                                   "GET /regular-report/new",         "templates/regular-report/form.html",            "RegularReportController\nCustomerReportService\nAttachFileRepository",              "다중 파일 첨부"),
    (27, "사업관리", "보고관리",   "정기보고",      "상세", "정기보고 상세",  "정기보고 상세 조회, 인라인 수정, 첨부파일 다운로드",                             "GET /regular-report/{id}",        "templates/regular-report/detail.html",          "RegularReportController\nCustomerReportService\nAttachFileRepository",              "파일 다운로드"),
    (28, "사업관리", "보고관리",   "정기보고",      "수정", "정기보고 수정",  "정기보고 정보 수정 및 첨부파일 추가/삭제",                                     "GET /regular-report/{id}/edit",   "templates/regular-report/form.html",            "RegularReportController\nCustomerReportService",                                   "form.html 공용"),
    (29, "사업관리", "보고관리",   "회의록",        "목록", "회의록 목록",   "회의록 목록 조회. 회의명·회의일 검색",                                         "GET /meeting-report",             "templates/meeting-report/list.html",            "MeetingReportController\nCustomerReportService\nCustomerReportRepository",          ""),
    (30, "사업관리", "보고관리",   "회의록",        "등록", "회의록 등록",   "회의록 정보(회의명·내용·참석자·첨부파일) 등록",                                  "GET /meeting-report/new",         "templates/meeting-report/form.html",            "MeetingReportController\nCustomerReportService\nAttachFileRepository",              "다중 파일 첨부"),
    (31, "사업관리", "보고관리",   "회의록",        "상세", "회의록 상세",   "회의록 상세 조회, 인라인 수정, 첨부파일 다운로드",                               "GET /meeting-report/{id}",        "templates/meeting-report/detail.html",          "MeetingReportController\nCustomerReportService\nAttachFileRepository",              "파일 다운로드"),
    (32, "사업관리", "보고관리",   "회의록",        "수정", "회의록 수정",   "회의록 정보 수정 및 첨부파일 추가/삭제",                                        "GET /meeting-report/{id}/edit",   "templates/meeting-report/form.html",            "MeetingReportController\nCustomerReportService",                                   "form.html 공용"),
    (33, "사업관리", "위험관리",   "위험관리",      "목록", "리스크 목록",   "사업 리스크 목록 조회. 유형·심각도·상태 검색",                                   "GET /risk",                       "templates/risk/list.html",                      "RiskController\nRiskService\nRiskRepository\nRiskSpec",                             "엑셀 다운로드, 파일 첨부"),
    (34, "사업관리", "위험관리",   "위험관리",      "등록", "리스크 등록",   "리스크 정보(유형·발생확률·영향도·대응방안 등) 등록",                             "GET /risk/new",                   "templates/risk/form.html",                      "RiskController\nRiskService\nAttachFileRepository",                                "다중 파일 첨부"),
    (35, "사업관리", "위험관리",   "위험관리",      "상세", "리스크 상세",   "리스크 상세 조회, 인라인 수정, 첨부파일 다운로드",                               "GET /risk/{id}",                  "templates/risk/detail.html",                    "RiskController\nRiskService\nAttachFileRepository",                                "파일 다운로드"),
    (36, "사업관리", "위험관리",   "이슈관리",      "목록", "이슈 목록",    "사업 이슈 목록 조회. 유형·우선순위·상태 검색",                                   "GET /issue",                      "templates/issue/list.html",                     "IssueController\nIssueService\nIssueRepository\nIssueSpec",                         "엑셀 다운로드, 파일 첨부"),
    (37, "사업관리", "위험관리",   "이슈관리",      "등록", "이슈 등록",    "이슈 정보(유형·우선순위·담당자·조치내용 등) 등록",                               "GET /issue/new",                  "templates/issue/form.html",                     "IssueController\nIssueService\nAttachFileRepository",                              "다중 파일 첨부"),
    (38, "사업관리", "위험관리",   "이슈관리",      "상세", "이슈 상세",    "이슈 상세 조회, 인라인 수정, 첨부파일 다운로드",                                 "GET /issue/{id}",                 "templates/issue/detail.html",                   "IssueController\nIssueService\nAttachFileRepository",                              "파일 다운로드"),
    (39, "사업수행", "설계",      "화면목록",      "목록", "화면목록 목록", "설계 산출물 중 화면목록 전체 조회. 메뉴·분류·화면명 검색",                          "GET /screen-list",                "templates/screen-list/list.html",               "ScreenListController\nScreenListService\nScreenListRepository\nScreenListSpec",     "엑셀 다운로드·업로드, 파일 첨부"),
    (40, "사업수행", "설계",      "화면목록",      "등록", "화면목록 등록", "화면목록 항목(메뉴Level·분류·화면명·URL·템플릿 등) 등록",                          "GET /screen-list/new",            "templates/screen-list/form.html",               "ScreenListController\nScreenListService\nAttachFileRepository",                     "다중 파일 첨부"),
    (41, "사업수행", "설계",      "화면목록",      "상세", "화면목록 상세", "화면목록 항목 상세 조회, 인라인 수정, 첨부파일 다운로드",                           "GET /screen-list/{id}",           "templates/screen-list/detail.html",             "ScreenListController\nScreenListService\nAttachFileRepository",                     "파일 다운로드"),
    (42, "사업수행", "설계",      "화면목록",      "수정", "화면목록 수정", "화면목록 항목 수정 및 첨부파일 추가/삭제",                                        "GET /screen-list/{id}/edit",      "templates/screen-list/form.html",               "ScreenListController\nScreenListService",                                          "form.html 공용"),
    (43, "커뮤니티", "-",        "공지사항",      "목록", "공지사항 목록", "사업 공지사항 목록 조회. 제목 검색",                                             "GET /notice",                     "templates/notice/list.html",                    "NoticeController\nCommunityService\nCommunityRepository",                           "파일 첨부"),
    (44, "커뮤니티", "-",        "공지사항",      "등록", "공지사항 등록", "공지사항 제목·내용·첨부파일 등록",                                               "GET /notice/new",                 "templates/notice/form.html",                    "NoticeController\nCommunityService\nAttachFileRepository",                          "다중 파일 첨부"),
    (45, "커뮤니티", "-",        "공지사항",      "상세", "공지사항 상세", "공지사항 상세 조회, 첨부파일 다운로드",                                           "GET /notice/{id}",                "templates/notice/detail.html",                  "NoticeController\nCommunityService\nAttachFileRepository",                          "파일 다운로드"),
    (46, "커뮤니티", "-",        "자료실",        "목록", "자료실 목록",   "자료실 파일 목록 조회. 제목 검색",                                               "GET /archive",                    "templates/archive/list.html",                   "ArchiveController\nCommunityService\nCommunityRepository",                          "파일 첨부"),
    (47, "커뮤니티", "-",        "자료실",        "등록", "자료실 등록",   "자료 제목·내용·첨부파일 등록",                                                   "GET /archive/new",                "templates/archive/form.html",                   "ArchiveController\nCommunityService\nAttachFileRepository",                         "다중 파일 첨부"),
    (48, "커뮤니티", "-",        "자료실",        "상세", "자료실 상세",   "자료 상세 조회, 첨부파일 다운로드",                                               "GET /archive/{id}",               "templates/archive/detail.html",                 "ArchiveController\nCommunityService\nAttachFileRepository",                         "파일 다운로드"),
    (49, "관리자",   "사업 관리",  "사업 목록",    "목록", "사업 목록",    "전체 사업 목록 조회. 사업명·상태 검색",                                           "GET /projects",                   "templates/project/list.html",                   "ProjectController\nProjectService\nProjectRepository\nProjectSpec",                 ""),
    (50, "관리자",   "사업 관리",  "사업 목록",    "등록", "사업 등록",    "신규 사업 정보(사업명·기간·발주처 등) 등록",                                       "GET /projects/new",               "templates/project/form.html",                   "ProjectController\nProjectService",                                                ""),
    (51, "관리자",   "사업 관리",  "사업 목록",    "수정", "사업 수정",    "등록된 사업 정보 수정",                                                          "GET /projects/{id}/edit",         "templates/project/form.html",                   "ProjectController\nProjectService",                                                "form.html 공용"),
    (52, "관리자",   "사용자 관리", "사용자 목록",  "목록", "사용자 목록",  "전체 사용자 목록 조회 및 관리",                                                   "GET /admin/users",                "templates/admin/user-list.html",                "UserAdminController",                                                              "관리자 전용"),
    (53, "관리자",   "사용자 관리", "사용자 목록",  "수정", "사용자 수정",  "사용자 정보(역할·상태 등) 수정",                                                  "GET /admin/users/{id}/edit",      "templates/admin/user-form.html",                "UserAdminController",                                                              "관리자 전용"),
    (54, "관리자",   "시스템 관리", "DB 정보",     "조회", "DB 정보 조회", "연결된 데이터베이스 테이블 목록 및 컬럼 정보 조회",                                  "GET /admin/system/database",      "templates/admin/system/database.html",          "SystemInfoController\nSystemInfoService",                                           "관리자 전용"),
]

col_widths = [5, 12, 12, 14, 7, 16, 38, 32, 36, 38, 18]
for col_idx, w in enumerate(col_widths, 1):
    ws.column_dimensions[get_column_letter(col_idx)].width = w
ws.row_dimensions[1].height = 22

category_colors = {
    "공통":    "EBF5FB",
    "사업관리": "EAF4EA",
    "사업수행": "FEF9E7",
    "커뮤니티": "F5EEF8",
    "관리자":  "FDFEFE",
}

for row_data in data:
    row_idx = row_data[0] + 1
    menu1 = row_data[1]
    fill_color = category_colors.get(menu1, "FFFFFF")
    row_fill = PatternFill(start_color=fill_color, end_color=fill_color, fill_type="solid")

    for col_idx, value in enumerate(row_data, 1):
        cell = ws.cell(row=row_idx, column=col_idx, value=value)
        cell.border = border
        cell.fill = row_fill
        cell.font = Font(size=9)
        if col_idx in (1, 5):
            cell.alignment = center_align
        else:
            cell.alignment = left_align

    ws.row_dimensions[row_idx].height = 45

ws.freeze_panes = "A2"
ws.auto_filter.ref = "A1:" + get_column_letter(len(headers)) + "1"

output_path = r"C:\Users\ilhun\Documents\e4net.pms\e4net_PMS_화면목록.xlsx"
wb.save(output_path)
print("저장 완료: " + output_path)
