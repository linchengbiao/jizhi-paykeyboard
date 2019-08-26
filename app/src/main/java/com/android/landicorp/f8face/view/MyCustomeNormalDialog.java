package com.android.landicorp.f8face.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.landicorp.f8face.R;

/**
 * Created by admin on 2019/6/5.
 */

public class MyCustomeNormalDialog extends Dialog {



    public MyCustomeNormalDialog(@NonNull Context context) {
        super(context);
    }

    public MyCustomeNormalDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected MyCustomeNormalDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder{
        private View view;
        private TextView titleView,cancelView,confirmView;
        private MyCustomeNormalDialog mDialog;

        public Builder(Context context){
            mDialog = new MyCustomeNormalDialog(context, R.style.transparent_dialog_style);
            view = LayoutInflater.from(context).inflate(R.layout.view_normal_dialog,null);
            cancelView = view.findViewById(R.id.tv_cancel);
            confirmView = view.findViewById(R.id.tv_confirm);
            mDialog.addContentView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            titleView = (TextView)view.findViewById(R.id.tv_title);
        }
        public Builder setTitle(String title){
            titleView.setText(title);
            return this;
        }
        public MyCustomeNormalDialog create(){

            cancelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onScanClickListner!=null){
                        if (mDialog.isShowing()){
                            mDialog.dismiss();
                        }
                        onScanClickListner.onCancel();
                    }
                }
            });
            confirmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onScanClickListner!=null){
                        mDialog.dismiss();
                        onScanClickListner.onClick();
                    }
                }
            });

            mDialog.setContentView(view);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(true);
            return mDialog;
        }
        public interface OnConfirmClickListner{
            public void onClick();
            public void onCancel();
        }

        public void setOnScanClickListner(OnConfirmClickListner onScanClickListner) {
            this.onScanClickListner = onScanClickListner;
        }

        private OnConfirmClickListner onScanClickListner;


    }

}
