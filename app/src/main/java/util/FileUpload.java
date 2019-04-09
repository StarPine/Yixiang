package util;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;


public class FileUpload {
	
	/**
	 * 上传图片
	 * 
	 * @param picPath
	 */
	public void PostPicture(final Activity activity,RequestParams params) {
//		RequestParams params = new RequestParams();
//
////		params.addBodyParameter("auth_token", RsSharedUtil.getString(
////				getApplicationContext(), AppConfig.ACCESS_TOKEN));
//		params.addQueryStringParameter(name, value);
//		params.addQueryStringParameter("action", action);
//
//		params.addBodyParameter("image", new File(picPath)); 
//		Log.d("auth_token", RsSharedUtil.getString(getApplicationContext(),
//				AppConfig.ACCESS_TOKEN));
		HttpUtils http = new HttpUtils();
		http.send(HttpMethod.POST, SystemValue.basic_url + "drawing.do", params,
				new RequestCallBack<String>() {

					@Override
					public void onStart() {
						Log.d("PostPicture", "准备上传文件...");
						Toast.makeText(activity, "准备上传文件1...", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						if (isUploading) {
							Log.d("PostPicture", "正在上传文件...");
							Toast.makeText(activity, "正在上传文件...", Toast.LENGTH_SHORT).show();
						} else {
							Log.d("PostPicture", "准备上传文件...");
							Toast.makeText(activity, "准备上传文件...", Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						Log.d("上传成功", responseInfo.result);
						Toast.makeText(activity, "uploadsuccess", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						Log.d("fail", "哈哈哈");
						Log.d("error", error.toString());
						Toast.makeText(activity, "failed:"+error.toString(), Toast.LENGTH_SHORT).show();
					}
				});
	}

}
