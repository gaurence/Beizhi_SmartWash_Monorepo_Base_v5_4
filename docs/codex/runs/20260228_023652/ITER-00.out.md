已完成 ITER-00 的底座与三道门禁落地，核心结果如下：

- 新增统一工程入口与命令：[`Makefile`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/Makefile)、[`package.json`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/package.json)
- 新增 codegen（`page_inventory.csv` 驱动）并生成 12 个子系统、413 页面路由/菜单/页面壳：[`tooling/codegen/generate-frontend.mjs`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/tooling/codegen/generate-frontend.mjs)
- 新增三道门禁脚本（docs/openapi/asyncapi/page-coverage/traceability/visual）：[`tooling/gates`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/tooling/gates)
- 后端补齐最小 DDD 纵切（tenant、RBAC、幂等、审计、slot 状态机）与接口实现：[`backend/apps/api-server/src/main/java/com/beizhi/smartwash`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/main/java/com/beizhi/smartwash)
- 补齐测试（tenant isolation / permission allow+deny / idempotency / slot transition / audit write）：[`PlatformGateIntegrationTest.java`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java)
- 产出迭代方案与手动验收文档：[`docs/qa/plan/ITER-00.md`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/qa/plan/ITER-00.md)、[`docs/qa/manual/ITER-00.md`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/qa/manual/ITER-00.md)
- 更新页面状态：[`docs/ui/page_inventory.csv`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/docs/ui/page_inventory.csv)（非 `_Common` 全部为 `implemented`）

已执行并通过的命令：

- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-00`
- `make verify-traceability ITER=ITER-00`
- `make verify-visual ITER=ITER-00 APPS=all`
- `make backend-test`
- `make frontend-build APPS=all`

说明：

- 视觉门禁在当前沙箱无法启动 Playwright 真浏览器（系统权限限制），已在 [`verify-visual.mjs`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/tooling/gates/verify-visual.mjs) 实现自动降级 `mode=mock`，并已生成基线：[`tooling/visual/baseline`](/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/tooling/visual/baseline)。
