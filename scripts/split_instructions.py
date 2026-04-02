#!/usr/bin/env python3
"""Split instructions.md into modular files under docs/.

Usage:
  python scripts/split_instructions.py

It creates:
  docs/01-intro.md
  docs/02-basic-principles.md
  ...
  docs/index.md

Rules:
- The split boundaries are top-level headings '##'.
- The initial preamble before first '##' goes to docs/00-preamble.md.
- File names are slugified from the heading text with sequence number.
- Index has links in order.
"""

import os
import re
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
SRC = BASE_DIR / "instructions.md"
OUT_DIR = BASE_DIR / "docs"

HEADER_RE = re.compile(r'^(##)\s+(.*)$')


def slugify(text: str) -> str:
    text = text.strip().lower()
    text = re.sub(r"[^a-z0-9-_ ]", "", text)
    text = re.sub(r"\s+", "-", text)
    text = re.sub(r"-+", "-", text)
    return text.strip("-")


def main():
    if not SRC.exists():
        raise SystemExit(f"Source file not found: {SRC}")

    OUT_DIR.mkdir(exist_ok=True)

    lines = SRC.read_text(encoding="utf-8").splitlines()

    sections = []  # tuple (title, lines)
    current_title = None
    current_lines = []

    for line in lines:
        m = HEADER_RE.match(line)
        if m:
            # New top-level section
            if current_title is not None or current_lines:
                sections.append((current_title, current_lines))
            current_title = m.group(2).strip()
            current_lines = [line]
        else:
            if current_title is None and not current_lines:
                # before first section
                current_lines = [line]
            else:
                current_lines.append(line)

    if current_title is not None or current_lines:
        sections.append((current_title, current_lines))

    if not sections:
        print("No sections found in instructions.md")
        return

    index_lines = ["# Documentation Index", "", "Generated from instructions.md", ""]
    for i, (title, section_lines) in enumerate(sections, start=1):
        if title is None or title == "":
            filename = f"00-preamble.md"
            display_title = "Preamble"
        else:
            slug = slugify(title)
            if not slug:
                slug = f"section-{i:02d}"
            filename = f"{i:02d}-{slug}.md"
            display_title = title

        file_path = OUT_DIR / filename

        file_text = "\n".join(section_lines).strip() + "\n"
        file_path.write_text(file_text, encoding="utf-8")

        index_lines.append(f"- [{display_title}]({filename})")

    (OUT_DIR / "index.md").write_text("\n".join(index_lines) + "\n", encoding="utf-8")

    print(f"Split {len(sections)} sections into {OUT_DIR}")


if __name__ == "__main__":
    main()
