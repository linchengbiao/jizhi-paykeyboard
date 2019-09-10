package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.TransitionDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.landicorp.f8face.IMI.DecodePanel;
import com.android.landicorp.f8face.IMI.GLPanel;
import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.inter.SendVoidMessageEvent;
import com.android.landicorp.f8face.inter.WxAmountMessageEvent;
import com.android.landicorp.f8face.inter.WxFacePayMessageEvent;
import com.android.landicorp.f8face.inter.WxGoShowAmountMessageEvent;
import com.android.landicorp.f8face.util.BitmapUtil;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.util.MoneyEditUtils;
import com.android.landicorp.f8face.view.GlideImageLoader;
import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiNect;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainScanActivity extends F8BaseCameraActivity implements View.OnClickListener{
    private LinearLayout faceScanLayout,payByHIDLayout,payBySalerLayout;

    private Banner banner;
    private ImageView ivVoice;
    private BeepManager beepManager;
    private boolean isActive, isPreview;
    private Surface mPreviewSurface;
    private TextView tvAmount;
    private boolean openVoice = true;
    private EditText tvAmountValue;

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
        tvAmountValue = (EditText) findViewById(R.id.et_amount);
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
        WxPayFace.getInstance().initWxpayface(getApplicationContext(), new IWxPayfaceCallback() {
            @Override
            public void response(Map map) throws RemoteException {

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
            final Integer[] resArray = new Integer[] { R.drawable.img_f8_standby_1, R.drawable.img_f8_standby_3, R.drawable.img_f8_standby_5 };
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
        EventBus.getDefault().register(this);
        if (isPayByHID){
            usbHidDevice();
        }
        initIndicatorView();
        //显示对应的付款界面
        payByHIDLayout.setVisibility(isPayBySaler?View.GONE:View.VISIBLE);
        payBySalerLayout.setVisibility(isPayByHID?View.GONE:View.VISIBLE);
        isCanScan = true;
        Log.d("lincb","onResume");
//        new Handler().postDelayed(new OpenDeviceRunnable(),500);
        if(mViewer != null){
            mViewer.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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
    }



    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WxPayFace.getInstance().releaseWxpayface(getApplicationContext());
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
//        amount = tvAmountValue.getText().toString();
        if (isPayBySaler){
            if (TextUtils.isEmpty(amount)||Double.parseDouble(amount)<=0){
                speak("请按键盘确认键，确认交易金额");
                return;
            }
            Intent mIntent = new Intent(this,F8ShowAmountActivity.class);
            mIntent.putExtra("Amount",amount);
            startActivity(mIntent);
        }else{
            doWxFacePay(amount);
        }
//        amount = tvAmountValue.getText().toString();
//        doWxFacePay(amount);

    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxFacePayMessageEvent event) {
        doFacePay(event.mAmount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxGoShowAmountMessageEvent event) {
        Intent mIntent = new Intent(this,F8ShowAmountActivity.class);
        mIntent.putExtra("Amount",tvAmountValue.getText().toString());
        startActivity(mIntent);
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxAmountMessageEvent event) {
        tvAmountValue.getText().append(event.value);
        MoneyEditUtils.afterDotTwo(tvAmountValue);
        if(".".equals(tvAmountValue.getText().toString())){
            tvAmountValue.setText("0.");
        }
        double amount = Double.valueOf(tvAmountValue.getText().toString());
        if(amount > 99999999){
            tvAmountValue.getText().delete(tvAmountValue.getText().length()-1,tvAmountValue.getText().length());
            speak("最大支付金额："+99999999+"元");
            return;
        }
        if(tvAmountValue.getText().toString().length() > 8){
            tvAmountValue.getText().delete(tvAmountValue.getText().length()-1,tvAmountValue.getText().length());
            return;
        }
        payKeyboard.updateDisplay(tvAmountValue.getText().toString(),true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SendVoidMessageEvent event){
        closeCamera();
        startActivity(new Intent(getApplicationContext(), F8SettingActivity.class));
    }




}
