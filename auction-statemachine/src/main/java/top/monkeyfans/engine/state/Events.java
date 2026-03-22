package top.monkeyfans.engine.state;

public enum Events {
    E1, E2,
    START_AUCTION,//到点开拍
    NEW_BID, // 收到新报价
    TIMEOUT, // 竞拍时间截至
    CONFIRM_DEAL,// 确认成交
    PAY_SUCCESS // 支付成功
}
