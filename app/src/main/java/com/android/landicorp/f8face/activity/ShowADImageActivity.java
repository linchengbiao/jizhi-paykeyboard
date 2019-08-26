package com.android.landicorp.f8face.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.util.ArraySet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.landicorp.f8face.R;
import com.android.landicorp.f8face.view.AdGridViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShowADImageActivity extends BaseActivity {
    private final int PHOTO_REQUEST_GALLERY = 100;
    private GridView gridView;
    AdGridViewAdapter adGridViewAdapter;
    private List<Map<String,Object>> list;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_adimage);
        gridView = findViewById(R.id.gv_image);
        list = new ArrayList<>();
        adGridViewAdapter = new AdGridViewAdapter(this,list);
        gridView.setAdapter(adGridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gallery();
            }
        });
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor  = sharedPreferences.edit();
        Set<String> images = sharedPreferences.getStringSet("Images",null);
        if (images!=null&&images.size()>0){
            Iterator<String> iterator = images.iterator();
            while (iterator.hasNext()){
                String path = iterator.next();
                photoPath(path);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_REQUEST_GALLERY) {
                // 从相册返回的数据
                if (data != null) {
                    // 得到图片的全路径
                    Uri uri = data.getData();
                    String[] proj = {MediaStore.Images.Media.DATA};
                    //好像是android多媒体数据库的封装接口，具体的看Android文档
                    Cursor cursor = managedQuery(uri, proj, null, null, null);
                    //按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    //将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    //最后根据索引值获取图片路径
                    String path = cursor.getString(column_index);
                    photoPath(path);
                }
            }

        }
    }

    /**
     * 从相册获取2
     */
    public void gallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    public void photoPath(String path) {
        Map<String,Object> map=new HashMap<>();
        map.put("path",path);
        list.add(map);
        adGridViewAdapter.notifyDataSetChanged();
    }

    private void saveImagesAd(){
        Set<String> arraySet = new ArraySet<>();
        for (int i=0;i<list.size();i++){
            String path = (String) list.get(i).get("path");
            arraySet.add(path);
        }
        editor.putStringSet("Images",arraySet);
        editor.commit();
    }



    @Override
    public void onBackPressed() {
        saveImagesAd();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        saveImagesAd();
        super.finish();
    }
}
