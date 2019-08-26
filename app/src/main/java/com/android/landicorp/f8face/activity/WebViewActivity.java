package com.android.landicorp.f8face.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.landicorp.f8face.R;

public class WebViewActivity extends AppCompatActivity {
    private WebView web_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        web_view = (WebView) findViewById(R.id.wv_id);
        web_view.getSettings().setSavePassword(true);
        web_view.requestFocus();
        WebSettings webSettings = web_view.getSettings();
        webSettings.setJavaScriptEnabled(true);

        web_view.getSettings().setSavePassword(true);
        web_view.loadUrl("https://mail.landicorp.com");
        web_view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        web_view.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100) {
                }
                if (progress == 100) {
                }
            }
        });
    }
}
