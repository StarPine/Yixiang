package com.example.mydrawing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

import util.FileUtils;
import util.FileUtils.NoSdcardException;
import util.ImageUtil;
import util.MatrixImageView;
import util.SelectPhotoUtil;
import util.SystemValue;
import util.VideoCapture;
import util.VideoUpload;
import util.WeChatShare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.http.client.util.URLEncodedUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
;

import entity.DrawingDBOp;
import entity.DrawingInfo;
import entity.MyApplication;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;



public class DrawingActivity extends Activity  {

	RelativeLayout video_img_rl,pic_show_rl,drawing_rl;
	
	String video_local_path;
	SelectPhotoUtil selectPhotoUtil=null;


	Uri photoUri;
	String pic_path, new_picPath,old_picPath;
	String video_server_path,video_img_path;
	DrawingDBOp dbOp;
	int cur_local_id;
	
	ImageView video_iv,video_play_iv;
	ImageView img_iv;
	ImageView pic_show_iv;
	EditText content_et;
	TextView date_tv;
	ImageButton back_ibtn;//,pic_show_back_ibtn;
	ImageButton share_friend_ibtn,share_circle_ibtn,change_pic_ibtn;
	
	final int share_friend = 301, share_circle = 302;
	int cur_share_state;
	
	boolean isUpload = false,uloadFinised = false;

	int windowWidth=0,windowHeight=0;
	VideoUpload videoUpload;

	private final int video_upload_success = 101;
	private final int upload_failed = 102;
	private final int drawing_upload_success = 103;
	ProgressDialog progressDialog;
	
	WeChatShare weChatShare;
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case video_upload_success:
				String qcloudPath = (String) msg.obj;
				uploadDrawing(qcloudPath);
				break;
			case upload_failed:
				progressDialog.dismiss();
			case drawing_upload_success:
				progressDialog.dismiss();
				break;
			}
		}
	};
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		setContentView(R.layout.drawing_layout);

		DisplayMetrics dm = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(dm);
		
		windowWidth = display.getWidth();
		windowHeight = display.getHeight();
		
		video_local_path = getIntent().getExtras().getString("video_local_path");
		video_img_path = getIntent().getExtras().getString("video_img_path");
		pic_path = getIntent().getExtras().getString("img_path");
		isUpload = getIntent().getExtras().getBoolean("isUpload");

		intiView();
		
		dbOp = new DrawingDBOp(DrawingActivity.this);
		if(!isUpload){
			cur_local_id = Integer.parseInt(SystemValue.curLocalDrawingInfo.getDrawingId());
			new_picPath = SystemValue.curLocalDrawingInfo.getDrawingImg();
		}		
		
		videoUpload.getAssign();
		weChatShare = new WeChatShare(this);
		
		
	}
	
	@Override  
    public void onBackPressed() { 
		if(!isUpload&&!uloadFinised){
			new AlertDialog.Builder(DrawingActivity.this,AlertDialog.THEME_HOLO_DARK)
			.setTitle("信息")
			.setMessage("是否放弃上传本次作品？")
			.setPositiveButton("退出",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(
								DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							finish();
						}
					}).setNegativeButton("取消", null).create().show();
		}else {
			 super.onBackPressed();  
		}
               
    }  
	

	void intiView(){
		progressDialog = new ProgressDialog(DrawingActivity.this);
		
		video_img_rl = (RelativeLayout) findViewById(R.id.video_img_rl);
		pic_show_rl = (RelativeLayout) findViewById(R.id.pic_show_rl);
		drawing_rl = (RelativeLayout) findViewById(R.id.drawing_rl);
		
		pic_show_iv = (ImageView) findViewById(R.id.pic_show_iv);
		change_pic_ibtn = (ImageButton) findViewById(R.id.change_pic_ibtn);
//		pic_show_back_ibtn = (ImageButton) findViewById(R.id.pic_show_back_ibtn);
		
		selectPhotoUtil = new SelectPhotoUtil(this);
		videoUpload = new VideoUpload(this);
		
		video_iv = (ImageView) findViewById(R.id.video_iv);
		img_iv = (ImageView) findViewById(R.id.img_iv);
		video_play_iv = (ImageView) findViewById(R.id.video_play_iv);
		content_et =(EditText) findViewById(R.id.content_et);
		back_ibtn = (ImageButton) findViewById(R.id.back_ibtn);
		share_friend_ibtn = (ImageButton) findViewById(R.id.share_friend_ibtn);
		share_circle_ibtn = (ImageButton) findViewById(R.id.share_circle_ibtn);
		date_tv = (TextView) findViewById(R.id.date_tv);
		
		LayoutParams params = (LayoutParams) video_img_rl.getLayoutParams();
		params.width = windowWidth;
		params.height = windowWidth/2/3*4;
		video_img_rl.setLayoutParams(params);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		date_tv.setText(sdf.format(new Date(System.currentTimeMillis())));
		
		
//		Bitmap bitmap = SystemValue.getVideoThumbnail(video_local_path, ((windowWidth-20)/2), 350, Thumbnails.MINI_KIND);
//		video_iv.setImageBitmap(bitmap);
		if(isUpload){
			ImageLoader.getInstance()
			.displayImage(
					SystemValue.basic_url
							+ video_img_path,
							video_iv);
			ImageLoader.getInstance()
			.displayImage(
					SystemValue.basic_url
							+ pic_path,
							img_iv);
			change_pic_ibtn.setVisibility(View.GONE);
			content_et.setFocusable(false);
			content_et.setFocusableInTouchMode(false);
		}
		else {
			img_iv.setImageBitmap(SystemValue.lessenUriImage(pic_path, (windowWidth/2), windowWidth/2/3*4));
			video_iv.setImageBitmap(SystemValue.lessenUriImage(video_img_path, (windowWidth/2), windowWidth/2/3*4));
		}
		
		if(SystemValue.curLocalDrawingInfo!=null){
			String content = SystemValue.curLocalDrawingInfo.getDescription();
			if(content!=null&&!content.equals("")&&!content.equals("null")){
				content_et.setText(content);
			}
		}
		
		
		
		video_play_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_VIEW);
		        String type = "video/mp4";
		        Uri uri = Uri.parse(video_local_path);
		        intent.setDataAndType(uri, type);
		        startActivity(intent);
			}
		});
		
		img_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pic_show_rl.setVisibility(View.VISIBLE);
				drawing_rl.setVisibility(View.GONE);
				if(isUpload){
					ImageLoader.getInstance()
					.displayImage(
							SystemValue.basic_url
									+ pic_path,
									pic_show_iv);
				}
				else {
					pic_show_iv.setImageBitmap(SystemValue.lessenUriImage(pic_path, (windowWidth), windowWidth/3*4));
				}
				
				
//				selectPhoto();
//				ContentValues values = new ContentValues();
//				photoUri = DrawingActivity.this.getContentResolver().insert(
//						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//				
//				selectPhotoUtil.selectPhoto(photoUri);
			}
		});
		
		change_pic_ibtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ContentValues values = new ContentValues();
				photoUri = DrawingActivity.this.getContentResolver().insert(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				
				selectPhotoUtil.selectPhoto(photoUri);
			}
		});
		pic_show_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pic_show_rl.setVisibility(View.GONE);
				drawing_rl.setVisibility(View.VISIBLE);
			}
		});
//		pic_show_iv.setOnSingleTapListener(new MatrixImageView.OnSingleTapListener() {
//			
//			@Override
//			public void onSingleTap() {
//				// TODO Auto-generated method stub
//				pic_show_rl.setVisibility(View.GONE);
//				drawing_rl.setVisibility(View.VISIBLE);
//			}
//		});
		
		share_friend_ibtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				cur_share_state = share_friend;
				if((isUpload&&!uloadFinised)||(!isUpload&&uloadFinised)){
					
					shareDrawing();
//					Toast.makeText(DrawingActivity.this, "该作品正在分享", Toast.LENGTH_SHORT).show();
					
				}else {
					if(pic_path==null||pic_path.equals("")||pic_path.equals("null")){
						Toast.makeText(DrawingActivity.this, "您还没有选择照片哟，同您的作品合个影呗！", Toast.LENGTH_SHORT).show();
						
					}
					else {
						File file = new File(pic_path);
						if(file.exists()&&file.length()>0){
							File file2 = new File(video_local_path);
							if(file2.exists()&&file2.length()>0){
								uploadVideoToQcloud(video_local_path);
							}
							else {
								Toast.makeText(DrawingActivity.this, "视频文件不存在，请重新录制！", Toast.LENGTH_SHORT).show();
							}
						}
						else {
							Toast.makeText(DrawingActivity.this, "您还没有选择照片哟，同您的作品合个影呗！", Toast.LENGTH_SHORT).show();
						}
						
					}
				}
				
			}
		});
		
		share_circle_ibtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				cur_share_state = share_circle;
				if((isUpload&&!uloadFinised)||(!isUpload&&uloadFinised)){
					
					shareDrawing();
//					Toast.makeText(DrawingActivity.this, "该作品正在分享", Toast.LENGTH_SHORT).show();
					
				}else {
					if(pic_path==null||pic_path.equals("")||pic_path.equals("null")){
						Toast.makeText(DrawingActivity.this, "您还没有选择照片哟，同您的作品合个影呗！", Toast.LENGTH_SHORT).show();
					}
					else {
						File file = new File(pic_path);
						if(file.exists()&&file.length()>0){
							File file2 = new File(video_local_path);
							if(file2.exists()&&file2.length()>0){
								uploadVideoToQcloud(video_local_path);
							}
							else {
								Toast.makeText(DrawingActivity.this, "视频文件不存在，请重新录制！", Toast.LENGTH_SHORT).show();
							}
						}
						else {
							Toast.makeText(DrawingActivity.this, "您还没有选择照片哟，同您的作品合个影呗！", Toast.LENGTH_SHORT).show();
						}
					}
				}
				
			}
		});
		
		back_ibtn.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!isUpload&&!uloadFinised){
					new AlertDialog.Builder(DrawingActivity.this,AlertDialog.THEME_HOLO_DARK)
					.setTitle("信息")
					.setMessage("是否放弃上传本次作品？")
					.setPositiveButton("退出",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(
										DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									finish();
								}
							}).setNegativeButton("取消", null).create().show();
				}else {
					 finish(); 
				}
		             
				
			}
		});
	}	
	
	private void shareDrawing(){
		
		if(WeChatShare.isWXAppInstalledAndSupported){
			int share_type = 0;
			if(cur_share_state == share_circle){//分享到朋友圈
				share_type = WeChatShare.WXSceneTimeline;
			}
			else if(cur_share_state == share_friend){//发送给朋友
				share_type = WeChatShare.WXSceneSession;
			}
			Bitmap thumb = ((BitmapDrawable)img_iv.getDrawable()).getBitmap();			
			String description = SystemValue.curLocalDrawingInfo.getDescription();
			String drawingid = SystemValue.curLocalDrawingInfo.getDrawingId();
			weChatShare.share(drawingid, share_type, thumb, description);
		}
		else {
			Toast.makeText(DrawingActivity.this, "未安装微信或微信版本太低！", Toast.LENGTH_SHORT).show();
		}
		
		
		
		
		
	}
	
	@Override
	public void finish(){
		String content = content_et.getText().toString().trim();
		if(!isUpload&&!content.equals("")){
			DrawingInfo curDrawingInfo = SystemValue.curLocalDrawingInfo;
//			int id = Integer.parseInt(curDrawingInfo.getDrawingId());
			dbOp.updateDrawingContent(cur_local_id, content);
		}
		if(uloadFinised){
			Toast.makeText(DrawingActivity.this, "delete111", Toast.LENGTH_SHORT).show();
			DrawingInfo curDrawingInfo = SystemValue.curLocalDrawingInfo;
//			int id = Integer.parseInt(curDrawingInfo.getDrawingId());
			dbOp.deleteDrawing(cur_local_id);									
			FileUtils.deleteFile(curDrawingInfo.getDrawingImg());
			FileUtils.deleteFile(curDrawingInfo.getDrawingVideo());
			FileUtils.deleteFile(curDrawingInfo.getVideoCover());
			Toast.makeText(DrawingActivity.this, "delete222", Toast.LENGTH_SHORT).show();
		}
		super.finish();
	}
	
	
	private void uploadVideoToQcloud(String path){
		if (SystemValue.curUserInfo == null
				|| SystemValue.curUserInfo.getUserId() == null
				|| SystemValue.curUserInfo.getUserId().equals("")
				|| SystemValue.curUserInfo.getUserId().equals("null")) {
			// 直接跳转到登录页面
			Intent intent = new Intent(DrawingActivity.this,LoginActivity.class);
			intent.putExtra("isFromRegister", false);
			startActivity(intent);
			return;
		}
		progressDialog.show();
		String destFilePath = SystemValue.dest_head+SystemValue.curUserInfo.getUserId()+System.currentTimeMillis()+".mp4";
		videoUpload.uploadToQcloud(path, destFilePath,mHandler);
	}
	
	void uploadDrawing(String video_server_path){
				
		if (SystemValue.curUserInfo == null
				|| SystemValue.curUserInfo.getUserId() == null
				|| SystemValue.curUserInfo.getUserId().equals("")
				|| SystemValue.curUserInfo.getUserId().equals("null")) {
			// 直接跳转到登录页面
			Intent intent = new Intent(DrawingActivity.this,LoginActivity.class);
			intent.putExtra("isFromRegister", false);
			startActivity(intent);
			finish();
		}

		RequestParams params = new RequestParams();

		// params.addBodyParameter("auth_token", RsSharedUtil.getString(
		// getApplicationContext(), AppConfig.ACCESS_TOKEN));
		params.addQueryStringParameter("action", "create_drawing");
		params.addQueryStringParameter("drawing_video",
				video_server_path);
		String content = content_et.getText().toString().trim();
		if(!content.equals("")){
//			try {
				params.addQueryStringParameter("drawing_description",
						content);
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		params.addQueryStringParameter("user_id",
				SystemValue.curUserInfo.getUserId());

//		new_picPath = ImageUtil.compressImage(DrawingActivity.this, pic_path);
		params.addBodyParameter("image", new File(new_picPath));
		params.addBodyParameter("cover", new File(SystemValue.curLocalDrawingInfo.getVideoCover()));

		HttpUtils http = new HttpUtils();
		http.send(HttpMethod.POST, SystemValue.basic_url + "drawing.do",
				params, new RequestCallBack<String>() {

					@Override
					public void onStart() {
						Log.d("PostPicture", "准备上传文件...");
//						Toast.makeText(DrawingActivity.this, "准备上传文件1...", Toast.LENGTH_SHORT)
//								.show();
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						if (isUploading) {
							Log.d("PostPicture", "正在上传文件...");
//							Toast.makeText(DrawingActivity.this, "正在上传文件...", Toast.LENGTH_SHORT)
//									.show();
						} else {
							Log.d("PostPicture", "准备上传文件...");
//							Toast.makeText(DrawingActivity.this, "准备上传文件...", Toast.LENGTH_SHORT)
//									.show();
						}
					}

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						Log.d("上传成功", responseInfo.result);
						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(
									responseInfo.result);

							
							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {
								uloadFinised = true;
								change_pic_ibtn.setVisibility(View.GONE);
								content_et.setFocusable(false);
								content_et.setFocusableInTouchMode(false);
								
								Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
								SystemValue.curLocalDrawingInfo = gson.fromJson(jsonObject.getString("drawinginfo"), DrawingInfo.class);

								Message msg = new Message();
						        msg.what = drawing_upload_success;
						        mHandler.sendMessage(msg);
								shareDrawing();
							} else {
								Message msg = new Message();
						        msg.what = upload_failed;
						        mHandler.sendMessage(msg);

							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("dj_JSONException_action", e.toString());
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(HttpException error, String m) {
						Message msg = new Message();
				        msg.what = upload_failed;
				        mHandler.sendMessage(msg);

					}
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String picPath = null;
		
		if (requestCode == SystemValue.GET_PICTURE) {
			if(data!=null){
				Uri uri = data.getData();
				// to do find the path of pic by uri
				picPath = ImageUtil.getRealFilePath(DrawingActivity.this, uri);
				Toast.makeText(
						DrawingActivity.this,
						"picpath:"+picPath, Toast.LENGTH_SHORT).show();
			}
			

		} else if (requestCode == SystemValue.TAKE_PICTURE) {

			String[] pojo = { MediaStore.Images.Media.DATA };
			Cursor cursor = DrawingActivity.this.managedQuery(photoUri, pojo, null,
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
				if(file.length()<=0){
					file.delete();
					return;
				}
				
				new_picPath = ImageUtil.compressImage(DrawingActivity.this, picPath,false);
				old_picPath = SystemValue.curLocalDrawingInfo.getDrawingImg();
//				dbOp.updateDrawing(cur_local_id, new_picPath, null);
				dbOp.updateDrawingImgPath(cur_local_id, new_picPath);
				SystemValue.curLocalDrawingInfo.setDrawingImg(new_picPath);
				FileUtils.deleteFile(old_picPath);
				
				img_iv.setImageBitmap(SystemValue.lessenUriImage(picPath, (windowWidth/2), windowWidth/2/3*4));
				pic_path = picPath;
				pic_show_rl.setVisibility(View.GONE);
				drawing_rl.setVisibility(View.VISIBLE);
				
			} else {
				Toast.makeText(
						DrawingActivity.this,
						DrawingActivity.this.getResources().getString(
								R.string.get_pic_failed), Toast.LENGTH_SHORT).show();
			}
		}else {
			Toast.makeText(
					DrawingActivity.this,
					DrawingActivity.this.getResources().getString(
							R.string.get_pic_failed), Toast.LENGTH_SHORT).show();
		}
	}
}
