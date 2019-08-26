package com.android.landicorp.f8face.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.landicorp.f8face.F8Application;
import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.baidu.controller.MySyntherizer;
import com.android.landicorp.f8face.data.WxFacePayData;
import com.android.landicorp.f8face.http.ReturnXMLParser;
import com.android.landicorp.f8face.inter.TradeStatusInter;
import com.android.landicorp.f8face.util.AppUtil;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.util.ToastUtil;
import com.android.landicorp.f8face.view.SpotsDialog;
import com.geekmaker.paykeyboard.IPayRequest;
import com.geekmaker.paykeyboard.PayKeyboard;
import com.landi.finance.face.aidl.FaceService;
import com.landi.finance.face.aidl.constant.Constant;
import com.landicorp.android.eptapi.device.UsbHidDevice;
import com.landicorp.android.eptapi.utils.IntegerBuffer;
import com.landicorp.android.scan.decode.DecodeEngine;
import com.landicorp.android.scan.scanDecoder.ScanDecoderParameter;
import com.landicorp.android.scan.util.LogUtils;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.tencent.wxpayface.IWxPayFaceCallbackAIDL;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.tencent.wxpayface.WxfacePayCommonCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by admin on 2019/6/18.
 */

public class F8BaseCameraActivity extends com.serenegiant.common.BaseActivity implements TradeStatusInter{
    private final Object mSync = new Object();
    private USBMonitor mUSBMonitor;
    private boolean isActive, isPreview;
    private Surface mPreviewSurface;
    protected SurfaceView mUVCCameraView;
    private BeepManager beepManager;
    protected SharedPreferences preference;
    protected static final int MSG_DO_FACE_PAY = 0;
    protected static final int MSG_DO_QR_PAY = 1;
    protected static final int MSG_DO_SCAN_PAY = 2;
    protected static final int MSG_DO_START_SCAN = 3;
    protected static final int MSG_SHOW_AD = 4;
    protected static final int MSG_STOP_AD = 5;
    protected static final int MSG_CANCEL_PAY = 6;
    protected static final int MSG_DO_STOP_SCAN = 7;
    protected static final int MSG_DO_RESTART = 8;
    protected  boolean isCanScan = true;
    public static FaceService face;
    public static final String TAG = "F8BaseCameraActivity";
    private String mAuthInfo;
    public boolean isPayByHID,isPayBySaler;
    private F8Application f8Application;
    private MySyntherizer mySyntherizer;
    protected UVCCamera mUVCCamera;
    protected Set<String> images;
    protected LinearLayout scanLayout;
    protected ImageView ivScannerIcon;
    private TranslateAnimation translateAnimation;
    private AlertDialog progressDialog;

    /**
     * HID透传数据
     */
    public void sendDataByHID(String data){
        if (isPayByHID&&usbHidDevice!=null){
            IntegerBuffer integerBuffer = new IntegerBuffer();
            integerBuffer.setData(data.length());
            int result = usbHidDevice.write(data.getBytes(),integerBuffer,0);
            if (result==0){
                speak("请等待收银员确认支付结果");
            }
            Log.d("wxPay","通过HID写入数据结果="+result);
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toast("获取USBHID设备为空。");
                }
            });
        }
    }

    protected Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_DO_SCAN_PAY:
                    //发起扫码支付
                    speak("扫码成功");
                    handler.sendEmptyMessage(MSG_DO_STOP_SCAN);
                    String faceCode = msg.getData()+"\r\n";
                    sendDataByHID(faceCode);
                    handler.sendEmptyMessage(MSG_DO_START_SCAN);
                    Intent mIntent = new Intent(F8BaseCameraActivity.this,F8WaitForResultActivity.class);
                    break;
                case MSG_DO_START_SCAN:
                    handler.postDelayed(()->{
                        try {
                            isCanScan = true;
                        }catch (Exception e){
                        }
                    },1500);
                    break;
                case MSG_DO_STOP_SCAN:
                    handler.post(()->{
                        isCanScan = false;
                    });
                    break;
            }

        }
    };

    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            face = null;
            Toast.makeText(F8BaseCameraActivity.this, "SDK绑定失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            face = FaceService.Stub.asInterface(service);

        }
    };
    public void bindService() {
        Intent intent = new Intent();
        intent.setPackage(Constant.SERVICE_PACKAGE);
        intent.setAction(Constant.SERVICE_ACTION);
        try{
            bindService(intent, connService, Context.BIND_AUTO_CREATE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FullScreen.fullScreen(this);
        FullScreen.NavigationBarStatusBar(this,true);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        f8Application = (F8Application)getApplication();
        payKeyboard = f8Application.getPayKeyboard();
        mySyntherizer = f8Application.getSynthesizer();
        ToastUtil.init(this);
        beepManager = new BeepManager(this);
        beepManager.updatePrefs();
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        isPayBySaler = preference.getBoolean(getString(R.string.key_pre_pay_saler),true);
        isPayByHID = preference.getBoolean(getString(R.string.key_pre_pay_self),false );
        progressDialog = new SpotsDialog.Builder().setContext(this).build();
    }


    @Override
    public void finish(){
        super.finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onStop() {
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        FullScreen.NavigationBarStatusBar(this,true);
        super.onResume();
//        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
//        synchronized (mSync) {
//            if (mUSBMonitor != null) {
//                mUSBMonitor.register();
//                List<UsbDevice> devices = mUSBMonitor.getDeviceList();
//                if (devices!=null&&devices.size()>0){
//                    for (UsbDevice device:devices){
//                        if (device.getProductId() == 515 && device.getVendorId() == 11707){
//                            mUSBMonitor.requestPermission(device);
//                        }
//                    }
//                }
//            }
//        }
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i("KeyboardUI","activity start!!!!!!");
        //获取设置界面数据
        images = preference.getStringSet("Images",null);
        isPayBySaler = preference.getBoolean(getString(R.string.key_pre_pay_saler),true);
        isPayByHID = preference.getBoolean(getString(R.string.key_pre_pay_self),false );
        updateSignal();
        mUVCCameraView = (SurfaceView)findViewById(R.id.camera_surface_view);
        mUVCCameraView.getHolder().addCallback(mSurfaceViewCallback);

        int ret = DecodeEngine.getInstance().init(true,true);
        if(ret!=0){
            String msg = null;
            switch(ret){
                case 1:
                    msg = "初始化失败";
                    break;
                case 2:
                    msg = "已经初始化过";
                    break;
                case 3:
                    msg = "license激活失败";
                    break;
            }

        }
        scanLayout.post(new Runnable() {
            @Override
            public void run() {
                int yDelta = scanLayout.getTop();
                yDelta = ivScannerIcon.getTop();
                translateAnimation = new TranslateAnimation(0,0,0,-yDelta-5);
                translateAnimation.setDuration(500);
                translateAnimation.setFillEnabled(true);
                translateAnimation.setFillAfter(false);
                translateAnimation.setFillBefore(true);//动画停留在第一帧
                translateAnimation.setStartOffset(500);
                translateAnimation.setRepeatCount(Animation.INFINITE);
                translateAnimation.setRepeatMode(Animation.RESTART);
                ivScannerIcon.startAnimation(translateAnimation);
            }
        });
        if (isPayByHID||this.getClass().isInstance(F8ShowAmountActivity.class)){
            scanLayout.setVisibility(View.VISIBLE);
        }else{
            scanLayout.setVisibility(View.GONE);
        }
        scanLayout.setVisibility(View.GONE);
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
                isActive = isPreview = false;
            }
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        final UVCCamera camera = new UVCCamera();
                        try {
                            camera.open(ctrlBlock);
//                            camera.getValue(UVCCamera.PU_BACKLIGHT);
                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                        } catch (final Exception e) {
                            try {
                                // fallback to YUV mode
                                camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                            } catch (final IllegalArgumentException e1) {
                                camera.destroy();
                                return;
                            }
                        }
                        mPreviewSurface = mUVCCameraView.getHolder().getSurface();
                        if (mPreviewSurface != null) {
                            isActive = true;
                            camera.setPreviewDisplay(mPreviewSurface);
                            camera.startPreview();
                            isPreview = true;
                        }
                        synchronized (mSync) {
                            mUVCCamera = camera;
                            //打开摄像头将摄像头设置为扫码模式
                            setScanMode();
                            mUVCCamera.setFrameCallback(new IFrameCallback() {
                                @Override
                                public void onFrame(ByteBuffer frame) {
                                    String resultTextString = null;
                                    try {
                                        byte[] b = new byte[frame.remaining()];
                                        frame.get(b, 0, b.length);
                                        resultTextString = DecodeEngine.getInstance().decode(b, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT);
//										SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddHHmmss");
//										Util.saveDataToFile(b,"lincb",dateFormat.format(new Date())+".jpeg");

//										saveBitmap(b,UVCCamera.DEFAULT_PREVIEW_WIDTH,UVCCamera.DEFAULT_PREVIEW_HEIGHT);
                                        Log.d("lincb","扫码结果 = "+resultTextString);
                                        if (!TextUtils.isEmpty(resultTextString)){
                                            if(ScanDecoderParameter.getBeepMode()==ScanDecoderParameter.USE_SPEAKER){
                                                beepManager.playBeepSoundAndVibrate();
                                            }
                                            Log.d("lincb","扫码结果 = "+resultTextString);
//											stringBuffer.append(resultTextString+"\n");
                                            Message message = new Message();
                                            message.obj = resultTextString.toString();
                                            handler.sendMessage(message);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, UVCCamera.PIXEL_FORMAT_YUV420SP);
                        }
                    }
                }
            }, 0);

        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            // XXX you should check whether the comming device equal to camera device that currently using
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.close();
                            if (mPreviewSurface != null) {
                                mPreviewSurface.release();
                                mPreviewSurface = null;
                            }
                            isActive = isPreview = false;
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(F8BaseCameraActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {

        }
    };

    private final SurfaceHolder.Callback mSurfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            if ((width == 0) || (height == 0)) return;
            mPreviewSurface = holder.getSurface();
            synchronized (mSync) {
                if (isActive && !isPreview && (mUVCCamera != null)) {
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                    isPreview = true;
                }
            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                isPreview = false;
            }
            mPreviewSurface = null;
        }
    };

    public void closeCamera(){
        synchronized (mSync) {
            isActive = isPreview = false;
            if (mUVCCamera != null) {
                //关闭摄像头之前设置为刷脸模式
                setFaceMode();
                mUVCCamera.stopPreview();
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
            mUVCCameraView = null;
        }
    }

    @Override
    protected synchronized void onDestroy() {
        synchronized (mSync){
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        super.onDestroy();
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    public void speak(String text) {
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        if (mySyntherizer!=null){
            int result = mySyntherizer.speak(text);
            checkResult(result, "speak");
        }

    }

    boolean isSuccess;
    public boolean isSuccessInfo(Map info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (info == null) {
                        ToastUtil.toast("调用返回为空, 请查看日志");
//                    new RuntimeException("调用返回为空").printStackTrace();
                        onTradeFaile();
                        isSuccess = false;
                    }
                    String code = (String)info.get(WxFacePayData.RETURN_CODE);
                    String msg = (String)info.get(WxFacePayData.RETURN_MSG);
                    Log.d(TAG, "response | getWxpayfaceRawdata " + code + " | " + msg);
                    if (code == null || !code.equals(WxfacePayCommonCode.VAL_RSP_PARAMS_SUCCESS)) {
                        ToastUtil.toast("调用返回非成功信息, 请查看日志");
                        onTradeFaile();
//                    new RuntimeException("调用返回非成功信息: " + msg).printStackTrace();
                        isSuccess = false;
                    }
                    Log.d(TAG, "调用返回成功");
                }catch (Exception e){

                }

            }
        });
        isSuccess =true;
        return isSuccess;
    }

    /**
     * 发起微信人脸支付
     */
    public void doWxFacePay(String amount){
        closeCamera();
        WxPayFace.getInstance().initWxpayface(this, new IWxPayFaceCallbackAIDL.Stub() {
            @Override
            public void response(Map info) throws RemoteException {
                if (!isSuccessInfo(info)) {
                    onTradeFaile();
                    return;
                }
                WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
                    @Override
                    public void response(Map info) throws RemoteException {
                        if (!isSuccessInfo(info)) {
                            onTradeFaile();
                            return;
                        }
                        Log.d(TAG, "response | getWxpayfaceRawdata" );
                        String rawdata = info.get("rawdata").toString();
                        try {
                            getAuthInfo(amount,rawdata);
                        } catch (Exception e) {
                            onTradeFaile();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }

    /**
     *
     * 请求后台获取人脸SDK调用凭证
     * 上一步：getWxpayfaceRawdata
     * @param
     * @return
     */
    public void getAuthInfo(String amount,String rawdata) throws IOException {
        Log.d(TAG, "enter | getAuthInfo ");
        try {
            progressDialog.show();
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };



            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
//            OkHttpClient client = OKHttpUtil.customClient(this);

            RequestBody body = RequestBody.create(null, rawdata);

            Request request = new Request.Builder()
                    .url("https://wxpay.wxutil.com/wxfacepay/api/getWxpayFaceAuthInfo.php")
                    .post(body)
                    .build();

            client.newCall(request)
                    .enqueue(new Callback() {

                        @Override
                        public void onFailure(okhttp3.Call call, IOException e) {
                            progressDialog.dismiss();
//                            showMes("onFailure | getAuthInfo " + e.toString());
                            Log.d(TAG, "onFailure | getAuthInfo " + e.toString());
                        }

                        @Override
                        public void onResponse(okhttp3.Call call, Response response) throws IOException {
                            try {
//                                showMes("onFailure | getAuthInfo ");
                                progressDialog.dismiss();
                                //0eqswg1dJXrl/1YcqZOTb9yOQsEzRNSjKlFMILZsoxwEn8zZyKxwU79DlnOLc/A55uvSz60lvmR0oDMMdjxuUnlxJRcz4h8YEI16NbTNrKFzxC5CXeLgMIaeOF7eL31o2DRqMg/0NRkCHkZf3pIKqfybNTQ3yL7+LzuDBPBENI572M1S3M4Aoz4dlnqAk9OmpCBuTYpD2lAuxlhDNTKqjlFvLWwJCVo3Xc/I6nj7OIIR2pt8YOwOYoMsTslpDtDU0nMvSN/Yk9mpTxMgZctRdTE7YRU6h0668FClzGfHrO3ZYO55rkeabsWSm/4lxFkWW4jIaw1Op0bVQeTxeTDNgX2HGTC2V/fuKEU1PjX/EfETCJdkKBLJwe6yg35OBxjcEiyIuz319aYLOfvyMx51qE5kF0/fx8XCfv5PZcd22v2g48NXZjyeojWcoLjmdgAxk9Z68rPTJBApVFFenzPecbZwqZsSkZDmYPW1OhskDRv5NRuZ5yqV8sgPZgNZXy2kErRZ+R7D93zrMDAFg0dRWyZWxnXs8bkmoFQFnW1uv5LrjnbpYDUhjvHDh2cIgrRzJHMB0Ecei36ehSSyG8o739mngwfQNigSf7W4YfG2TSNjggcnjkJvagoMXMkbgLQ=
                                mAuthInfo = ReturnXMLParser.parseGetAuthInfoXML(response.body().byteStream());
                                //获取人脸SDK调用凭证成功
                                HashMap params = new HashMap();
                                params.put(WxFacePayData.PARAMS_FACE_AUTHTYPE, "FACEPAY");
                                params.put(WxFacePayData.PARAMS_APPID, WxFacePayData.WECHAT_APP_ID_TEST);
                                params.put(WxFacePayData.PARAMS_MCH_ID, WxFacePayData.WECHAT_MCH_ID_TEST);
                                params.put(WxFacePayData.PARAMS_STORE_ID, WxFacePayData.WECHAT_STORE_ID_TEST);
                                params.put(WxFacePayData.PARAMS_OUT_TRADE_NO, "" + (System.currentTimeMillis() / 100000));
                                params.put(WxFacePayData.PARAMS_CODE_TYPE,isPayBySaler?"0":"1");
                                params.put(WxFacePayData.PARAMS_TELEPHONE, "");
                                params.put(WxFacePayData.PARAMS_ASK_FACE_PERMIT,"");
                                String strAmount = String.valueOf(new BigDecimal(100).multiply(new BigDecimal(amount)).intValue());
                                params.put(WxFacePayData.PARAMS_TOTAL_FEE, strAmount);
                                params.put(WxFacePayData.PARAMS_ASK_RET_PAGE,"0");
                                params.put(WxFacePayData.PARAMS_AUTHINFO, mAuthInfo);
//                                params.put(WxFacePayData.PARAMS_IGNORE_PAY_RESULT,"1");
                                WxPayFace.getInstance().getWxpayfaceCode(params, new IWxPayfaceCallback() {
                                    @Override
                                    public void response(Map info) throws RemoteException {
                                        runOnUiThread(new Runnable() {
                                            final String code = info.get("return_code").toString();
                                            @Override
                                            public void run() {
                                                if (TextUtils.equals(code, WxfacePayCommonCode.VAL_RSP_PARAMS_SUCCESS)){
                                                    Log.d(TAG, "response | getWxpayfaceCode" );
                                                    final String faceCode = (String)info.get(WxFacePayData.PARAMS_FACE_CODE);
//                                                    final String faceCode = "134602233436552566";
                                                    IntegerBuffer integerBuffer = new IntegerBuffer();
                                                    integerBuffer.setData(faceCode.length());
                                                    if (isPayByHID&&usbHidDevice!=null){
                                                        Log.d("wxPay","开始通过HID写入数据");
                                                        int result = usbHidDevice.write(faceCode.getBytes(),integerBuffer,0);
                                                        Log.d("wxPay","通过HID写入数据结果="+result);
                                                        startActivity(new Intent(F8BaseCameraActivity.this,F8WaitForResultActivity.class));
                                                        return;
                                                    }
                                                    if (TextUtils.equals(code, WxfacePayCommonCode.VAL_RSP_PARAMS_SUCCESS)) {
                                                        HashMap<String,String> map = new HashMap<>();
                                                        map.put(WxFacePayData.PARAMS_APPID,WxFacePayData.WECHAT_APP_ID_TEST);
                                                        map.put(WxFacePayData.PARAMS_MCH_ID,WxFacePayData.WECHAT_MCH_ID_TEST);
                                                        map.put(WxFacePayData.PARAMS_STORE_ID,WxFacePayData.WECHAT_STORE_ID_TEST);
                                                        map.put(WxFacePayData.PARAMS_AUTHINFO,mAuthInfo);
                                                        map.put(WxFacePayData.PARAMS_PAY_RESULT,WxFacePayData.RETURN_SUCCESS);
                                                        onTradeSucc();
                                                        if (isPayBySaler){
                                                            finish();
                                                        }
                                                        startActivity(new Intent(F8BaseCameraActivity.this,ResultSuccActivity.class));
//                                                        WxPayFace.getInstance().updateWxpayfacePayResult(map, new IWxPayfaceCallback() {
//                                                            @Override
//                                                            public void response(Map info) throws RemoteException {
//                                                                //跳转到
//                                                                if (isSuccessInfo(info)){
//                                                                    onTradeSucc();
//                                                                    if (isPayBySaler){
//                                                                        finish();
//                                                                    }
//                                                                    startActivity(new Intent(F8BaseCameraActivity.this,ResultSuccActivity.class));
//                                                                }
//                                                            }
//                                                        });
                                                    }
                                                }else if (TextUtils.equals(code, WxfacePayCommonCode.VAL_RSP_PARAMS_USER_CANCEL)) {
                                                    ToastUtil.toast("用户取消");
                                                    onTradeCancel();
                                                    // mResultTxt.setText("用户取消");
                                                } else if (TextUtils.equals(code, WxfacePayCommonCode.VAL_RSP_PARAMS_SCAN_PAYMENT)) {
                                                    //mResultTxt.setText("扫码支付");
                                                    ToastUtil.toast("扫码支付");
                                                }else{
                                                    ToastUtil.toast("失败，错误码:"+code);
                                                    onTradeFaile();
                                                }
                                            }
                                        });


                                    }
                                });



                            } catch (Exception e) {
                                showMes("onFailure | getAuthInfo " + e.toString());
                                progressDialog.dismiss();
                                e.printStackTrace();
                            }
                            Log.d(TAG, "onResponse | getAuthInfo " + mAuthInfo);

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showMes("onFailure | getAuthInfo " + e.toString());
            progressDialog.dismiss();
            throw new RuntimeException(e);
        }
    }

    private void showMes(String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.toast(msg);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onTradeCancel();
        this.finish();
    }


    private PayKeyboard payKeyboard;
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
        if(payKeyboard!=null && !payKeyboard.isReleased()) payKeyboard.updateSign(w,g);
    }

    @Override
    public void onTradeSucc() {
        WxPayFace.getInstance().releaseWxpayface(this);
        IPayRequest iPayRequest = f8Application.getmIrequest();
        if (iPayRequest!=null){
            iPayRequest.setResult(true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (payKeyboard!=null&&!payKeyboard.isReleased()){
                        payKeyboard.reset();
                    }
                }
            },2000);
        }
    }

    @Override
    public void onTradeFaile() {
        WxPayFace.getInstance().releaseWxpayface(this);
        if (isPayBySaler){
            finish();
        }
        IPayRequest iPayRequest = f8Application.getmIrequest();
        if (iPayRequest!=null){
            iPayRequest.setResult(false);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (payKeyboard!=null&&!payKeyboard.isReleased()){
                        payKeyboard.reset();
                    }
                }
            },2000);
        }
    }

    @Override
    public void onTradeCancel() {
        WxPayFace.getInstance().releaseWxpayface(this);
        if (isPayBySaler){
            finish();
        }
        IPayRequest iPayRequest = f8Application.getmIrequest();
        if (iPayRequest!=null){
            iPayRequest.setResult(false,true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (payKeyboard!=null&&!payKeyboard.isReleased()){
                        payKeyboard.reset();
                    }
                }
            },2000);
        }
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            LogUtils.i("lincb","error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    /**
     * 设置微信扫码模式
     */
    public void setScanMode(){
        if (mUVCCamera!=null){
            mUVCCamera.updateCameraParams();
            mUVCCamera.setValue(UVCCamera.PU_BACKLIGHT,100);
        }
    }

    /**
     *  设置微信刷脸模式
     */
    public void setFaceMode(){
        try{
            if (mUVCCamera!=null){
                mUVCCamera.updateCameraParams();
                mUVCCamera.setValue(UVCCamera.PU_BACKLIGHT,50);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    protected UsbHidDevice usbHidDevice;    //虚拟HID通讯
    protected void  usbHidDevice(){
        usbHidDevice = UsbHidDevice.getInstance();
        int resultCode = usbHidDevice.open(0);
        if (resultCode == UsbHidDevice.ERROR_NONE){
            usbHidDevice.setMode(UsbHidDevice.ModeType.HID_SEND_MODE, UsbHidDevice.ModeValue.CHINSE_MODE_ORIGIN);
        }else{
            Log.d("wxPay","打开USBHID失败");
            usbHidDevice = null;
        }
    }

}
