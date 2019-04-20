package com.example.mydrawing.second;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.mydrawing.MainActivity;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder.Exception;
import com.googlecode.javacv.cpp.opencv_core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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


    public static final int MSG_SAVE_SUCCESS = 100;
    public static final int MSG_STATE = 101;
    public static final int DATA_MISS = 106;
    public static final int TEMP_DATA = 107;
    static final String IMAGE_TYPE = ".jpg";


    public static String genMp4(String path, final List<Integer> saved_frame_indexs,
                                final double frameRate, final Handler handler, String localVideoPath, final Context context) {
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
                            + "videoTemp_" + saved_frame_indexs.get(index) + IMAGE_TYPE);
                    while (!file.exists() && index < maxIndex) {
                        index++;
                        file = new File(tempFilePath
                                + "videoTemp_" + saved_frame_indexs.get(index) + IMAGE_TYPE);
                    }
                    if (!file.exists()) {
                        handler.sendMessage(handler.obtainMessage(DATA_MISS, savePath));
                    } else {
                        Bitmap testBitmap = getImageByPath(tempFilePath
                                + "videoTemp_" + saved_frame_indexs.get(index) + IMAGE_TYPE);

                        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                                savePath, testBitmap.getWidth(),
                                testBitmap.getHeight());
                        recorder.setFormat("mp4");
                        recorder.setFrameRate(frameRate);// 录像帧率
                        recorder.setVideoQuality(0);//设置视频质量
                        recorder.start();


                        while (index < maxIndex) {
                            String firstFile = tempFilePath + "videoTemp_" + saved_frame_indexs.get(index) + IMAGE_TYPE;
                            Bitmap firstBitmap = getImageByPath(firstFile);
                            float lum = (float) 1.5;
                            Bitmap imageoperationBitmap = imageoperation(firstBitmap, lum, lum, lum);
                            savebitmap(imageoperationBitmap, firstFile);

                            opencv_core.IplImage image = cvLoadImage(tempFilePath
                                    + "videoTemp_" + saved_frame_indexs.get(index)
                                    + IMAGE_TYPE);
                            if (image != null) {

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return savePath;
    }

    private static void savebitmap(Bitmap bitmap, String path) {
        //创建文件，因为不存在2级目录，所以不用判断exist，要保存png，这里后缀就是png，要保存jpg，后缀就用jpg
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static Bitmap getImageByPath(String path) {
        return BitmapFactory.decodeFile(path);
    }


    //传入需要修改的Bitmap和色彩三元素
    private static Bitmap imageoperation(Bitmap mbitmap, float hue, float saturation, float lum) {
        //传入的Bitmap默认不可修改，需啊哟创建新的Bitmap
        Bitmap mbitmap_fu = Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //创建画布，在新的bitmap上绘制
        Canvas canvas = new Canvas(mbitmap_fu);
        //设置画笔抗锯齿，后面在Bitmap上绘制需要使用到画笔
        Paint mpaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorMatrix huematrix = new ColorMatrix();
        huematrix.setRotate(0, hue);//设置色调
        huematrix.setRotate(1, hue);
        huematrix.setRotate(2, hue);

        ColorMatrix saturationmatrix = new ColorMatrix();
        saturationmatrix.setSaturation(saturation);//设置饱和度

        ColorMatrix lummatrix = new ColorMatrix();
        //参数：rscale gscale bscale 透明度
        lummatrix.setScale(lum, lum, lum, 1);//设置亮度

        ColorMatrix imagematrix = new ColorMatrix();
//        imagematrix.postConcat(huematrix);
//        imagematrix.postConcat(saturationmatrix);
        imagematrix.postConcat(lummatrix);
        //通过画笔的setColorFilter进行设置
        mpaint.setColorFilter(new ColorMatrixColorFilter(imagematrix));
        canvas.drawBitmap(mbitmap, 0, 0, mpaint);
        return mbitmap_fu;
    }


}
