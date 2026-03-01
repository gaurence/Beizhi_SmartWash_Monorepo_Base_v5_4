# Beizhi SmartWash Codex Iteration Prompts v5.4 (Anti-disconnect)

This set is split into two files per iteration:
- `ITER-XX_PLAN.md`: planning only. Writes `docs/qa/manual/ITER-XX.plan.md` and stops.
- `ITER-XX_DO.md`: implementation in time-boxed chunks (≤ 8 minutes or ≤ 5 files), commits each chunk, writes `docs/qa/manual/ITER-XX.md`, then stops.

Run order:

- ITER-00: `ITER-00_PLAN.md` → `ITER-00_DO.md` (repeat DO as many chunks as needed)
- ITER-01: `ITER-01_PLAN.md` → `ITER-01_DO.md` (repeat DO as many chunks as needed)
- ITER-02: `ITER-02_PLAN.md` → `ITER-02_DO.md` (repeat DO as many chunks as needed)
- ITER-03: `ITER-03_PLAN.md` → `ITER-03_DO.md` (repeat DO as many chunks as needed)
- ITER-04: `ITER-04_PLAN.md` → `ITER-04_DO.md` (repeat DO as many chunks as needed)
- ITER-05: `ITER-05_PLAN.md` → `ITER-05_DO.md` (repeat DO as many chunks as needed)
- ITER-06: `ITER-06_PLAN.md` → `ITER-06_DO.md` (repeat DO as many chunks as needed)
- ITER-07: `ITER-07_PLAN.md` → `ITER-07_DO.md` (repeat DO as many chunks as needed)
- ITER-08: `ITER-08_PLAN.md` → `ITER-08_DO.md` (repeat DO as many chunks as needed)
- ITER-09: `ITER-09_PLAN.md` → `ITER-09_DO.md` (repeat DO as many chunks as needed)
- ITER-10: `ITER-10_PLAN.md` → `ITER-10_DO.md` (repeat DO as many chunks as needed)
- ITER-11: `ITER-11_PLAN.md` → `ITER-11_DO.md` (repeat DO as many chunks as needed)
- ITER-12: `ITER-12_PLAN.md` → `ITER-12_DO.md` (repeat DO as many chunks as needed)
- ITER-13: `ITER-13_PLAN.md` → `ITER-13_DO.md` (repeat DO as many chunks as needed)
- ITER-14: `ITER-14_PLAN.md` → `ITER-14_DO.md` (repeat DO as many chunks as needed)
- ITER-15: `ITER-15_PLAN.md` → `ITER-15_DO.md` (repeat DO as many chunks as needed)
- ITER-16: `ITER-16_PLAN.md` → `ITER-16_DO.md` (repeat DO as many chunks as needed)
- ITER-17: `ITER-17_PLAN.md` → `ITER-17_DO.md` (repeat DO as many chunks as needed)
- ITER-18: `ITER-18_PLAN.md` → `ITER-18_DO.md` (repeat DO as many chunks as needed)
- ITER-19: `ITER-19_PLAN.md` → `ITER-19_DO.md` (repeat DO as many chunks as needed)
- ITER-20: `ITER-20_PLAN.md` → `ITER-20_DO.md` (repeat DO as many chunks as needed)
- ITER-21: `ITER-21_PLAN.md` → `ITER-21_DO.md` (repeat DO as many chunks as needed)
- ITER-22: `ITER-22_PLAN.md` → `ITER-22_DO.md` (repeat DO as many chunks as needed)

Automation script:
- `run_all_iterations_v2.sh` runs PLAN then DO for each iteration with retries and resume.
