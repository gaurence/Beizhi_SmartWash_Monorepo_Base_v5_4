Changed files (this chunk):
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java`
- `docs/contracts/openapi.yaml`
- `docs/traceability/page_to_api.md`
- `docs/qa/manual/ITER-14.md`

Commands run:
- `make verify-docs` ✅
- `make verify-openapi` ✅
- `make verify-asyncapi` ✅
- `make verify-page-coverage ITER=ITER-14` ✅
- `make verify-traceability ITER=ITER-14` ✅
- `make verify-visual ITER=ITER-14 APPS="customer-app store-admin technician-app"` ✅
- `make backend-test` ✅
- `make frontend-build APPS="customer-app store-admin technician-app"` ✅

Pass/fail summary:
- Gates A/B/C: PASS
- Module test/build checks: PASS
- `git commit`: ❌ blocked in this environment (`.git/index.lock` creation denied by sandbox policy)

Next TODO (next smallest chunk):
- Implement `StaffWorkforce` ITER-14 valet driver management endpoints (`PUT /staff/{staffId}`, `GET /staff/{staffId}/performance`) with tests + contract/traceability alignment.

To finish this chunk commit on your side, run:
```bash
git add backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java \
        backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java \
        docs/contracts/openapi.yaml \
        docs/traceability/page_to_api.md \
        docs/qa/manual/ITER-14.md
git commit -m "ITER-14: add tech role-mode profile/switch API slice"
```

