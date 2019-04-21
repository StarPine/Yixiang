package com.example.mydrawing.second;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.mydrawing.R;
import com.liimou.artdrawing.ArtDrawingLib;
import com.liimou.artdrawing.DetectEdges;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import entity.DrawingDBOp;
import entity.DrawingInfo;
import entity.MyApplication;
import util.FileUtils;
import util.FileUtils.NoSdcardException;
import util.ImageUtil;
import util.SelectPhotoUtil;
import util.SystemValue;
import util.VerticalSeekBar;

public class VideoActivity_se extends Activity implements Callback,
        PictureCallback {

    RelativeLayout flexibleLayout = null, pause_rl = null, reciprocal_rl,
            seekbar_rl = null, ensureVideoLayout = null, video_bottom_tool_rl = null,
            surface_rl = null;
    LayoutInflater inflater = null;
    RelativeLayout videoGenLayout = null;
    ProgressBar video_gen_pb;
    String selectedImgPath = null;
    SelectPhotoUtil selectPhotoUtil;
    Uri photoUri;
    String pic_take_path;
    ImageView reciprocal_iv, last_pic_iv, red_circle_iv;

    DrawingDBOp dbOp;

    boolean isImage = false, isVideo = false;

    boolean isRunning = true;
    boolean isPause = false;
    int index = 0;
    ImageButton finish_ibtn, ensure_cancel, ensure_ok;
    ImageButton pause, continue_ibtn;
    ImageButton back_ibtn;
    TextView time_tv;
    VerticalSeekBar vSeekBar_left, vSeekBar_right;

    static final String IMAGE_TYPE = ".jpg";

    String tempFilePath, localVideoPath, localImagePath, localTakeImagePath;


    public String savePath;

    public static final int MSG_SAVE_SUCCESS = 100;
    public static final int MSG_STATE = 101;
    public static final int JUDGE_FINISH = 102;
    public static final int DO_IMAGE = 103;
    public static final int SAVE_IMAGE = 104;
    public static final int RECORD_TIME = 105;
    public static final int DATA_MISS = 106;
    public static final int TEMP_DATA = 107;

    long total_record_sec = 0;
    String timeString;
    int cur_progress = 0;

    private int[] pixels = null;
    private byte[] frameData = null;
    //    private int previewSizeWidth = 1280;
//    private int previewSizeHeight = 720;
    private int previewSizeWidth = 640;
    private int previewSizeHeight = 480;
    private boolean bProcessing = false;
    private Bitmap bitmap = null;

    private int[] reciprocal_drawables = new int[]{R.drawable.three_icon,
            R.drawable.two_icon, R.drawable.one_icon};
    private int reciprocal_index = 0;
    private boolean isReciprocal = false;
    int screenWidth, screenHeight;

    private float[] srcPts = new float[]{485, 445, 485, 30, 125, 105, 125, 350};
    private float[] dstPts = new float[]{0, 0, 480, 0, 480, 640, 0, 640};
//    private float[] srcPts = new float[]{0, 720,0, 0, 1280, 0, 1280, 720 };//后置
//    private float[] srcPts = new float[]{730, 710,730,40,260, 170, 260, 580  };
//    private float[] dstPts = new float[]{0, 0, 720, 0, 720, 1280, 0, 1280};

    public Handler mHandler = new Handler() {

        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TEMP_DATA:
                    String msgtxt = (String) msg.obj;
//				Toast.makeText(VideoActivity.this, msgtxt, 5).show();
                    break;
                case DATA_MISS:
                    new AlertDialog.Builder(VideoActivity_se.this,
                            AlertDialog.THEME_HOLO_DARK)
                            .setTitle("信息")
                            .setMessage("抱歉，数据已丢失，请重新录制！")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0,
                                                            int arg1) {

                                            editor.putString("state", "finished");
                                            editor.apply();
                                            finish();
                                        }
                                    }).create().show();
                    break;
                case MSG_SAVE_SUCCESS://视频保存成功
                    if (msg.obj != null) {
                        isVideo = true;
                        savePath = (String) msg.obj;

                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                                + tempFilePath + File.separator + "videoTemp_"
                                + saved_frame_indexs.get(saved_frame_indexs.size() - 1) + IMAGE_TYPE;
                        path = ImageUtil.compressImage(VideoActivity_se.this, path, true);
                        DrawingInfo drawingInfo = new DrawingInfo();
                        drawingInfo.setDrawingVideo(savePath);
                        drawingInfo.setCreateDate(new Timestamp(System.currentTimeMillis()));
                        drawingInfo.setVideoCover(path);

                        dbOp = new DrawingDBOp(VideoActivity_se.this);

                        int id = dbOp.insertDrawing(drawingInfo);
                        drawingInfo.setDrawingId(id + "");
                        SystemValue.curLocalDrawingInfo = drawingInfo;
                        editor.putString("state", "finished");
                        editor.apply();
                        FileUtils.deleteDirectoryContent(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + tempFilePath);


                        endofmp4 = System.currentTimeMillis();
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        if (timerTask != null) {
                            timerTask.cancel();
                            timerTask = null;
                        }
                        video_gen_pb.setProgress(100);

                        mHandler.sendEmptyMessage(JUDGE_FINISH);
                    } else {
                        new AlertDialog.Builder(VideoActivity_se.this,
                                AlertDialog.THEME_HOLO_DARK).setTitle("信息")
                                .setMessage("视频保存失败").setNegativeButton("确定", null).create().show();

                        index = 0;
                        flexibleLayout.setVisibility(View.GONE);
                    }
                    finish_ibtn.setEnabled(false);
                    break;
                case MSG_STATE:
                    if (msg.obj != null) {
                    }
                    break;
                case DO_IMAGE:
                    doImageProcessing();
                    break;
                case SAVE_IMAGE:
                    saveImageProcessing();
                    break;
                case JUDGE_FINISH:
                    judgeFinish();
                    break;
                case RECORD_TIME:
                    if (isRunning) {
                        if (!isPause) {
                            total_record_sec++;
                            timeString = SystemValue
                                    .getTimeString(total_record_sec);
                            time_tv.setText(timeString);
                            if (red_circle) {
                                red_circle = false;
                                red_circle_iv.setVisibility(View.GONE);
                            } else {
                                red_circle = true;
                                red_circle_iv.setVisibility(View.VISIBLE);
                            }

                            if (isReciprocal) {
                                if (reciprocal_index < 3) {
                                    reciprocal_iv
                                            .setImageResource(reciprocal_drawables[reciprocal_index]);
                                    reciprocal_index++;
                                } else {
                                    reciprocal_rl.setVisibility(View.GONE);
                                    isRunning = false;
                                    flexibleLayout.removeAllViews();
                                    flexibleLayout.addView(ensureVideoLayout);
                                    flexibleLayout.setVisibility(View.VISIBLE);
                                    last_pic_iv.setImageBitmap(bitmap);
                                }

                            }
                        }

                    } else {
                        if (cur_progress < 90) {
                            cur_progress += 2;
                        } else if (cur_progress < 98) {
                            cur_progress += 1;
                        }

                        video_gen_pb.setProgress(cur_progress);
                    }

                    break;
            }
        }

    };

    SurfaceView sView;
    SurfaceHolder surfaceHolder;
    Camera camera;
    ImageView cameraPreView = null;
    ImageView refImageView;
    long endofmp4 = 0;

    int total_frame_size = 0, frame_index_removed = 1;
    int max_frame_num = 200;
    List<Integer> saved_frame_indexs = new ArrayList<Integer>();
    int my_frame_rate = 1;// 抽帧间隔
    int cur_rate = 1;

    Timer timer;
    TimerTask timerTask;

    int ref_index = -1;
    String ref_path;

    DetectEdges detectEdges;
    String state;
    boolean red_circle = true;
    Bitmap originBitmap;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    Bitmap watermark;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // 第一个按下的手指的点
    private PointF startPoint = new PointF();
    // 两个按下的手指的触摸点的中点
    private PointF midPoint = new PointF();
    // 初始的两个手指按下的触摸点的距离
    private float oriDis = 1f;
    private String saveMp4Path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().addActivity(this);
        setContentView(R.layout.video_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        DisplayMetrics dm = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        screenHeight = dm.heightPixels; // 屏幕高（像素，如：800p）


        int scale = (int) (screenWidth * 1.0f / previewSizeWidth);
        scale = -1;
        if (scale > 0) {
            previewSizeHeight = (int) (previewSizeHeight * scale);
            previewSizeWidth = (int) (previewSizeWidth * scale);

            int len = srcPts.length;
            for (int i = 0; i < len; i++) {
                srcPts[i] = srcPts[i] * scale;
                dstPts[i] = dstPts[i] * scale;
            }
        }


        String videosec = getIntent().getExtras().getString("record_sec");
        if (videosec.equals("30s")) {
            max_frame_num = 600;
        } else if (videosec.equals("15s")) {
            max_frame_num = 300;
        }

        inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
        tempFilePath = SystemValue.tempFilePath;
        localVideoPath = SystemValue.localVideoPath;
        localImagePath = SystemValue.localImagePath;
        try {
            new FileUtils().creatSDDir("MyDrawings");
            new FileUtils().creatSDDir(tempFilePath);
            new FileUtils().creatSDDir(localVideoPath);
            new FileUtils().creatSDDir(localImagePath);
        } catch (NoSdcardException e) {
            e.printStackTrace();
        }

        ArtDrawingLib.SetAffineCorrection(srcPts, dstPts);
        pixels = new int[previewSizeWidth * previewSizeHeight];
        bitmap = Bitmap.createBitmap(previewSizeHeight, previewSizeWidth,
                Bitmap.Config.ARGB_8888);
        selectPhotoUtil = new SelectPhotoUtil(this);

        sView = (SurfaceView) this.findViewById(R.id.surfaceid);
        surfaceHolder = sView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        LayoutParams params = (LayoutParams) sView.getLayoutParams();
        params.width = screenWidth;
        params.height = screenWidth / 3 * 4;
        surface_rl.setLayoutParams(params);
        seekbar_rl.setLayoutParams(params);
        last_pic_iv.setLayoutParams(params);


        timerTask = new TimerTask() {

            @Override
            public void run() {

                mHandler.sendEmptyMessage(RECORD_TIME);
            }
        };
        timer = new Timer(true);
        timer.schedule(timerTask, 0, 1000);

        mHandler.postDelayed(toolAlphaRunnable, 2000);

        state = getIntent().getExtras().getString("ref_state");
        if (state.equals("s_ref")) {
            ref_index = getIntent().getExtras().getInt("pic_index");
            vSeekBar_right.setVisibility(View.GONE);
        } else if (state.equals("new_ref")) {
            ref_path = getIntent().getExtras().getString("ref_path");
            originBitmap = SystemValue.lessenUriImage(ref_path, screenWidth, screenHeight);
            detectEdges = new DetectEdges(refImageView, ref_path, screenWidth, screenHeight);
            vSeekBar_right.setProgress(140);
        } else {
            vSeekBar_left.setVisibility(View.GONE);
            vSeekBar_right.setVisibility(View.GONE);
        }
        vSeekBar_left.setProgress(140);

        sharedPreferences = getSharedPreferences("preVideo",
                Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        editor = sharedPreferences.edit();

        preVideoDeal();
        watermark = BitmapFactory.decodeResource(getResources(), R.drawable.water_market);

        pic_take_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + localImagePath + "/pictaketemp.jpg";
        photoUri = Uri.fromFile(new File(pic_take_path));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mOpenCVCallBack);
        } else {
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("Starpine", "OpenCV Manager已安装，可以学习OpenCV啦。");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    void preVideoDeal() {
        editor.putString("videoUrl", "");
        String status = getIntent().getExtras().getString("preVideo");
        if (status != null) {
            if (status.equals("recording")) {
                isPause = true;
                pause.setVisibility(View.GONE);
                back_ibtn.setVisibility(View.GONE);
                pause_rl.setVisibility(View.VISIBLE);
                finish_ibtn.setVisibility(View.INVISIBLE);
                recoverData();

            } else if (status.equals("convertion")) {
                recoverData();
                finishVideo();
                finish_ibtn.setVisibility(View.VISIBLE);
            } else {
                isPause = false;
                editor.putString("state", "recording");
                if (max_frame_num == 300) {
                    editor.putString("videosec", "15s");
                } else if (max_frame_num == 600) {
                    editor.putString("videosec", "30s");
                } else if (max_frame_num == 200) {
                    editor.putString("videosec", "10s");
                }

                editor.putString("ref_state", state);
                if (state.equals("new_ref")) {
                    editor.putString("picPath", ref_path);
                } else if (state.equals("s_ref")) {
                    editor.putInt("pic_index", ref_index);
                }
                //提交当前数据
                editor.apply();
                finish_ibtn.setVisibility(View.VISIBLE);
            }
        } else {
            isPause = false;
            editor.putString("state", "recording");
            if (max_frame_num == 300) {
                editor.putString("videosec", "15s");
            } else if (max_frame_num == 600) {
                editor.putString("videosec", "30s");
            }
            editor.putString("ref_state", state);
            if (state.equals("new_ref")) {
                editor.putString("picPath", ref_path);
            } else if (state.equals("s_ref")) {
                editor.putInt("pic_index", ref_index);
            }
            //提交当前数据
            editor.apply();
            finish_ibtn.setVisibility(View.VISIBLE);
        }

    }

    void recoverData() {
        String string = sharedPreferences.getString("saved_frame_indexs", "");
        if (string != null && !string.equals("")) {
            string = string.replace("[", "").replace("]", "");
            if (!string.equals("")) {
                String[] arrString = string.split(",");
                if (arrString != null) {
                    int len = arrString.length;
                    saved_frame_indexs.clear();
                    for (int i = 0; i < len; i++) {
                        saved_frame_indexs.add(Integer.parseInt(arrString[i].trim()));
                    }
                    if (saved_frame_indexs.size() > 0) {
                        index = saved_frame_indexs.get(len - 1);
                        frame_index_removed = sharedPreferences.getInt("frame_index_removed", 1);
                    }
                }
            }

            total_record_sec = sharedPreferences.getLong("total_record_sec", 0);
            timeString = SystemValue
                    .getTimeString(total_record_sec);
            time_tv.setText(timeString);
        } else {
            new AlertDialog.Builder(VideoActivity_se.this,
                    AlertDialog.THEME_HOLO_DARK)
                    .setTitle("信息")
                    .setMessage("抱歉，数据已丢失，请重新录制！")
                    .setPositiveButton("确定",
                            null).create().show();
        }
    }

    void initView() {

        flexibleLayout = (RelativeLayout) findViewById(R.id.flexible_rl);
        videoGenLayout = (RelativeLayout) inflater.inflate(
                R.layout.video_gen_layout, null);
        ensureVideoLayout = (RelativeLayout) inflater.inflate(R.layout.video_ensure_layout, null);
        pause_rl = (RelativeLayout) findViewById(R.id.pause_rl);
        reciprocal_rl = (RelativeLayout) findViewById(R.id.reciprocal_rl);
        seekbar_rl = (RelativeLayout) findViewById(R.id.seekbar_rl);
        video_bottom_tool_rl = (RelativeLayout) findViewById(R.id.video_bottom_tool_rl);
        surface_rl = (RelativeLayout) findViewById(R.id.surface_rl);

        reciprocal_iv = (ImageView) findViewById(R.id.reciprocal_iv);

        video_gen_pb = (ProgressBar) videoGenLayout
                .findViewById(R.id.video_gen_pb);
        video_gen_pb.setProgress(0);

        ensure_ok = (ImageButton) ensureVideoLayout.findViewById(R.id.ensure_video_ibtn);
        ensure_cancel = (ImageButton) ensureVideoLayout.findViewById(R.id.cancel_ensure_ibtn);
        last_pic_iv = (ImageView) ensureVideoLayout.findViewById(R.id.last_pic_iv);


        cameraPreView = (ImageView) findViewById(R.id.camera_preview);
        refImageView = (ImageView) findViewById(R.id.ref_img_iv);

        finish_ibtn = (ImageButton) findViewById(R.id.finish_ibtn);
        pause = (ImageButton) findViewById(R.id.pause_ibtn);
        continue_ibtn = (ImageButton) findViewById(R.id.continue_ibtn);
        back_ibtn = (ImageButton) findViewById(R.id.back_ibtn);
        time_tv = (TextView) findViewById(R.id.time_tv);
        red_circle_iv = (ImageView) findViewById(R.id.red_circle_iv);
        vSeekBar_left = (VerticalSeekBar) findViewById(R.id.vseekbar_left);
        vSeekBar_right = (VerticalSeekBar) findViewById(R.id.vseekbar_right);

        finish_ibtn.setVisibility(View.INVISIBLE);

        pause.setVisibility(View.VISIBLE);
        back_ibtn.setVisibility(View.VISIBLE);
        pause.setImageResource(R.drawable.pause_icon);

        refImageView.setOnTouchListener(new TouchListener());
        setOnclickListener();

    }

    void setOnclickListener() {

        ensure_cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                isRunning = true;
                isPause = false;
                isReciprocal = false;
                pause_rl.setVisibility(View.GONE);
                flexibleLayout.setVisibility(View.GONE);

                pause.setVisibility(View.VISIBLE);
                back_ibtn.setVisibility(View.VISIBLE);
                reciprocal_rl.setVisibility(View.GONE);
                reciprocal_index = 0;
                reciprocal_iv.setImageBitmap(null);
            }
        });

        ensure_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                finishVideo();
            }
        });

        back_ibtn.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View arg0) {

                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);
                new AlertDialog.Builder(VideoActivity_se.this,
                        AlertDialog.THEME_HOLO_DARK)
                        .setTitle("信息")
                        .setMessage("是否放弃本次视频？")
                        .setPositiveButton("退出",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        FileUtils.deleteDirectoryContent(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + SystemValue.tempFilePath);

                                        editor.putString("state", "finished");
                                        editor.apply();
                                        finish();
                                    }
                                }).setNegativeButton("取消", null).create().show();
            }

        });

        finish_ibtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);

                isReciprocal = true;
                isPause = false;
                pause_rl.setVisibility(View.GONE);
                pause.setVisibility(View.GONE);
                back_ibtn.setVisibility(View.GONE);
                reciprocal_rl.setVisibility(View.VISIBLE);

            }
        });
        pause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);

                isPause = true;
                pause.setVisibility(View.GONE);
                back_ibtn.setVisibility(View.GONE);
                pause_rl.setVisibility(View.VISIBLE);
                finish_ibtn.setVisibility(View.INVISIBLE);
            }
        });

        continue_ibtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);
                isPause = false;
                pause.setVisibility(View.VISIBLE);
                back_ibtn.setVisibility(View.VISIBLE);
                pause_rl.setVisibility(View.GONE);
                finish_ibtn.setVisibility(View.VISIBLE);

            }
        });


        seekbar_rl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);
            }
        });

        vSeekBar_left.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                float alpha = 0;
                if (state.equals("s_ref")) {
                    if (progress <= 100) {
                        alpha = (float) ((100 - progress) / 100.0);
                    } else {
                        alpha = (float) ((progress - 100) / 100.0);
                    }
                    refImageView.setAlpha(alpha);
                } else if (state.equals("new_ref")) {
                    if (progress <= 100) {
                        refImageView.setImageBitmap(originBitmap);
                        alpha = (float) ((100 - progress) / 100.0);
                    } else {
                        detectEdges.changeCanny2(vSeekBar_left.getProgress());
                        alpha = (float) ((progress - 100) / 100.0);
                    }
                    refImageView.setAlpha(alpha);
                }

                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);
            }
        });

        vSeekBar_right.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                detectEdges.changeCanny2(progress);
                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);
            }
        });

        video_bottom_tool_rl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                setToolAlpha(1f);
                mHandler.postDelayed(toolAlphaRunnable, 2000);
            }
        });

        pause_rl.setOnClickListener(null);
        flexibleLayout.setOnClickListener(null);
        reciprocal_rl.setOnClickListener(null);
    }

    void finishVideo() {
        reciprocal_rl.setVisibility(View.GONE);
        isRunning = false;
        imgsToMp4();
        mHandler.sendEmptyMessage(JUDGE_FINISH);
    }

    //    用仿射保存位图
    void toSaveBitmapWithAffine(final byte[] data) {

        if (data != null) {
            Log.i("test", "截取第" + index + "张图片成功...去保存");
            OutputStream os;
            try {
                File file = new FileUtils().createFileInSDCard("videoTemp_"
                        + index + IMAGE_TYPE, tempFilePath);
                os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);//不创建水印图

                os.flush();
                os.close();
                if (index >= max_frame_num) {
                    FileUtils.deleteFile(
                            "videoTemp_"
                                    + saved_frame_indexs
                                    .get(frame_index_removed)
                                    + IMAGE_TYPE, tempFilePath);
                    saved_frame_indexs.remove(frame_index_removed);
                    editor.putString("saved_frame_indexs", String.valueOf(saved_frame_indexs));
//					editor.putString("saved_frame_indexs", saved_frame_indexs.toString());

                    if (frame_index_removed < max_frame_num - 1) {
                        frame_index_removed++;
                    } else {
                        frame_index_removed = 1;
                    }
                    editor.putInt("frame_index_removed", frame_index_removed);
                    //提交当前数据
//			        editor.apply();
                }
                saved_frame_indexs.add(index);
                editor.putString("saved_frame_indexs", String.valueOf(saved_frame_indexs));
//				editor.putString("saved_frame_indexs", saved_frame_indexs.toString());
                editor.putLong("total_record_sec", total_record_sec);
                //提交当前数据
                editor.apply();
                index++;


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSdcardException e) {
                e.printStackTrace();
            }

        }
    }

    // 将图片转换成视频
    void imgsToMp4() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // mHandler.sendMessage(mHandler.obtainMessage(MSG_STATE,
                // "结束录制，保存数据中，请耐心等候..."));

                editor.putString("state", "convertion");
                editor.apply();
                saveMp4Path = VideoCapture_se.genMp4(tempFilePath, saved_frame_indexs, 20,
                        mHandler, localVideoPath, VideoActivity_se.this);
            }
        }).start();
    }

    @Override
    public void onPictureTaken(byte[] arg0, Camera arg1) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

        if (isRunning) {
            final Camera.Parameters p = camera.getParameters();
            List<Size> pictureSizes = p.getSupportedPictureSizes();
            List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
            p.setPreviewSize(previewSizeWidth, previewSizeHeight);
            p.setPictureFormat(PixelFormat.JPEG); // Sets the image format for
            // picture 设定相片格式为JPEG，默认为NV21
            p.setPreviewFormat(ImageFormat.NV21); // Sets the image format
            camera.setParameters(p);
            camera.setDisplayOrientation(90);
            camera.setPreviewCallback(new PreviewCallback() {

                Size size = p.getPreviewSize();
                byte[] data;

                @Override
                public void onPreviewFrame(byte[] arg0, Camera arg1) {

                    frameData = arg0;
                    // myHandler.post(DoImageProcessing);
                    if (isRunning) {
                        mHandler.sendEmptyMessage(DO_IMAGE);
                        if (!bProcessing && !isPause) {
                            if (saved_frame_indexs.size() > 1
                                    && saved_frame_indexs.get(1) > 1)
                                my_frame_rate = saved_frame_indexs.get(1) / 2;

                            if (cur_rate == my_frame_rate) {
                                mHandler.sendEmptyMessage(SAVE_IMAGE);
                                // myHandler.post(SaveImageProcessing);
                                cur_rate = 1;
                            } else {
                                cur_rate++;
                                index++;
                            }
                        }
                    }
                }
            });
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (Exception E) {

            }
            camera.startPreview();
        }

    }


    @SuppressLint("NewApi")
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {

        int cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(camIdx, cameraInfo);
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//后置
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//前置
                try {
                    camera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("camera_error",
                            "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        if (camera == null) {
            camera = Camera.open();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {

        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void doImageProcessing() {

        bProcessing = true;
        ArtDrawingLib.AffineCorrectionImage(previewSizeWidth,
                previewSizeHeight, frameData, pixels);
        bitmap.setPixels(pixels, 0, previewSizeHeight, 0, 0, previewSizeHeight,
                previewSizeWidth);
        cameraPreView.setImageBitmap(bitmap);
        bProcessing = false;

    }

    private void saveImageProcessing() {
        toSaveBitmapWithAffine(frameData);
    }

    private void judgeFinish() {
        if (isVideo) {
            //是否添加到相册
            ContentResolver localContentResolver = this.getContentResolver();
            File file = new File(saveMp4Path);
//            Uri localUri = Uri.fromFile(file);
            ContentValues localContentValues = FileUtils.getVideoContentValues(this, file, System.currentTimeMillis());
            Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
            Intent intentRe = new Intent();
            setResult(606, intentRe);
            finish();
        } else if (!isVideo) {
            flexibleLayout.removeAllViews();
            flexibleLayout.addView(videoGenLayout);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String picPath = null;

        if (requestCode == SystemValue.GET_PICTURE) {
            if (data != null) {
                Uri uri = data.getData();
                picPath = ImageUtil.getRealFilePath(VideoActivity_se.this, uri);
            }

        } else if (requestCode == SystemValue.TAKE_PICTURE) {

            String[] pojo = {MediaStore.Images.Media.DATA};
            if (photoUri != null) {
                picPath = pic_take_path;
            }

        }


        if (picPath != null) {
            final File file = new File(picPath);
            if (file.exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    int size = fis.available();
                    if (size > 0) {
                        selectedImgPath = picPath;
                        isImage = true;
                        mHandler.sendEmptyMessage(JUDGE_FINISH);
                    } else {

                        file.delete();
                    }
                    fis.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(VideoActivity_se.this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("信息").setMessage("是否放弃本次视频？")
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        FileUtils.deleteDirectoryContent(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + SystemValue.tempFilePath);
                        editor.putString("state", "finished");
                        editor.apply();
                        finish();
                    }
                }).setNegativeButton("取消", null).create().show();

    }

    @Override
    public void onPause() {
        isPause = true;
        pause_rl.setVisibility(View.VISIBLE);
        super.onPause();
    }

    private Runnable toolAlphaRunnable = new Runnable() {

        @Override
        public void run() {
            setToolAlpha(0.2f);
        }
    };

    void setToolAlpha(Float alpha) {
        finish_ibtn.setAlpha(alpha);
        vSeekBar_left.setAlpha(alpha);
        vSeekBar_right.setAlpha(alpha);
        back_ibtn.setAlpha(alpha);
        pause.setAlpha(alpha);
//		seekbar_rl.setAlpha(alpha);
    }

    private final class TouchListener implements View.OnTouchListener {
        private PointF startPoint = new PointF();//PointF(浮点对)
        private Matrix matrix = new Matrix();//矩阵对象
        private Matrix currentMatrix = new Matrix();//存放照片当前的矩阵
        private int mode = 0;//确定是放大还是缩小
        private static final int DRAG = 1;//拖拉模式
        private static final int ZOOM = 2;//缩放模式
        private float startDis;//开始距离
        private PointF midPoint;//中心点


        //参数1:用户触摸的控件；参数2:用户触摸所产生的事件
        public boolean onTouch(View v, MotionEvent event) {
            //判断事件的类型
            //得到低八位才能获取动作，所以要屏蔽高八位(通过与运算&255)
            //ACTION_MASK就是一个常量，代表255
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN://手指下压
                    mode = DRAG;
                    currentMatrix.set(refImageView.getImageMatrix());//记录ImageView当前的移动位置
                    startPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE://手指在屏幕移动，改事件会不断被调用
                    if (mode == DRAG) {//拖拉模式
                        float dx = event.getX() - startPoint.x;//得到在x轴的移动距离
                        float dy = event.getY() - startPoint.y;//得到在y轴的移动距离
                        matrix.set(currentMatrix);//在没有进行移动之前的位置基础上进行移动
                        //实现位置的移动
                        matrix.postTranslate(dx, dy);
                    } else if (mode == ZOOM) {//缩放模式
                        float endDis = distance(event);//结束距离
                        if (endDis > 10f) {//防止不规则手指触碰
                            //结束距离除以开始距离得到缩放倍数
                            float scale = endDis / startDis;
                            //通过矩阵实现缩放
                            //参数：1.2.指定在xy轴的放大倍数;3,4以哪个参考点进行缩放
                            //开始的参考点以两个触摸点的中心为准
                            matrix.set(currentMatrix);//在没有进行缩放之前的基础上进行缩放
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        }

                    }

                    break;
                case MotionEvent.ACTION_UP://手指离开屏幕
                case MotionEvent.ACTION_POINTER_UP://当屏幕上已经有手指离开屏幕，屏幕上还有一个手指，就会触发这个事件
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN://当屏幕上已经有触点(手指)，再有一个手指按下屏幕，就会触发这个事件
                    mode = ZOOM;
                    startDis = distance(event);
                    if (startDis > 10f) {//防止不规则手指触碰
                        midPoint = mid(event);
                        currentMatrix.set(refImageView.getImageMatrix());//记录ImageView当前的缩放倍数
                    }
                    break;

                default:
                    break;
            }
            //将imageView的矩阵位置改变
            refImageView.setImageMatrix(matrix);
            return true;
        }

    }

    //计算两点之间的距离(勾股定理)
    public float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    //计算两个点的中心点
    public static PointF mid(MotionEvent event) {
        float midx = (event.getX(1) + event.getX(0)) / 2;
        float midy = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midx, midy);
    }

}
