package com.android.landicorp.f8face.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.util.BitmapUtil;

public class ShowQrcodeActivity extends BaseActivity {

    private ImageView qrcodeIv;
    private RelativeLayout qrcodeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qrcode);
        qrcodeIv = (ImageView)findViewById(R.id.iv_qrcode);
        qrcodeLayout = (RelativeLayout)findViewById(R.id.ll_qrcode);
        qrcodeLayout.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapUtil.createQRImage("123456678",qrcodeLayout.getWidth()-100,qrcodeLayout.getHeight()-300,null);
                qrcodeIv.setImageBitmap(bitmap);
            }
        });

    }
}
