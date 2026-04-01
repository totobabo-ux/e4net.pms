/* ============================================================
   e4net PMS - Common JavaScript
   ============================================================ */

// 사이드바 토글
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.menu-group-header').forEach(function (header) {
        header.addEventListener('click', function () {
            this.closest('.menu-group').classList.toggle('open');
        });
    });
});
