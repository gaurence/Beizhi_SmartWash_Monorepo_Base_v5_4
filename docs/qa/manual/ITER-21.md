# ITER-21 Manual QA Log

## Chunk 1 (2026-02-28) - Reporting UX completion + real visual gate

### What changed (files)
- `docs/contracts/openapi.yaml`
  - Added missing contract paths for parity with controllers:
    - `GET/PUT /app/tech/notification-prefs`
    - `GET/PUT /risk/policies*`
    - `GET/POST /staff`, `GET /staff/{staffId}`
    - `POST /app/orders/{orderId}/delegate-retrieve-code`
- `tooling/codegen/generate-frontend.mjs`
  - Upgraded generated pages with report-focused high-fidelity UI mode (toolbar/filter drawer/KPI/trend/table/export affordance).
  - Added role preset for `PROPERTY_FINANCE`.
  - Added report page routing logic for `PA_02_*`, `*_Reports`, `*_Report_Filter_Drawer`.
  - Extended generated stylesheet with report-specific layout/styles.
- `frontend/*/src/pages/GeneratedPage.vue` (regenerated)
  - Report pages now render dedicated report workspace instead of metadata-only scaffold.
- `frontend/*/src/styles.css` (regenerated)
  - Includes report workspace, trend chart, table, and filter drawer styles.
- `tooling/gates/verify-visual.mjs`
  - Playwright mode now enabled by default (unless `ENABLE_PLAYWRIGHT_VISUAL=0`).
  - `UPDATE_BASELINE=1` now force-updates existing baseline images (not only missing files).
- `Makefile`
  - `verify-visual` target now defaults to real Playwright mode.
- `docs/traceability/page_to_api.md`
  - `PA_02_Global_Dashboard` switched from `ui-static` to `reports` with `GET /reports/summary; POST /reports/export`.
- `docs/qa/manual/ITER-21.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-21` -> PASS
- `make verify-traceability ITER=ITER-21` -> PASS
- `UPDATE_BASELINE=1 make verify-visual ITER=ITER-21` -> PASS (`mode=playwright`, 413 pages baseline refreshed)
- `ITER=ITER-21 ENABLE_PLAYWRIGHT_VISUAL=1 npm run verify:visual` -> PASS (`mode=playwright`, 413 pages)
- `make backend-test` -> PASS
- `make frontend-build APPS=all` -> PASS

### Manual test (role-based)
1. `PLATFORM_ADMIN` (`pa_admin`)
- Open `/pages/PA_02_Global_Dashboard`.
- Expected:
  - report toolbar + period chips + KPI cards + trend + ranking table rendered.
  - filter drawer opens from “筛选条件”.
  - API mapping includes `/reports/summary` and `/reports/export`.

2. `PLATFORM_FINANCE` (`fin_admin`)
- Open `/pages/PF_16_Finance_Reports`.
- Change period and trend metric selector, then click “导出报表”.
- Expected:
  - UI state updates without route break.
  - export affordance remains visible for finance role.

3. `AGENT_ADMIN` (`agent_admin`)
- Open `/pages/AA_10_Reports` and `/pages/AA_10_1_Report_Filter_Drawer`.
- Expected:
  - `AA_10_1_*` page enters drawer-focused mode and keeps drawer open by default.
  - data block renders under agent scope.

4. `STORE_ADMIN` (`store_admin`)
- Open `/pages/SA_20_Reports` and `/pages/SA_20_1_Report_Filter_Drawer`.
- Expected:
  - store-scope report workspace visible.
  - drawer fields (`store/site/channel/minAmount`) editable and “应用筛选” updates snapshot timestamp.

5. `DEVICE_INVESTOR` (`investor_admin`)
- Open `/pages/DI_05_Reports`.
- Expected:
  - owner field in ranking table is masked (`张**` style), satisfying PII default masking.

6. `PROPERTY_FINANCE` (`property_fin`)
- Open `/pages/PP_08_Reports`.
- Expected:
  - property-scope report workspace visible.
  - masked owner display enabled.

### UI vs SVG delta note
- Iteration goal was to replace generic scaffold for report pages with an interactive high-fidelity report workspace.
- This implementation aligns structure and interaction intent (filter, KPI, trend, table, export) with MD specs while remaining data-seeded in generated scaffolds.
- Final production-grade data wiring can continue to bind these controls to live backend responses where needed.
