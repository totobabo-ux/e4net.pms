/* ============================================================
   e4net PMS - Common JavaScript
   ============================================================ */

/* ── 첨부파일 업로드 컴포넌트 ──────────────────────────────────
 * 사용법: initAttachUpload('inputId', 'pendingListId', 'formId')
 *   inputId      : <input type="file" name="attachFiles" ...> 의 id
 *   pendingListId: 추가 파일 목록을 렌더링할 컨테이너의 id
 *   formId       : 제출할 <form> 의 id
 * ─────────────────────────────────────────────────────────── */
function initAttachUpload(inputId, pendingListId, formId) {
    var input = document.getElementById(inputId);
    var list  = document.getElementById(pendingListId);
    var form  = document.getElementById(formId);
    if (!input || !list) return;

    // File 객체 자체가 아닌 메타정보 + File 참조를 함께 저장
    var pendingFiles = [];   // [{ file: File, name: string, size: number }]

    // 파일 선택 이벤트
    input.addEventListener('change', function () {
        var selectedCount = this.files.length;
        console.log('[attach] change 이벤트 - 선택된 파일 수:', selectedCount);

        for (var i = 0; i < selectedCount; i++) {
            var f    = this.files[i];
            var name = f.name;
            var size = f.size;
            var dup  = pendingFiles.some(function (p) { return p.name === name && p.size === size; });
            if (!dup) {
                pendingFiles.push({ file: f, name: name, size: size });
                console.log('[attach] 추가:', name, '(', size, 'bytes) — 누적:', pendingFiles.length);
            } else {
                console.log('[attach] 중복 제외:', name);
            }
        }

        // input 초기화: 같은 파일을 다시 선택할 수 있도록
        try { this.value = ''; } catch (e) {}

        renderPending();
    });

    function renderPending() {
        list.innerHTML = '';
        var label = document.getElementById(pendingListId + '_label');
        if (label) label.style.display = pendingFiles.length > 0 ? '' : 'none';

        console.log('[attach] renderPending - 파일 수:', pendingFiles.length);
        pendingFiles.forEach(function (entry, idx) {
            var row = document.createElement('div');
            row.className = 'attach-row attach-pending';
            row.innerHTML =
                '<span class="attach-icon">&#128196;</span>' +
                '<span class="attach-name">' + escHtml(entry.name) + '</span>' +
                '<span class="attach-size">' + formatSize(entry.size) + '</span>' +
                '<button type="button" class="btn-sm-del" onclick="__removePending_' + pendingListId + '(' + idx + ')">제거</button>';
            list.appendChild(row);
        });
    }

    // 전역 제거 핸들러 (onclick 에서 호출)
    window['__removePending_' + pendingListId] = function (idx) {
        console.log('[attach] 제거:', pendingFiles[idx].name);
        pendingFiles.splice(idx, 1);
        renderPending();
    };

    // 폼 제출 시 pendingFiles → input.files 동기화
    if (form) {
        form.addEventListener('submit', function () {
            if (pendingFiles.length === 0) return;
            console.log('[attach] submit - 전송할 파일 수:', pendingFiles.length);
            try {
                var dt = new DataTransfer();
                pendingFiles.forEach(function (entry) {
                    try {
                        dt.items.add(entry.file);
                    } catch (e) {
                        // File 참조가 무효화된 경우 Blob으로 재구성 시도
                        try {
                            var fallback = new File([entry.file], entry.name, { type: entry.file.type });
                            dt.items.add(fallback);
                        } catch (e2) {
                            console.warn('[attach] 파일 추가 실패:', entry.name, e2);
                        }
                    }
                });
                if (dt.files.length > 0) {
                    input.files = dt.files;
                    console.log('[attach] DataTransfer 설정 완료 - 파일 수:', dt.files.length);
                }
            } catch (e) {
                console.warn('[attach] DataTransfer 미지원 환경:', e);
            }
        });
    }

    function escHtml(str) {
        return String(str)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;')
            .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function formatSize(bytes) {
        if (!bytes || bytes === 0) return '0 B';
        if (bytes < 1024)         return bytes + ' B';
        if (bytes < 1048576)      return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / 1048576).toFixed(1) + ' MB';
    }
}

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
