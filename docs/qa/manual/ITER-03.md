# ITER-03 Manual QA Notes

## Chunk 2026-02-28-01

### What Changed (files)
- `docs/contracts/openapi.yaml`
  - Added ITER-03 contract placeholders and operationIds:
    - `listStores`, `createStore` on `GET/POST /tenants/{tenantId}/stores`
    - `getStore`, `updateStore` on `GET/PUT /stores/{storeId}`
    - `listAgreements`, `createAgreement` on `GET/POST /stores/{storeId}/agreements`
- `docs/traceability/page_to_api.md`
  - Updated `AA_03_*`, `AA_04_*`, `AA_05_*` rows (except Step3 existing IoT mapping) from `ui-static` to `tenant-org` where applicable.
  - Added store/agreement endpoints for those page mappings.
- `frontend/agent-admin/src/generated/pages.ts`
  - Regenerated from traceability so Agent Admin pages expose updated ITER-03 endpoint mappings.
- `frontend/agent-admin/generated/pages.json`
  - Regenerated page metadata snapshot for Agent Admin.

### What Was Verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-03` -> PASS
- `make verify-traceability ITER=ITER-03` -> PASS
- `make verify-visual ITER=ITER-03` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS=agent-admin` -> PASS

### How To Manually Test (role-based)
1. AGENT_ADMIN:
- Open `Agent_Admin_Portal` and visit:
  - `/pages/AA_03_Stores_List`
  - `/pages/AA_03_1_Store_Create`
  - `/pages/AA_04_Store_Detail`
  - `/pages/AA_04_2_Store_Contracts`
  - `/pages/AA_05_Store_Onboarding`
- Check each page "API Mapping" block shows the updated store/agreement endpoints from this chunk.
- Verify Step3 page `/pages/AA_05_3_Onboarding_Step3_DeviceBind` still shows cabinet/open-slot endpoints.

2. PLATFORM_ADMIN:
- Review `docs/contracts/openapi.yaml` and confirm new operationIds are present:
  - `listStores`, `createStore`, `updateStore`, `listAgreements`, `createAgreement`.
- Confirm `docs/traceability/page_to_api.md` `AA_03_*`~`AA_05_*` mappings now point to actual endpoints (no longer static placeholders for store/contract flow).

3. PLATFORM_IOTOPS:
- Open `/pages/AA_05_3_Onboarding_Step3_DeviceBind`.
- Confirm open-slot/event endpoint mapping remains unchanged and visible for device bind stage.

### Closed TODO (next chunk)
- Closed on 2026-02-28: merged into consolidated completion pass.

### Execution Note
- `git add` / `git commit` could not be completed in this environment because writing `.git/index.lock` is denied (`Operation not permitted`).
