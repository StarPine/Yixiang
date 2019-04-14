package com.example.mydrawing2;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import util.StringConverter;
import util.SystemValue;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mydrawing.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;
;

import entity.MerchantInfo;
import entity.MerchantVideo;
import entity.MyApplication;

@ContentView(R.layout.shop_video_layout)
public class VideoShop extends Activity {

	@ViewInject(R.id.merchant_image_iv)
	ImageView merchant_image_iv;

	@ViewInject(R.id.merchant_name_tv)
	TextView merchant_name_tv;

	@ViewInject(R.id.more)
	ImageButton more;

	@ViewInject(R.id.purchase)
	ImageButton purchase;

	@ViewInject(R.id.gview)
	GridView gview;

	ListView listView;
	int screenWidth, screenHeight;

	List<MerchantInfo> merchantInfos = new ArrayList<MerchantInfo>();
	List<MerchantVideo> curMerchantVideos = new ArrayList<MerchantVideo>();
	Gson gson;

	final int get_merchantinfo_success = 101;
	final int get_video_success = 102;
	final int get_merchantinfo_failed = 103;
	final int get_video_failed = 104;

	RequestQueue volleyRequestQueue;

	ProgressDialog progressDialog;
	int cur_position = 0;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			progressDialog.dismiss();
			switch (msg.what) {
			case get_merchantinfo_success:
				if (merchantInfos != null && merchantInfos.size() > 0) {
					initMerchant();

				} else {
					new AlertDialog.Builder(VideoShop.this,
							AlertDialog.THEME_HOLO_DARK).setTitle("信息")
							.setMessage("暂时没有视频数据，请等待服务更新！")
							.setPositiveButton("确定", null).create().show();
				}

				break;
			case get_video_success:
				if (curMerchantVideos == null || curMerchantVideos.size() == 0) {
					new AlertDialog.Builder(VideoShop.this,
							AlertDialog.THEME_HOLO_DARK).setTitle("信息")
							.setMessage("该商家还没有上传视频哟！")
							.setPositiveButton("确定", null).create().show();
				} else {
					initVideo();
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		ViewUtils.inject(this);
		volleyRequestQueue = Volley.newRequestQueue(this);
		
		DisplayMetrics dm = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(dm);
		screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
		screenHeight = dm.heightPixels; // 屏幕高（像素，如：800p）
		
		gson = new GsonBuilder()
		.setDateFormat(
				"yyyy-MM-dd HH:mm:ss.SS")
		.create();
		
		getMerchant();
	}

	void initMerchant() {

		MerchantInfo merchantInfo = merchantInfos.get(0);
		getVideos(merchantInfo.getMerchantId());
		merchant_name_tv.setText(merchantInfo.getName());
		ImageLoader.getInstance().displayImage(
				SystemValue.basic_url + merchantInfo.getImage(),
				merchant_image_iv);

		View view = LayoutInflater.from(VideoShop.this).inflate(
				R.layout.merchant_menu_layout, null);

		listView = (ListView) view.findViewById(R.id.listview);
		listView.setAdapter(merchantAdapter);
		
		final PopupWindow popupWindow = new PopupWindow(view, 600,LayoutParams.WRAP_CONTENT, true);  
		final int[] location = new int[2];
		more.getLocationOnScreen(location);  
//		popupWindow.setWidth(LayoutParams.WRAP_CONTENT);                  
//		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);    
		 popupWindow.setOutsideTouchable(true); 
         popupWindow.setBackgroundDrawable(new BitmapDrawable());
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				cur_position = position;
				MerchantInfo merchantInfo = merchantInfos.get(position);
				getVideos(merchantInfo.getMerchantId());
				merchant_name_tv.setText(merchantInfo.getName());
				ImageLoader.getInstance().displayImage(
						SystemValue.basic_url + merchantInfo.getImage(),
						merchant_image_iv);
				popupWindow.dismiss();
			}
		});

		
        
        

		more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean isShowing = popupWindow.isShowing();
				if (isShowing) {
					popupWindow.dismiss();
				} else {
//					popupWindow.showAtLocation(more, Gravity.NO_GRAVITY, location[0]-popupWindow.getWidth(), location[1]-popupWindow.getHeight());
//					popupWindow.showAtLocation( more, Gravity.NO_GRAVITY, 0, 0);  
					popupWindow.showAsDropDown(more);
				}
			}
		});

		gview.setAdapter(videoAdapter);
		gview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				MerchantVideo merchantVideo = curMerchantVideos.get(position);
				Intent intent = new Intent(VideoShop.this,
						VideoAndRecording.class);
				Bundle bundle = new Bundle();
				bundle.putString("preVideo", "finished");
				bundle.putString("videoUrl", merchantVideo.getPath());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	void initVideo() {
		MerchantInfo merchantInfo = merchantInfos.get(cur_position);

		final String link = merchantInfo.getLink().trim();
		
		purchase.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (link != null && !link.equals("")) {
					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");					
					Uri content_url = Uri.parse(link);
					intent.setData(content_url);
					intent.addCategory("android.intent.category.BROWSABLE");
					startActivity(intent);
				}

			}
		});
		

	}

	void getMerchant() {

		mHandler.post(new Runnable() {

			@Override
			public void run() {

				progressDialog = ProgressDialog.show(VideoShop.this, "", "");

				String url = SystemValue.basic_url
						+ "drawing.do?action=getAllActivatedMerchants";
				Log.d("getAll", "ActivatedMerchants"+url);
				StringRequest stringRequest = new StringRequest(Method.GET,
						url, new Response.Listener<String>() {

							@Override
							public void onResponse(String response) {

								Log.d("getAlls", "ActivatedMerchant"+response);

								try {
									// 获取response中的data
									JSONObject jsonObject = new JSONObject(
											response);

									String result = jsonObject
											.getString("result");
									if (result != null
											&& result.equals("success")) {
										String merchant_json = jsonObject
												.getString("merchantInfos");
										
										merchantInfos = gson
												.fromJson(
														merchant_json,
														new TypeToken<List<MerchantInfo>>() {
														}.getType());
										mHandler.sendEmptyMessage(get_merchantinfo_success);
									} else if(result==null||result.equals("failed")){
										mHandler.sendEmptyMessage(get_merchantinfo_failed);
									}
									else {
										mHandler.sendEmptyMessage(get_merchantinfo_success);
									}

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									Log.d("dj_JSONException_action",
											e.toString());
									e.printStackTrace();
								}
							}

						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {

								mHandler.sendEmptyMessage(get_merchantinfo_failed);
								Toast.makeText(
										VideoShop.this,
										getResources().getString(
												R.string.download_error), Toast.LENGTH_SHORT)
										.show();

							}
						});
				stringRequest.setRetryPolicy(new DefaultRetryPolicy(30 * 1000,
						0, 0f));
				volleyRequestQueue.add(stringRequest);
			}
		});

	}

	void getVideos(final int merchantId) {

		mHandler.post(new Runnable() {

			@Override
			public void run() {

				progressDialog = ProgressDialog.show(VideoShop.this, "", "");
				String url = SystemValue.basic_url
						+ "drawing.do?action=getAllMerchantVideosByMerchantID&merchantid="
						+ merchantId;
				Log.d("getAllMerchant", "VideosByMerchantID"+url);
				StringRequest stringRequest = new StringRequest(Method.GET,
						url, new Response.Listener<String>() {

							@Override
							public void onResponse(String response) {

								Log.d("getAllMerchant",
										"VideosByMerchantID"+response);

								try {
									// 获取response中的data
									JSONObject jsonObject = new JSONObject(
											response);

									curMerchantVideos.clear();
									String result = jsonObject
											.getString("result");
									if (result != null
											&& result.equals("success")) {
										String video_json = jsonObject
												.getString("merchantVideos");
										
										try {
											curMerchantVideos = gson
													.fromJson(
															video_json,
															new TypeToken<List<MerchantVideo>>() {
															}.getType());
										} catch (Exception e) {
											Log.e("gsonerror", e.getMessage());
										}

										mHandler.sendEmptyMessage(get_video_success);
									} else if(result==null||result.equals("failed")){
										mHandler.sendEmptyMessage(get_video_failed);
									}
									else {
										mHandler.sendEmptyMessage(get_video_success);
									}

									videoAdapter.notifyDataSetChanged();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									Log.d("dj_JSONException_action",
											e.toString());
									e.printStackTrace();
								}
							}

						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {

								mHandler.sendEmptyMessage(get_video_failed);
								Toast.makeText(
										VideoShop.this,
										getResources().getString(
												R.string.download_error), Toast.LENGTH_SHORT)
										.show();

							}
						});
				stringRequest.setRetryPolicy(new DefaultRetryPolicy(30 * 1000,
						0, 0f));
				volleyRequestQueue.add(stringRequest);
			}
		});

	}

	BaseAdapter merchantAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {


			MerchantViewHolder mHolder;
			if (convertView == null) {
				convertView = LayoutInflater.from(VideoShop.this).inflate(
						R.layout.merchant_menu_item, null);
				mHolder = new MerchantViewHolder();
				mHolder.headView = (ImageView) convertView
						.findViewById(R.id.head_iv);
				mHolder.nameView = (TextView) convertView
						.findViewById(R.id.name_tv);
				convertView.setTag(mHolder);
			} else {
				mHolder = (MerchantViewHolder) convertView.getTag();
			}

			MerchantInfo merchantInfo = merchantInfos.get(position);
			ImageLoader.getInstance().displayImage(
					SystemValue.basic_url + merchantInfo.getImage(),
					mHolder.headView);
			mHolder.nameView.setText(merchantInfo.getName());

			return convertView;
		}

		@Override
		public long getItemId(int position) {

			return 0;
		}

		@Override
		public Object getItem(int position) {

			return null;
		}

		@Override
		public int getCount() {

			if (merchantInfos == null) {
				return 0;
			}
			return merchantInfos.size();
		}
	};

	BaseAdapter videoAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			MerchantViewHolder mHolder;
			if (convertView == null) {
				convertView = new ImageView(VideoShop.this);
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(
						(int) (screenWidth / 3.2), (int) (screenWidth / 3.2));
				convertView.setLayoutParams(params);
				mHolder = new MerchantViewHolder();
				mHolder.headView = (ImageView) convertView;
				convertView.setTag(mHolder);
			} else {
				mHolder = (MerchantViewHolder) convertView.getTag();
			}

			MerchantVideo merchantVideo = curMerchantVideos.get(position);
			ImageLoader.getInstance().displayImage(
					SystemValue.basic_url + merchantVideo.getCover(),
					mHolder.headView);

			return convertView;
		}

		@Override
		public long getItemId(int position) {

			return 0;
		}

		@Override
		public Object getItem(int position) {

			return null;
		}

		@Override
		public int getCount() {

			if (curMerchantVideos == null) {
				return 0;
			}
			return curMerchantVideos.size();
		}
	};

	class MerchantViewHolder {
		ImageView headView;
		TextView nameView;
	}
}
