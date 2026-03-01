Changed files:
- [AppCustomerController.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java)
- [AppCustomerService.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java)
- [InMemoryPlatformRepository.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java)
- [PlatformGateIntegrationTest.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java)
- [ITER-08.md](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/qa/manual/ITER-08.md)

Commands run:
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-08` -> PASS
- `make verify-traceability ITER=ITER-08` -> PASS
- `make verify-visual ITER=ITER-08` -> PASS
- `make backend-test` -> FAIL (compile issue in new sort), fixed, rerun -> PASS
- `make frontend-build APPS="platform-admin store-admin customer-app"` -> PASS
- `git add ... && git commit -m "ITER-08: add app order api chunk"` -> FAIL (`.git/index.lock: Operation not permitted`)

Pass/fail summary:
- Required gates/tests/builds are green after fix.
- Commit is blocked by sandbox Git permission (`index.lock` creation denied).

Next TODO:
- Next chunk: implement `CatalogPricingController` + `CatalogPricingService` tenant-isolated catalog/dynamic-pricing CRUD and wire ITER-08 non-`ui-static` traceability rows to concrete endpoints.
