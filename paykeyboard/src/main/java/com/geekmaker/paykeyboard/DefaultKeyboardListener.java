
package com.geekmaker.paykeyboard;

import android.util.Log;

/**
 * 键盘侦听接口默认实现类
 */
public class DefaultKeyboardListener implements IKeyboardListener {
    private static final String TAG = "KeyboardSDK";

    public DefaultKeyboardListener() {
    }

    public void onKeyDown(int keyCode, String keyName) {
        Log.d("KeyboardSDK", String.format("on key down %s,%s", keyCode, keyName));
    }

    public void onKeyUp(int keyCode, String keyName) {
        Log.d("KeyboardSDK", String.format("on key up %s,%s", keyCode, keyName));
    }

    public void onPay(IPayRequest request) {
        Log.d("KeyboardSDK", String.format("on pay request %s", request.getMoney()));
    }

    public void onAvailable() {
        Log.d("KeyboardSDK", "keyboard available");
    }

    public void onException(Exception error) {
        Log.d("KeyboardSDK", String.format("keyboard exception %s", error.getMessage()));
    }

    public void onRelease() {
    }
}
