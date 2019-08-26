package com.android.landicorp.f8face.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.landicorp.f8face.R;


/**
 * Created by admin on 2019/6/13.
 */

public class F8ToolBarView extends Toolbar{
    private Context mContext;
    private Handler handler = new Handler();
//    private ImageView leftIv,rightIv;
    private TextView tvLeftTitle,rightTv,centerTitle;

    public F8ToolBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    public F8ToolBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public void init(Context context){
        mContext = context;
        View toolBarView = LayoutInflater.from(context).inflate(R.layout.view_title,this);
        rightTv = (TextView)findViewById(R.id.tv_right);
        tvLeftTitle = (TextView)findViewById(R.id.tv_left);
        centerTitle = (TextView)findViewById(R.id.tv_center_title);
    }

    /**
     * 设置标题
     * @param title
     */
    public void setCenterTitle(String title){
        if (TextUtils.isEmpty(title))
            centerTitle.setVisibility(GONE);
        else
            centerTitle.setText(title);
    }

    private int mDelayTime;
    public interface OnDelayTimeListener{
        public void onDelayTime();
    }

    public void setDelayTimeListener(OnDelayTimeListener delayTimeListener) {
        this.delayTimeListener = delayTimeListener;
    }

    private OnDelayTimeListener delayTimeListener;
    /**
     * 支付成功后显示的倒计时 默认三秒
     */
    public void showLeftTextClock(int delayTime,OnDelayTimeListener listener){
        mDelayTime = delayTime;
        delayTimeListener = listener;
        tvLeftTitle.setTextColor(getResources().getColor(R.color.f8_pre_setting_title));
        tvLeftTitle.setText(delayTime+"秒");
        handler.postDelayed(runnable,1000);
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mDelayTime--;
            tvLeftTitle.setText(mDelayTime+"秒");
            handler.postDelayed(runnable,1000);
            if (mDelayTime==0){
                handler.removeCallbacks(runnable);
                delayTimeListener.onDelayTime();
            }
        }
    };
    public void setLeftTextColor(int color){
        tvLeftTitle.setTextColor(color);
    }
    /**
     * 设置左边bar title
     * @param rId
     * @param leftTitle
     */
    public void setLeftIv(int rId,String leftTitle){
        //如果设置了左边图标，就隐藏返回按钮
        if (rId>0){
            Drawable drawableLeft = getResources().getDrawable(rId);
            tvLeftTitle.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                    null, null, null);
            tvLeftTitle.setCompoundDrawablePadding(10);
        }if (!TextUtils.isEmpty(leftTitle)){
            tvLeftTitle.setText(leftTitle);
        }
    }


    public void setRightTv(int rId,String rightTitle){
        if (!TextUtils.isEmpty(rightTitle)){
            rightTv.setText(rightTitle);
        }else{
            rightTv.setVisibility(GONE);
        }

        if (rId>0){
            Drawable drawableLeft = getResources().getDrawable(rId);
            rightTv.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                    null, null, null);
            rightTv.setCompoundDrawablePadding(4);
        }



    }

    public void hideToolBar(){
        this.setVisibility(GONE);
    }

    public void hideLeftImage(){
        tvLeftTitle.setVisibility(GONE);
    }
    public void setLeftClickListener(OnClickListener listener){
        tvLeftTitle.setOnClickListener(listener);
    }

}
