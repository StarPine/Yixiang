package com.example.mydrawing;

import java.io.File;
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
;

import entity.DrawingInfo;
import entity.MyApplication;
import entity.UserInfo;

@ContentView(R.layout.register_layout)
public class ForgetPwd extends Activity{
	
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
	@ViewInject(R.id.title1_tv)
	TextView title1_tv;
	
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
	@ViewInject(R.id.title2_tv)
	TextView title2_tv;
	
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
		
		title1_tv.setText("重置密码");
		title2_tv.setText("重置密码");
		
		phone_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

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

				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {

				
			}
		});
		
		pwd_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

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

				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {

				
			}
		});
		
		next_step_itbn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				if(isNext1){
					phone = phone_et.getText().toString().trim();
					pwd = pwd_et.getText().toString().trim();
					
					if(phone.length()==0||phone.length()!=11){
						Toast.makeText(ForgetPwd.this, "手机号无效，请输入正确的手机号", Toast.LENGTH_SHORT).show();
						
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

				if(!isWaited){
					sendValidateCode();
				}
			}
		});
		
		validate_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

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

				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {

				
			}
		});
		
		next_step_itbn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				if(isNext2){
					forgetPwd();
				}
			}
		});
		
		back_ibtn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				finish();
			}
		});
		back_ibtn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

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

						Log.d("sendValidtaeCode", "ToServer_response"+response);

						
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

						
						
					}
				});
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
		volleyRequestQueue.add(stringRequest);
	}
	
	void forgetPwd(){
		String url = SystemValue.basic_url
				+ "userInfo.do?action=forget_pwd";
		url += "&phone=" + phone+"&new_user_pwd="+pwd;
		Log.d("register", url);
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {

						Log.d("changeContent_response", response);

						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(response);

							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {

								Intent intent = new Intent(ForgetPwd.this,LoginActivity.class);
								intent.putExtra("isFromRegister", true);
								startActivity(intent);
								
							} else {
								if(result!=null&&result.equals("not exist")){
									Toast.makeText(
											ForgetPwd.this,
											"账号不存在！",
											Toast.LENGTH_SHORT).show();
								}
								else {
									Toast.makeText(
											ForgetPwd.this,
											"密码重置失败，请稍后重试！",
											Toast.LENGTH_SHORT).show();
								}
								
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

						Toast.makeText(
								ForgetPwd.this,
								"注册失败，请稍后重试！",
								Toast.LENGTH_SHORT).show();
						
					}
				});
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

				mHandler.post(count_timeRunnable);
			}
		};
		timer = new Timer();
		timer.schedule(task, 1000, 1000);
	}
	
	Runnable count_timeRunnable = new Runnable() {
		
		@Override
		public void run() {

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