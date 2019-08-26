package com.android.landicorp.f8face.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.fragment.F8WxSettingFragment;
import com.android.landicorp.f8face.inter.KeyBoardCancelMessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class F8SettingActivity extends BaseActivity implements F8WxSettingFragment.OnFragmentInteractionListener{
    private Fragment settingFragment;
    private FragmentTransaction transaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f8_setting);
        FragmentManager fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        settingFragment = new F8WxSettingFragment();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLeftIv(R.drawable.ic_arrow_back,getString(R.string.title_back));
        setLeftTextColor(getResources().getColor(R.color.white));
        setCenterTitle(getString(R.string.title_app_setting));
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isPayBySaler",isPayBySaler);
            settingFragment.setArguments(bundle);
            transaction.add(R.id.fl_pre,settingFragment);
            transaction.commit();
        }catch (Exception e){

        }

    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(KeyBoardCancelMessageEvent event) {
        onTradeCancel();
    }

    @Override
    public void onTradeCancel() {
        this.finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
