# Engineering Guardrails (AGENTS.md) — Beizhi SmartWash v5.4

> **Single Source of Truth**  
> Implementation MUST reference and remain consistent with:
> - `docs/requirements/Beizhi_v5_4.md`
> - `docs/design/Beizhi_DDD_Detailed_Design_v5_4.md`
> - `docs/ui/page_inventory.csv` + `docs/ui/svg/**` + `docs/ui/md/**`
> - `docs/traceability/page_to_api.md`
> - `docs/contracts/openapi.yaml` / `docs/contracts/asyncapi.yaml`

## 1. Multi‑tenant isolation (Hard requirement)
- **All business tables MUST have `tenant_id`** (except platform-wide dictionaries explicitly documented).
- Tenant MUST be derived from JWT claims (no “tenantId from request body” trust).
- Cross-tenant access must be denied (403) or invisible (404) as specified by the requirement/design.

## 2. RBAC + ABAC (Hard requirement)
- RBAC: `permission_key` is required for every API and every UI page.
- ABAC: “by-order/by-task” scoped authorization tokens for:
  - Technician/Valet pickup key, return key
  - Emergency open slot (requires step-up)
  - Evidence upload URLs (short TTL, scoped to order/task)
- Tokens MUST be short-lived and bound to:
  - `tenant_id`, `user_id`, `order_id`/`task_id`, `cabinet_id`/`slot_id`, `action`, `ttl`.

## 3. Audit & compliance (Hard requirement)
- **Append-only audit log** for high-risk actions:
  - Cabinet open slot / emergency open
  - Key deposit/pickup/return/retrieve
  - Refund / settlement / payout account changes
  - Cabinet-store binding changes (M:N bindings)
  - Commercial agreement changes / revenue share policy changes
  - Export sensitive data
  - Permission / role / scope changes
- Audit record MUST include: `request_id`, actor, tenant, resource, result, ip/user-agent when available.

## 4. Evidence chain (Non-repudiation)
- Evidence records are **append-only**; no overwrites.
- Object storage URLs must be time-limited; store immutable hashes where applicable.
- Redaction: PII must be masked when displayed to non-authorized roles.

## 5. Idempotency & concurrency
- State transitions MUST be idempotent:
  - Use `Idempotency-Key` header + server-side idempotency record.
  - `request_id` must be generated and propagated end-to-end.
- For cabinet operations:
  - Ensure **slot state machine** correctness (RESERVED/OCCUPIED/FAULT/LOCKED).
  - Concurrency control must prevent double-open/double-assign.

## 6. Device investor & cabinet sharing (v5.4 additions)
- A **Cabinet** can be invested by: platform / agent / store / third-party investor.
- Investor has **data visibility and revenue share** for invested cabinets.
- Cabinet ↔ Store relationship can be **1:1 or M:N** via `CabinetStoreBinding`.
- Binding changes are high-risk and require audit; approvals may be required.

## 7. PII / Sensitive data rules
- Never log: phone, ID card, driver license, plate number, precise location, payment identifiers.
- Use masking in UI and API responses unless role explicitly allows.
- Export endpoints must enforce extra permission + audit.

## 8. Testing & verification (PR cannot merge without)
- Contract validation: OpenAPI/AsyncAPI must be valid.
- Page gates: `page_inventory.csv` must be fully covered by routes/pages.
- Traceability gates: page ↔ API ↔ permission must be complete.
- Add unit tests for:
  - tenant isolation
  - permission checks (allow + deny)
  - idempotency
  - slot state transitions
  - audit write
