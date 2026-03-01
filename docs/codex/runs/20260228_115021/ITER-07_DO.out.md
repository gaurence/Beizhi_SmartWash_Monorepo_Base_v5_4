Changed files:
- [CustomerProfile.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/CustomerProfile.java)
- [Vehicle.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/Vehicle.java)
- [VehicleDocument.java](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/VehicleDocument.java)
- [ITER-07.md](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/qa/manual/ITER-07.md)

Commands run:
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-07` -> PASS
- `make verify-traceability ITER=ITER-07` -> PASS
- `make verify-visual ITER=ITER-07` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="customer-app"` -> PASS
- `git add ... && git commit -m "ITER-07: add customer-vehicle domain models"` -> FAIL (`.git/index.lock: Operation not permitted` in sandbox)

Pass/fail summary:
- Gate A/B/C: PASS
- Module tests/build: PASS
- Commit: FAIL (sandbox cannot write `.git`)

Next TODO:
- Implement ITER-07 KYC/vehicle APIs in `AppCustomerController` + `AppCustomerService` with tenant isolation, RBAC, idempotency, audit append, and PII-masked responses.

To unblock commit outside this sandbox, run:
`git add backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/CustomerProfile.java backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/Vehicle.java backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/VehicleDocument.java docs/qa/manual/ITER-07.md && git commit -m "ITER-07: add customer-vehicle domain models"`
