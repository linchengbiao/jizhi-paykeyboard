package com.android.landicorp.f8face.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.view.AmountKeyBoard;
import com.android.landicorp.f8face.view.ClearEditText;
import com.android.landicorp.f8face.view.RoundImageView;
import com.android.landicorp.f8face.view.SpotsDialog;
import com.landi.finance.face.aidl.constant.ParamKey;


public class InputPinActivity extends BaseActivity implements View.OnClickListener{

    private ClearEditText clearEditText;
    private RoundImageView faceImageView,faceImageViewSmall;
    private RelativeLayout topLayout,bodyLayout;
    private LinearLayout llFaceLayout;
    private AlertDialog progressDialog;
    private Bitmap faceBitMap;
    private AmountKeyBoard amountKeyBoard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreen.fullScreen(this);
        setContentView(R.layout.activity_input_pin);
        clearEditText = (ClearEditText)findViewById(R.id.clear_et_pin);
        faceImageView = (RoundImageView)findViewById(R.id.iv_face);
        faceImageViewSmall = (RoundImageView)findViewById(R.id.iv_face_logo);
        topLayout = (RelativeLayout)findViewById(R.id.rl_top);
        bodyLayout =  (RelativeLayout)findViewById(R.id.rl_body);
        llFaceLayout = (LinearLayout)findViewById(R.id.ll_face);
        amountKeyBoard = (AmountKeyBoard)findViewById(R.id.rl_body);
        progressDialog = new SpotsDialog.Builder().setContext(this).build();
        byte[] b = getIntent().getExtras().getByteArray(ParamKey.KEY_IMAGE_DATA);
        faceBitMap = BitmapFactory.decodeByteArray(b, 0, b.length);
        faceImageView.setImageBitmap(faceBitMap);

        amountKeyBoard.init(null, new AmountKeyBoard.OnKeyPressedListener() {
            @Override
            public void onKeyPressSucc(String amount) {
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(InputPinActivity.this,ResultSuccActivity.class));
                        finish();
                    }
                },6000);

            }

            @Override
            public void onTimeOut() {

            }
        });
        amountKeyBoard.setKeyBoardTypePwd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCenterTitle("");
        showLeftIv(R.drawable.ic_colose_white,"");
        showRightIv(0,getString(R.string.btn_rephoto));
        final AnimationSet animationSet = new AnimationSet(true);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setDuration(500);
        animationSet.setFillAfter(true);

        faceImageViewSmall.post(new Runnable() {
            @Override
            public void run() {
                ScaleAnimation scaleAnimation = new ScaleAnimation(1,0.2f,1,0.2f);
                animationSet.addAnimation(scaleAnimation);
                //获取动画view的坐标
                int animationViewLeft = faceImageView.getLeft();
                int animationViewTop = faceImageView.getTop();
                float statusBarHeight = getStatusBarHeight();
                float narBarHeight = getNavigationBarHeight();
                TranslateAnimation translate = new TranslateAnimation(0, llFaceLayout.getLeft()-animationViewLeft, 0, llFaceLayout.getTop()-animationViewTop+(narBarHeight+statusBarHeight));
                animationSet.addAnimation(translate);
                faceImageView.startAnimation(animationSet);
            }
        });

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.btn_confirm:
//                progressDialog.show();
//                startActivity(new Intent(InputPinActivity.this,ResultSuccActivity.class));
//                this.finish();
//                break;
        }
    }

//    @Override
//    public void doCancel() {
//        super.doCancel();
//        finish();
//    }
}
