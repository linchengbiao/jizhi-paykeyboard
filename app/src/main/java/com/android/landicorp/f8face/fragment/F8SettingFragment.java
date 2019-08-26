package com.android.landicorp.f8face.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.widget.ListView;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.view.CusPreferenceCheckBox;
import com.android.landicorp.f8face.view.CusPreferenceSwitch;
import com.android.landicorp.f8face.view.CusPreferenceWithArrowView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link F8SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link F8SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class F8SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private CusPreferenceWithArrowView wifiPref,wlanPre;
    private CusPreferenceSwitch mobileNetPref;
    private CheckBoxPreference paySelfPre,salerPayPre;
    private OnFragmentInteractionListener mListener;

    public F8SettingFragment() {
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
    public static F8SettingFragment newInstance(String param1, String param2) {
        F8SettingFragment fragment = new F8SettingFragment();
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
        addPreferencesFromResource(R.xml.pref_setting);
        wifiPref = (CusPreferenceWithArrowView)findPreference(getString(R.string.key_pre_wifi));
        mobileNetPref = (CusPreferenceSwitch)findPreference(getString(R.string.key_pre_mobile));
        wlanPre = (CusPreferenceWithArrowView)findPreference(getString(R.string.key_pre_line));
        paySelfPre = (CheckBoxPreference)findPreference(getString(R.string.key_pre_pay_self));
        salerPayPre= (CusPreferenceCheckBox)findPreference(getString(R.string.key_pre_pay_saler));

        wifiPref.setOnPreferenceClickListener(this);
        mobileNetPref.setOnPreferenceClickListener(this);
        wlanPre.setOnPreferenceClickListener(this);
        paySelfPre.setOnPreferenceChangeListener(this);
        salerPayPre.setOnPreferenceChangeListener(this);
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

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equalsIgnoreCase(getString(R.string.key_pre_wifi))){

        }else if(preference.getKey().equalsIgnoreCase(getString(R.string.key_pre_mobile))){

        }else if(preference.getKey().equalsIgnoreCase(getString(R.string.key_pre_line))){

        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference==paySelfPre){
            paySelfPre.setChecked((Boolean)newValue);
            salerPayPre.setChecked(!(Boolean)newValue);
        }else if(preference == salerPayPre){
            salerPayPre.setChecked((Boolean)newValue);
            paySelfPre.setChecked(!(Boolean)newValue);
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
}
