Changed files:
- [AppCustomerController.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java)
- [AppCustomerService.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java)
- [InMemoryPlatformRepository.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java)
- [PlatformGateIntegrationTest.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java)
- [ITER-15.md](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/qa/manual/ITER-15.md)

Commands run:
- `make verify-docs` PASS
- `make verify-openapi` PASS
- `make verify-asyncapi` PASS
- `make verify-page-coverage ITER=ITER-15` PASS
- `make verify-traceability ITER=ITER-15` PASS
- `make verify-visual ITER=ITER-15 APPS="customer-app platform-admin"` PASS
- `make backend-test` FAIL (compile), fixed, re-run PASS
- `make frontend-build APPS="customer-app platform-admin"` PASS

Pass/fail summary:
- Gate A PASS
- Gate B PASS
- Gate C PASS
- Module checks PASS
- `docs/qa/manual/ITER-15.md` updated with changes, verification, manual role-based steps, next TODO
- Commit blocked by sandbox: cannot write `.git/index.lock` (`Operation not permitted`)

Next TODO:
- Implement frontend high-fidelity pages and flow wiring for `CA_08_5_Onsite_Location_Input`, `CA_08_6_Onsite_KeyCabinet_Select`, `PA_42_CabinetRoutingPolicy` in the next chunk.
