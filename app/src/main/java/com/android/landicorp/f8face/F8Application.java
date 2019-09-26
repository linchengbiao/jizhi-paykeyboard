package com.android.landicorp.f8face;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.landicorp.f8face.data.MainHandlerConstant;
import com.android.landicorp.f8face.inter.KeyBoardCancelMessageEvent;
import com.android.landicorp.f8face.inter.SendVoidMessageEvent;
import com.android.landicorp.f8face.inter.WxFacePayMessageEvent;
import com.android.landicorp.f8face.util.AppUtil;
import com.android.landicorp.f8face.util.StringUtil;
import com.android.landicorp.f8face.util.TTSUtils;
import com.android.landicorp.f8face.util.ToastUtil;
import com.geekmaker.paykeyboard.DefaultKeyboardListener;
import com.geekmaker.paykeyboard.ICheckListener;
import com.geekmaker.paykeyboard.IKeyboardListener;
import com.geekmaker.paykeyboard.IPayRequest;
import com.geekmaker.paykeyboard.PayKeyboard;
import com.geekmaker.paykeyboard.USBDetector;
import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.device.UsbHidDevice;
import com.landicorp.android.eptapi.exception.ReloginException;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.exception.ServiceOccupiedException;
import com.landicorp.android.eptapi.exception.UnsupportMultiProcess;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

/**
 * Created by admin on 2019/6/17.
 */

public class F8Application extends Application implements ICheckListener {
    public static final String TAG = "F8Application";
    protected PayKeyboard keyboard;
    protected USBDetector detector;
    private Handler handler = new Handler();
    protected UsbHidDevice usbHidDevice;    //虚拟HID通讯


    public F8Application() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        detector =  PayKeyboard.getDetector(this);
        detector.setListener(this);
        try {
            DeviceService.login(getApplicationContext());
        } catch (ServiceOccupiedException e) {
            e.printStackTrace();
        } catch (ReloginException e) {
            e.printStackTrace();
        } catch (UnsupportMultiProcess unsupportMultiProcess) {
            unsupportMultiProcess.printStackTrace();
        } catch (RequestException e) {
            e.printStackTrace();
        }
        TTSUtils.init(this);
        openKeyboard();
        WxPayFace.getInstance().initWxpayface(getApplicationContext(), new IWxPayfaceCallback() {
            @Override
            public void response(Map map) throws RemoteException {
            }
        });

    }

    private void showToast(final String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }


    public boolean isPayByHID,isPayBySaler;
    private IPayRequest mIrequest;
    public IPayRequest getmIrequest() {
        return mIrequest;
    }
    public PayKeyboard getPayKeyboard(){
        return keyboard;
    };


    private long mLastClickTime = 0;
    public static final int TIME_INTERVAL = 1000;



    private IKeyboardListener mKeyboardListener = new DefaultKeyboardListener() {
        @Override
        public void onRelease() {
            super.onRelease();
            keyboard = null;
            Log.i("KeyboardUI", "Keyboard release!!!!!!");
        }

        @Override
        public void onAvailable() {
            super.onAvailable();
            // keyboard.updateSign(PayKeyboard.SIGN_TYPE_W,4);
            updateSignal();
        }

        @Override
        public void onException(Exception e) {
            Log.i("KeyboardUI", "usb exception!!!!");
            keyboard = null;
            super.onException(e);
        }

        @Override
        public void onPay(final IPayRequest request) {
            super.onPay(request);
            mIrequest = request;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (request.getMoney()>0){
                        String amount = String.valueOf(request.getMoney());
                        amount = StringUtil.subZeroAndDot(amount);
                        EventBus.getDefault().post(new WxFacePayMessageEvent(amount));
                    }else{
                        TTSUtils.speak("请输入交易金额");
                    }
                }
            });
        }

        @Override
        public void onKeyDown(final int keyCode, final String keyName) {
            super.onKeyDown(keyCode, keyName);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //设置
                    if (keyName.equalsIgnoreCase(PayKeyboard.KEY_LIST)){
                        EventBus.getDefault().post(new SendVoidMessageEvent());
                    }else if(keyName.equalsIgnoreCase(PayKeyboard.KEY_PAY)){
                        //如果有输入键盘，则这边响应事件要过滤，否则重复监听onPay
                        if (mIrequest!=null && mIrequest.getMoney()>0){
                            return;
                        }
                        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        //如果是HID模式，不需要输入金额
                        if (isPayByHID){
                            EventBus.getDefault().post(new WxFacePayMessageEvent("0"));
                        }else{
                              //进入金额显示界面
                            TTSUtils.speak("请输入交易金额");
                            ToastUtil.toast("金额输入格式错误");
                        }
                    }else if(keyName.equalsIgnoreCase(PayKeyboard.KEY_OPT)){
                        //取消
                        TTSUtils.speak("取消");
                        EventBus.getDefault().post(new KeyBoardCancelMessageEvent());
                    }else if (keyName.equalsIgnoreCase(PayKeyboard.KEY_FACE_PAY)){
                        //按刷脸键进入刷脸
                        if (System.currentTimeMillis() - mLastClickTime >= TIME_INTERVAL) {
                            mLastClickTime = System.currentTimeMillis();
                            EventBus.getDefault().post(new WxFacePayMessageEvent("0"));
                        } else {

                        }

                    }else if(keyName.equalsIgnoreCase(PayKeyboard.KEY_Backspace)){
                        //退格键
                        TTSUtils.speak("退格");
                    }else if(keyName.equalsIgnoreCase(PayKeyboard.KEY_ESC)){
                        //退格键
                        TTSUtils.speak("清除");
                    }
                }
            });
        }
        @Override
        public void onKeyUp(int keyCode, String keyName) {
            super.onKeyUp(keyCode, keyName);
        }
    };

    private void openKeyboard(){
        if(keyboard==null||keyboard.isReleased()){
            keyboard = PayKeyboard.get(this);
            if (keyboard==null){
                return;
            }
            keyboard.setLayout(0);
            if(keyboard!=null) {
                keyboard.setBaudRate(9600);
                keyboard.setListener(mKeyboardListener);
                try{
                    keyboard.open();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            Log.i("KeyboardUI","keyboard exists!!!");
        }
    }

    public void updateSignal(){
        int w =4;
        int g = 0;
        if (AppUtil.getNetWorkType(this, ConnectivityManager.TYPE_MOBILE)){
            g = 1;
        }
        if (AppUtil.getNetWorkType(this, ConnectivityManager.TYPE_WIFI)){
            int rssi = AppUtil.getNetWorkSingleUnit(this);
            //0到-50表示信号最好，-50到-70表示信号偏差，小于-70表示最差
            if (rssi>-50){
                w = 4;
            }else if(rssi>-70){
                w=2;
            }else {
                w = 1;
            }
        }else{
            w=0;
        }

        if(keyboard!=null && !keyboard.isReleased()) keyboard.updateSign(w,g);
    }

    @Override
    public void onAttach() {
        openKeyboard();
    }

    private void release(){
        try{
            if(keyboard!=null&&!keyboard.isReleased()){
                keyboard.release();
                keyboard=null;
            }
            if(detector!=null){
                detector.release();
                detector = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    protected void handle(Message msg) {
        int what = msg.what;
        switch (what) {
            case MainHandlerConstant.PRINT:
                break;
            case MainHandlerConstant.UI_CHANGE_INPUT_TEXT_SELECTION:

                break;
            case MainHandlerConstant.UI_CHANGE_SYNTHES_TEXT_SELECTION:
//                SpannableString colorfulText = new SpannableString(mInput.getText().toString());
//                if (msg.arg1 <= colorfulText.toString().length()) {
//                    colorfulText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, msg.arg1, Spannable
//                            .SPAN_EXCLUSIVE_EXCLUSIVE);
//                }
                break;
            default:
                break;
        }
    }


}
