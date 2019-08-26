package com.android.landicorp.f8face.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.landicorp.f8face.R;

/**
 * Created by admin on 2019/6/20.
 */

public class CusPreferenceSwitch extends SwitchPreference{

    private Context mContext;
    private String title;
    private String value;
    private String summary;

    public CusPreferenceSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PreferenceStyle);
        title = typedArray.getString(R.styleable.PreferenceStyle_Title);
        value = typedArray.getString(R.styleable.PreferenceStyle_Value);
        summary = typedArray.getString(R.styleable.PreferenceStyle_summary);
    }

    private ImageView ivRight;
    private TextView tvSummary;
    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(mContext).inflate(R.layout.view_preference_switch,parent,false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView tvTitle = view.findViewById(R.id.tv_pre_title);
        tvSummary = view.findViewById(R.id.tv_pre_summary);
        tvSummary.setText(summary);
        tvTitle.setText(title);

//        checkBox.setOnCheckedChangeListener();
    }

}
