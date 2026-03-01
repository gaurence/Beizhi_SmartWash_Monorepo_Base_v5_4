一次性配置（先做）

确保在本地分支，不要在 detached HEAD：
git checkout master
确认远端：
git remote -v
私有仓库认证二选一：
HTTPS：用 PAT（推荐配系统凭据）
SSH：配置公钥并加到 GitHub
GitHub -> 本地（拉取最新）

先看本地是否有未提交改动：
git status -sb
拉取并同步：
git fetch origin --prune
git pull --rebase origin master
验证是否同步完成：
git status -sb
git rev-list --left-right --count master...origin/master
0 0 表示完全一致。

本地 -> GitHub（推送本地改动）

提交本地改动：
git add -A
git commit -m "your message"
推送前先对齐远端（防止被拒）：
git fetch origin --prune
git rebase origin/master
推送：
git push origin master
双方持续同步的日常顺序

开始工作前：fetch + pull --rebase
开发并 commit
推送前再 fetch + rebase
push
常见问题

non-fast-forward 推送被拒：先 git fetch + git rebase origin/master，再 git push。
rebase 冲突：
# 手工改冲突文件后
git add <file>
git rebase --continue
# 放弃本次 rebase
git rebase --abort
本地有未提交改动又要拉远端：先提交，或临时 git stash。
如果你要，我可以再给你一个“每天一键同步”的脚本版本。