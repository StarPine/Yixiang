package util;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.io.File;
import java.util.List;

import util.FileUtils.NoSdcardException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.mydrawing.VideoActivity;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder.Exception;
import com.googlecode.javacv.cpp.opencv_core;

public class VideoCapture {
	private static int switcher = 0;// 录像键
	private static boolean isPaused = false;// 暂停键
	private static String filepath = "";
	private static String filename = null;
	

	public static final int MSG_SAVE_SUCCESS=100;
	public static final int MSG_STATE=101;
	public static final int DATA_MISS = 106;
	public static final int TEMP_DATA = 107;
	static final String IMAGE_TYPE = ".jpg";

//	public static void genMp4(Context context, String path, final int maxIndex,
//			final double frameRate,final Handler handler) {
//		// init
//		VideoCapture.context = context;
//		if (path != null) {
//			filepath = path;
//		}
//		filename = "test_" + System.currentTimeMillis() + ".mp4";
//		String temp = null;
//		try {
//			temp = new FileUtils().getSDCardRoot() + "ScreenRecord"
//					+ File.separator + filename;
//		} catch (NoSdcardException e1) {
//			e1.printStackTrace();
//		}
//		final String savePath = temp; 
//
//		switcher = 1;
//		new Thread() {
//			public void run() {
//				Log.d("test", "开始将图片转成视频啦...frameRate=" + frameRate);
//				try {
//					new FileUtils().creatSDDir(filepath);
//					
//					handler.sendMessage(handler.obtainMessage(MainActivity.MSG_STATE,"开始将图片转成视频，请稍后。。"));
//
//					String tempFilePath = new FileUtils().getSDCardRoot()
//							+ filepath + File.separator;
//					Log.i("test", "tempFilePath=" + tempFilePath);
//					Bitmap testBitmap = getImageByPath(tempFilePath
//							+ "videoTemp_0" + MainActivity.IMAGE_TYPE);
//
//					FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
//							savePath, testBitmap.getWidth(),
//							testBitmap.getHeight());
//
//					recorder.setFormat("mp4");
//					recorder.setFrameRate(frameRate);// 录像帧率
//					recorder.start();
//
//					int index = 0;
//					while (index < maxIndex) {
//						opencv_core.IplImage image = cvLoadImage(tempFilePath
//								+ "videoTemp_" + index
//								+ MainActivity.IMAGE_TYPE);
//						recorder.record(image);
//						index++;
//						handler.sendMessage(handler.obtainMessage(MainActivity.MSG_STATE, "converttomp4:[max:"+maxIndex+" , "+index+"]"));
//					}
//					Log.d("test", "录制完成....");
//					recorder.stop();
//					handler.sendMessage(handler.obtainMessage(MainActivity.MSG_SAVE_SUCCESS, savePath));
////					FileUtils.deleteDirectoryContent(tempFilePath);
//				} catch (FileUtils.NoSdcardException e) {
//					e.printStackTrace();
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();
//	}
	
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
					
//					handler.sendMessage(handler.obtainMessage(MSG_STATE,"开始将图片转成视频，请稍后。。"));

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
						
						recorder.start();
						

						while (index < maxIndex) {
//							opencv_core.IplImage image = WaterMark.createBitmap(tempFilePath
//									+ "videoTemp_" + saved_frame_indexs.get(index)
//									+ IMAGE_TYPE, context);
							opencv_core.IplImage image = cvLoadImage(tempFilePath
									+ "videoTemp_" + saved_frame_indexs.get(index)
									+ IMAGE_TYPE);							
							if(image!=null){
								
								recorder.record(image);
							}
//							Toast.makeText(context, "max:index "+maxIndex+":"+index, 10).show();
//							handler.sendMessage(handler.obtainMessage(TEMP_DATA, "max:index "+maxIndex+":"+index));
							
							index++;
						}
						Log.d("test", "录制完成....");
						recorder.stop();
						handler.sendMessage(handler.obtainMessage(MSG_SAVE_SUCCESS, savePath));
//						FileUtils.deleteDirectoryContent(tempFilePath);
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

	public static void stop() {
		switcher = 0;
		isPaused = false;
	}

	public static void pause() {
		if (switcher == 1) {
			isPaused = true;
		}
	}

	public static void restart() {
		if (switcher == 1) {
			isPaused = false;
		}
	}

	public static boolean isStarted() {
		if (switcher == 1) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isPaused() {
		return isPaused;
	}

//	private static Bitmap getImageFromAssetsFile(String filename) {
//		Bitmap image = null;
//		AssetManager am = context.getResources().getAssets();
//		try {
//			InputStream is = am.open(filename);
//			image = BitmapFactory.decodeStream(is);
//			is.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return image;
//	}

	private static Bitmap getImageByPath(String path) {
		return BitmapFactory.decodeFile(path);
	}
	
}
