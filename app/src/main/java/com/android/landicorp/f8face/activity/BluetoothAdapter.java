//package com.android.landicorp.f8face.activity;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import com.android.landicorp.f8face.R;
//import com.landicorp.robert.comm.api.DeviceInfo;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BluetoothAdapter extends BaseAdapter {
//
//	private Context context;
//	private List<DeviceInfo> deviceInfos;
//
//	public BluetoothAdapter(Context c) {
//		super();
//		context = c;
//		deviceInfos = new ArrayList<DeviceInfo>();
//	}
//
//	public void clear() {
//		deviceInfos.clear();
//		this.notifyDataSetChanged();
//	}
//
//	public void addDevice(DeviceInfo deviceInfo) {
//		deviceInfos.add(deviceInfo);
//		this.notifyDataSetChanged();
//	}
//
//	public DeviceInfo getDeviceInfo(int position) {
//		return deviceInfos.get(position);
//	}
//
//	@Override
//	public int getCount() {
//		// TODO Auto-generated method stub
//		return deviceInfos.size();
//	}
//
//	@Override
//	public Object getItem(int position) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public long getItemId(int position) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		// TODO Auto-generated method stub
//		if (convertView == null) {
//			LayoutInflater l = (LayoutInflater) context
//					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			convertView = l.inflate(R.layout.choose_bond_blue_listview_item,null);
//
//			TextView tvPosName = (TextView) convertView
//					.findViewById(R.id.tv_qpos_name);
//			tvPosName.setText(deviceInfos.get(position).getName());
//			TextView tvMac = (TextView) convertView
//					.findViewById(R.id.tv_bond_status);
//			tvMac.setText(deviceInfos.get(position).getIdentifier());
//		} else {
//			TextView tvPosName = (TextView) convertView
//					.findViewById(R.id.tv_qpos_name);
//			tvPosName.setText(deviceInfos.get(position).getName());
//			TextView tvMac = (TextView) convertView
//					.findViewById(R.id.tv_bond_status);
//			tvMac.setText(deviceInfos.get(position).getIdentifier());
//		}
//
//		return convertView;
//	}
//
//}
