package com.android.landicorp.f8face.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.android.landicorp.f8face.R;

public class F8WxPayTypeSettingActivity extends BaseActivity implements View.OnClickListener{
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f8_wx_pay_type_setting);
        findViewById(R.id.btn_select).setOnClickListener(this);
        findViewById(R.id.btn_select2).setOnClickListener(this);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        editor  = preference.edit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        speak(getString(R.string.voice_setting_paytype));
//        hideToolBar();
        setCenterTitle(getString(R.string.title_wx_select_type));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_select:
                editor.putBoolean(getString(R.string.key_pre_pay_saler),true);
                editor.putBoolean(getString(R.string.key_pre_pay_self),false);
                editor.commit();
                finish();
//                Intent mIntent = new Intent(F8WxPayTypeSettingActivity.this,MainActivity.class);
//                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(mIntent);
                break;
            case R.id.btn_select2:
                editor.putBoolean(getString(R.string.key_pre_pay_saler),false);
                editor.putBoolean(getString(R.string.key_pre_pay_self),true);
                editor.commit();
//                Intent mIntent2 = new Intent(F8WxPayTypeSettingActivity.this,MainActivity.class);
//                mIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(mIntent2);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        speak(getString(R.string.voice_setting_paytype));
    }
}
