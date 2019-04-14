package util;



import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.tencent.upload.Const.FileType;
import com.tencent.upload.UploadManager;
import com.tencent.upload.task.ITask.TaskState;
import com.tencent.upload.task.IUploadTaskListener;
import com.tencent.upload.task.data.FileInfo;
import com.tencent.upload.task.impl.VideoUploadTask;


public class VideoUpload {
	UploadManager videoUploadMgr = null;
	public final String APP_ID = "10074595";
	final String BUCKET = "drawing";
	private final int video_upload_success = 101;
	private final int video_upload_failed = 102;
	Context context;
	

	RequestQueue volleyRequestQueue;
	public VideoUpload(Context context){
		videoUploadMgr = new UploadManager(context, APP_ID, FileType.Video, "qclouddrawingvideo");

		volleyRequestQueue = Volley.newRequestQueue(context);
		this.context = context;
	}
	

	public void uploadToQcloud(String srcFilePath,String destFilePath,final Handler mHandler){
		VideoUploadTask task = new VideoUploadTask(BUCKET, srcFilePath, destFilePath, 
			    "", null, new IUploadTaskListener() {
			    @Override
			    public void onUploadSucceed(final FileInfo result) {
			        Log.i("Demo", "upload succeed: " + result.url);
			        Message msg = new Message();
			        msg.what = video_upload_success;
			        msg.obj = result.url;
			        mHandler.sendMessage(msg);
			    }

			    @Override
			    public void onUploadStateChange(TaskState state) {

			        }

			    @Override
			    public void onUploadProgress(long totalSize, long sendSize){
			            long p = (long) ((sendSize * 100) / (totalSize * 1.0f));
			            Log.i("Demo", "上传进度: " + p + "%");
//			            Toast.makeText(context, "上传进度: " + p + "%", Toast.LENGTH_SHORT).show();
			        }

			    @Override
			    public void onUploadFailed(final int errorCode, final String errorMsg) {
			    	Message msg = new Message();
			        msg.what = video_upload_failed;
			        mHandler.sendMessage(msg);
			    }
			});


			task.setAuth(SystemValue.VIDEO_SIGN);
			videoUploadMgr.upload(task);  // 开始上传
	} 
	

	public void getAssign(){
		String url = SystemValue.basic_url
				+ "userInfo.do?action=get_video_assign";
		StringRequest stringRequest = new StringRequest(Method.GET, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {

						Log.d("drawings_response", response);

						try {
							// 获取response中的data
							JSONObject jsonObject = new JSONObject(response);

							SystemValue.VIDEO_SIGN = jsonObject.getString("result");
							

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.d("dj_JSONException_action", e.toString());
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

						
					}
				});
		volleyRequestQueue.add(stringRequest);
	}
}
