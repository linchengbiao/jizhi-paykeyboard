package com.android.landicorp.f8face.activity;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.landicorp.f8face.F8Application;
import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.inter.TradeStatusInter;
import com.android.landicorp.f8face.util.AppUtil;
import com.android.landicorp.f8face.util.FullScreen;
import com.android.landicorp.f8face.util.TTSUtils;
import com.android.landicorp.f8face.view.F8ToolBarView;
import com.geekmaker.paykeyboard.IPayRequest;
import com.geekmaker.paykeyboard.PayKeyboard;

public class BaseActivity extends AppCompatActivity implements TradeStatusInter{
    private Handler handler = new Handler();
    public boolean isPayByHID,isPayBySaler;
    private F8Application f8Application;
    protected F8ToolBarView toolbar;
    protected SharedPreferences preference;
    protected SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FullScreen.fullScreen(this);
        FullScreen.NavigationBarStatusBar(this,true);
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        f8Application = (F8Application)getApplication();
        payKeyboard = f8Application.getPayKeyboard();
        //获取设置界面数据
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preference.edit();
        isPayBySaler = preference.getBoolean(getString(R.string.key_pre_pay_saler),true);
        isPayByHID = preference.getBoolean(getString(R.string.key_pre_pay_self),false );
    }

    @Override
    protected void onResume() {
        FullScreen.NavigationBarStatusBar(this,true);
        super.onResume();
        toolbar = (F8ToolBarView)findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.f8_wx_green);
        setSupportActionBar(toolbar);
        if (toolbar!=null){
            toolbar.setLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        showLeftIv(R.drawable.ic_arrow_back,getString(R.string.title_back));
        setLeftTextColor(getResources().getColor(R.color.white));
        updateSignal();

    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    public void speak(String text) {
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        TTSUtils.speak(text);

    }


    public void hideToolBar(){
        toolbar.hideToolBar();
    }


    public void setCenterTitle(String title){
        toolbar.setCenterTitle(title);
    }

    /**
     * 设置toolbar左边按钮，隐藏系统自带返回键
     */
    public void showLeftIv(int srcId,String leftTitle){
        toolbar.setLeftIv(srcId,leftTitle);
    }
    /**
     * 设置toolbar左边文本框字体颜色
     */
    public void setLeftTextColor(int color){
        toolbar.setLeftTextColor(color);
    }
    /**
     * 设置toolbar右侧标题
     * @param title
     */
    public void showRightIv(int srcId,String title){
        toolbar.setRightTv(srcId,title);
    }
    /**
     * 支付成功后显示的倒计时 默认三秒
     */
    public void showLeftTextClock(int delayTime, F8ToolBarView.OnDelayTimeListener listener){
        toolbar.showLeftTextClock(delayTime,listener);
    }


    /**
     * 设置了android:fitsSystemWindows="true"属性的view会自动添加一个值等于导航栏高度的paddingBottom
     * 这里获取状态栏高度
     * @return
     */
    public float getStatusBarHeight() {
        float result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimension(resourceId);
        }

        return result;
    }

    /**
     * 返回值就是导航栏的高度
     * @return
     */
    public float getNavigationBarHeight() {
        float result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimension(resourceId);
        }
        return result;
    }

    public int px2dip(float pxValue){
        final float scale = getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onTradeCancel();
        this.finish();
    }
    private PayKeyboard payKeyboard;
    public void updateSignal(){
        int w =4;
        int g = 0;
        if (AppUtil.getNetWorkType(this, ConnectivityManager.TYPE_MOBILE)){
            g = 1;
        }
        if (AppUtil.getNetWorkType(this, ConnectivityManager.TYPE_WIFI)){
            int rssi = AppUtil.getNetWorkSingleUnit(this);
            //0到-50表示信号最好，-50到-70表示信号偏差，小于-70表示最差
            if (rssi>-50){
                w = 4;
            }else if(rssi>-70){
                w=2;
            }else {
                w = 1;
            }
        }else{
            w=0;
        }
        if(payKeyboard!=null && !payKeyboard.isReleased()) payKeyboard.updateSign(w,g);
    }

    @Override
    public void onTradeSucc() {
        IPayRequest iPayRequest = f8Application.getmIrequest();
        if (iPayRequest!=null){
            iPayRequest.setResult(true);
        }
    }
    @Override
    public void onTradeFaile() {
        IPayRequest iPayRequest = f8Application.getmIrequest();
        if (iPayRequest!=null){
            iPayRequest.setResult(false);
        }
    }

    @Override
    public void onTradeCancel() {
        IPayRequest iPayRequest = f8Application.getmIrequest();
        if (iPayRequest!=null){
            iPayRequest.setResult(false,true);
        }
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }
}
