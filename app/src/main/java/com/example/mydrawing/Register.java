package com.example.mydrawing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import util.FileUtils;
import util.SystemValue;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;

import entity.DrawingInfo;
import entity.MyApplication;
import entity.UserInfo;

@ContentView(R.layout.register_layout)
public class Register extends Activity{
	
	@ViewInject(R.id.register_ll1)
	LinearLayout register_ll1;
	@ViewInject(R.id.register_ll2)
	LinearLayout register_ll2;
	
	@ViewInject(R.id.phone_et)
	EditText phone_et;
	@ViewInject(R.id.pwd_et)
	EditText pwd_et;
	@ViewInject(R.id.next_step_itbn1)
	ImageButton next_step_itbn1;
	@ViewInject(R.id.back_ibtn1)
	ImageButton back_ibtn1;
	
	@ViewInject(R.id.validate_info_tv)
	TextView validate_info_tv;
	@ViewInject(R.id.validate_et)
	EditText validate_et;
	@ViewInject(R.id.validate_btn)
	Button validate_btn;
	@ViewInject(R.id.validate_iv)
	ImageView validate_iv;
	@ViewInject(R.id.next_step_itbn2)
	ImageButton next_step_itbn2;
	@ViewInject(R.id.back_ibtn2)
	ImageButton back_ibtn2;
	
	boolean isNext1 = false,isNext2 = false,isWaited = true,isPage2 = false;
	String phone,pwd;
	int wait_sec = 60;
	
	Timer timer;
	TimerTask task;
	private RequestQueue volleyRequestQueue;
	String validate_code;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		ViewUtils.inject(this);
		volleyRequestQueue = Volley.newRequestQueue(this);
		
		initView();
	}
	
	Handler mHandler = new Handler(Looper.getMainLooper());
	
	void initView(){
		register_ll1.setVisibility(View.VISIBLE);
		register_ll2.setVisibility(View.GONE);
		
		phone_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				phone = phone_et.getText().toString().trim();
				pwd = pwd_et.getText().toString().trim();
				if(!phone.equals("")
						&&!pwd.equals("")
						&&pwd.length()>=6){
					next_step_itbn1.setImageResource(R.drawable.next_step_icon_s);
					isNext1 = true;
				}
				else {
					next_step_itbn1.setImageResource(R.drawable.next_step_icon);
					isNext1 = false;
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		pwd_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				phone = phone_et.getText().toString().trim();
				pwd = pwd_et.getText().toString().trim();
				if(!phone.equals("")
						&&!pwd.equals("")
						&&pwd.length()>=6){
					next_step_itbn1.setImageResource(R.drawable.next_step_icon_s);
					isNext1 = true;
				}
				else {
					next_step_itbn1.setImageResource(R.drawable.next_step_icon);
					isNext1 = false;
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		next_step_itbn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isNext1){
					phone = phone_et.getText().toString().trim();
					pwd = pwd_et.getText().toString().trim();
					
					if(phone.length()==0||phone.length()!=11){
						Toast.makeText(Register.this, "手机号无效，请输入正确的手机号", Toast.LENGTH_SHORT).show();
						
					}
					else {
						sendValidateCode();
						register_ll1.setVisibility(View.GONE);
						register_ll2.setVisibility(View.VISIBLE);
						validate_info_tv.setText(phone);
						validate_et.setText("");
						pwd_et.clearFocus();
						phone_et.clearFocus();
						validate_et.setFocusable(true);
						validate_et.setFocusableInTouchMode(true);
						validate_et.requestFocus();
						isNext2 = false;
						isPage2 = true;
					}
					
					
				}
				
			}
		});
		validate_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!isWaited){
					sendValidateCode();
				}
			}
		});
		
		validate_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				if(validate_code!=null&&
						!validate_code.equals("")&&
						validate_et.getText().toString().trim().equals(validate_code)){
					next_step_itbn2.setImageResource(R.drawable.next_step_icon_s);
					isNext2 = true;
				}
				else {
					next_step_itbn2.setImageResource(R.drawable.next_step_icon);
					isNext2 = false;
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		next_step_itbn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isNext2){
					register();
				}
			}
		});
		
		back_ibtn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		back_ibtn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isPage2 = false;
				register_ll2.setVisibility(View.GONE);
				register_ll1.setVisibility(View.VISIBLE);
				isWaited = false;
				wait_sec = 60;
				validate_iv.setImageResource(R.drawable.validate_resend_icon);
				validate_btn.setText("重新发送");
				if(timer!=null){
					timer.cancel();
					timer = null;
				}
			}
		});
	}
	
	@Override  
    public void onBackPressed() { 
		if(isPage2){
			isPage2 = false;
			register_ll2.setVisibility(View.GONE);
			register_ll1.setVisibility(View.VISIBLE);
			isWaited = false;
			wait_sec = 60;
			validate_iv.setImageResource(R.drawable.validate_resend_icon);
			validate_btn.setText("重新发送");
			if(timer!=null){
				timer.cancel();
				timer = null;
			}
		}else {
			 super.onBackPressed();  
		}
               
    }  
	
	void sendValidtaeCodeToServer(){
		
		validate_code = "";
		for(int i = 0;i < 6;i++){
			validate_code += (int)(Math.random()*10);
		}
		
		String url = SystemValue.basic_url
				+ "userInfo.do?action=send_validate_code";
		url += "&validate_code=" + validate_code;
		url += "&phone=" + phone;
		Log.d("sendValidtae", "CodeToServer"+url);
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						// TODO Auto-generated method stub
						Log.d("sendValidtae", "CodeToServer_response"+response);

						
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
	
//	void register(){
//		UserInfo userInfo = new UserInfo();
//		userInfo.setUserPhone(phone);
//		userInfo.setUserPwd(pwd);
//		
//		final Gson gson = new GsonBuilder().setDateFormat("yyyy.MM.dd").create();
//		
//		String uString = gson.toJson(userInfo);
//		
//		String url = SystemValue.basic_url
//				+ "userInfo.do?action=register";
//		url += "&userinfo=" + uString;
//		Log.d("register", url);
//		StringRequest stringRequest = new StringRequest(Method.GET, url,
//				new Response.Listener<String>() {
//
//					@Override
//					public void onResponse(String response) {
//						// TODO Auto-generated method stub
//						Log.d("changeContent_response", response);
//
//						try {
//							// 获取response中的data
//							JSONObject jsonObject = new JSONObject(response);
//
//							String result = jsonObject.getString("result");
//							if (result != null && result.equals("success")) {
//
//								Intent intent = new Intent(Register.this,LoginActivity.class);
//								intent.putExtra("isFromRegister", true);
//								startActivity(intent);
//								
//							} else {
//								Toast.makeText(
//										Register.this,
//										"注册失败，请稍后重试！",
//										Toast.LENGTH_SHORT).show();
//								//
//							}
//
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							Log.d("delete_drawings_response_JSONException",
//									e.toString());
//							e.printStackTrace();
//						}
//					}
//
//				}, new Response.ErrorListener() {
//
//					@Override
//					public void onErrorResponse(VolleyError error) {
//						// TODO Auto-generated method stub
//						Toast.makeText(
//								Register.this,
//								"注册失败，请稍后重试！",
//								1000).show();
//						
//					}
//				});
//		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
//		volleyRequestQueue.add(stringRequest);
//	}
	
	void register(){
		UserInfo userInfo = new UserInfo();
		userInfo.setUserPhone(phone);
		userInfo.setUserPwd(pwd);
		
		final Gson gson = new GsonBuilder().setDateFormat("yyyy.MM.dd").create();
		
		final String uString = gson.toJson(userInfo);
		
		String url = SystemValue.basic_url
				+ "userInfo.do";
//		url += "&userinfo=" + uString;
		Log.d("register", url);
		StringRequest stringRequest = new StringRequest(Method.POST, url,
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

								Intent intent = new Intent(Register.this,LoginActivity.class);
								intent.putExtra("isFromRegister", true);
								startActivity(intent);
								
							} else {
								if(result!=null&&result.equals("exist")){
									Toast.makeText(
											Register.this,
											"该号码已存在！",
											Toast.LENGTH_SHORT).show();
								}
								else {
									Toast.makeText(
											Register.this,
											"注册失败，请稍后重试！",
											Toast.LENGTH_SHORT).show();
								}
								
								//
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("delete_drawingsn",
									"_response_JSONExceptio"+e.toString());
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						Toast.makeText(
								Register.this,
								"注册失败，请稍后重试！",
								Toast.LENGTH_SHORT).show();
						
					}
				}){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
			                 //创建一个集合，放的是keyvalue的key是参数名与value是参数值
			                 Map<String, String> map = new HashMap<String, String>();
			                 map.put("userinfo", uString);
			                 map.put("action", "register");
			                 return map;
			             }
			 
			 
			         };
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
		volleyRequestQueue.add(stringRequest);
	}
	
	void sendValidateCode(){
		
		sendValidtaeCodeToServer();
		
		validate_iv.setImageResource(R.drawable.validate_send_wait_icon);
		validate_btn.setText("重新发送("+wait_sec+"秒)");
		task = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHandler.post(count_timeRunnable);
			}
		};
		timer = new Timer();
		timer.schedule(task, 0, 1000);
	}
	
	Runnable count_timeRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(wait_sec>0){
				isWaited = true;
				wait_sec--;
				validate_btn.setText("重新发送("+wait_sec+"秒)");
			}
			else {
				isWaited = false;
				wait_sec = 60;
				validate_iv.setImageResource(R.drawable.validate_resend_icon);
				validate_btn.setText("重新发送");
				if(timer!=null){
					timer.cancel();
					timer = null;
				}
			}
		}
	};

}