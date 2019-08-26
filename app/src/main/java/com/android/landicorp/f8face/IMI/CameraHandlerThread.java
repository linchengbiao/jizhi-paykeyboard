package com.android.landicorp.f8face.IMI;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by admin on 2019/8/21.
 */

public class CameraHandlerThread extends HandlerThread{

    private Handler mHandler;

    public CameraHandlerThread(String name) {
        super(name);
        mHandler = new Handler(getLooper());
    }
    synchronized void notifyCameraOpened() {
        notify();
    }
}
