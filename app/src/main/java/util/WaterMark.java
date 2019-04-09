package util;

import java.io.FileInputStream;

import android.annotation.TargetApi;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.mydrawing.R;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
;

public class WaterMark {

	public static IplImage createBitmap(String path,Context context){
		try {
			
		
		FileInputStream fa=new FileInputStream(path);
		Bitmap src=BitmapFactory.decodeStream(fa);
		Bitmap watermark = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_market);
		
		
		Bitmap newb = null;//创建一个保存水印的位图
        if(src == null){
            return null;
        }
        int w = src.getWidth();//原图片的宽
        int h = src.getHeight();//原图片的高
        int ww = watermark.getWidth();//水印图片的宽
        int wh = watermark.getHeight();//水印图片的高
        Log.v("wz",w+","+h+","+ww+","+wh);//日志文件中查看位图大小
		
		//压缩图片的bitmap
        float scale = (float) (1000000.0/(w*h));
    Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap src1 = Bitmap.createBitmap(src, 0, 0, src.getWidth(),src.getHeight(), matrix, true);
    //释放掉原始位图
        src.recycle();
		w = src1.getWidth();
		h = src1.getHeight();
        
        newb = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);//创建一个新的和src一样大小的位图
        Canvas cv = new Canvas(newb);//创建一个同等 大小的画布

        cv.drawBitmap(src1, 0, 0, null);//从坐标0,0开始把src画入画布
        cv.drawBitmap(watermark, 0, h-wh, null);//在src中画入水印
        cv.save();//保存
        cv.restore();//存储
		
        
        
        return bitmapToIplImage(newb);//返回带水印的位图
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
   }
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
    public static Bitmap createBitmap(Bitmap src, Context context){
		try {
			
		
		Bitmap watermark = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_market);
		
		
		Bitmap newb = null;//创建一个保存水印的位图
        if(src == null){
            return null;
        }
        int w = src.getWidth();//原图片的宽
        int h = src.getHeight();//原图片的高
        int ww = watermark.getWidth();//水印图片的宽
        int wh = watermark.getHeight();//水印图片的高
        Log.v("wz",w+","+h+","+ww+","+wh);//日志文件中查看位图大小

            watermark.setHeight(20);
        Toast.makeText(context, "height "+wh+":"+watermark.getHeight(), Toast.LENGTH_SHORT).show();
		
		//压缩图片的bitmap
//        float scale = (float) (1000000.0/(w*h));
//    Matrix matrix = new Matrix();
//        matrix.setScale(scale, scale);
//        Bitmap src1 = Bitmap.createBitmap(src, 0, 0, src.getWidth(),src.getHeight(), matrix, true);
//    //释放掉原始位图
////        src.recycle();
//		w = src1.getWidth();
//		h = src1.getHeight();
        
        newb = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);//创建一个新的和src一样大小的位图
        Canvas cv = new Canvas(newb);//创建一个同等 大小的画布

        cv.drawBitmap(src, 0, 0, null);//从坐标0,0开始把src画入画布
        cv.drawBitmap(watermark, 5, h-wh-5, null);//在src中画入水印
        cv.save();//保存
        cv.restore();//存储
		
        
        
        return newb;//返回带水印的位图
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
   }
	
	/**
     * Bitmap转化为IplImage
     * @param bitmap
     * @return
     */
    public static IplImage bitmapToIplImage(Bitmap bitmap) {
        IplImage iplImage;
        iplImage = IplImage.create(bitmap.getWidth(), bitmap.getHeight(),
                opencv_core.IPL_DEPTH_8U, 4);
        bitmap.copyPixelsToBuffer(iplImage.getByteBuffer());
        return iplImage;
    }
    
    public static Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        //创建一个bitmap
        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        //将该图片作为画布
        Canvas canvas = new Canvas(newb);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(src, 0, 0, null);
        //在画布上绘制水印图片
        canvas.drawBitmap(watermark, 5, height-watermark.getHeight()-5, null);
        // 保存
        canvas.save(Canvas.ALL_SAVE_FLAG);
        // 存储
        canvas.restore();
        return newb;
    }
}
