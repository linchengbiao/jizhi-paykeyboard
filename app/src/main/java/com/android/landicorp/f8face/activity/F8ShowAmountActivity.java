package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.landicorp.f8face.IMI.DecodePanel;
import com.android.landicorp.f8face.IMI.GLPanel;
import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.inter.KeyBoardCancelMessageEvent;
import com.android.landicorp.f8face.inter.WxFacePayMessageEvent;
import com.android.landicorp.f8face.util.FullScreen;
import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiNect;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

public class F8ShowAmountActivity extends F8BaseCameraActivity implements View.OnClickListener{
    private TextView tvAmount;
    private FrameLayout faceScanLayout;
    private TranslateAnimation translateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreen.fullScreenWithoutBar(this);
        setContentView(R.layout.activity_f8_show_amount);
        tvAmount = findViewById(R.id.tv_amount);
        scanLayout = (LinearLayout)findViewById(R.id.ll_scan);
        ivScannerIcon = (ImageView)findViewById(R.id.iv_scanner_icon);
        scanLayout.setOnClickListener(this);
        faceScanLayout = (FrameLayout)findViewById(R.id.ll_face_scan);
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
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }
    private String  amount;
    @Override
    protected void onResume() {
        super.onResume();
        amount = getIntent().getStringExtra("Amount");
        speak("请支付"+String.valueOf(amount)+"元，点击开始刷脸付。");
        tvAmount.setText(getString(R.string.yuan)+" "+String.valueOf(amount)+"元");
        scanLayout.setVisibility(View.VISIBLE);
        WxPayFace.getInstance().startCodeScanner(new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
                if (info != null){
                    String return_code = (String) info.get("return_code");
                    String return_msg = (String) info.get("return_msg");
                    String code_msg = (String) info.get("code_msg");
                    final String resultString = "startCodeScanner, return_code : " + return_code + " return_msg : " + return_msg + " code_msg: " + code_msg;
                    if (return_code.equals("SUCCESS")){
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               speak("扫码成功");
                               handler.sendEmptyMessage(MSG_DO_STOP_SCAN);
                               onTradeSucc();
                               if (isPayByHID){
                                   sendDataByHID(code_msg);
                                   handler.sendEmptyMessage(MSG_DO_START_SCAN);
                                   Intent mIntent = new Intent(F8ShowAmountActivity.this,F8WaitForResultActivity.class);
                                   mIntent.putExtra("Amount",amount);
                                   startActivity(mIntent);
                               }else{
                                   Intent intent = new Intent(F8ShowAmountActivity.this,ResultSuccActivity.class);
                                   intent.putExtra("Amount",amount);
                                   startActivity(intent);
                                   finish();
                               }
                           }
                       });

                    }
                }
            }
        });
        isCanScan = true;
        Log.d("lincb","onResume");
//        new Handler().postDelayed(new MainScanActivity.OpenDeviceRunnable(),500);
//        if(mViewer != null){
//            mViewer.onResume();
//        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("lincb","onPause");
        releaseScanCamera();
    }
    @Override
    protected synchronized void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_scan:
                Toast.makeText(F8ShowAmountActivity.this, getString(R.string.title_scan_pay_tips2), Toast.LENGTH_SHORT).show();
                break;
            case R.id.ll_face_scan:
                //调用人脸支付库
                setFaceMode();
                doWxFacePay(amount);

                break;
        }
    }



    public void goSettingPage(){

        startActivity(new Intent(getApplicationContext(), F8SettingActivity.class));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxFacePayMessageEvent event) {
        setFaceMode();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doWxFacePay(amount);
            }
        },100);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(KeyBoardCancelMessageEvent event) {
        this.onTradeCancel();
    }


    @Override
    public void onBackPressed() {
        closeCamera();
        super.onBackPressed();
    }


}


