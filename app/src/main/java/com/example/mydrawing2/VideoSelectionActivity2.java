package com.example.mydrawing2;

import java.io.File;
import util.ImageUtil;
import util.SelectPhotoUtil;
import util.SystemValue;

import com.example.mydrawing.R;
import com.example.mydrawing.VideoActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import entity.MyApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

@ContentView(R.layout.drawing_selection_layout2)
public class VideoSelectionActivity2 extends Activity{
	
	ImageButton ten_s_ibtn;

	ImageButton thirty_s_ibtn;
	
	ImageButton sixty_s_ibtn,time_select_cancel;

	@ViewInject(R.id.no_ref_ibtn)
	ImageButton no_ref_ibtn;
	
	@ViewInject(R.id.video_ref_iv)
	ImageView video_ref_iv;
	
	
	
	@ViewInject(R.id.gview)
	GridView gridView;
	
	@ViewInject(R.id.album_ref_ibtn)
	ImageButton album_ref_ibtn;
	@ViewInject(R.id.photograph_ref_ibtn)
	ImageButton photograph_ref_ibtn;
	
//	boolean isThrity = true;
	String videosec = "10s";
	int screenWidth, screenHeight;
	SelectPhotoUtil selectPhotoUtil;
	Uri photoUri;	
	SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	
	Intent intent;
	Bundle bundle;
	View timeSelectView;
	Builder builder;
	AlertDialog dialog;
	
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
//		Toast.makeText(this, "w,h: "+screenWidth+" "+screenHeight, Toast.LENGTH_SHORT).show();
		
		initView();
		
		intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
		bundle = new Bundle();
		
		selectPhotoUtil = new SelectPhotoUtil(this);
		gridView.setAdapter(myAdapter);
		
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub

//				Intent intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
//				Bundle bundle = new Bundle();
				bundle.clear();
				bundle.putString("record_sec", videosec);
				bundle.putString("ref_state", "s_ref");
				bundle.putInt("pic_index", position);
				bundle.putString("preVideo", "finished");
				showTimeSelection();
//				intent.putExtras(bundle);
//				startActivity(intent);
//				finish();
			}
		});
		preVideoDeal();
	}
	
	void initView(){
		int w = (int) (screenWidth*0.92);
		int h = w * 240 / 556;
		RelativeLayout.LayoutParams rlparams = new RelativeLayout.LayoutParams(w, h);
		video_ref_iv.setLayoutParams(rlparams);
		
		w = (int) ((screenWidth*0.86)/3);
		h = w * 239 / 174;
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(w, h);
		llParams.weight = 1;
		album_ref_ibtn.setLayoutParams(llParams);
		photograph_ref_ibtn.setLayoutParams(llParams);
		no_ref_ibtn.setLayoutParams(llParams);
	}
	
	void preVideoDeal(){
		sharedPreferences= getSharedPreferences("preVideo",
                Activity.MODE_PRIVATE);
		//实例化SharedPreferences.Editor对象
        editor = sharedPreferences.edit();
		final String state = sharedPreferences.getString("state", "");
		final String videosec = sharedPreferences.getString("videosec", "");
		final String ref_state = sharedPreferences.getString("ref_state", "");
		final String videoUrl = sharedPreferences.getString("videoUrl", "");
		
		if(state!=null&&!state.equals("")){
			if(state.equals("recording")){
				new Builder(this,
						AlertDialog.THEME_HOLO_DARK)
						.setTitle("信息")
						.setMessage("上次视频录制未正常退出，是否继续上次视频录制？")
						.setPositiveButton("继续",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
//										Intent intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
//										Bundle bundle = new Bundle();
										bundle.clear();
										bundle.putString("record_sec", videosec);
										bundle.putString("preVideo", state);
										if(videoUrl.equals("")){
											bundle.putString("ref_state", ref_state);
											if(ref_state.equals("new_ref")){
												String picPath = sharedPreferences.getString("picPath", "");
												bundle.putString("ref_path", picPath);

											} else if(ref_state.equals("s_ref")){
												int position = sharedPreferences.getInt("pic_index", -1);
												bundle.putInt("pic_index", position);
											}
											
											intent.putExtras(bundle);
											startActivity(intent);
											finish();

										}else {
											Intent intent1 = new Intent(VideoSelectionActivity2.this,VideoAndRecording.class);
											bundle.putString("videoUrl", videoUrl);
											intent1.putExtras(bundle);
											startActivity(intent1);
											finish();
										}
										
									}
								}).setNegativeButton("放弃", 
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										editor.putString("state", "finished");
										editor.apply();
									}
								}).create().show();
			}
			
			if(state.equals("convertion")){
				new Builder(this,
						AlertDialog.THEME_HOLO_DARK)
						.setTitle("信息")
						.setMessage("上次视频压缩出错，是否继续压缩？")
						.setPositiveButton("继续",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
//										Intent intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
//										Bundle bundle = new Bundle();
										bundle.clear();
										bundle.putString("record_sec", videosec);
										bundle.putString("preVideo", state);
										
										if(videoUrl.equals("")){
											bundle.putString("ref_state", ref_state);
											if(ref_state.equals("new_ref")){
												String picPath = sharedPreferences.getString("picPath", "");
												bundle.putString("ref_path", picPath);
											}
											else if(ref_state.equals("s_ref")){
												int position = sharedPreferences.getInt("pic_index", -1);
												bundle.putInt("pic_index", position);
											}
											intent.putExtras(bundle);
											startActivity(intent);
											finish();
										}
										else {
											Intent intent1 = new Intent(VideoSelectionActivity2.this,VideoAndRecording.class);
											bundle.putString("videoUrl", videoUrl);
											intent1.putExtras(bundle);
											startActivity(intent1);
											finish();
										}
										
									}
								}).setNegativeButton("放弃", 
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										editor.putString("state", "finished");
										editor.apply();
									}
								}).create().show();
			}
		}
		
	}
	
	void showTimeSelection(){
		if(builder==null){
			builder = new Builder(this);
			
		}
		if(timeSelectView==null){
			timeSelectView = LayoutInflater.from(this).inflate(R.layout.time_selection_layout, null);
			ten_s_ibtn = (ImageButton) timeSelectView.findViewById(R.id.ten_s_ibtn);
			thirty_s_ibtn = (ImageButton) timeSelectView.findViewById(R.id.thirty_s_ibtn);
			sixty_s_ibtn = (ImageButton) timeSelectView.findViewById(R.id.sixty_s_ibtn);
			time_select_cancel = (ImageButton) timeSelectView.findViewById(R.id.cancel_ibtn);
			
			
			ten_s_ibtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					videosec = "10s";
					ten_s_ibtn.setImageResource(R.drawable.ten_s_selected);
					thirty_s_ibtn.setImageResource(R.drawable.fifteen_s_unselect);
					sixty_s_ibtn.setImageResource(R.drawable.thirty_s_unselect);
					bundle.putString("record_sec", videosec);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				}
			});
			thirty_s_ibtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					videosec = "15s";
					ten_s_ibtn.setImageResource(R.drawable.ten_s_unselect);
					thirty_s_ibtn.setImageResource(R.drawable.fifteen_s_select);
					sixty_s_ibtn.setImageResource(R.drawable.thirty_s_unselect);
					bundle.putString("record_sec", videosec);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				}
			});
			sixty_s_ibtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					videosec = "30s";
					ten_s_ibtn.setImageResource(R.drawable.ten_s_unselect);
					thirty_s_ibtn.setImageResource(R.drawable.fifteen_s_unselect);
					sixty_s_ibtn.setImageResource(R.drawable.thirty_s_selected);
					bundle.putString("record_sec", videosec);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				}
			});
			time_select_cancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			
			builder.setView(timeSelectView);
			dialog = builder.create();
		}
		dialog.show();
		
	}
	
	@OnClick({R.id.back_ibtn,R.id.no_ref_ibtn,R.id.album_ref_ibtn,R.id.photograph_ref_ibtn,R.id.video_ref_iv})
	private void onClick(View v){
		switch (v.getId()) {
		case R.id.video_ref_iv:
			Intent intent = new Intent(VideoSelectionActivity2.this,VideoShop.class);			
			startActivity(intent);
			break;
		case R.id.back_ibtn:
			finish();
			break;
		case R.id.no_ref_ibtn:
//			Intent intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
//			Bundle bundle = new Bundle();
			bundle.clear();
			bundle.putString("record_sec", videosec);
			bundle.putString("ref_state", "no_ref");
			bundle.putString("preVideo", "finished");
			showTimeSelection();
//			intent.putExtras(bundle);
//			startActivity(intent);
//			finish();
			break;
		case R.id.album_ref_ibtn:
//			ContentValues values = new ContentValues();
//			photoUri = VideoSelectionActivity2.this.getContentResolver().insert(
//					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//			selectPhotoUtil.selectPhoto(photoUri);
			selectPhotoUtil.getPic();
			break;
		case R.id.photograph_ref_ibtn:
			ContentValues values = new ContentValues();
			photoUri = VideoSelectionActivity2.this.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			selectPhotoUtil.takePic(photoUri);
			break;
		}
	}
	
	BaseAdapter myAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ImageView imageView = new ImageView(VideoSelectionActivity2.this);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					(int) (screenWidth / 3.2), (int) (screenWidth / 3.2));
			imageView.setLayoutParams(params);
			imageView.setImageResource(gv_pics[position]);
			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			return imageView;
		}
		
		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
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
				picPath = ImageUtil.getRealFilePath(VideoSelectionActivity2.this, uri);
				
			}
			

		} else if (requestCode == SystemValue.TAKE_PICTURE) {

			String[] pojo = { MediaStore.Images.Media.DATA };
			Cursor cursor = VideoSelectionActivity2.this.managedQuery(photoUri, pojo, null,
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
				
//				Intent intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
//				Bundle bundle = new Bundle();
				bundle.clear();
				bundle.putString("record_sec", videosec);
				bundle.putString("ref_state", "new_ref");
				bundle.putString("ref_path", picPath);
				bundle.putString("preVideo", "finished");
				showTimeSelection();
//				intent.putExtras(bundle);
//				startActivity(intent);
//				finish();
			} 
		}
	}
}
