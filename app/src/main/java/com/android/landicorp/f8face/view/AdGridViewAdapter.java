package com.android.landicorp.f8face.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.landicorp.f8face.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2019/7/9.
 */

public class AdGridViewAdapter extends BaseAdapter{
    private Context mContext;
    private List<Map<String,Object>> list;
    private LayoutInflater inflater;
    private int maxImages = 9;

    public AdGridViewAdapter(Context context, List list){
        this.mContext = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int count = list == null ? 1 : list.size() + 1;
        if (count >= maxImages) {
            return list.size();
        } else {
            return count;
        }
    }
    public void notifyDataSetChanged(List<Map<String, Object>> datas) {
        this.list = datas;
        this.notifyDataSetChanged();
    }
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView = inflater.inflate(R.layout.item_grid_image,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        /**代表+号之前的需要正常显示图片**/
        if (list != null && position < list.size()) {
            final File file = new File(list.get(position).get("path").toString());
            Glide.with(mContext)
                    .load(file)
                    .priority(Priority.HIGH)
                    .into(viewHolder.ivAd);
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (file.exists()) {
                        file.delete();
                    }
                    list.remove(position);
                    notifyDataSetChanged();
                }
            });
        } else {
            /**代表+号的需要+号图片显示图片**/
            Glide.with(mContext)
                    .load(R.drawable.image_add)
                    .priority(Priority.HIGH)
                    .centerCrop()
                    .into(viewHolder.ivAd);
            viewHolder.ivAd.setScaleType(ImageView.ScaleType.FIT_XY);
            viewHolder.ivDelete.setVisibility(View.GONE);
        }
        return convertView;
    }

    public class ViewHolder{
        public ImageView ivAd;
        public ImageView ivDelete;
        public ViewHolder(View view){
            ivAd = view.findViewById(R.id.iv_gd);
            ivDelete = view.findViewById(R.id.iv_delete);
        }
    }
}
