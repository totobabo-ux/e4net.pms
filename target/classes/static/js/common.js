/* ============================================================
   e4net PMS - Common JavaScript
   ============================================================ */

// 사이드바 토글
document.addEventListener('DOMContentLoaded', function () {
    // 1단계: 사업관리 / 사업수행 / 관리자
    document.querySelectorAll('.menu-section-header').forEach(function (header) {
        header.addEventListener('click', function () {
            this.closest('.menu-section').classList.toggle('open');
        });
    });
    // 2단계: 표준관리 / 인력관리 등
    document.querySelectorAll('.menu-group-header').forEach(function (header) {
        header.addEventListener('click', function () {
            this.closest('.menu-group').classList.toggle('open');
        });
    });
});
