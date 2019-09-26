//package com.android.landicorp.f8face.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import com.android.landicorp.f8face.R;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChooseBluActivity extends BaseActivity implements OnItemClickListener,OnClickListener {
//    private static final String DEBUG_TAG = "ReaderSetActivity";
//    private BluetoothAdapter bluetoothAdapter;
//    private ListView lvBluetooth;
//    private ProgressBar progressBar;
////    private Pattern pattern = Pattern.compile("^M3[0-9]-[0-9]{8}");
//    private Button stopsearchbtn;
//    BasicReader reader;
//
//    public void onCreate(Bundle b) {
//        super.onCreate(b);
//        this.setContentView(R.layout.choose_bond_blue_activity);
//        bluetoothAdapter = new BluetoothAdapter(this);
//        lvBluetooth = (ListView) this.findViewById(R.id.listview);
//        lvBluetooth.setAdapter(bluetoothAdapter);
//        lvBluetooth.setOnItemClickListener(this);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.VISIBLE);
//        stopsearchbtn = (Button)findViewById(R.id.stopsearchbtn);
//        stopsearchbtn.setOnClickListener(this);
//        reader = BasicReader.getInstance();
//        reader.init(this);
//        reader.startSearchDev(new CommunicationManagerBase.DeviceSearchListener() {
//            @Override
//            public void discoverOneDevice(DeviceInfo deviceInfo) {
//                if (deviceInfo.getName() != null) {
//                    bluetoothAdapter.addDevice(deviceInfo);
//                }
//            }
//
//            @Override
//            public void discoverComplete() {
//                progressBar.setVisibility(View.GONE);
//            }
//        },false,true,60000);
//    }
//
//    @Override
//    public void onClick(View arg0) {
//    	if(arg0.getId() == R.id.stopsearchbtn)
//    	{
//
//    		Toast.makeText(getApplicationContext(), "停止搜索", Toast.LENGTH_SHORT).show();
//    	}
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//        deviceInfo = bluetoothAdapter.getDeviceInfo(arg2);
//        editor.putString("deviceid",deviceInfo.getIdentifier());
//        editor.putString("devicename",deviceInfo.getName());
//        reader.openDevice(deviceInfo);
//        String value = "111111";
//        byte[] b = value.getBytes();
//        List<Byte> data = new ArrayList<>();
//        for (int i=0;i<b.length;i++){
//            data.add(b[i]);
//        }
//        reader.sendAPDU("111111".getBytes());
//        Intent mIntent = new Intent();
//        mIntent.putExtra("devicename", deviceInfo.getName());
//        mIntent.putExtra("deviceid", deviceInfo.getIdentifier());
//        mIntent.putExtra("devicechannel", deviceInfo.getDevChannel());
//        setResult(RESULT_OK, mIntent);
//        finish();
//    }
//
//
//}
