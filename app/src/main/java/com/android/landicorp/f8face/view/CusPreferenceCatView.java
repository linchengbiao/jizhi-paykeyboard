package com.android.landicorp.f8face.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.landicorp.f8face.R;

/**
 * Created by admin on 2019/6/19.
 */

public class CusPreferenceCatView extends PreferenceCategory{
    private Context mContext;
    private LinearLayout topLayout;
    private String title;
    private int height;
    public CusPreferenceCatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceStyle);
        title = a.getString(R.styleable.PreferenceStyle_Title);
        height = (int) a.getDimension(R.styleable.PreferenceStyle_Height,50f);
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(mContext).inflate(R.layout.view_preference_category,parent,false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView tvTitle = view.findViewById(R.id.tv_pre_catetory_title);
        topLayout = view.findViewById(R.id.ll_cat);
        topLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,height));
        tvTitle.setText(title);

    }

}
