package com.android.landicorp.f8face.inter;

/**
 * Created by admin on 2019/6/21.
 */

public class WxFacePayMessageEvent {
    public final String mAmount;
    //进行人脸支付
    public WxFacePayMessageEvent(String amount){
        this.mAmount = amount;
    }


}
