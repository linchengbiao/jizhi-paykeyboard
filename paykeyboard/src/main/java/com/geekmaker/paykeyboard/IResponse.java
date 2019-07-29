
package com.geekmaker.paykeyboard;

/**
 * I2C信响应接口
 */
public interface IResponse {
    /**
     * 正常返回结果
     * @param data 原始数据
     * @param seq 请求序列号
     */
    void onResult(byte[] data, int seq);

    /**
     * 返回错误结果
     * @param code 错误代码
     * @param seq 请求序列号
     */
    void onError(short code, int seq);
}
