

package com.geekmaker.paykeyboard;

/**
 * 支付请求接口
 */
public interface IPayRequest {
    /**
     * 本次支付请求的金额
     * @return
     */
    double getMoney();

    /**
     * 设置结果
     * @param fail 是否失败
     */
    void setResult(boolean fail);

    /**
     * 设置结果
     * @param fail 是否失败
     * @param cancel 是否人为取消
     */
    void setResult(boolean fail, boolean cancel);
}
