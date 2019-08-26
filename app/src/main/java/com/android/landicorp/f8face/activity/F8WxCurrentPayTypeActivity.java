package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.view.MyCustomeNormalDialog;

/**
 * 显示当前的支付模式
 */
public class F8WxCurrentPayTypeActivity extends F8BaseSettingActivity implements View.OnClickListener {
    private MyCustomeNormalDialog customeDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f8_wx_current_pay_type);
        findViewById(R.id.btn_reset).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCenterTitle(getString(R.string.title_wx_setting2));
        if (isPayBySaler){
            findViewById(R.id.rl_aecr).setVisibility(View.GONE);
            findViewById(R.id.rl_alone).setVisibility(View.VISIBLE);
        }else if (isPayByHID){
            findViewById(R.id.rl_aecr).setVisibility(View.VISIBLE);
            findViewById(R.id.rl_alone).setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_reset:
                showDialog();
                break;
        }
    }

    private void showDialog(){
        MyCustomeNormalDialog.Builder builder = new MyCustomeNormalDialog.Builder(F8WxCurrentPayTypeActivity.this);
        builder.setOnScanClickListner(new MyCustomeNormalDialog.Builder.OnConfirmClickListner() {

            @Override
            public void onClick() {
                Intent mIntent = new Intent(F8WxCurrentPayTypeActivity.this,F8WxPayTypeSettingActivity.class);
                startActivity(mIntent);
            }

            @Override
            public void onCancel() {

            }
        });
        builder.setTitle("确定重置收银模式").create();
        customeDialog = builder.create();
        customeDialog.show();
    }


}
