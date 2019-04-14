package com.example.mydrawing;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import util.SystemValue;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;

import entity.DrawingInfo;
import entity.MyApplication;
import entity.UserInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

@ContentView(R.layout.login_layout)
public class LoginActivity extends Activity{
	
	@ViewInject(R.id.login_title_tv)
	TextView login_title_tv;
	@ViewInject(R.id.phone_et)
	EditText phone_et;
	@ViewInject(R.id.pwd_et)
	EditText pwd_et;
	@ViewInject(R.id.login_ibtn)
	ImageButton login_ibtn;
	@ViewInject(R.id.back_ibtn)
	ImageButton back_ibtn;
	
	@ViewInject(R.id.register_tv)
	TextView register_tv;
	@ViewInject(R.id.forget_pwd_tv)
	TextView forget_pwd_tv;
	
	boolean isFromRegister = false;
	boolean isLogin = false;
	String phone,pwd;
	private RequestQueue volleyRequestQueue;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		ViewUtils.inject(this);
		volleyRequestQueue = Volley.newRequestQueue(this);
		isFromRegister = getIntent().getExtras().getBoolean("isFromRegister");
		
		
		initView();
		
		
	}
	
	void initView(){
		if(isFromRegister){
			login_title_tv.setText("用户登录(3/3)");
		}
		phone_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

				phone = phone_et.getText().toString().trim();
				pwd = pwd_et.getText().toString().trim();
				if(!phone.equals("")
						&&!pwd.equals("")
						&&pwd.length()>=6){
					login_ibtn.setImageResource(R.drawable.login_icon_s);
					isLogin = true;
				}
				else {
					login_ibtn.setImageResource(R.drawable.login_icon);
					isLogin = false;
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
					login_ibtn.setImageResource(R.drawable.login_icon_s);
					isLogin = true;
				}
				else {
					login_ibtn.setImageResource(R.drawable.login_icon);
					isLogin = false;
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
		
		login_ibtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				if(isLogin){
					phone = phone_et.getText().toString().trim();
					pwd = pwd_et.getText().toString().trim();
					login();
					
				}
				
			}
		});
		
		register_tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(LoginActivity.this,Register.class);
				startActivity(intent);
			}
		});
		
		forget_pwd_tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(LoginActivity.this,ForgetPwd.class);
				startActivity(intent);
			}
		});
		
		back_ibtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				finish();
			}
		});
		
	}
	
	void login(){
		String url = SystemValue.basic_url
				+ "userInfo.do?action=login";
		url += "&user_phone=" + phone;
		url += "&user_pwd=" + pwd;
		Log.d("login", url);
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {

						Log.d("login_response", response);

						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(response);

							String result = jsonObject.getString("result");
							if (result != null && result.equals("success")) {

								Gson gson = new GsonBuilder().create();
								SystemValue.curUserInfo = gson.fromJson(jsonObject.getString("userInfo"), UserInfo.class);
								Intent intent = new Intent(LoginActivity.this,MainActivity.class);
								startActivity(intent);
								finish();
								
							} else {
								Toast.makeText(
										LoginActivity.this,
										"登录失败，请稍后重试！",
										Toast.LENGTH_SHORT).show();
								//
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("delete",
									"_drawings_response_JSONException"+e.toString());
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

						Toast.makeText(
								LoginActivity.this,
								"登录失败，请稍后重试！",
								Toast.LENGTH_SHORT).show();
						
					}
				});
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(30*1000,0,0f));
		volleyRequestQueue.add(stringRequest);
	}

}
