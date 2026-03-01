Changed files:
- [StaffWorkforceController.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/StaffWorkforceController.java)
- [StaffWorkforceService.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/StaffWorkforceService.java)
- [InMemoryPlatformRepository.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java)
- [PlatformGateIntegrationTest.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java)
- [ITER-04.md](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/qa/manual/ITER-04.md)

Commands run:
- `make verify-docs` PASS
- `make verify-openapi` PASS
- `make verify-asyncapi` PASS
- `make verify-page-coverage ITER=ITER-04` PASS
- `make verify-traceability ITER=ITER-04` PASS
- `make verify-visual ITER=ITER-04` PASS (mock mode)
- `make backend-test` PASS
- `make frontend-build APPS="store-admin technician-app"` PASS

Pass/fail summary:
- Gate A PASS
- Gate B PASS
- Gate C PASS
- Module checks PASS
- Commit FAIL (sandbox cannot write `.git/index.lock`, operation not permitted)

Next TODO:
- Implement wash-team APIs (`list/create/update/member assign/remove`) with tenant isolation + idempotency + audit, add tests, then sync OpenAPI/traceability rows away from `ui-static`.

To finalize commit locally, run:
```bash
git add backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/StaffWorkforceController.java \
  backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/StaffWorkforceService.java \
  backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java \
  backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java \
  docs/qa/manual/ITER-04.md && \
git commit -m "ITER-04: staff roster foundation"
```
