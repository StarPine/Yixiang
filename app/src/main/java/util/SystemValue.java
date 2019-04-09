package util;

//import com.tencent.a.a.a.a.c;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import entity.DrawingInfo;
import entity.UserInfo;

public class SystemValue {
	
	public static String basic_url = "http://10.249.55.58:8080/MyDrawingServer/";
//	public static String basic_url = "http://www.cool-la-share.cn:80/MyDrawingServer/";
//	public static String basic_url = "http://123.207.29.211:8080/MyDrawingServer/";
	public static String VIDEO_SIGN = null;
	public static String dest_head = "/sdk/";
	public static int cur_fragment_index = 0;
	public static int per_fragment_index = 0;
	public static final int HOME_PAGE = 0;
	public static final int HOME_LOCAL = 1;
	public static final int HOME_ME = 2;
	
	public static String default_drawing_content = "写上你的作品名称或心情吧（字数不超过20）";
	
	public static final int TAKE_PICTURE = 1; 
	public static final int GET_PICTURE = 2; 
	
	public static UserInfo curUserInfo;
	public static DrawingInfo curLocalDrawingInfo;
	
	public static String tempFilePath = "MyDrawings/video_temp";
	public static String localVideoPath = "MyDrawings/local_video";
	public static String localImagePath = "MyDrawings/local_image";
	public static String localAppPath = "MyDrawings/local_app"; 
	
	
	public final static Bitmap lessenUriImage(String path,int width,int height) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		// 调用上面定义的方法计算inSampleSize?
		options.inSampleSize = calculateInSampleSize(options, width,
						height);
		// 使用获取到的inSampleSize值再次解析图?
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}

	/**
	 * 计算inSampleSize，用于压缩图�?
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight)
	{
		// 源图片的宽度
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;

		if (width > reqWidth && height > reqHeight)
		{
			// 计算出实际宽度和目标宽度的比�?
			int widthRatio = Math.round((float) width / (float) reqWidth);
			int heightRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = Math.max(widthRatio, heightRatio);
		}
		return inSampleSize;
	}
	
	/**  
     * 获取视频缩略图  
     * @param videoPath  
     * @param width  
     * @param height  
     * @param kind  
     * @return  
     */    
    public static Bitmap getVideoThumbnail(String videoPath, int width , int height, int kind){    
     Bitmap bitmap = null;    
     bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);    
     bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);    
     return bitmap;    
    }
    
    public static String getTimeString(long sec){

    	int cur_sec = 0,cur_min = 0,cur_hour = 0;
    	String result = "00:00:00";
    	
    	cur_sec = (int) (sec %60);
    	cur_min = (int) (sec / 60);
    	cur_hour = (int) (sec/3600);
    	
    	if(cur_hour<10){
    		result = "0"+cur_hour+":";
    	}
    	else {
			result = cur_hour+":";
		}
    	
    	if(cur_min<10){
    		result += "0"+cur_min+":";
    	}
    	else {
			result += cur_min+":";
		}
    	if(cur_sec<10){
    		result += "0"+cur_sec;
    	}
    	else {
			result += cur_sec;
		}
    	
    	return result;
    }
    
 
}
