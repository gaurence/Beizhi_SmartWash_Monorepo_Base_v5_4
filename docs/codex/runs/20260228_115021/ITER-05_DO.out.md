Changed files:
- [backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java)
- [backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java)
- [backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java)
- [docs/contracts/openapi.yaml](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/contracts/openapi.yaml)
- [docs/qa/manual/ITER-05.md](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/qa/manual/ITER-05.md)

Commands run:
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-05`
- `make verify-traceability ITER=ITER-05`
- `make verify-visual ITER=ITER-05`
- `make backend-test`
- `make frontend-build APPS="platform-admin store-admin agent-admin platform-iotops platform-ops cabinet-screen"`
- `git add ... && git commit -m "ITER-05: add ota and remote cabinet commands"`

Pass/fail summary:
- Verification gates/tests/builds: PASS
- Git commit: FAIL (`.git/index.lock` permission denied in sandbox)

Next TODO:
- Add dedicated OTA/remote/replay permission keys and RBAC wiring.
- Implement IoT replay APIs/channels.
- Replace remaining ITER-05 `ui-static` IoT/OTA traceability mappings with concrete API mappings.
