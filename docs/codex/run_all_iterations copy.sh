#!/usr/bin/env bash
set -euo pipefail

# Optional: Sequentially run Codex CLI prompts (if you have Codex CLI installed).
# This script assumes:
# - You are at repo root
# - You have a command `codex` that can run an agent with a prompt file
#   e.g. codex run --prompt-file docs/codex/ITER-01.md
#
# If your CLI differs, adapt accordingly.

PROMPT_DIR="docs/codex"

for f in $(ls "${PROMPT_DIR}/ITER-"*.md | sort); do
  echo "=== Running ${f} ==="
  # Example command (adjust!):
  codex run --prompt-file "${f}"
done
