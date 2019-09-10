package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.view.F8ToolBarView;

import net.frakbot.jumpingbeans.JumpingBeans;

/**
 * 等待收银元确认交易结果
 */
public class F8WaitForResultActivity extends BaseActivity {
    private TextView tvWaitTips;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreen.fullScreen(this);
        setContentView(R.layout.activity_f8_wait_for_result);
        tvWaitTips = (TextView)findViewById(R.id.tv_wait_info);
        JumpingBeans jumpingBeans1 = JumpingBeans.with(tvWaitTips)
                .appendJumpingDots()
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setBackgroundResource(R.color.white);
        setSupportActionBar(toolbar);
        showLeftTextClock(15, new F8ToolBarView.OnDelayTimeListener() {
            @Override
            public void onDelayTime() {
                finish();
//                Intent mIntent = new Intent(F8WaitForResultActivity.this,MainActivity.class);
//                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(mIntent);

            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mIntent = new Intent(F8WaitForResultActivity.this,ResultSuccActivity.class);
                mIntent.putExtra("Amount","0.01");
                startActivity(mIntent);
            }
        },2000);
        showLeftIv(R.drawable.ic_close_gray,"");
        showRightIv(0,"");
        speak("请等待收银员确认支付结果");
    }
}
