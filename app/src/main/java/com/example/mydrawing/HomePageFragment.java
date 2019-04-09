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
import util.RefreshListView;
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
import android.app.ProgressDialog;
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
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.android.volley.DefaultRetryPolicy;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
;

import entity.DrawingDBOp;
import entity.DrawingInfo;

public class HomePageFragment extends Fragment {

	// 活动列表
//	@ViewInject(R.id.me_drawing_lv)
//	private ListView me_drawing_lv;
	@ViewInject(R.id.me_drawing_lv)
	private RefreshListView me_drawing_lv;
	
	@ViewInject(R.id.no_data_rl1)
	RelativeLayout no_data_rl1;

	// 数据
	private List<DrawingInfo> drawingInfos = new ArrayList<DrawingInfo>();

	// 适配器
	private ListViewAdapter lvAdapter;

	private RequestQueue volleyRequestQueue;
	private String url = null;
	// pageSize,固定为5条话题
	private static int PAGE_SIZE = 10;
	// pageIndex,从0递增
	private int all_index = 1;

	// 上拉刷新，增加数据
	private int FOOT = 1;
	// 下拉刷新，替换数据
	private int HEAD = 0;

	// 上下拉刷新
//	@ViewInject(R.id.refresh1)
//	private PullToRefreshView refresh;
//	RefreshReceiver refreshReceiver;
	


	int windowWidth = 0, windowHeight = 0;

//	@ViewInject(R.id.pic_show_rl)
	RelativeLayout pic_show_rl;

//	@ViewInject(R.id.pic_show_iv)
	ImageView pic_show_iv;
//	@ViewInject(R.id.pic_show_back_ibtn)
//	ImageButton pic_show_back_ibtn;
	
	final int loadDataFinished = 201;
	ProgressDialog progressDialog;


	Handler mHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case loadDataFinished:
				progressDialog.dismiss();
				break;

			default:
				break;
			}
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.me_layout, container, false);

		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Rect frame = new Rect();
		getActivity().getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(frame);

		windowWidth = display.getWidth();
		windowHeight = display.getHeight();

		ViewUtils.inject(this, view);
		volleyRequestQueue = Volley.newRequestQueue(getActivity());
//		refresh.setOnHeaderRefreshListener(this);
//		refresh.setOnFooterRefreshListener(this);
		progressDialog = new ProgressDialog(getActivity());

		initView();
		setOnclic();

		return view;
	}

	public void initView() {
		

		lvAdapter = new ListViewAdapter(getActivity(), drawingInfos);
		me_drawing_lv.setAdapter(lvAdapter);
		getAction(1, PAGE_SIZE, HEAD);
		
		MainActivity mainActivity = (MainActivity)getActivity();
		pic_show_rl = mainActivity.pic_show_rl;
		pic_show_iv = mainActivity.pic_show_iv;

		me_drawing_lv.setOnRefreashListener(new RefreshListView.OnRefreashListener() {
			
			@Override
			public void onRefreash() {
				// TODO Auto-generated method stub
				getAction(1, PAGE_SIZE, HEAD);
			}
			
			@Override
			public void onLoadMore() {
				// TODO Auto-generated method stub
				getAction(all_index+1, PAGE_SIZE, FOOT);
				
			}
		});
	}

	private void setOnclic() {
		
//		pic_show_iv.setOnSingleTapListener(new MatrixImageView.OnSingleTapListener() {
//			
//			@Override
//			public void onSingleTap() {
//				// TODO Auto-generated method stub
//				pic_show_rl.setVisibility(View.GONE);
//			}
//		});
		pic_show_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pic_show_rl.setVisibility(View.GONE);
			}
		});

		
	}


	
	@Override
	public void onHiddenChanged(boolean hidden){
		super.onHiddenChanged(hidden);
		if(!hidden&&SystemValue.per_fragment_index!=SystemValue.cur_fragment_index){
			all_index = 1;
//			mHandler.post(new Runnable() {
//				
//				@Override
//				public void run() {
					// TODO Auto-generated method stub
					getAction(all_index, PAGE_SIZE, HEAD);
//				}
//			});
			
		}
	}
	

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
			// TODO Auto-generated method stub
			if(list.size()==0){
				no_data_rl1.setVisibility(View.VISIBLE);
			}
			else {
				no_data_rl1.setVisibility(View.GONE);
			}
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub

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
			

			// 显示内容
			name_collect_ll.setVisibility(View.GONE);
			

			LayoutParams params = (LayoutParams) show_rl.getLayoutParams();
			params.height = windowWidth/2/3*4;
			show_rl.setLayoutParams(params);
			
			

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
			ImageLoader
					.getInstance()
					.displayImage(
							SystemValue.basic_url + drawingInfo.getDrawingImg(),
							img_iv);
			ImageLoader.getInstance().displayImage(
					SystemValue.basic_url + drawingInfo.getVideoCover(),
					video_iv);

			final int p = position;

			share_ibtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					SystemValue.curLocalDrawingInfo = drawingInfo;
					Intent intent = new Intent(getActivity(),
							DrawingActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("video_local_path", drawingInfo.getDrawingVideo());
					bundle.putString("img_path", drawingInfo.getDrawingImg());
					bundle.putString("video_img_path", drawingInfo.getVideoCover());
					bundle.putBoolean("isUpload", true);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			video_play_iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
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
					// TODO Auto-generated method stub
					ImageLoader.getInstance()
							.displayImage(
									SystemValue.basic_url
											+ drawingInfo.getDrawingImg(),
									pic_show_iv);
					pic_show_rl.setVisibility(View.VISIBLE);
				}
			});

			

			return convertView;
		}

	}

	
	// 从本地数据库得到数据
	private void getAction(final int pageIndex, final int pagerSize, final int type) {
		progressDialog.show();
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				String url = SystemValue.basic_url
						+ "drawing.do?action=getAllDrawings";
				url += "&page=" + pageIndex + "&count=" + pagerSize;
				Log.d("getAllDrawings", url);
				StringRequest stringRequest = new StringRequest(Method.GET, url,
						new Response.Listener<String>() {

							@Override
							public void onResponse(String response) {
								// TODO Auto-generated method stub
								Log.d("getAllDrawings_response", response);

								try {
									// 获取response中的data
									JSONObject jsonObject = new JSONObject(response);

									ArrayList<DrawingInfo> datas = new ArrayList<DrawingInfo>();
									String result = jsonObject.getString("result");
									if (result != null && result.equals("success")) {
										String drawing_json = jsonObject
												.getString("drawinginfos");
										Gson gson = new GsonBuilder().setDateFormat(
												"yyyy-MM-dd HH:mm:ss.SS").create();
										datas = gson.fromJson(drawing_json,
												new TypeToken<List<DrawingInfo>>() {
												}.getType());
										// 替换或者增加数据
										if (type == FOOT){
											if(datas!=null&&datas.size()>0){
												drawingInfos.addAll(datas);
												all_index++;
											}
										}									
										else {
											drawingInfos.clear();
											drawingInfos.addAll(datas);
										}
										lvAdapter.notifyDataSetChanged();
									} else {
										
									}
									me_drawing_lv.onRefreadshComplete();
									if(type == FOOT){
										me_drawing_lv.noFooterView();
									}

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									Log.d("dj_JSONException_action", e.toString());
									e.printStackTrace();
								}
								finally{
									Message msMessage = new Message();
									msMessage.what = loadDataFinished;
									mHandler.sendMessage(msMessage);
								}
							}

						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								// TODO Auto-generated method stub

								me_drawing_lv.onRefreadshComplete();
								if(type == FOOT){
									me_drawing_lv.noFooterView();
								}
								Message msMessage = new Message();
								msMessage.what = loadDataFinished;
								mHandler.sendMessage(msMessage);
							}
						});
				stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
				volleyRequestQueue.add(stringRequest);
			}
		});
		
		
	}

	
//	@Override
//	public void toRefresh() {
//		// TODO Auto-generated method stub
//		all_index = 1;
//		refresh.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				getAction(all_index, PAGE_SIZE, HEAD);
//				refresh.onHeaderRefreshComplete();
//			}
//		}, 2000);
//	}

//	@Override
//	public void onFooterRefresh(PullToRefreshView view) {
//		// TODO Auto-generated method stub
//		refresh.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				getAction(++all_index, PAGE_SIZE, FOOT);
//				refresh.onFooterRefreshComplete();
//			}
//		}, 2000);
//	}

//	@Override
//	public void onHeaderRefresh(PullToRefreshView view) {
//		// TODO Auto-generated method stub
//		all_index = 1;
//		refresh.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				getAction(all_index, PAGE_SIZE, HEAD);
//				refresh.onHeaderRefreshComplete();
//			}
//		}, 2000);
//	}

}
