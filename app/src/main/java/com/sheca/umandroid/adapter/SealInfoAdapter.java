package com.sheca.umandroid.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.junyufr.szt.util.Base64ImgUtil;
import com.sheca.umandroid.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SealInfoAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;

	public SealInfoAdapter(Activity a, List<Map<String, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.sealitem, null);

		TextView sealname = (TextView) vi.findViewById(R.id.sealname);
		TextView validtime = (TextView) vi.findViewById(R.id.validtime);
		ImageView sealPic = (ImageView) vi.findViewById(R.id.list_image);
		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);
			
			// Setting all values in listview
			sealname.setText(certmap.get("sealname"));
			validtime.setText("印章有效期:"+certmap.get("validtime"));
		
			Bitmap bitMap = bitMapScale(stringtoBitmap(certmap.get("picdata")),0.3f);
			sealPic.setImageBitmap(bitMap);
			sealPic.invalidate();
		}

		return vi;
	}
	
	public Bitmap stringtoBitmap(String picString){
	    //将字符串转换成Bitmap类型
	    Bitmap bitmap = null;
	    try {
	    	byte[] bitmapdata = Base64ImgUtil.GenerateImageByte(picString);
	    	bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length); 
	    } catch (Exception e) {
	       e.printStackTrace();
	    }
	   
	    return bitmap;
	}
	
	public Bitmap bitMapScale(Bitmap bitmap,float scale) {
		if(null == bitmap)
			return bitmap;
		
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        
        return resizeBmp;
	}

}