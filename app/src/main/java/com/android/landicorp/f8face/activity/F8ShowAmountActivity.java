package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.inter.KeyBoardCancelMessageEvent;
import com.android.landicorp.f8face.util.FullScreen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
        scanLayout.setVisibility(View.GONE);
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
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doWxFacePay(amount);
//                        goSettingPage();
                    }
                },100);

                break;
        }
    }



    public void goSettingPage(){

        startActivity(new Intent(getApplicationContext(), F8SettingActivity.class));
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


