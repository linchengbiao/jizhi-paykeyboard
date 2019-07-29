
package com.geekmaker.paykeyboard;

/**
 * 键盘事件接口
 */
public interface IKeyboardListener {
    /**
     * keyDown
     * @param keyCode 键码
     * @param keyCode 键名
     */
    void onKeyDown(int keyCode, String keyName);

    /**
     * keyUp
     * @param keyCode 键码
     * @param keyName 键名
     */
    void onKeyUp(int keyCode, String keyName);

    /**
     * 发起支付请求事件
     * @param payRequest 支付请求
     */
    void onPay(IPayRequest payRequest);

    /**
     * 键盘可用事件
     */
    void onAvailable();

    /**
     * 键盘异常事件
     * @param keyCode
     */
    void onException(Exception keyCode);

    /**
     * 键盘资源释放事件
     */
    void onRelease();
}
