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

public class MyCustomeDialog extends Dialog {



    public MyCustomeDialog(@NonNull Context context) {
        super(context);
    }

    public MyCustomeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected MyCustomeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder{
        private View view;
        private TextView titleView;
        private LinearLayout scanLayout,topLayout;
        private MyCustomeDialog mDialog;

        public Builder(Context context){
            mDialog = new MyCustomeDialog(context, R.style.transparent_dialog_style);
            view = LayoutInflater.from(context).inflate(R.layout.view_simple_dialog,null);
            mDialog.addContentView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            titleView = (TextView)view.findViewById(R.id.tv_title);
            topLayout = (LinearLayout)view.findViewById(R.id.ll_top);
            scanLayout = (LinearLayout)view.findViewById(R.id.ll_scan_pay);
        }
        public Builder setTitle(String title){
            titleView.setText(title);
            return this;
        }
        public MyCustomeDialog create(){

            scanLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onScanClickListner!=null){
                        onScanClickListner.onClick();
                    }

                }
            });
            mDialog.setContentView(view);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(true);
            return mDialog;
        }
        public interface OnScanClickListner{
            public void onClick();
            public void onCancel();
        }

        public void setOnScanClickListner(OnScanClickListner onScanClickListner) {
            this.onScanClickListner = onScanClickListner;
        }

        private OnScanClickListner onScanClickListner;


    }

}
