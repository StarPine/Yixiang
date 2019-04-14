package com.example.mydrawing;

import java.io.File;

import util.ImageUtil;
import util.SelectPhotoUtil;
import util.SystemValue;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import entity.MyApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

@ContentView(R.layout.drawing_selection_layout)
public class VideoSelectionActivity extends Activity{
	
	@ViewInject(R.id.ten_s_ibtn)
	ImageButton ten_s_ibtn;

	@ViewInject(R.id.thirty_s_ibtn)
	ImageButton thirty_s_ibtn;
	
	@ViewInject(R.id.sixty_s_ibtn)
	ImageButton sixty_s_ibtn;
	
	@ViewInject(R.id.back_ibtn)
	ImageButton back_ibtn;
	
	@ViewInject(R.id.ok_ibtn)
	ImageButton ok_ibtn;
	
	@ViewInject(R.id.gview)
	GridView gridView;
	
	@ViewInject(R.id.take_get_pic_ibtn)
	ImageButton take_get_pic_ibtn;
	
//	boolean isThrity = true;
	String videosec = "10s";
	int screenWidth, screenHeight;
	SelectPhotoUtil selectPhotoUtil;
	Uri photoUri;	
	SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	
	int[] gv_pics = new int[]{R.drawable.s0001,R.drawable.s0002,R.drawable.s0003,R.drawable.s0004
			,R.drawable.s0005,R.drawable.s0006};
//			,R.drawable.s0007,R.drawable.s0008
//			,R.drawable.s0009,R.drawable.s0010,R.drawable.s0011,R.drawable.s0012
//			,R.drawable.s0013,R.drawable.s0014,R.drawable.s0015,R.drawable.s0016
//			,R.drawable.s0017,R.drawable.s0018};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		ViewUtils.inject(this);
		
		DisplayMetrics dm = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(dm);
		screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
		screenHeight = dm.heightPixels; // 屏幕高（像素，如：800p）
		
		selectPhotoUtil = new SelectPhotoUtil(this);
		gridView.setAdapter(myAdapter);
		
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {


				Intent intent = new Intent(VideoSelectionActivity.this,VideoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("record_sec", videosec);
				bundle.putString("ref_state", "s_ref");
				bundle.putInt("pic_index", position);
				bundle.putString("preVideo", "finished");
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
		preVideoDeal();
	}
	
	void preVideoDeal(){
		sharedPreferences= getSharedPreferences("preVideo",
                Activity.MODE_PRIVATE);
		//实例化SharedPreferences.Editor对象
        editor = sharedPreferences.edit();
		final String state = sharedPreferences.getString("state", "");
		final String videosec = sharedPreferences.getString("videosec", "");
		final String ref_state = sharedPreferences.getString("ref_state", "");
		
		if(state!=null&&!state.equals("")){
			if(state.equals("recording")){
				new AlertDialog.Builder(this,
						AlertDialog.THEME_HOLO_DARK)
						.setTitle("信息")
						.setMessage("上次视频录制未正常退出，是否继续上次视频录制？")
						.setPositiveButton("继续",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

										Intent intent = new Intent(VideoSelectionActivity.this,VideoActivity.class);
										Bundle bundle = new Bundle();
										bundle.putString("record_sec", videosec);
										bundle.putString("ref_state", ref_state);
										if(ref_state.equals("new_ref")){
											String picPath = sharedPreferences.getString("picPath", "");
											bundle.putString("ref_path", picPath);
										}
										else if(ref_state.equals("s_ref")){
											int position = sharedPreferences.getInt("pic_index", -1);
											bundle.putInt("pic_index", position);
										}
										bundle.putString("preVideo", state);
										intent.putExtras(bundle);
										startActivity(intent);
										finish();
									}
								}).setNegativeButton("放弃", 
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

										editor.putString("state", "finished");
										editor.apply();
									}
								}).create().show();
			}
			
			if(state.equals("convertion")){
				new AlertDialog.Builder(this,
						AlertDialog.THEME_HOLO_DARK)
						.setTitle("信息")
						.setMessage("上次视频压缩出错，是否继续压缩？")
						.setPositiveButton("继续",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

										Intent intent = new Intent(VideoSelectionActivity.this,VideoActivity.class);
										Bundle bundle = new Bundle();
										bundle.putString("record_sec", videosec);
										bundle.putString("ref_state", ref_state);
										if(ref_state.equals("new_ref")){
											String picPath = sharedPreferences.getString("picPath", "");
											bundle.putString("ref_path", picPath);
										}
										else if(ref_state.equals("s_ref")){
											int position = sharedPreferences.getInt("pic_index", -1);
											bundle.putInt("pic_index", position);
										}
										bundle.putString("preVideo", state);
										intent.putExtras(bundle);
										startActivity(intent);
										finish();
									}
								}).setNegativeButton("放弃", 
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

										editor.putString("state", "finished");
										editor.apply();
									}
								}).create().show();
			}
		}
		
	}
	
	@OnClick({R.id.ten_s_ibtn,R.id.thirty_s_ibtn,R.id.sixty_s_ibtn,R.id.back_ibtn,R.id.ok_ibtn,R.id.take_get_pic_ibtn})
	private void onClick(View v){
		switch (v.getId()) {
		case R.id.ten_s_ibtn:
//			isThrity = true;
			videosec = "10s";
			ten_s_ibtn.setImageResource(R.drawable.ten_s_selected);
			thirty_s_ibtn.setImageResource(R.drawable.thirty_s_unselect);
			sixty_s_ibtn.setImageResource(R.drawable.sixty_s_unselect);
			break;
		case R.id.thirty_s_ibtn:
//			isThrity = true;
			videosec = "30s";
			ten_s_ibtn.setImageResource(R.drawable.ten_s_unselect);
			thirty_s_ibtn.setImageResource(R.drawable.thirty_s_selected);
			sixty_s_ibtn.setImageResource(R.drawable.sixty_s_unselect);
			break;
		case R.id.sixty_s_ibtn:
//			isThrity = false;
			videosec = "60s";
			ten_s_ibtn.setImageResource(R.drawable.ten_s_unselect);
			thirty_s_ibtn.setImageResource(R.drawable.thirty_s_unselect);
			sixty_s_ibtn.setImageResource(R.drawable.sixty_s_selected);
			break;
		case R.id.back_ibtn:
			finish();
			break;
		case R.id.ok_ibtn:
			Intent intent = new Intent(VideoSelectionActivity.this,VideoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("record_sec", videosec);
			bundle.putString("ref_state", "no_ref");
			bundle.putString("preVideo", "finished");
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
			break;
		case R.id.take_get_pic_ibtn:
			ContentValues values = new ContentValues();
			photoUri = VideoSelectionActivity.this.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			selectPhotoUtil.selectPhoto(photoUri);
			break;
		}
	}
	
	BaseAdapter myAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {

			ImageView imageView = new ImageView(VideoSelectionActivity.this);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					(int) (screenWidth / 3.2), (int) (screenWidth / 3.2));
			imageView.setLayoutParams(params);
			imageView.setImageResource(gv_pics[position]);
			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			return imageView;
		}
		
		@Override
		public long getItemId(int arg0) {

			return 0;
		}
		
		@Override
		public Object getItem(int arg0) {

			return null;
		}
		
		@Override
		public int getCount() {

			return gv_pics.length;
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String picPath = null;
		
		if (requestCode == SystemValue.GET_PICTURE) {
			if(data!=null){
				Uri uri = data.getData();
				// to do find the path of pic by uri
				picPath = ImageUtil.getRealFilePath(VideoSelectionActivity.this, uri);
				
			}
			

		} else if (requestCode == SystemValue.TAKE_PICTURE) {

			String[] pojo = { MediaStore.Images.Media.DATA };
			Cursor cursor = VideoSelectionActivity.this.managedQuery(photoUri, pojo, null,
					null, null);
			if (cursor != null) {
				int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
				cursor.moveToFirst();
				picPath = cursor.getString(columnIndex);
				// cursor.close();
			}
		}

		if (picPath != null) {
			final File file = new File(picPath);
			if (file.exists()) {
				
				Intent intent = new Intent(VideoSelectionActivity.this,VideoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("record_sec", videosec);
				bundle.putString("ref_state", "new_ref");
				bundle.putString("ref_path", picPath);
				bundle.putString("preVideo", "finished");
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			} 
		}
	}
}
