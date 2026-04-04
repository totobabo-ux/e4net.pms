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

/* ── 목록 첨부파일 아이콘 공통 함수 ──────────────────────────
 * - 첨부파일이 있는 경우에만 아이콘(1개) 표시
 * - 클릭 시 파일 목록 모달 오픈, 모달에서 다운로드 가능
 * ────────────────────────────────────────────────────────── */
(function () {

    /* 공통 첨부 모달 동적 생성 */
    function createAttachModal() {
        var modal = document.createElement('div');
        modal.id = 'attachModal';
        modal.className = 'modal-overlay';
        modal.innerHTML =
            '<div class="modal modal-sm">' +
                '<div class="modal-header">' +
                    '<span class="modal-title">&#128206; 첨부파일 목록</span>' +
                    '<button class="modal-close" onclick="closeAttachModal()">&times;</button>' +
                '</div>' +
                '<div class="modal-body" id="attachModalBody"></div>' +
                '<div class="modal-footer">' +
                    '<button type="button" class="btn btn-cancel" onclick="closeAttachModal()">닫기</button>' +
                '</div>' +
            '</div>';
        document.body.appendChild(modal);
        /* 배경 클릭 시 닫기 */
        modal.addEventListener('click', function (e) {
            if (e.target === this) closeAttachModal();
        });
    }

    window.openAttachModal = function (files, baseUrl, entityId) {
        var body = document.getElementById('attachModalBody');
        if (!body) return;
        var html = '<ul class="attach-file-list">';
        files.forEach(function (f) {
            var url  = baseUrl + '/' + entityId + '/attachment/' + f.id + '/download';
            var name = escHtml(f.fileName || '파일');
            var size = formatSize(f.fileSize);
            html += '<li class="attach-file-item">' +
                        '<span class="attach-file-icon">&#128206;</span>' +
                        '<a class="attach-file-name" href="' + url + '" target="_blank">' + name + '</a>' +
                        '<span class="attach-file-size">' + size + '</span>' +
                    '</li>';
        });
        html += '</ul>';
        body.innerHTML = html;
        document.getElementById('attachModal').classList.add('open');
    };

    window.closeAttachModal = function () {
        var modal = document.getElementById('attachModal');
        if (modal) modal.classList.remove('open');
    };

    function loadAttachIcons() {
        /* 공통 모달 생성 */
        if (!document.getElementById('attachModal')) createAttachModal();

        var tables = document.querySelectorAll('table[data-entity-type]');
        tables.forEach(function (table) {
            var entityType = table.dataset.entityType;
            var baseUrl    = table.dataset.baseUrl;
            if (!entityType || !baseUrl) return;

            var cells = table.querySelectorAll('td.attach-icons-cell[data-entity-id]');
            if (cells.length === 0) return;

            var ids = Array.from(cells).map(function (c) { return c.dataset.entityId; }).join(',');

            fetch('/api/attachments?entityType=' + encodeURIComponent(entityType) + '&ids=' + ids)
                .then(function (r) { return r.json(); })
                .then(function (map) {
                    cells.forEach(function (cell) {
                        var entityId = cell.dataset.entityId;
                        var files    = map[entityId];
                        /* 첨부파일 없으면 빈 셀 유지 */
                        if (!files || files.length === 0) return;

                        /* 파일 데이터 JSON 직렬화 후 data 속성으로 전달 */
                        var btn = document.createElement('button');
                        btn.className = 'btn-attach-icon';
                        btn.title = files.length + '개 파일';
                        btn.innerHTML = '&#128206;';
                        if (files.length > 1) {
                            var badge = document.createElement('span');
                            badge.className = 'attach-badge';
                            badge.textContent = files.length;
                            btn.appendChild(badge);
                        }
                        /* 클로저로 데이터 캡처 */
                        (function (f, base, eid) {
                            btn.addEventListener('click', function (e) {
                                e.stopPropagation();
                                openAttachModal(f, base, eid);
                            });
                        })(files, baseUrl, entityId);

                        cell.appendChild(btn);
                    });
                })
                .catch(function () {});
        });
    }

    function escHtml(s) {
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function formatSize(bytes) {
        if (!bytes || bytes === 0) return '';
        if (bytes < 1024)     return bytes + ' B';
        if (bytes < 1048576)  return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / 1048576).toFixed(1) + ' MB';
    }

    document.addEventListener('DOMContentLoaded', loadAttachIcons);
})();
