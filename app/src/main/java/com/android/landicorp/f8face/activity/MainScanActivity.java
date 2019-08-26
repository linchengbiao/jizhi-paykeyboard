package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.TransitionDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.landicorp.f8face.IMI.DecodePanel;
import com.android.landicorp.f8face.IMI.GLPanel;
import com.android.landicorp.f8face.IMI.LdBitmapFactory;
import com.android.landicorp.f8face.IMI.SimpleViewer;
import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.inter.SendVoidMessageEvent;
import com.android.landicorp.f8face.inter.WxFacePayMessageEvent;
import com.android.landicorp.f8face.util.BitmapUtil;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.util.ToastUtil;
import com.android.landicorp.f8face.view.GlideImageLoader;
import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiDeviceAttribute;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.ImiFrameType;
import com.hjimi.api.iminect.ImiNect;
import com.hjimi.api.iminect.ImiPixelFormat;
import com.hjimi.api.iminect.ImiPropertIds;
import com.landicorp.android.scan.decode.DecodeEngine;
import com.tencent.wxpayface.WxPayFace;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainScanActivity extends F8BaseCameraActivity implements View.OnClickListener{
    private LinearLayout faceScanLayout,payByHIDLayout,payBySalerLayout;

    private Banner banner;
    private ImageView ivVoice;
    private BeepManager beepManager;
    private boolean isActive, isPreview;
    private Surface mPreviewSurface;
    private TextView tvAmount;
    private boolean openVoice = true;
    private GLPanel mGLPanel;
    private SimpleViewer mViewer;
    private MainListener mainlistener;
    private DecodePanel mDecodePanel;
    private Surface mSurface;
    private ImiDevice mDevice;
    private ImiFrameMode mCurrMode = null;
    private ImiDeviceAttribute mDeviceAttribute = null;
    private static final int DEVICE_OPEN_SUCCESS = 0;
    private static final int DEVICE_OPEN_FALIED = 1;
    private static final int DEVICE_DISCONNECT = 2;
    private static final int MSG_EXIT = 5;
    public static final int CAMERA_PREVIEW_WIDTH = 640;
    public static final int CAMERA_PREVIEW_HEIGHT = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FullScreen.fullScreen(this);
        FullScreen.NavigationBarStatusBar(this,true);
        setContentView(R.layout.activity_main_scan);
        payByHIDLayout = (LinearLayout)findViewById(R.id.ll_pay_HID);
        payBySalerLayout = (LinearLayout)findViewById(R.id.ll_pay_saler);
        tvAmount = (TextView)findViewById(R.id.tv_amount);
        banner = (Banner)findViewById(R.id.barner);
        scanLayout = (LinearLayout)findViewById(R.id.ll_scan);
        ivScannerIcon = (ImageView)findViewById(R.id.iv_scanner_icon);
        ivVoice = (ImageView)findViewById(R.id.iv_voice);
        ivVoice.setOnClickListener(this);
        scanLayout.setOnClickListener(this);
        faceScanLayout = (LinearLayout)findViewById(R.id.ll_face_scan);
        faceScanLayout.setOnClickListener(this);
        mUVCCameraView = (SurfaceView)findViewById(R.id.camera_surface_view);
        mGLPanel = (GLPanel) findViewById(R.id.sv_color_view);
        ImiNect.initialize();
        mDevice = ImiDevice.getInstance();
        mainlistener = new MainListener();
        mUVCCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mSurface = surfaceHolder.getSurface();
                mDecodePanel = new DecodePanel();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mDecodePanel.stopDecoder();
            }
        });
    }

    public void initIndicatorView(){

        List<Uri> imageUriList = new ArrayList<>();
        if (images!=null&&images.size()>0){
            Iterator<String> iterator = images.iterator();
            while (iterator.hasNext()){
                String path = iterator.next();
                Uri uri = BitmapUtil.getImageContentUri(this,path);
                imageUriList.add(uri);
            }
        }else{
            final Integer[] resArray = new Integer[] { R.drawable.img_f8_standby_3, R.drawable.img_f8_standby_4, R.drawable.img_f8_standby_5 };
            imageUriList = BitmapUtil.convertResIdToUrl(this,resArray);
        }

        //设置banner样式
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(imageUriList);
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.Default);
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置不需要指示器
        banner.updateBannerStyle(BannerConfig.NOT_INDICATOR);
        //设置轮播时间
        banner.setDelayTime(2000);
        //设置指示器位置（当banner模式中有指示器时）
//        banner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
        banner.start();

    }
    private LocationManager locationManager;
    private String provider;
    @Override
    protected void onResume() {
        super.onResume();
        if (isPayByHID){
            usbHidDevice();
        }
        initIndicatorView();
        //显示对应的付款界面
        payByHIDLayout.setVisibility(isPayBySaler?View.GONE:View.VISIBLE);
        payBySalerLayout.setVisibility(isPayByHID?View.GONE:View.VISIBLE);
        isCanScan = true;
        Log.d("lincb","onResume");
        new Handler().postDelayed(new OpenDeviceRunnable(),500);
        if(mViewer != null){
            mViewer.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("lincb","onPause");
        releaseScanCamera();
    }

    /**
     * 顶部扫码动画
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            final TransitionDrawable background = (TransitionDrawable) faceScanLayout.getBackground();
            background.setCrossFadeEnabled(true);
            background.setChangingConfigurations(ActivityInfo.CONFIG_COLOR_MODE);
            background.startTransition(500);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.postDelayed(runnable,500);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_scan:
                Toast.makeText(MainScanActivity.this,getString(R.string.title_scan_pay_tips2),Toast.LENGTH_SHORT).show();
                break;
            case R.id.ll_face_scan:
                //调用人脸支付库
                doFacePay("0");
                break;
            case R.id.iv_voice:
                if (!openVoice){
                    openVoice = true;
                    ivVoice.setImageResource(R.drawable.ic_volume_off);
                }else{
                    openVoice = false;
                    ivVoice.setImageResource(R.drawable.ic_volume_high);
                }
                break;
        }
    }

    public void doFacePay(String amount){
        //如果是独立收银模式，跳转进入显示金额界面
        if (isPayBySaler){
            Intent mIntent = new Intent(this,F8ShowAmountActivity.class);
            mIntent.putExtra("Amount",amount);
            startActivity(mIntent);
        }else{
            doWxFacePay(amount);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxFacePayMessageEvent event) {
        doFacePay(event.mAmount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SendVoidMessageEvent event){
        closeCamera();
        startActivity(new Intent(getApplicationContext(), F8SettingActivity.class));
    }
    private MyHandler MainHandler = new MyHandler(this);

    private class MainListener implements ImiDevice.OpenDeviceListener{

        @Override
        public void onOpenDeviceSuccess() {
            //open device success.
            Log.d("lincb","打开摄像头成功");

            mDeviceAttribute = mDevice.getAttribute();
            mDevice.setProperty(ImiPropertIds.IMI_CAM_PROPERTY_COLOR_BACKLIGHT_COMPENSATION,2);
            MainHandler.sendEmptyMessage(DEVICE_OPEN_SUCCESS);
        }

        @Override
        public void onOpenDeviceFailed(String errorMsg) {
            //open device falied.
            MainHandler.sendMessage(MainHandler.obtainMessage(DEVICE_OPEN_FALIED, errorMsg));
        }
    }

    private class OpenDeviceRunnable implements Runnable{

        @Override
        public void run() {
            if (mDevice!=null){
                mDevice.close();
            }
            ImiFrameMode frameMode = new ImiFrameMode(ImiPixelFormat.IMI_PIXEL_FORMAT_IMAGE_RGB24, CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT, 30);
            mDevice.setFrameMode(ImiDevice.ImiStreamType.COLOR, frameMode);
            mCurrMode = frameMode;
            Log.d("lincb","准备打开摄像头");
            mDevice.open(MainScanActivity.this, ImiDevice.ImiStreamType.COLOR.toNative(), mainlistener);
        }
    }
    private class ExitRunnable implements Runnable {

        @Override
        public void run() {
            if(mDevice != null) {
                mDevice.setProperty(ImiPropertIds.IMI_CAM_PROPERTY_COLOR_BACKLIGHT_COMPENSATION,1);
                mDevice.close();
                Log.d("lincb","close camera");
            }
        }
    }

    private void runViewer() {
        Log.d("lincb","runViewer扫码开启 = "+isCanScan);
        mViewer = new SimpleViewer(mDevice, ImiFrameType.COLOR);
        mViewer.setOnGetFrameResult(new SimpleViewer.OnGetFrameResult() {
            @Override
            public void onFrame(final ByteBuffer byteBuffer) {
                try {
                    if (isCanScan){
                        int len = byteBuffer.capacity();
                        byte[] yuv = new byte[len];
                        byteBuffer.get(yuv);
                        yuv = LdBitmapFactory.createYUVData(yuv,CAMERA_PREVIEW_WIDTH,CAMERA_PREVIEW_HEIGHT);
                        String resultTextString = DecodeEngine.getInstance().decode(yuv, CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT);
//                        Log.d("lincb","扫码结果 = "+resultTextString);
                        if (!TextUtils.isEmpty(resultTextString)&&isCanScan){
                            if (beepManager!=null){
                                beepManager.playBeepSoundAndVibrate();
                            }
                            Log.d("lincb","扫码结果 = "+resultTextString);
                            isCanScan = false;
                            Message message = new Message();
                            message.obj = resultTextString+"\r\n";
                            message.what = MSG_DO_SCAN_PAY;
                            handler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void getFrameFail() {

            }
        });
        switch (mCurrMode.getFormat())
        {
            case IMI_PIXEL_FORMAT_IMAGE_H264:
                mDecodePanel.initDecoder(mSurface, mCurrMode.getResolutionX(),
                        mCurrMode.getResolutionY());
                mViewer.setDecodePanel(mDecodePanel);
                break;
            case IMI_PIXEL_FORMAT_IMAGE_YUV420SP:
                ImiDevice.ImiFrame frame = mDevice.readNextFrame(ImiDevice.ImiStreamType.COLOR,0);
                ByteBuffer byteBuffer = frame.getData();
                String resultTextString = null;
                try {
                    byte[] b = new byte[byteBuffer.remaining()];
                    byteBuffer.get(b, 0, b.length);
                    resultTextString = DecodeEngine.getInstance().decode(b, 640, 480);
                    if (!TextUtils.isEmpty(resultTextString)){
                        Log.d("lincb","扫码结果 = "+resultTextString);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                mUVCCameraView.setVisibility(View.GONE);
                mGLPanel.setVisibility(View.VISIBLE);
                mViewer.setGLPanel(mGLPanel);
                break;
        }

        mViewer.onStart();
    }

    class MyHandler extends Handler {
        WeakReference<MainScanActivity> mActivity;
        public MyHandler(MainScanActivity activity) {
            mActivity = new WeakReference<MainScanActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainScanActivity mainActivity = mActivity.get();
            switch (msg.what)
            {
                case DEVICE_OPEN_FALIED:
                    ToastUtil.toast("摄像头被占用无法打开。");
                    break;
                case DEVICE_DISCONNECT:
                    break;
                case DEVICE_OPEN_SUCCESS:
                    mainActivity.runViewer();
                    break;
                case MSG_EXIT:
                    mainActivity.Exit();
                    break;
            }
        }
    }

    private void Exit() {
//        finish();
        ImiDevice.destroy();
        ImiNect.destroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 释放扫码摄像头
     */
    public void releaseScanCamera(){
        new Thread(new ExitRunnable()).start();

    }
    public void releaseWxFace(){
        Log.d("lincb","releaseWxFace");
        WxPayFace.getInstance().releaseWxpayface(this);
    }
}
