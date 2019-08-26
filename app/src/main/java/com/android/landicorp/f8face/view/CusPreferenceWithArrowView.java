package com.android.landicorp.f8face.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.landicorp.f8face.R;

/**
 * Created by admin on 2019/6/19.
 */

public class CusPreferenceWithArrowView extends Preference{
    private Context mContext;
    private String title;
    private String value;
    private String summary;
    private Drawable drawable;
    private boolean showSwitch;
    private boolean lineVisible;

    public CusPreferenceWithArrowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs,defStyle);
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PreferenceStyle);
        title = typedArray.getString(R.styleable.PreferenceStyle_Title);
        value = typedArray.getString(R.styleable.PreferenceStyle_Value);
        summary = typedArray.getString(R.styleable.PreferenceStyle_summary);
        drawable = typedArray.getDrawable(R.styleable.PreferenceStyle_src);
        showSwitch = typedArray.getBoolean(R.styleable.PreferenceStyle_showSwitch,false);
        lineVisible = typedArray.getBoolean(R.styleable.PreferenceStyle_LineVisible,true);
    }

    public CusPreferenceWithArrowView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(mContext).inflate(R.layout.view_preference_arrow,parent,false);
    }
    private ImageView ivRight;
    private TextView tvValue,tvSummary;
    private Switch aSwitch;
    private LinearLayout rightLayout;

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView tvTitle = view.findViewById(R.id.tv_pre_title);
        tvValue = view.findViewById(R.id.tv_pre_value);
        tvSummary = view.findViewById(R.id.tv_pre_summary);
        ivRight = view.findViewById(R.id.iv_right);
        aSwitch = view.findViewById(R.id.right_switch);
        rightLayout = view.findViewById(R.id.ll_right);
        View lineView = view.findViewById(R.id.view_line);
        tvTitle.setText(title);
        tvValue.setText(value);
        ivRight.setImageDrawable(drawable);
        lineView.setVisibility(lineVisible?View.VISIBLE:View.GONE);
        tvSummary.setText(TextUtils.isEmpty(summary)?"":summary);
        if (TextUtils.isEmpty(summary)){
            tvSummary.setVisibility(View.GONE);
        }
        rightLayout.setVisibility(View.VISIBLE);
        if (showSwitch&&TextUtils.isEmpty(value)){
            view.findViewById(R.id.ll_right).setVisibility(View.GONE);
            aSwitch.setVisibility(View.VISIBLE);
        }
    }

    public void setSummary(String summary) {
        if (TextUtils.isEmpty(summary)){
            tvSummary.setVisibility(View.GONE);
        }
        tvSummary.setText(summary);
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public void setShowSwitch(boolean showSwitch) {
        this.showSwitch = showSwitch;
        rightLayout.setVisibility(View.VISIBLE);
        if (showSwitch){
            rightLayout.setVisibility(View.GONE);
            aSwitch.setVisibility(View.VISIBLE);
        }
    }

    public void setIvRight(Drawable drawable) {
        ivRight.setImageDrawable(drawable);
    }

    public void setTvValue(String value) {
        this.value = value;
//        tvValue.setText(value);
    }

    public void setTvSummary(String summary) {
        tvSummary.setText(summary);
    }

}
