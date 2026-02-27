# 自动串行执行（可选）

你当前使用的是 VS Code Codex Extension（IDE 内 Agent）。IDE 扩展通常不提供“无人值守串行跑完所有 ITER”的官方能力。

如果你想夜间自动串行跑：
1. 安装/使用 Codex CLI（如有）。
2. 把本目录复制到仓库 `docs/codex/`。
3. 在仓库根目录执行：
   ```bash
   bash docs/codex/run_all_iterations.sh
   ```

> 注意：脚本中的 `codex run ...` 命令是示例。不同版本/产品的 CLI 参数可能不同，请按你实际环境调整。
