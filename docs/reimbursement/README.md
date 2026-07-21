# 智能报销模块

独立服务：`reimbursement-server`，端口 `48090`。

支持两条链路：

- 人工创建报销草稿、编辑、提交到 BPM。
- 绑定邮箱授权码后，从邮箱导入，Dify 通过短期 Redis Token 读取邮件并回填 AI 草稿。

数据库只包含四张业务表：

- `reimbursement_claim`
- `reimbursement_item`
- `reimbursement_attachment`
- `reimbursement_mailbox_connection`

Redis 只使用一种业务 Key：`reimbursement:mail-access:<sha256(rawToken)>`。

## 环境变量

- `YUDAO_REIMBURSEMENT_AI_SUBMIT_MODE`
- `YUDAO_REIMBURSEMENT_DIFY_ENABLED`
- `DIFY_API_BASE_URL`
- `DIFY_REIMBURSEMENT_WORKFLOW_API_KEY`
- `DIFY_REIMBURSEMENT_TIMEOUT_MILLIS`
- `YUDAO_REIMBURSEMENT_MAILBOX_ENCRYPTION_KEY`
- `YUDAO_REIMBURSEMENT_MAIL_ACCESS_TTL_MINUTES`
- `YUDAO_REIMBURSEMENT_ALLOW_CUSTOM_MAILBOX`
- `YUDAO_REIMBURSEMENT_INTERNAL_SERVICE_TOKEN`

生成 AES-256 Key：

```bash
openssl rand -base64 32
```
