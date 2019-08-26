package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.util.FullScreen;


public class F8FacePayMainActivity extends BaseActivity implements View.OnClickListener{

    private LinearLayout leftScanLayout,rightUnionLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreen.fullScreenWithoutBar(this);
        setContentView(R.layout.activity_f8_face_pay_main);
//        indicatorView = (ImageIndicatorView)findViewById(R.id.ll_img_header);
        leftScanLayout = (LinearLayout)findViewById(R.id.ll_left_scan);
        rightUnionLayout = (LinearLayout)findViewById(R.id.ll_right_union);
        leftScanLayout.setOnClickListener(this);
        rightUnionLayout.setOnClickListener(this);
        initIndicatorView();

    }

    public void initIndicatorView(){
        final Integer[] resArray = new Integer[] { R.drawable.img_f8_header_1, R.drawable.img_f8_header_2 };
//        indicatorView.setupLayoutByDrawable(resArray);
//        indicatorView.setIndicateStyle(ImageIndicatorView.INDICATE_USERGUIDE_STYLE);
//        indicatorView.show();
//        AutoPlayManager autoBrocastManager =  new AutoPlayManager(indicatorView);
//        autoBrocastManager.setBroadcastEnable(true);
//        autoBrocastManager.setBroadcastTimeIntevel(3 * 1000, 3 * 1000);//set first play time and interval
//        autoBrocastManager.loop();
//        this.mAutoPlayManager = autoBrocastManager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLeftIv(R.drawable.ic_arrow_back,"");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (mAutoPlayManager != null) {
//            mAutoPlayManager.stop();
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_left_scan:
                Intent mIntent = new Intent(F8FacePayMainActivity.this,InputAmountActivity.class);
                startActivity(mIntent);
                break;
            case R.id.ll_right_union:
                break;
        }

    }
}
