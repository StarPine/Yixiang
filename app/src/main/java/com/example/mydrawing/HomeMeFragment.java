package com.example.mydrawing;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
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
import com.nostra13.universalimageloader.core.ImageLoader;
;

import entity.DrawingDBOp;
import entity.DrawingInfo;

public class HomeMeFragment extends Fragment{// implements
		//OnHeaderRefreshListener, OnFooterRefreshListener, Refresh {

	// 活动列表
//	@ViewInject(R.id.me_drawing_lv)
//	private ListView me_drawing_lv;
	@ViewInject(R.id.me_drawing_lv)
	private RefreshListView me_drawing_lv;
	
	@ViewInject(R.id.no_data_rl)
	RelativeLayout no_data_rl;

	// 数据
	private List<DrawingInfo> drawingInfos = new ArrayList<DrawingInfo>();

	// 适配器
	public static ListViewAdapter lvAdapter;

	private RequestQueue volleyRequestQueue;
	private String url = null;
	// pageSize,固定为5条话题
	public static int PAGE_SIZE = 10;
	// pageIndex,从0递增
	private int all_index = 1;

	// 上拉刷新，增加数据
	private int FOOT = 1;
	// 下拉刷新，替换数据
	public static int HEAD = 0;

	// 上下拉刷新
//	@ViewInject(R.id.refresh1)
//	private PullToRefreshView refresh;
//	RefreshReceiver refreshReceiver;

	public static boolean isEditStatu = false;
	private Uri photoUri;
	private ImageView cur_img_to_change_iv;
	private int cur_drawing_to_change_index;
	String new_picPath, old_picPath;
	DrawingInfo curDrawingInfo;

	int windowWidth = 0, windowHeight = 0;
	SelectPhotoUtil selectPhotoUtil = null;

//	@ViewInject(R.id.pic_show_rl)
	RelativeLayout pic_show_rl;

//	@ViewInject(R.id.pic_show_iv)
	ImageView pic_show_iv;
//	@ViewInject(R.id.pic_show_back_ibtn)
//	ImageButton pic_show_back_ibtn;

	private final int upload_success = 101;
	private final int upload_failed = 102;
	private final int drawing_upload_success = 103;
	ProgressDialog progressDialog;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case upload_success:
				progressDialog.dismiss();
				break;
			case upload_failed:
				progressDialog.dismiss();
				break;
			}
		}
	};

//	@Override
//	public void onStart() {
//		// TODO Auto-generated method stub
//		super.onStart();
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction("MeDrawingService");
//		refreshReceiver = new RefreshReceiver();
//		refreshReceiver.setRefresh(this);
//		getActivity().registerReceiver(refreshReceiver, intentFilter);
//	}
//
//	@Override
//	public void onStop() {
//		// TODO Auto-generated method stub
//		getActivity().unregisterReceiver(refreshReceiver);
//		super.onStop();
//	}

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
		selectPhotoUtil = new SelectPhotoUtil(getActivity());

		initView();
		setOnclic();
		return view;
	}

	public void initView() {
		progressDialog = new ProgressDialog(getActivity());
		lvAdapter = new ListViewAdapter(getActivity(), drawingInfos);
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				me_drawing_lv.setAdapter(lvAdapter);
				getAction(1, PAGE_SIZE, HEAD);
			}
		}, Toast.LENGTH_SHORT);

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
				getAction(1+all_index, PAGE_SIZE, FOOT);
				
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
		Toast.makeText(getActivity(), "mehidden:"+hidden, Toast.LENGTH_SHORT).show();
		super.onHiddenChanged(hidden);
		if(!hidden&&SystemValue.per_fragment_index!=SystemValue.cur_fragment_index){
			all_index = 1;
			getAction(all_index, PAGE_SIZE, HEAD);
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
				no_data_rl.setVisibility(View.VISIBLE);
			}
			else {
				no_data_rl.setVisibility(View.GONE);
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
			if (isEditStatu) {
				edit_rl.setVisibility(View.VISIBLE);
			} else {
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
					// 分享
					cur_drawing_to_change_index = p;
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

			// 编辑
			change_img_ibtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					cur_img_to_change_iv = img_iv;
					cur_drawing_to_change_index = p;

					ContentValues values = new ContentValues();
					photoUri = getActivity().getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							values);
					selectPhotoUtil.selectPhoto(photoUri);
					SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
				}
			});
			delete_video_ibtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(getActivity(),
							AlertDialog.THEME_HOLO_DARK)
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
					// TODO Auto-generated method stub
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

					new AlertDialog.Builder(getActivity(),
							AlertDialog.THEME_HOLO_DARK)
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
		if (SystemValue.curUserInfo == null
				|| SystemValue.curUserInfo.getUserId() == null
				|| SystemValue.curUserInfo.getUserId().equals("")
				|| SystemValue.curUserInfo.getUserId().equals("null")) {
			// 直接跳转到登录页面

			Intent intent = new Intent(getActivity(), LoginActivity.class);
			intent.putExtra("isFromRegister", false);
			startActivity(intent);
			return;
		}

		String url = SystemValue.basic_url
				+ "drawing.do?action=update_drawing_descri";
		url += "&drawing_id=" + drawingInfos.get(position).getDrawingId();
		try {
			url += "&drawing_description=" + URLEncoder.encode(content,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		url += "&user_id=" + SystemValue.curUserInfo.getUserId();
		Log.d("changeContent", url);
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						// TODO Auto-generated method stub
						Log.d("changeContent_response", response);

						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(response);

							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {

								drawingInfos.get(position).setDescription(
										content);
								Toast.makeText(
										getActivity(),
										getActivity()
												.getResources()
												.getString(
														R.string.content_change_success),
										Toast.LENGTH_SHORT).show();
								//
								lvAdapter.notifyDataSetChanged();
							} else {
								Toast.makeText(
										getActivity(),
										getActivity().getResources().getString(
												R.string.content_change_failed),
										Toast.LENGTH_SHORT).show();
								//
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("delete_drawings",
									"_response_JSONException"+e.toString());
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						Toast.makeText(
								getActivity(),
								getActivity().getResources().getString(
										R.string.content_change_failed), Toast.LENGTH_SHORT)
								.show();
						// try {
						// // JSONObject jsonObject = new
						// JSONObject(error.data());
						// // Log.d("dj_VolleyError_action",
						// // jsonObject.toString());
						// } catch (Exception e1) {
						// // TODO Auto-generated catch block
						// e1.printStackTrace();
						// }
					}
				});
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
		volleyRequestQueue.add(stringRequest);

	}

	private void deleteVideo(final int position) {
		if (SystemValue.curUserInfo == null
				|| SystemValue.curUserInfo.getUserId() == null
				|| SystemValue.curUserInfo.getUserId().equals("")
				|| SystemValue.curUserInfo.getUserId().equals("null")) {
			// 直接跳转到登录页面

			Intent intent = new Intent(getActivity(), LoginActivity.class);
			intent.putExtra("isFromRegister", false);
			startActivity(intent);
			return;
		}

		String url = SystemValue.basic_url + "drawing.do?action=delete_drawing";
		url += "&drawing_id=" + drawingInfos.get(position).getDrawingId();
		url += "&user_id=" + SystemValue.curUserInfo.getUserId();
		Log.d("delete_drawing", url);
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						// TODO Auto-generated method stub
						Log.d("delete", "_drawings_response"+response);

						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(response);

							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {

								drawingInfos.remove(position);
								Toast.makeText(
										getActivity(),
										getActivity()
												.getResources()
												.getString(
														R.string.drawing_delete_success),
										Toast.LENGTH_SHORT).show();
								//
								lvAdapter.notifyDataSetChanged();
							} else {
								Toast.makeText(
										getActivity(),
										getActivity().getResources().getString(
												R.string.drawing_delete_failed),
										Toast.LENGTH_SHORT).show();
								//
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("delete_drawings",
									"_response_JSONException"+e.toString());
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						Toast.makeText(
								getActivity(),
								getActivity().getResources().getString(
										R.string.drawing_delete_failed), Toast.LENGTH_SHORT)
								.show();

					}
				});
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
		volleyRequestQueue.add(stringRequest);

	}

	// 从本地数据库得到数据
	public void getAction(final int pageIndex, final int pagerSize, final int type) {
		if(SystemValue.curUserInfo==null||SystemValue.curUserInfo.getUserId()==null){
			return;
		}
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				String url = SystemValue.basic_url
						+ "drawing.do?action=getPersonDrawings";
				url += "&page=" + pageIndex + "&count=" + pagerSize;
				url += "&user_id=" + SystemValue.curUserInfo.getUserId();
				Log.d("get_me_drawings", url);
				StringRequest stringRequest = new StringRequest(Method.GET, url,
						new Response.Listener<String>() {

							@Override
							public void onResponse(String response) {
								// TODO Auto-generated method stub
								Log.d("get_me", "_drawings_response"+response);

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
							}

						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								// TODO Auto-generated method stub
								Toast.makeText(
										getActivity(),
										getActivity().getResources().getString(
												R.string.download_error), Toast.LENGTH_SHORT).show();
								// try {
								// // JSONObject jsonObject = new
								// JSONObject(error.data());
								// // Log.d("dj_VolleyError_action",
								// // jsonObject.toString());
								// } catch (Exception e1) {
								// // TODO Auto-generated catch block
								// e1.printStackTrace();
								// }
							}
						});
				stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
				volleyRequestQueue.add(stringRequest);
			}
		});

		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String picPath = null;
		if (requestCode == SystemValue.GET_PICTURE) {
			if (data != null) {
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

				new AlertDialog.Builder(getActivity(),
						AlertDialog.THEME_HOLO_DARK)
						.setTitle("信息")
						.setMessage("确定修改作品图片吗？")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										new_picPath = ImageUtil.compressImage(
												getActivity(),
												file.getAbsolutePath(), false);
										uploadPic(new_picPath);
									}
								}).create().show();

			} else {

			}
		}
	}

	// 上传图片
	private void uploadPic(final String pic_path) {

		if (SystemValue.curUserInfo == null
				|| SystemValue.curUserInfo.getUserId() == null
				|| SystemValue.curUserInfo.getUserId().equals("")
				|| SystemValue.curUserInfo.getUserId().equals("null")) {
			// 直接跳转到登录页面
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			intent.putExtra("isFromRegister", false);
			startActivity(intent);
			return;
		}

		progressDialog.show();
		RequestParams params = new RequestParams();

		// params.addBodyParameter("auth_token", RsSharedUtil.getString(
		// getApplicationContext(), AppConfig.ACCESS_TOKEN));
		params.addQueryStringParameter("action", "update_drawing_pic");
		params.addQueryStringParameter("drawing_id",
				drawingInfos.get(cur_drawing_to_change_index).getDrawingId());
		params.addQueryStringParameter("user_id",
				SystemValue.curUserInfo.getUserId());

		final String new_pic_path = ImageUtil.compressImage(getActivity(),
				pic_path, false);

		params.addBodyParameter("image", new File(new_pic_path));

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
								// cur_img_to_change_iv.setImageBitmap(SystemValue.lessenUriImage(pic_path,
								// (windowWidth*3/4), 150));
								String drawing_json = jsonObject
										.getString("drawinginfo");
								Gson gson = new GsonBuilder().setDateFormat(
										"yyyy-MM-dd HH:mm:ss.SS").create();
								DrawingInfo data = gson.fromJson(drawing_json,
										DrawingInfo.class);
								drawingInfos.get(cur_drawing_to_change_index)
										.setDrawingImg(data.getDrawingImg());
								FileUtils.deleteFile(new_pic_path);
								lvAdapter.notifyDataSetChanged();
								Message msg = new Message();
						        msg.what = upload_success;
						        mHandler.sendMessage(msg);

							} else {
								Message msg = new Message();
						        msg.what = upload_failed;
						        mHandler.sendMessage(msg);

							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Message msg = new Message();
					        msg.what = upload_failed;
					        mHandler.sendMessage(msg);
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
//
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
//
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
