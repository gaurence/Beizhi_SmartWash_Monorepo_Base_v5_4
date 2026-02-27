# Backend Skeleton (Maven multi-module)

起步说明：
- 以 `docs/contracts/openapi.yaml` 为接口契约真源（建议先冻结）
- 建议实现顺序：Auth/IAM → Cabinet/Slot → Order/Task → Evidence → Payment/Refund → Settlement → Notifications

运行（起步）：
```bash
cd backend
mvn -q -pl apps/api-server spring-boot:run
# http://localhost:8080/healthz
```
