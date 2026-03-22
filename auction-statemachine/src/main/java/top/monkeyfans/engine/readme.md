1. 状态定义 (States)

   CREATED: 订单/竞拍场次已创建。

   PUBLISHED: 已发布，等待开拍。

   ONGOING: 竞拍中（实时接收 API 报价）。

   PENDING_SETTLE: 竞拍结束，等待系统判定最高价或触发成交逻辑。

   SUCCEEDED: 成交确认。

   PAYING / PAID: 支付中 / 已支付。

   CLOSED / CANCELLED: 违约流拍或手动撤单。

2. 事件驱动 (Events)

   START_AUCTION: 到点开拍。

   NEW_BID: 收到新报价（来自三方 API 或本平台）。

   TIMEOUT: 竞拍时间到。

   CONFIRM_DEAL: 卖家/系统确认成交。

   PAY_SUCCESS: 支付回调成功。

二、 实时竞拍 API 对接架构

对接三方 API 时，不能让 API 直接修改数据库，而应该将其视为状态机的输入源。

    接入层 (Adapter)：

        使用 WebSocket 或 Webflux 保持与三方平台的长连接。

        将三方返回的原始 JSON 转化为状态机识别的 NEW_BID Event。

    流控层 (Throttling)：

        竞拍末期会有“秒级多次报价”，通过 Redis 预校验价格（必须高于当前最高价）再送入状态机。

    状态机执行器 (Statemachine Engine)：

        Guard 校验：校验该 API 报价是否合法（如：是否在竞拍时间内、报价人保证金是否足够）。

        Action 执行：更新 Redis 中的当前最高价，记录报价日志，并通过消息队列（MQ）广播给其他终端。
## 接入层 (Adapter)：

1. 使用 WebSocket 或 Webflux 保持与三方平台的长连接。
2. 将三方返回的原始 JSON 转化为状态机识别的 NEW_BID Event

## 流控层 (Throttling)：

1. 竞拍末期会有“秒级多次报价”，通过 Redis 预校验价格（必须高于当前最高价）再送入状态机。

## 状态机执行器 (Statemachine Engine)：

1. Guard 校验：校验该 API 报价是否合法（如：是否在竞拍时间内、报价人保证金是否足够）。

2. Action 执行：更新 Redis 中的当前最高价，记录报价日志，并通过消息队列（MQ）广播给其他终端。

三、 报价、成交与结算逻辑设计
1. 报价环节 (Bidding)

   设计要点：使用 Redis 的 Sorted Set (ZSET) 存储报价序列。

   状态机作用：当 NEW_BID 触发时，Action 负责更新“当前领先人”。如果三方 API 传回的报价低于当前价，状态机通过 Guard 直接拒绝，不进行状态跳转。

2. 成交环节 (Closing)

   判定逻辑：当达到 TIMEOUT 事件时，状态机进入 PENDING_SETTLE。

   Action 逻辑：

        检查最高价是否达到“保留价”。

        如果达到，触发 CONFIRM_DEAL 转向 SUCCEEDED。

        如果未达标，转向 CLOSED（流拍）。

3. 结算环节 (Settlement)

   保证金锁定：在 ONGOING 阶段，通过 API 对接支付网关预授权（锁定资金）。

   最终结算：

        进入 SUCCEEDED 后，生成正式账单。

        调用支付 API 将预授权转为实扣。

        分润计算：Action 中计算平台佣金、卖家净得