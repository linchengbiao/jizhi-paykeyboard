package com.android.landicorp.f8face.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.view.ClearEditText;
import com.android.landicorp.f8face.view.RoundImageView;
import com.android.landicorp.f8face.view.SpotsDialog;


public class InputAmountActivity extends BaseActivity implements View.OnClickListener{

    private ClearEditText clearEditText;
    private RoundImageView faceImageView,faceImageViewSmall;
    private RelativeLayout topLayout,bodyLayout;
    private LinearLayout llFaceLayout;
    private AlertDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreen.fullScreen(this);
        setContentView(R.layout.activity_input_amount);
        clearEditText = (ClearEditText)findViewById(R.id.clear_et_pin);
        faceImageView = (RoundImageView)findViewById(R.id.iv_face);
        faceImageViewSmall = (RoundImageView)findViewById(R.id.iv_face_logo);
        topLayout = (RelativeLayout)findViewById(R.id.rl_top);
        bodyLayout =  (RelativeLayout)findViewById(R.id.rl_body);
        llFaceLayout = (LinearLayout)findViewById(R.id.ll_face);
        progressDialog = new SpotsDialog.Builder().setContext(this).build();
        findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCenterTitle("");
        showLeftIv(R.drawable.ic_arrow_back_blue,getString(R.string.title_sale_amount));
        setLeftTextColor(getResources().getColor(R.color.f8_input_amount_tips));
        showRightIv(-1,"");
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_confirm:
                progressDialog.show();
                startActivity(new Intent(InputAmountActivity.this,ResultSuccActivity.class));
                this.finish();
                break;
            case R.id.btn_one:
                break;
            case R.id.btn_two:
                break;
            case R.id.btn_three:
                break;
        }
    }
}
