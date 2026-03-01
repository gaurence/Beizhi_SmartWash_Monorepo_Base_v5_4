# Auto-run (Anti-disconnect v2)

This prompt set is split into PLAN and DO phases per iteration:
- `ITER-XX_PLAN.md`: planning only, writes `docs/qa/manual/ITER-XX.plan.md`, stops.
- `ITER-XX_DO.md`: implements in small chunks (≤ 8 minutes OR ≤ 5 files), commits each chunk, updates `docs/qa/manual/ITER-XX.md`, stops.

## Run via script (Codex CLI)

From repo root:

```bash
# optional: resume from a specific iteration
START=ITER-02 MAX_RETRY=5 bash docs/codex/run_all_iterations_v2.sh
```

Outputs are written to:
`docs/codex/runs/<timestamp>/{ITER}_PLAN.out.md` and `{ITER}_DO.out.md`

## Notes
- Use `approval_policy = "never"` for unattended overnight runs (set in `.codex/config.toml`).
- Keep network access disabled where possible; pre-warm dependencies before overnight runs.
