#!/usr/bin/env bash
set -euo pipefail
CODX_PID=""
trap 'echo "Interrupted. Stopping..."; [[ -n "$CODX_PID" ]] && kill -TERM "$CODX_PID" 2>/dev/null; exit 130' INT TERM

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROMPTS_DIR="$REPO_ROOT/docs/codex"
RUNS_ROOT="$REPO_ROOT/docs/codex/runs"
STAMP="$(date +%Y%m%d_%H%M%S)"
OUT_DIR="$RUNS_ROOT/$STAMP"
mkdir -p "$OUT_DIR"
cd "$REPO_ROOT"

# 从环境变量控制：START=ITER-02  /  MAX_RETRY=3
START="${START:-ITER-00}"
MAX_RETRY="${MAX_RETRY:-3}"

# 判断输出是否表示成功（你也可以改成更严格：grep 某个“ALL PASSED”）
is_done() {
  local out="$1"
  [[ -f "$out" ]] && grep -q "✅" "$out"
}

run_one() {
  local prompt_file="$1"
  local base="$(basename "$prompt_file" .md)"
  local out="$OUT_DIR/${base}.out.md"

  if is_done "$out"; then
    echo "=== Skip $base (already done) ==="
    return 0
  fi

  echo "=== Running $base ==="
  local attempt=1
  while (( attempt <= MAX_RETRY )); do
    echo "Attempt $attempt/$MAX_RETRY"
    set +e
    codex --ask-for-approval never exec --cd "$REPO_ROOT" --sandbox workspace-write - < "$prompt_file" > "$out"
    rc=$?
    set -e
    if [[ $rc -eq 0 ]]; then
      echo "=== Done $base ==="
      return 0
    fi

    echo "=== Failed $base (rc=$rc) ==="
    # 简单退避
    sleep $((attempt * 10))
    attempt=$((attempt + 1))
  done

  echo "=== Giving up on $base after $MAX_RETRY retries ==="
  return 1
}

for prompt_file in "$PROMPTS_DIR"/ITER-*.md; do
  base="$(basename "$prompt_file" .md)"
  if [[ "$base" < "$START" ]]; then
    continue
  fi
  run_one "$prompt_file"
done

echo "All done. Outputs in: $OUT_DIR"