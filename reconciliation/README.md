# 银企对账状态流转模块

基于 `state-machine-core` 引擎实现的银企对账单状态流转示例。对账单对象实现框架 `FsmOrder` 接口，从而复用同一套状态机引擎（无需修改框架即可接入新业务域）。

## 状态流转

```
INIT(待生成) --GENERATE--> GENERATED(已生成待比对) --START_COMPARE--> COMPARING(比对中)
COMPARING --FINISH_COMPARE--> ┬ 金额相等 → CONFIRMED(已确认平账)
                             └ 金额不等 → DIFFERENT(存在差异)
DIFFERENT --CONFIRM--> CONFIRMED --CLOSE--> CLOSED(已归档)
INIT --CLOSE--> CLOSED（支持作废直接从初始态关闭）
```

- `FINISH_COMPARE` 的下一状态由 `getNextState()` **动态判定**（比对结论）；
- `CONFIRM` 用 `filter()` 二次筛选，只处理确有差异的账单；
- 流转事件携带期望状态做**乐观锁**校验（不匹配抛 `ORDER_STATE_NOT_MATCH`）。

## 快速使用

通过 `ReconciliationService` 领域入口调用：

```java
@Autowired
private ReconciliationService reconciliationService;

// 一键平账链路：生成→比对→平账→关闭
ReconciliationBill bill = reconciliationService.runBalancedFlow(
        "BILL-001", "ICBC", "DAILY", "ACC-123", new BigDecimal("10000.00"));
// bill.getOrderState() == "CLOSED"

// 一键差异链路：生成→比对→差异→人工确认→关闭
ReconciliationBill bill2 = reconciliationService.runDifferenceFlow(
        "BILL-002", "CCB", "MONTHLY", "ACC-456",
        new BigDecimal("10000.00"), new BigDecimal("9950.00"));
// bill2.getOrderState() == "CLOSED"
```

也可逐步调用 `generate / startCompare / finishCompare / confirm / close`，或使用引擎原生 `sendEvent(event, fsmOrder)`。

## 模块组件

| 层次 | 类 | 职责 |
| --- | --- | --- |
| 领域对象 | `ReconciliationBill` | 对账单，实现 `FsmOrder` |
| 枚举 | `ReconciliationStateEnum` / `ReconciliationEventEnum` / `BankEnum` | 状态、事件、银行渠道 |
| 事件 | `ReconciliationEvent` | 统一事件（新建携带初始单据 / 流转携带期望状态） |
| 处理器 | `Generate/StartCompare/FinishCompare/Confirm/CloseBillProcessor` | 5 个 `@OrderProcessor` |
| 校验器 | `BillParamChecker` / `AccountChecker` / `RiskChecker` | 参数 / 账户 / 风控（含 needRelease 演示） |
| 插件 | `AuditLogPluginHandler` | `@ProcessorPlugin` 审计追踪号 |
| 编排服务 | `ReconciliationService` | 内存仓储 + 事件封装 + 两条一键链路 |

## 构建与测试

在仓库根目录执行：

```bash
mvn test
```

当前测试：core 模块 1 项 + reconciliation 模块 8 项，全部通过。
