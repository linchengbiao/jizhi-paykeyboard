package com.android.landicorp.f8face.util;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;

public class MoneyEditUtils {
    
    private static DecimalFormat df = new DecimalFormat("#0.00");

    public static void afterDotTwo(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String money = s.toString();
                try {
                    if (TextUtils.isEmpty(money)) {
                        money = "0.00";
                    } else {
                        money = df.format(Double.valueOf(money));
                    }
                } catch (NumberFormatException e) {
                    //避免输入多余的小数点
                    editText.setText(money.substring(0, money.length() - 1));
                    editText.setSelection(editText.length());
                }
                if (s.toString().contains(".")) {
                    if (s.toString().indexOf(".") > 9) {
                        s = s.toString().subSequence(0, 9) + s.toString().substring(s.toString()
                                .indexOf("."));
                        editText.setText(s);
                        editText.setSelection(9);
                    }
                } else {
                    if (s.toString().length() > 9) {
                        s = s.toString().subSequence(0, 9);
                        editText.setText(s);
                        editText.setSelection(9);
                    }
                }
                // 判断小数点后只能输入两位
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                //如果第一个数字为0，第二个不为点，就不允许输入
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                    }
                }
                //如果第一个输入的为点，自动在前面加0 要不会闪退
                if (s.toString().startsWith(".")) {
                    editText.setText("0.");
                    editText.setSelection(2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editText.getText().toString().trim().equals("")) {
                    if (editText.getText().toString().trim().substring(0, 1).equals(".")) {
                        editText.setText(String.format("0%s", editText.getText().toString().trim
                                ()));
                        editText.setSelection(1);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
        });
    }
}