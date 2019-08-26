package com.android.landicorp.f8face.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.view.F8ToolBarView;

public class ResultSuccActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FullScreen.fullScreen(this);
        FullScreen.NavigationBarStatusBar(this,true);
        setContentView(R.layout.activity_result_succ);
        findViewById(R.id.btn_main_page).setOnClickListener(this);
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
//                Intent mIntent = new Intent(ResultSuccActivity.this,MainActivity.class);
//                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(mIntent);
            }
        });
        showLeftIv(R.drawable.ic_close_gray,"");
        showRightIv(0,"");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_main_page:
                finish();
//                Intent mIntent = new Intent(ResultSuccActivity.this,MainActivity.class);
//                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(mIntent);
                //                MyCustomeDialog.Builder builder = new MyCustomeDialog.Builder(ResultSuccActivity.this);
//                builder.setOnScanClickListner(new MyCustomeDialog.Builder.OnScanClickListner() {
//                    @Override
//                    public void onClick() {
//                        Toast.makeText(ResultSuccActivity.this,"进入扫码",Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onCancel() {
//
//                    }
//                });
//                builder.setTitle("提示").create();
//                MyCustomeDialog customeDialog = builder.create();
//                customeDialog.show();
                break;

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
