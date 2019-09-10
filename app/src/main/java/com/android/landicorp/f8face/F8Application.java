package com.android.landicorp.f8face;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.android.landicorp.f8face.baidu.controller.InitConfig;
import com.android.landicorp.f8face.baidu.controller.MySyntherizer;
import com.android.landicorp.f8face.baidu.controller.NonBlockSyntherizer;
import com.android.landicorp.f8face.baidu.listener.UiMessageListener;
import com.android.landicorp.f8face.baidu.util.AutoCheck;
import com.android.landicorp.f8face.baidu.util.OfflineResource;
import com.android.landicorp.f8face.data.MainHandlerConstant;
import com.android.landicorp.f8face.inter.KeyBoardCancelMessageEvent;
import com.android.landicorp.f8face.inter.SendVoidMessageEvent;
import com.android.landicorp.f8face.inter.WxFacePayMessageEvent;
import com.android.landicorp.f8face.util.AppUtil;
import com.android.landicorp.f8face.util.StringUtil;
import com.android.landicorp.f8face.util.ToastUtil;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
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
import com.landicorp.android.scan.util.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
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

    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId = "16619533";

    protected String appKey = "DzKgh7ebnfVbhfFooYi6eoHa";

    protected String secretKey = "n5GrTRQLW0GSTKDHNMLGBWQZe4TaFtol";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_FEMALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;
    public MySyntherizer getSynthesizer(){
        return synthesizer;
    }

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
//            usbHidDevice();
        } catch (ServiceOccupiedException e) {
            e.printStackTrace();
        } catch (ReloginException e) {
            e.printStackTrace();
        } catch (UnsupportMultiProcess unsupportMultiProcess) {
            unsupportMultiProcess.printStackTrace();
        } catch (RequestException e) {
            e.printStackTrace();
        }
        initialTts();
        openKeyboard();
//        initUniversalImageLoader();
//        initImagePicker();

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
                    //数字输入
//                    if(keyCode > 0 && keyCode <=5 || keyCode >=14 && keyCode <= 16
//                            || keyCode ==9 || keyCode == 19 || keyCode == 31){
//                        EventBus.getDefault().post(new WxAmountMessageEvent(keyName));
//                        return;
//                    }

                    //设置
                    if (keyName.equalsIgnoreCase(PayKeyboard.KEY_LIST)){
                        EventBus.getDefault().post(new SendVoidMessageEvent());
                    }else if(keyName.equalsIgnoreCase(PayKeyboard.KEY_PAY)){
                        //如果有输入键盘，则这边响应事件要过滤，否则重复监听onPay
                        if (mIrequest!=null && mIrequest.getMoney()>0){
                            return;
                        }
                        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        isPayBySaler = preference.getBoolean(getString(R.string.key_pre_pay_saler),false);
                        isPayByHID = preference.getBoolean(getString(R.string.key_pre_pay_self),true);
                        //如果是HID模式，不需要输入金额
                        if (isPayByHID){
                            EventBus.getDefault().post(new WxFacePayMessageEvent("0"));
                        }else{
                              //进入金额显示界面
                            speak("请输入交易金额");
                            ToastUtil.toast("金额输入格式错误");
                        }
                    }else if(keyName.equalsIgnoreCase(PayKeyboard.KEY_OPT)){
                        EventBus.getDefault().post(new KeyBoardCancelMessageEvent());
                    }else if (keyName.equalsIgnoreCase(PayKeyboard.KEY_FACE_PAY)){
                        //按刷脸键进入刷脸
                        if (System.currentTimeMillis() - mLastClickTime >= TIME_INTERVAL) {
                            mLastClickTime = System.currentTimeMillis();
                            EventBus.getDefault().post(new WxFacePayMessageEvent("0"));
                        } else {

                        }

//                        if (isPayByHID){
//                            EventBus.getDefault().post(new WxFacePayMessageEvent("0"));
//                        }else{
////                            EventBus.getDefault().post(new WxGoShowAmountMessageEvent(""));
//                        }
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

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(handler);

        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
//                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                         Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        synthesizer = new NonBlockSyntherizer(this, initConfig, handler); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "6");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "6");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "7");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
        }
        return offlineResource;
    }
    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    TextToSpeech tts;
    public void speak(String text) {


        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        if (synthesizer!=null){

            int result = synthesizer.speak(text);
            checkResult(result, "speak");
        }

    }
    private void checkResult(int result, String method) {
        if (result != 0) {
            LogUtils.i("lincb","error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

//    private void  usbHidDevice(){
//        usbHidDevice = UsbHidDevice.getInstance();
//        int resultCode = usbHidDevice.open(0);
//        if (resultCode == UsbHidDevice.ERROR_NONE){
//            usbHidDevice.setMode(UsbHidDevice.ModeType.HID_SEND_MODE, UsbHidDevice.ModeValue.CHINSE_MODE_ORIGIN);
//        }else{
//            Log.d("wxPay","打开USBHID失败");
//            usbHidDevice = null;
//        }
//    }

//    private void initUniversalImageLoader() {
//        //初始化ImageLoader
//        ImageLoader.getInstance().init(
//                ImageLoaderConfiguration.createDefault(getApplicationContext()));
//    }
//
//    /**
//     * 初始化仿微信控件ImagePicker
//     */
//    private void initImagePicker() {
//        ImagePicker imagePicker = ImagePicker.getInstance();
//        imagePicker.setImageLoader(new UILImageLoader());   //设置图片加载器
//        imagePicker.setShowCamera(true);  //显示拍照按钮
//        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
//        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
//        imagePicker.setSelectLimit(9);    //选中数量限制
//        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
//        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
//        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
//        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
//        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
//    }
}
