package com.example.mydrawing.second;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder.Exception;
import com.googlecode.javacv.cpp.opencv_core;

import java.io.File;
import java.util.List;

import util.FileUtils;
import util.FileUtils.NoSdcardException;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

/**
 * 第二版
 */
public class VideoCapture_se {
	private static int switcher = 0;// 录像键
	private static boolean isPaused = false;// 暂停键
	private static String filepath = "";
	private static String filename = null;
	

	public static final int MSG_SAVE_SUCCESS=100;
	public static final int MSG_STATE=101;
	public static final int DATA_MISS = 106;
	public static final int TEMP_DATA = 107;
	static final String IMAGE_TYPE = ".jpg";


	
	public static void genMp4(String path, final List<Integer> saved_frame_indexs,
			final double frameRate,final Handler handler, String localVideoPath,final Context context) {
		// init
		if (path != null) {
			filepath = path;
		}
		filename = "test_" + System.currentTimeMillis() + ".mp4";
		String temp = null;
		try {
			temp = new FileUtils().getSDCardRoot() + localVideoPath
					+ File.separator + filename;
		} catch (NoSdcardException e1) {
			e1.printStackTrace();
		}
		final String savePath = temp;

		switcher = 1;
		new Thread() {
			public void run() {
				Log.d("test", "开始将图片转成视频啦...frameRate=" + frameRate);
				try {
					new FileUtils().creatSDDir(filepath);
					String tempFilePath = new FileUtils().getSDCardRoot()
							+ filepath + File.separator;
					Log.i("test", "tempFilePath=" + tempFilePath);
					int index = 0;
					int maxIndex = saved_frame_indexs.size();
					File file = new File(tempFilePath
							+ "videoTemp_"+saved_frame_indexs.get(index) + IMAGE_TYPE);
					while(!file.exists()&&index < maxIndex){
						index++;
						file = new File(tempFilePath
								+ "videoTemp_"+saved_frame_indexs.get(index) + IMAGE_TYPE);
					}
					if(!file.exists()){
						handler.sendMessage(handler.obtainMessage(DATA_MISS, savePath));
					}
					else{
						Bitmap testBitmap = getImageByPath(tempFilePath
								+ "videoTemp_"+saved_frame_indexs.get(index) + IMAGE_TYPE);

						FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
								savePath, testBitmap.getWidth(),
								testBitmap.getHeight());
						recorder.setFormat("mp4");
						recorder.setFrameRate(frameRate);// 录像帧率
						recorder.setVideoQuality(0);//设置视频质量
						recorder.start();
						

						while (index < maxIndex) {

							opencv_core.IplImage image = cvLoadImage(tempFilePath
									+ "videoTemp_" + saved_frame_indexs.get(index)
									+ IMAGE_TYPE);							
							if(image!=null){
								
								recorder.record(image);
							}
							index++;
						}
						Log.d("test", "录制完成....");
						recorder.stop();
						handler.sendMessage(handler.obtainMessage(MSG_SAVE_SUCCESS, savePath));
					}
					
				} catch (NoSdcardException e) {
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private static Bitmap getImageByPath(String path) {
		return BitmapFactory.decodeFile(path);
	}
	
}
