package com.android.landicorp.f8face.view;

import android.content.Context;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.util.PhoneFormat;
import com.android.landicorp.f8face.util.ToastUtil;


/**
 * Created by admin on 2017/9/23.
 * C10金额输入键盘
 */

public class AmountKeyBoard extends RelativeLayout implements View.OnClickListener{
    private StringBuilder textBuilder;
    private Context mContext;
    private OnKeyPressedListener listener;
    public ClearEditText amountTv;
    private TextView tvLine;
    private int maxLen = 9;
    private boolean phoneFormat;
    private Handler uiHanlder;


    public AmountKeyBoard(Context context) {
        super(context);
        textBuilder = new StringBuilder();
        phoneFormat = false;
        mContext = context;
    }

    public AmountKeyBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        phoneFormat = false;
        textBuilder = new StringBuilder();
        mContext = context;
    }

    public AmountKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textBuilder = new StringBuilder();
        phoneFormat = false;
        mContext = context;
    }

    /**
     * 设置密码格式
     */
    public void setKeyBoardTypePwd(){
        amountTv.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD|InputType.TYPE_CLASS_TEXT);
        InputFilter[] fillter = new InputFilter[]{new InputFilter.LengthFilter(6)};
        amountTv.setFilters(fillter);
    }

    /**
     * 设置手机格式 3-4-4格式
     */
    public void setKeyBoardTypePhone(){
        phoneFormat = true;
    }

    @Override
    public void onClick(View view) {
        if (listener==null){
            return ;
        }
        int id = view.getId();
        int code = -1;
        switch (id){
            case R.id.btn_zero:
                //resetTimer();
                code = KeyEvent.KEYCODE_0;
                onKeyCode(code);
                break;
            case R.id.btn_one:
                //resetTimer();
                code = KeyEvent.KEYCODE_1;
                onKeyCode(code);
                break;
            case R.id.btn_two:
                //resetTimer();
                code = KeyEvent.KEYCODE_2;
                onKeyCode(code);
                break;
            case R.id.btn_three:
                //resetTimer();
                code = KeyEvent.KEYCODE_3;
                onKeyCode(code);
                break;
            case R.id.btn_four:
                //resetTimer();
                code = KeyEvent.KEYCODE_4;
                onKeyCode(code);
                break;
            case R.id.btn_five:
                //resetTimer();
                code = KeyEvent.KEYCODE_5;
                onKeyCode(code);
                break;
            case R.id.btn_six:
                //resetTimer();
                code = KeyEvent.KEYCODE_6;
                onKeyCode(code);
                break;
            case R.id.btn_serven:
                //resetTimer();
                code = KeyEvent.KEYCODE_7;
                onKeyCode(code);
                break;
            case R.id.btn_eight:
                //resetTimer();
                code = KeyEvent.KEYCODE_8;
                onKeyCode(code);
                break;
            case R.id.btn_nine:
                //resetTimer();
                code = KeyEvent.KEYCODE_9;
                onKeyCode(code);
                break;
            case R.id.btn_clear:
                //resetTimer();
                code = KeyEvent.KEYCODE_DEL;
                clear();
                break;
            case R.id.btn_dot:
                //resetTimer();
                code = KeyEvent.KEYCODE_NUMPAD_DOT;

                break;
            case R.id.btn_confirm:
                //stopTimer();
                if (amountTv.getText().toString().equalsIgnoreCase(mContext.getString(R.string.title_sale_amount_tips))||amountTv.getText().toString().equalsIgnoreCase("0.00")||TextUtils.isEmpty(amountTv.getText().toString())){
                    ToastUtil.toast(mContext.getString(R.string.title_sale_amount_tips));
                    return;
                }
                if (listener != null) {
                    listener.onKeyPressSucc(amountTv.getText().toString());
                }
                break;
            default:
                break;
        }
    }

    /**
     * Apos 界面传入金额文本框
     * @param mListener
     */
    public void init(ClearEditText aposAmountEt, OnKeyPressedListener mListener){

        LayoutInflater.from(mContext).inflate(R.layout.view_amount_layout, this);
        amountTv = (ClearEditText) this.findViewById(R.id.amount_tv);
        tvLine = (TextView)this.findViewById(R.id.tv_line);
        if (amountTv==null){
            amountTv = aposAmountEt;
        }
        amountTv.setOnClearTextListener(new ClearEditText.OnClearTextListener() {
            @Override
            public void onClearSucc() {
                clear();
            }
        });
        ToastUtil.init(mContext);
        //uiHanlder = new Handler();
        //startTimer();
        amountTv.clearFocus();
        amountTv.setInputType(InputType.TYPE_NULL);//不弹出软键盘
        this.findViewById(R.id.btn_zero).setOnClickListener(this);
        this.findViewById(R.id.btn_one).setOnClickListener(this);
        this.findViewById(R.id.btn_two).setOnClickListener(this);
        this.findViewById(R.id.btn_three).setOnClickListener(this);
        this.findViewById(R.id.btn_four).setOnClickListener(this);
        this.findViewById(R.id.btn_five).setOnClickListener(this);
        this.findViewById(R.id.btn_six).setOnClickListener(this);
        this.findViewById(R.id.btn_serven).setOnClickListener(this);
        this.findViewById(R.id.btn_eight).setOnClickListener(this);
        this.findViewById(R.id.btn_nine).setOnClickListener(this);
        this.findViewById(R.id.btn_dot).setOnClickListener(this);
        this.findViewById(R.id.btn_clear).setOnClickListener(this);
        this.findViewById(R.id.btn_confirm).setOnClickListener(this);
        this.listener = mListener;
    }

    private void onKeyCode(int keyCode) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
//            amountTv.append(Integer.toString(keyCode - KeyEvent.KEYCODE_0));
            appendCode(keyCode);
            tvLine.setBackgroundColor(mContext.getColor(R.color.f8_btn_end_color));
        }

    }

    public interface OnKeyPressedListener{
        void onKeyPressSucc(String amount);
        void onTimeOut();
    }



    /**
     * 长度判断，不包括小数点；不包括无效0；
     * 比如00123.45的前两位和小树点
     * @param code
     */
    private void appendCode(int code) {
        String oldValue = textBuilder.toString();
        oldValue.replaceAll(".", "");

        if (TextUtils.isEmpty(oldValue) || Long.toString(Long.parseLong(oldValue)).length() < maxLen) {

            if(code == KeyEvent.KEYCODE_F1){
                textBuilder.append("00");
            }else{
                textBuilder.append(Integer.toString(code - KeyEvent.KEYCODE_0));
            }
        }

        onTextChanged();
    }

    /**
     * 删除
     */
    private void del() {
        if (textBuilder.length() > 0) {
            textBuilder.deleteCharAt(textBuilder.length() - 1);
        }

        onTextChanged();
    }

    /**
     * 清空
     */
    public void clear() {
        if (textBuilder.length() > 0) {
            textBuilder.delete(0, textBuilder.length());
        }
        tvLine.setBackgroundColor(mContext.getColor(R.color.f8_input_pin_edit));


        onTextChanged();
    }


    /**
     * 文本更新
     */
    private void onTextChanged() {
        if (TextUtils.isEmpty(textBuilder.toString())) {
            amountTv.setText("");
        } else {
            if (phoneFormat){
                PhoneFormat.onTextChanged344(amountTv,textBuilder.toString());
            }else{
                amountTv.setText(textBuilder.toString());
            }
        }
        amountTv.setSelection(amountTv.getText().length());
    }


    //---------------------------------- 金额超时定时器 ------------------------------------
//    private final int mTimeout = 60;
//    private Runnable rTimeOut = new Runnable() {
//        @Override
//        public void run() {
//            onTimeout();
//        }
//    };
//
//    protected void startTimer() {
//        if (uiHanlder != null) {
//            uiHanlder.removeCallbacks(rTimeOut);
//            uiHanlder.postDelayed(rTimeOut, mTimeout * 1000);
//        }
//    }
//
//    protected void resetTimer() {
//        if (uiHanlder != null) {
//            uiHanlder.removeCallbacks(rTimeOut);
//            uiHanlder.postDelayed(rTimeOut, mTimeout * 1000);
//        }
//    }
//
//    protected void stopTimer() {
//        if (uiHanlder != null) {
//            uiHanlder.removeCallbacks(rTimeOut);
//        }
//    }
//
//    protected void onTimeout() {
//        listener.onTimeOut();
//    }
}
