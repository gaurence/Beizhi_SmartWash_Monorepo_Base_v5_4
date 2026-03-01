# ITER-05 Manual QA Log

## Chunk 01 (2026-02-28)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`
- `docs/contracts/openapi.yaml`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-05` -> PASS
- `make verify-traceability ITER=ITER-05` -> PASS
- `make verify-visual ITER=ITER-05` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="platform-admin store-admin agent-admin platform-iotops platform-ops cabinet-screen"` -> PASS

### How to manually test (role-based)
1. `PLATFORM_IOTOPS` (permission includes `iot.open-slot.execute`, `asset.cabinet.read`):
- `POST /iot/cabinets/CAB-A1/commands/ota` with header `Idempotency-Key: idem-ota-manual-1` and body:
  ```json
  {
    "targetVersion": "v5.4.1",
    "packageUrl": "https://example.com/ota-v5.4.1.bin"
  }
  ```
- Repeat the same request with same idempotency key; expected same `commandId`.
- `GET /iot/cabinets/CAB-A1/events`; expected one `OTA_COMMAND` event for this idempotent request.

2. `PLATFORM_OPS`:
- `POST /iot/cabinets/CAB-A1/commands/remote` with body:
  ```json
  {
    "commandName": "REBOOT",
    "args": {
      "delaySec": 5
    }
  }
  ```
- Without permission `iot.open-slot.execute`; expected `403 FORBIDDEN`.
- With permission and header `Idempotency-Key: idem-remote-manual-1`; expected `200 OK` and payload contains `commandType=REMOTE`, `commandName=REBOOT`.

3. `PLATFORM_ADMIN` (audit check):
- Trigger one OTA command and one remote command as above.
- `GET /audit/logs` with permission `audit.log.read`.
- Expected append-only audit actions include `IOT_OTA_COMMAND` and `IOT_REMOTE_COMMAND`, each with `request_id`.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

### Execution note
- `git add` / `git commit` could not run in this sandbox because writing `.git/index.lock` is denied (`Operation not permitted`).
