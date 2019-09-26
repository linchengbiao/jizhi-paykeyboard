package com.android.landicorp.f8face.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.Toast;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.activity.F8WxCurrentPayTypeActivity;
import com.android.landicorp.f8face.activity.ShowADImageActivity;
import com.android.landicorp.f8face.view.CusPreferenceWithArrowView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

//import com.lqr.imagepicker.ImagePicker;
//import com.lqr.imagepicker.bean.ImageItem;
//import com.lqr.imagepicker.ui.ImageGridActivity;
//import com.lqr.imagepicker.ui.ImagePreviewActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link F8WxSettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link F8WxSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class F8WxSettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public static final int IMAGE_PICKER = 100;
    private CusPreferenceWithArrowView wxSettingPreHb,wxSettingPrePayType,wxSettingPreAccount,wxSettingSystem,wxBleHIDSetting;
    private OnFragmentInteractionListener mListener;

    public F8WxSettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment F8SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static F8WxSettingFragment newInstance(String param1, String param2) {
        F8WxSettingFragment fragment = new F8WxSettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        addPreferencesFromResource(R.xml.pref_wx_setting);
        wxSettingPreHb = (CusPreferenceWithArrowView)findPreference(getString(R.string.key_wx_pre1));
        wxSettingPrePayType = (CusPreferenceWithArrowView)findPreference(getString(R.string.key_wx_pre2));
        wxSettingPreAccount = (CusPreferenceWithArrowView)findPreference(getString(R.string.key_wx_pre3));
        wxSettingSystem = (CusPreferenceWithArrowView)findPreference(getString(R.string.key_wx_pre4));
        wxBleHIDSetting  = (CusPreferenceWithArrowView)findPreference(getString(R.string.key_wx_pre5));

        wxSettingPreHb.setOnPreferenceClickListener(this);
        wxSettingPrePayType.setOnPreferenceClickListener(this);
        wxSettingPreAccount.setOnPreferenceClickListener(this);
        wxSettingSystem.setOnPreferenceClickListener(this);
        wxBleHIDSetting.setOnPreferenceClickListener(this);
    }




//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_f8_setting, container, false);
//    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Field mField = null;
        try {
            //反射调用getListView 隐藏分割线
            Class<? extends PreferenceFragment> clazz = this.getClass();
            Method method = clazz.getMethod("getListView");
            ListView listView = (ListView)method.invoke(this);
            listView.setDivider(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bundle bundle = getArguments();
        boolean isPayBySaler = bundle.getBoolean("isPayBySaler");

        wxSettingPrePayType.setTvValue(isPayBySaler?"独立收银模式":"连接收银机模式");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equalsIgnoreCase(getString(R.string.key_wx_pre1))){
            pickImage();
        }else if(preference.getKey().equalsIgnoreCase(getString(R.string.key_wx_pre2))){
            Intent mIntent = new Intent(getActivity(), F8WxCurrentPayTypeActivity.class);
            startActivity(mIntent);
        }else if(preference.getKey().equalsIgnoreCase(getString(R.string.key_wx_pre3))){
            Toast.makeText(getActivity(),"正在开放中，敬请期待",Toast.LENGTH_LONG).show();
        }else if(preference.getKey().equalsIgnoreCase(getString(R.string.key_wx_pre4))){
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }else if(preference.getKey().equalsIgnoreCase(getString(R.string.key_wx_pre5))){
//            Intent mIntent = new Intent(getActivity(), ChooseBluActivity.class);
//            startActivity(mIntent);
            Toast.makeText(getActivity(),"正在开放中，敬请期待",Toast.LENGTH_LONG).show();

        }
        return true;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void pickImage() {
        Intent intent = new Intent(getActivity(), ShowADImageActivity.class);
        startActivityForResult(intent, IMAGE_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {//返回多张照片
//            if (data != null) {
//                //是否发送原图
//                boolean isOrig = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
//                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
//
//                Log.e("CSDN_LQR", isOrig ? "发原图" : "不发原图");//若不发原图的话，需要在自己在项目中做好压缩图片算法
//                for (ImageItem imageItem : images) {
//                    Log.e("CSDN_LQR", imageItem.path);
//                }
//            }
//        }
    }
}
