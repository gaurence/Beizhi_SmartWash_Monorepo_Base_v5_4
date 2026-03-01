#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROMPTS_DIR="$REPO_ROOT/docs/codex"
RUNS_ROOT="$REPO_ROOT/docs/codex/runs"
STAMP="${STAMP:-$(date +%Y%m%d_%H%M%S)}"
OUT_DIR="$RUNS_ROOT/$STAMP"
mkdir -p "$OUT_DIR"
cd "$REPO_ROOT"

START="${START:-ITER-00}"
MAX_RETRY="${MAX_RETRY:-3}"

# Print helpful header
echo "Repo: $REPO_ROOT"
echo "Out : $OUT_DIR"
echo "Start: $START  Retries: $MAX_RETRY"

# trap for clean interrupt
trap 'echo "Interrupted. Stopping..."; exit 130' INT TERM

is_done() {
  local out="$1"
  [[ -f "$out" ]] && grep -q "✅" "$out"
}

run_file() {
  local prompt_file="$1"
  local out="$2"
  local base="$3"

  local attempt=1
  while (( attempt <= MAX_RETRY )); do
    echo "=== Running $base (attempt $attempt/$MAX_RETRY) ==="
    set +e
    # Note: codex-cli 0.106.0 doesn't support --ask-for-approval on subcommand; use config for approval_policy.
    codex exec --cd "$REPO_ROOT" --sandbox workspace-write - < "$prompt_file" > "$out"
    rc=$?
    set -e

    if [[ $rc -eq 0 ]]; then
      echo "=== Done $base ==="
      return 0
    fi

    echo "=== Failed $base (rc=$rc). Retrying after backoff... ==="
    sleep $((attempt * 10))
    attempt=$((attempt + 1))
  done

  echo "=== Giving up on $base after $MAX_RETRY retries ==="
  return 1
}

for plan_file in "$PROMPTS_DIR"/ITER-*_PLAN.md; do
  [[ -f "$plan_file" ]] || continue
  iter="$(basename "$plan_file" _PLAN.md)"
  if [[ "$iter" < "$START" ]]; then
    continue
  fi

  do_file="$PROMPTS_DIR/${iter}_DO.md"
  if [[ ! -f "$do_file" ]]; then
    echo "Missing DO file for $iter: $do_file"
    exit 2
  fi

  plan_out="$OUT_DIR/${iter}_PLAN.out.md"
  do_out="$OUT_DIR/${iter}_DO.out.md"

  # Run PLAN if not done
  if ! is_done "$plan_out"; then
    run_file "$plan_file" "$plan_out" "${iter}_PLAN"
  else
    echo "=== Skip ${iter}_PLAN (already done) ==="
  fi

  # Run DO if not done (DO may need multiple chunks; rerun as needed)
  if ! is_done "$do_out"; then
    run_file "$do_file" "$do_out" "${iter}_DO"
  else
    echo "=== Skip ${iter}_DO (already done) ==="
  fi
done

echo "All done. Outputs in: $OUT_DIR"
