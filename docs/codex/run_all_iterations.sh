#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROMPTS_DIR="$REPO_ROOT/docs/codex"
OUT_DIR="$REPO_ROOT/docs/codex/runs/$(date +%Y%m%d_%H%M%S)"

mkdir -p "$OUT_DIR"

# 可选：确保在仓库根目录执行
cd "$REPO_ROOT"

for prompt_file in "$PROMPTS_DIR"/ITER-*.md; do
  [ -f "$prompt_file" ] || continue
  base="$(basename "$prompt_file" .md)"
  echo "=== Running $prompt_file ==="

  # 关键修复：用 stdin 传入 prompt 文件内容（PROMPT = "-"）
  codex exec \
    --cd "$REPO_ROOT" \
    --sandbox workspace-write \
    - < "$prompt_file" > "$OUT_DIR/${base}.out.md"

  echo "=== Done $base -> $OUT_DIR/${base}.out.md ==="
done

echo "All done. Outputs in: $OUT_DIR"