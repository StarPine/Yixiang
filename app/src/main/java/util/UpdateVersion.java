package util;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import util.FileUtils.NoSdcardException;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mydrawing.LoginActivity;
import com.example.mydrawing.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import entity.Advertisement;
import entity.UserInfo;
import entity.VersionRecord;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class UpdateVersion {
	
	Context context;
	private RequestQueue volleyRequestQueue;
	final int has_version_to_update = 401;
	final int download_finished = 402;
	final int get_advertise =404;
	VersionRecord lastetVersionRecord;
	Advertisement advertisement;
	String sdPath;
	
	
	public UpdateVersion(Context context){
    	this.context = context;
		volleyRequestQueue = Volley.newRequestQueue(context);
		
	}

	// 获取当前版本的版本号  
    public int getLocalVersion() {   
        try {  
            PackageManager packageManager = context.getPackageManager();  
            PackageInfo packageInfo = packageManager.getPackageInfo(  
            		context.getPackageName(), 0);  
            return packageInfo.versionCode;  
        } catch (NameNotFoundException e) {  
            e.printStackTrace();  
            return -1;  
        }  
    }  
    
    public void getLatestVersion(final Handler mHandler){
    	String url = SystemValue.basic_url
				+ "userInfo.do?action=get_latest_version";
		url += "&app_type=" + "and";
		Log.d("getLatestVersion", url);
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						// TODO Auto-generated method stub
						Log.d("getLatestVersion_response", response);

						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(response);

							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {

								Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-DD hh:mm:ss.SS").create();
								lastetVersionRecord = gson.fromJson(jsonObject.getString("versionRecord"), VersionRecord.class);
								
								if(lastetVersionRecord!=null){
									int cur_version = getLocalVersion();
									int latest_version = lastetVersionRecord.getVersionNum();
									if(latest_version > cur_version){
										Message message = new Message();
										message.what = has_version_to_update;
										mHandler.sendMessage(message);
									}
								}
								
								
								
							} else {
								
								//
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("delete_drawings_response_JSONException",
									e.toString());
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						
						
					}
				});
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
		volleyRequestQueue.add(stringRequest);
    }
    
    public void downloadLastetVersion(final ProgressBar progressBar, final Handler mHandler){
    	if(lastetVersionRecord!=null){
    		String url = SystemValue.basic_url+lastetVersionRecord.getVersionPath();
    		sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+SystemValue.localAppPath+File.separator+"MyDrawing.apk";
    		try {
				new FileUtils().creatSDDir("MyDrawings");
				new FileUtils().creatSDDir(SystemValue.localAppPath);
				
				File file = new File(sdPath);
				if(file.exists()){
					file.delete();
				}
				
			} catch (NoSdcardException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		HttpUtils http = new HttpUtils();  
            http.download(url, sdPath, true, false,  
                    new RequestCallBack<File>() {   
                        @Override  
                        public void onStart() {  
//                            tv.setText("正在连接");  
                        	System.out.println("正在连接");
  
                        }  
  
                        @Override  
                        public void onLoading(long total, long current,  
                                boolean isUploading) {  
                            super.onLoading(total, current, isUploading);  
//                            btn_down.setText("正在下载");  
                        	System.out.println("正在下载");
                            progressBar.setProgress((int) ((double) current  
                                    / (double) total * 100));  
                        }  
  
                        @Override  
                        public void onSuccess(ResponseInfo<File> responseInfo) {  
//                            tv.setText(responseInfo.result.getPath());  
                        	new Thread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Message message = new Message();
									message.what = download_finished;
									mHandler.sendMessage(message);
									update();
								}
							}).start();
                        }  
  
                        @Override  
                        public void onFailure(HttpException error, String msg) {  
//                            tv.setText(msg);  
                        	System.out.println("error"); 
                        }  
                    });  
    	}
    	
    }
    
  //安装文件，一般固定写法  
    void update() {                 
    	if(sdPath!=null){
    		File file = new File(sdPath);
    		if(file.exists()&&file.length()>0){
    			Intent intent = new Intent(Intent.ACTION_VIEW);  
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
    	        intent.setDataAndType(Uri.fromFile(file),  
    	                "application/vnd.android.package-archive");  
    	        context.startActivity(intent); 
    	        android.os.Process.killProcess(android.os.Process.myPid());
    		}
    	}
         
    }  
    
    public void getAdvertisement(final Handler mHandler){
    	String url = SystemValue.basic_url
				+ "userInfo.do?action=get_advertisement";
		
		Log.d("get_advertisement", url);
		final Message message = new Message();
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						// TODO Auto-generated method stub
						Log.d("getLatestVersion_response", response);

						try {
							
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(response);

							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {

								Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-DD hh:mm:ss.SS").create();
								advertisement = gson.fromJson(jsonObject.getString("advertisement"), Advertisement.class);
								
								if(advertisement!=null){
										

									message.obj = advertisement;
								}
								
								
								
							} else {
								
								//
							}
							
							message.what = get_advertise;
							mHandler.sendMessage(message);

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("delete_drawings_response_JSONException",
									e.toString());
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						message.what = get_advertise;
						mHandler.sendMessage(message);
						
					}
				});
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
		volleyRequestQueue.add(stringRequest);
    }
}
