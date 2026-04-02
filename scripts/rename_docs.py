#!/usr/bin/env python3
"""Rename docs files to meaningful names."""

import os
import shutil
from pathlib import Path

docs_dir = Path(__file__).parent.parent / "docs"

# Mapping: old_name -> new_name
rename_map = {
    "00-preamble.md": "00-introduction.md",
    "02-1.md": "01-basic-principles.md",
    "03-2-tech-stack.md": "02-tech-stack.md",
    "04-3-uiux.md": "03-ui-ux-guidelines.md",
    "05-4.md": "04-db-conventions.md",
    "06-5.md": "05-authentication-session.md",
    "07-6-jpa-specification.md": "06-jpa-specification-pattern.md",
    "08-7.md": "07-project-structure.md",
    "09-8-activepage.md": "08-activepage-sidebar.md",
    "10-9.md": "09-session-user-info.md",
    "11-10.md": "10-file-upload-single.md",
    "12-11.md": "11-shared-table-pattern.md",
    "13-12.md": "12-exception-handling.md",
    "14-13-db.md": "13-database-changes.md",
    "15-14.md": "14-development-checklist.md",
    "16-15-spring-boot.md": "15-spring-boot-startup.md",
    "17-16-attach_file.md": "16-attach_file-multi-upload.md",
    "18-17-excelutil.md": "17-excel-utility.md",
    "19-18-curl.md": "18-curl-integration-tests.md",
}

for old, new in rename_map.items():
    old_path = docs_dir / old
    new_path = docs_dir / new
    if old_path.exists():
        shutil.move(str(old_path), str(new_path))
        print(f"✓ {old} → {new}")
    else:
        print(f"✗ {old} not found")

print("\nDone!")
