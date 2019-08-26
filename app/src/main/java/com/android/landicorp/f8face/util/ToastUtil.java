package com.android.landicorp.f8face.util;

/**
 * Created by admin on 2019/4/4.
 */

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.landicorp.f8face.R;


/**
 * Toast提示工具类
 * @author baoxl
 *
 */
public class ToastUtil {
    private static final long TOAST_THRESHOLD = 2000;
    private static long previous = 0;
    private static Toast toast;
    private static Context context;
    private static TextView tipTv;

    private ToastUtil() {}

    public static void init(Context ctx) {
        context = ctx;
    }

    public static void toast(String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    public static void toast(int id) {
        toast(id, Toast.LENGTH_SHORT);
    }

    public static void toast(int id, int duration) {
        String message = context.getString(id);
        toast(message, duration);
    }

    public static void toast(String text, int duration) {
        long now = System.currentTimeMillis();
        if (now - previous < TOAST_THRESHOLD) {
            tipTv.setText(text);
            toast.show();
        } else {
            if (toast != null) {
                toast.cancel();
            }
            toast = new Toast(context);
            View view = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
            tipTv = (TextView) view.findViewById(R.id.toast_textView_tip);
            tipTv.setText(text);
            toast.setDuration(duration);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setView(view);
            toast.show();
        }
        previous = now;
    }

    public static void toast(String text, int duration, int xOffset, int yOffset) {
        long now = System.currentTimeMillis();
        if (now - previous < TOAST_THRESHOLD) {
            tipTv.setText(text);
            toast.show();
        } else {
            if (toast != null) {
                toast.cancel();
            }
            toast = new Toast(context);
            View view = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
            tipTv = (TextView) view.findViewById(R.id.toast_textView_tip);
            tipTv.setText(text);
            toast.setDuration(duration);
            toast.setView(view);
            toast.setGravity(Gravity.NO_GRAVITY, xOffset, yOffset);
            toast.show();
        }
        previous = now;
    }

    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
