package com.android.landicorp.f8face.inter;

/**
 * Created by admin on 2019/6/24.
 * 交易状态
 */


public interface TradeStatusInter {
    public void onTradeSucc();
    public void onTradeFaile();
    public void onTradeCancel();

}
