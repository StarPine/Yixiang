package com.example.mydrawing;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import util.FileUtils;
import util.ImageUtil;
import util.MatrixImageView;
import util.PullToRefreshView;
import util.SelectPhotoUtil;
import util.PullToRefreshView.OnFooterRefreshListener;
import util.PullToRefreshView.OnHeaderRefreshListener;
import util.RefreshReceiver;
import util.RefreshReceiver.Refresh;
import util.SystemValue;
import util.VideoCapture;
import util.VideoUpload;
import android.R.integer;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;
;

import entity.DrawingDBOp;
import entity.DrawingInfo;

public class HomeLocalFragment extends Fragment{

	// 活动列表
	@ViewInject(R.id.me_drawing_lv)
	private ListView me_drawing_lv;
	
	@ViewInject(R.id.no_data_rl)
	private RelativeLayout no_data_rl;

	// 数据
	private List<DrawingInfo> drawingInfos = new ArrayList<DrawingInfo>();

	// 适配器
	public static  ListViewAdapter lvAdapter;

	private RequestQueue volleyRequestQueue;
	private String url = null;
	public static boolean isEditStatu = false;
	private Uri photoUri;
	private ImageView cur_img_to_change_iv;
	private int cur_drawing_to_change_index;
	String new_picPath,old_picPath;
	DrawingInfo curDrawingInfo;
	
	
	int windowWidth=0,windowHeight=0;
	SelectPhotoUtil selectPhotoUtil=null;
	DrawingDBOp dbOp;

//	@ViewInject(R.id.pic_show_rl)
	RelativeLayout pic_show_rl;
	
//	@ViewInject(R.id.pic_show_iv)
	ImageView pic_show_iv;
//	@ViewInject(R.id.pic_show_back_ibtn)
//	ImageButton pic_show_back_ibtn;
	
	VideoUpload videoUpload;
	
	private final int video_upload_success = 101;
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case video_upload_success:
				String qcloudPath = (String) msg.obj;
				uploadDrawing(qcloudPath);
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.local_layout, container, false);
		
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Rect frame = new Rect();
		getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

		windowWidth = display.getWidth();
		windowHeight = display.getHeight();

		ViewUtils.inject(this, view);
		volleyRequestQueue = Volley.newRequestQueue(getActivity());
		selectPhotoUtil = new SelectPhotoUtil(getActivity());
		videoUpload = new VideoUpload(getActivity());
		
		
		initView();
		setOnclic();
		
		
		return view;
	}

	public void initView() {
		

		if(dbOp==null){
			dbOp = new DrawingDBOp(getActivity());
		}	
		
		lvAdapter = new ListViewAdapter(getActivity(), drawingInfos);
		me_drawing_lv.setAdapter(lvAdapter);
		getAction();
		
		MainActivity mainActivity = (MainActivity)getActivity();
		pic_show_rl = mainActivity.pic_show_rl;
		pic_show_iv = mainActivity.pic_show_iv;

	}
	
	private void setOnclic(){
		pic_show_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				pic_show_rl.setVisibility(View.GONE);
			}
		});
//		pic_show_iv.setOnSingleTapListener(new MatrixImageView.OnSingleTapListener() {
//			
//			@Override
//			public void onSingleTap() {
//
//				pic_show_rl.setVisibility(View.GONE);
////				pic_show_iv.setScaleType(ScaleType.FIT_CENTER);
//			}
//		});
		
		
	}
	

	@Override
	public void onHiddenChanged(boolean hidden) {

		Toast.makeText(getActivity(), "localhidden:"+hidden, Toast.LENGTH_SHORT).show();
		if(!hidden&&SystemValue.per_fragment_index!=SystemValue.cur_fragment_index){
			getAction();
		}
		
		super.onHiddenChanged(hidden);
	}
	
//	@Override
//	public void onResume(){
//		super.onResume();
//		Toast.makeText(getActivity(), "onresume", Toast.LENGTH_SHORT).show();
//		getAction();
//	}

	class ListViewAdapter extends BaseAdapter {
		private Context mContext;
		private LayoutInflater inflater;
		private List<DrawingInfo> list;
		private String avatar;
		private String userName;

		public ListViewAdapter(Context context, List<DrawingInfo> list) {
			this.mContext = context;
			inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.list = list;
		}

		@Override
		public int getCount() {

			if(list.size()==0){
				no_data_rl.setVisibility(View.VISIBLE);
			}
			else {
				no_data_rl.setVisibility(View.GONE);
			}
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {

			return null;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {


			final DrawingInfo drawingInfo = list.get(position);

			convertView = inflater.inflate(R.layout.drawing_item, null);

			// 显示控件
			RelativeLayout show_rl = (RelativeLayout) convertView
					.findViewById(R.id.show_rl);
			LinearLayout name_collect_ll = (LinearLayout) convertView
					.findViewById(R.id.name_collect_ll);
			// TextView user_name_tv =
			// (TextView)convertView.findViewById(R.id.user_name_tv);
			TextView content_tv = (TextView) convertView
					.findViewById(R.id.content_tv);
			TextView date_tv = (TextView) convertView
					.findViewById(R.id.date_tv);
			ImageView video_iv = (ImageView) convertView
					.findViewById(R.id.video_iv);
			ImageView video_play_iv = (ImageView) convertView
					.findViewById(R.id.video_play_iv);
			final ImageView img_iv = (ImageView) convertView
					.findViewById(R.id.img_iv);
			ImageButton share_ibtn = (ImageButton) convertView
					.findViewById(R.id.share_ibtn);
			// 编辑控件
			RelativeLayout edit_rl = (RelativeLayout) convertView
					.findViewById(R.id.edit_rl);
			ImageButton change_content_ibtn = (ImageButton) convertView
					.findViewById(R.id.change_content_ibtn);
			ImageButton delete_video_ibtn = (ImageButton) convertView
					.findViewById(R.id.delete_video_ibtn);
			ImageButton change_img_ibtn = (ImageButton) convertView
					.findViewById(R.id.change_img_ibtn);


			// 显示内容
			name_collect_ll.setVisibility(View.GONE);
			if(isEditStatu){
				edit_rl.setVisibility(View.VISIBLE);
			}
			else {
				edit_rl.setVisibility(View.GONE);
			}
			
			LayoutParams params = (LayoutParams) show_rl.getLayoutParams();
			params.height = windowWidth/2/3*4;
			show_rl.setLayoutParams(params);
			edit_rl.setLayoutParams(params);
			
			

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String dateString = "";
			if (drawingInfo.getCreateDate() != null) {
				dateString = sdf.format(drawingInfo.getCreateDate());
			}

			date_tv.setText(dateString);
			String content = drawingInfo.getDescription();
			if (content == null || content.equals("") || content.equals("null")) {
				content = SystemValue.default_drawing_content;
			}
			content_tv.setText(content);
			final Bitmap bitmapimg = SystemValue.lessenUriImage(drawingInfo.getDrawingImg(), windowWidth/2, windowWidth/2/3*4);
			img_iv.setImageBitmap(bitmapimg);
			
			Bitmap bitmapvid = SystemValue.lessenUriImage(drawingInfo.getVideoCover(), windowWidth/2, windowWidth/2/3*4);
			video_iv.setImageBitmap(bitmapvid);
			
			final int p = position;
			
			
			share_ibtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					// 分享
					cur_drawing_to_change_index = p;
					SystemValue.curLocalDrawingInfo = drawingInfo;
//					uploadVideoToQcloud(p);
					Intent intent = new Intent(getActivity(),
							DrawingActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("video_local_path", drawingInfo.getDrawingVideo());
					bundle.putString("img_path", drawingInfo.getDrawingImg());
					bundle.putString("video_img_path", drawingInfo.getVideoCover());
					bundle.putBoolean("isUpload", false);
					intent.putExtras(bundle);
					startActivity(intent);
					SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
					SystemValue.cur_fragment_index = -1;
				}
			});
			video_play_iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
					Intent intent = new Intent(Intent.ACTION_VIEW);
			        String type = "video/mp4";
			        Uri uri = Uri.parse(drawingInfo.getDrawingVideo());
			        intent.setDataAndType(uri, type);
			        startActivity(intent);
				}
			});
			
			img_iv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {

					pic_show_rl.setVisibility(View.VISIBLE);
					Bitmap bitmap = SystemValue.lessenUriImage(drawingInfo.getDrawingImg(), windowWidth, windowWidth/3*4);
					pic_show_iv.setImageBitmap(bitmap);
				}
			});

			// 编辑
			change_img_ibtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					cur_img_to_change_iv = img_iv;
					cur_drawing_to_change_index = p;

					ContentValues values = new ContentValues();
					photoUri = getActivity().getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);					
					selectPhotoUtil.selectPhoto(photoUri);
					SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
				}
			});
			delete_video_ibtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					new AlertDialog.Builder(getActivity(),AlertDialog.THEME_HOLO_DARK)
					.setTitle("删除作品")
					.setMessage("确定删除该作品，删除后不可恢复！")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int which) {
									cur_drawing_to_change_index = p;
									deleteVideo(p);
								}
							}).setNegativeButton("取消", null).show();
					
				}
			});
			change_content_ibtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
//
					RelativeLayout ll = new RelativeLayout(getActivity());
					final EditText et = new EditText(getActivity());
					ImageView iView = new ImageView(getActivity());
					et.setBackgroundResource(R.drawable.editext_box);					
					et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});  
					
					LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					params.setMargins(5, 10, 5, 10);
					et.setLayoutParams(params);
//					iView.setImageResource(R.drawable.while_line);
					ll.addView(et);					
//					ll.addView(iView);
//					ll.setOrientation(LinearLayout.VERTICAL);

					new AlertDialog.Builder(getActivity(),AlertDialog.THEME_HOLO_DARK)
							.setTitle("请输入作品描述")
							.setView(ll)
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											String input = et.getText()
													.toString();
											if (input.equals("")) {
												Toast.makeText(getActivity(),
														"内容不能为空！",
														Toast.LENGTH_LONG)
														.show();
											} else {
												cur_drawing_to_change_index = p;
												changeContent(p, input);
											}
										}
									}).setNegativeButton("取消", null).show();

				}
			});

			return convertView;
		}

	}


	private void changeContent(final int position, final String content) {
		curDrawingInfo = drawingInfos.get(position);
		int id = Integer.parseInt(curDrawingInfo.getDrawingId());
		dbOp.updateDrawingContent(id, content);
		drawingInfos.get(position).setDescription(
										content);
		lvAdapter.notifyDataSetChanged();
							

				
	}

	private void deleteVideo(int position) {
		curDrawingInfo = drawingInfos.get(position);
		int id = Integer.parseInt(curDrawingInfo.getDrawingId());
		dbOp.deleteDrawing(id);		
		drawingInfos.remove(position);
		lvAdapter.notifyDataSetChanged();
		
		FileUtils.deleteFile(curDrawingInfo.getDrawingImg());
		FileUtils.deleteFile(curDrawingInfo.getDrawingVideo());
		FileUtils.deleteFile(curDrawingInfo.getVideoCover());
		
	}

	

	// 从本地数据库得到数据
	public void getAction() {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {

				videoUpload.getAssign();		

				List<DrawingInfo> temps = dbOp.queryDrawings();
				if (temps!=null&&temps.size()>0) {
					drawingInfos.clear();
					drawingInfos.addAll(temps);
//					no_data_rl.setVisibility(View.GONE);
				}
				else {
//					no_data_rl.setVisibility(View.VISIBLE);
				}
				
				lvAdapter.notifyDataSetChanged();
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
				picPath = ImageUtil.getRealFilePath(getActivity(), uri);
			}
			

		} else if (requestCode == SystemValue.TAKE_PICTURE) {

			String[] pojo = { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().managedQuery(photoUri, pojo, null,
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
				
				curDrawingInfo = drawingInfos.get(cur_drawing_to_change_index);
				int id = Integer.parseInt(curDrawingInfo.getDrawingId());
				
				new_picPath = ImageUtil.compressImage(getActivity(), picPath,false);
				old_picPath = curDrawingInfo.getDrawingImg();
//				dbOp.updateDrawing(id, new_picPath, null);
				dbOp.updateDrawingImgPath(id, new_picPath);
				curDrawingInfo.setDrawingImg(new_picPath);
				FileUtils.deleteFile(old_picPath);
				lvAdapter.notifyDataSetChanged();
				
			} else {
				
			}
		}
	}
	
	private void uploadVideoToQcloud(int position){
		if (SystemValue.curUserInfo == null
				|| SystemValue.curUserInfo.getUserId() == null
				|| SystemValue.curUserInfo.getUserId().equals("")
				|| SystemValue.curUserInfo.getUserId().equals("null")) {
			// 直接跳转到登录页面
			Intent intent = new Intent(getActivity(),LoginActivity.class);
			intent.putExtra("isFromRegister", false);
			startActivity(intent);
			return;
		}
		DrawingInfo drawingInfo = drawingInfos.get(position);
		String miboo_video_path = drawingInfo.getDrawingVideo();
		String destFilePath = SystemValue.dest_head+System.currentTimeMillis()+".mp4";
		Toast.makeText(getActivity(), "uploadVideoToQcloud", Toast.LENGTH_SHORT).show();
		videoUpload.uploadToQcloud(miboo_video_path, destFilePath,mHandler);
	}

	// 上传作品
	private void uploadDrawing(final String qcloudPath) {

		if (SystemValue.curUserInfo == null
				|| SystemValue.curUserInfo.getUserId() == null
				|| SystemValue.curUserInfo.getUserId().equals("")
				|| SystemValue.curUserInfo.getUserId().equals("null")) {
			// 直接跳转到登录页面
			Intent intent = new Intent(getActivity(),LoginActivity.class);
			intent.putExtra("isFromRegister", false);
			startActivity(intent);
			return;
		}

		RequestParams params = new RequestParams();

		// params.addBodyParameter("auth_token", RsSharedUtil.getString(
		// getApplicationContext(), AppConfig.ACCESS_TOKEN));
		params.addQueryStringParameter("action", "create_drawing");
		params.addQueryStringParameter("user_id",
				SystemValue.curUserInfo.getUserId());
		params.addQueryStringParameter("drawing_video",qcloudPath);
		
		final String new_pic_path = ImageUtil.compressImage(getActivity(), drawingInfos.get(cur_drawing_to_change_index).getDrawingImg(),false);

		params.addBodyParameter("image", new File(new_pic_path));
		params.addBodyParameter("cover", new File(drawingInfos.get(cur_drawing_to_change_index).getVideoCover()));

		HttpUtils http = new HttpUtils();
		http.send(HttpMethod.POST, SystemValue.basic_url + "drawing.do",
				params, new RequestCallBack<String>() {

					@Override
					public void onStart() {
						Log.d("PostPicture", "准备上传文件...");
						Toast.makeText(getActivity(), "准备上传文件1...", Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						if (isUploading) {
							Log.d("PostPicture", "正在上传文件...");
							Toast.makeText(getActivity(), "正在上传文件...", Toast.LENGTH_SHORT)
									.show();
						} else {
							Log.d("PostPicture", "准备上传文件...");
							Toast.makeText(getActivity(), "准备上传文件...", Toast.LENGTH_SHORT)
									.show();
						}
					}

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						Log.d("上传成功", responseInfo.result);
						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(
									responseInfo.result);

							// ArrayList<DrawingInfo> datas = new
							// ArrayList<DrawingInfo>();
							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {
//								cur_img_to_change_iv.setImageBitmap(SystemValue.lessenUriImage(pic_path, (windowWidth*3/4), 150));
								deleteVideo(cur_drawing_to_change_index);
								
							} else {
								Toast.makeText(
										getActivity(),
										getActivity().getResources().getString(
												R.string.upload_error), Toast.LENGTH_SHORT)
										.show();

							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("dj_JSONException_action", e.toString());
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						Log.d("fail", "哈哈哈");
						Log.d("error", error.toString());
						Toast.makeText(
								getActivity(),
								getActivity().getResources().getString(
										R.string.upload_error), Toast.LENGTH_SHORT).show();

					}
				});

	}
}
