package com.example.weixin_yuyin.view;

import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.weixin_yuyin.MainActivity.Recorder;
import com.example.weixin_yuyin.R;

public class RecorderAdapter extends ArrayAdapter<Recorder> {

	private List<Recorder> mDatas;
	private Context mContext;
	
	private int minItemWidth;
	private int maxItemWidth;
	
	private LayoutInflater mInflater;
	public RecorderAdapter(Context context, List<Recorder> datas) {
		super(context,-1 ,datas);
		
		mContext = context;
		mDatas = datas;
		
		mInflater = LayoutInflater.from(context);
		
		WindowManager wm =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		
		maxItemWidth = (int) (outMetrics.widthPixels * 0.7f);
		minItemWidth = (int) (outMetrics.widthPixels * 0.15f);
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(viewHolder == null)
		{
			convertView = mInflater.inflate(R.layout.item_recorder, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.seconds = (TextView) convertView.findViewById(R.id.id_time);
			viewHolder.length = convertView.findViewById(R.id.id_recorder_length);
			
			convertView.setTag(viewHolder);
		}else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.seconds.setText(Math.round(getItem(position).getTime()) + "\"");
		ViewGroup.LayoutParams lp = viewHolder.length.getLayoutParams();
		lp.width = (int) (minItemWidth + maxItemWidth /60f * getItem(position).getTime());
		return convertView;
	}
	
	private class ViewHolder
	{
		TextView seconds;
		View length;
	}

}
