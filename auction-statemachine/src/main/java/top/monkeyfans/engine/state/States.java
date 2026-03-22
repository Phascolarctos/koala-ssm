package top.monkeyfans.engine.state;

public enum States {
    SI, S1, S2,
    CREATED, // 订单创建
    PUBLISHED, // 发布拍卖信息
    ONGOING, // 竞拍中 接受api报价
    PENDING_SETTLE, // 竞拍结束
    SUCCEEDED, // 成交确认
    PAYING, // 支付中
    PAID, // 支付完成
    CLOSED, // 流拍卖
    CANCELLED; // 手动撤单
}
