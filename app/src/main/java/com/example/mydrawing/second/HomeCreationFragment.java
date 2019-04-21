package com.example.mydrawing.second;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mydrawing.R;
import com.example.mydrawing.zxing.android.CaptureActivity;
import com.example.mydrawing2.VideoAndRecording;
import com.example.mydrawing2.VideoSelectionActivity2;
import com.lidroid.xutils.ViewUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import util.FileUtils;
import util.ImageUtil;
import util.MacAddressUtil;
import util.SelectPhotoUtil;
import util.SystemValue;

import static android.app.Activity.RESULT_OK;

/**
 * 创作fragment
 * Created by Jerry on 2019/4/6.
 */

public class HomeCreationFragment extends Fragment implements View.OnClickListener {

    private LinearLayout buttons_rl;
    private ImageButton album_ref_ibtn;
    private ImageButton photograph_ref_ibtn;
    private ImageButton no_ref_ibtn;
    private ImageButton ten_s_ibtn;
    private ImageButton thirty_s_ibtn;
    private ImageButton sixty_s_ibtn, time_select_cancel,velocity_but,quality_but;
    private ImageView message_iv;
    Intent intent;
    Bundle bundle;
    View timeSelectView;
    Dialog dialog;
    String videosec = "10s";
    Activity activity;
    Uri photoUri;
    int screenWidth, screenHeight;
    SelectPhotoUtil selectPhotoUtil;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final int REQUEST_CODE_SCAN = 0x0000;
    private MyDialog myDialogialog;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.creation_fragment, null);

        activity = getActivity();
        ViewUtils.inject(activity);
        //获取屏幕分辨率信息
        DisplayMetrics dm = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        screenHeight = dm.heightPixels; // 屏幕高（像素，如：800p）
        initview(view);
        preVideoDeal();
        initData();

        return view;
    }

    private void initData() {
        intent = new Intent(activity, VideoActivity_se.class);
        bundle = new Bundle();
        selectPhotoUtil = new SelectPhotoUtil(activity);

    }

    private void initview(View view) {
        buttons_rl = view.findViewById(R.id.buttons_rl);
        album_ref_ibtn = view.findViewById(R.id.album_ref_ibtn);
        photograph_ref_ibtn = view.findViewById(R.id.photograph_ref_ibtn);
        no_ref_ibtn = view.findViewById(R.id.no_ref_ibtn);
        buttons_rl.setOnClickListener(this);
        album_ref_ibtn.setOnClickListener(this);
        photograph_ref_ibtn.setOnClickListener(this);
        no_ref_ibtn.setOnClickListener(this);

        int w = (int) ((screenWidth*0.95)/3);
        int h = w * 239 / 174;
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(w, h);
        llParams.weight = 1;
        album_ref_ibtn.setLayoutParams(llParams);
        photograph_ref_ibtn.setLayoutParams(llParams);
        no_ref_ibtn.setLayoutParams(llParams);
    }

    /**
     * 跳转到扫码界面扫码
     */
    private void goScan() {
        Intent intent = new Intent(activity, CaptureActivity.class);
//        startActivityForResult(intent, REQUEST_CODE_SCAN);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String picPath = null;

        if (requestCode == SystemValue.GET_PICTURE) {
            if(data!=null){
                Uri uri = data.getData();
                picPath = ImageUtil.getRealFilePath(activity, uri);
            }
        } else if (requestCode == SystemValue.TAKE_PICTURE) {

            String[] pojo = { MediaStore.Images.Media.DATA };
            Cursor cursor = activity.managedQuery(photoUri, pojo, null,
                    null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
                cursor.moveToFirst();
                picPath = cursor.getString(columnIndex);
            }
        }

        if (picPath != null) {
            final File file = new File(picPath);
            if (file.exists()) {
                bundle.clear();
                bundle.putString("record_sec", videosec);
                bundle.putString("ref_state", "new_ref");
                bundle.putString("ref_path", picPath);
                bundle.putString("preVideo", "finished");
                showTimeSelection();
            }
        }
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                //返回的文本内容
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                //返回的BitMap图像
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);

                Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();

            }
        }
    }

    void preVideoDeal(){
        sharedPreferences= activity.getSharedPreferences("preVideo",
                Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        editor = sharedPreferences.edit();
        final String state = sharedPreferences.getString("state", "");
        final String videosec = sharedPreferences.getString("videosec", "");
        final String ref_state = sharedPreferences.getString("ref_state", "");
        final String videoUrl = sharedPreferences.getString("videoUrl", "");
        if(state!=null&&!state.equals("")){
            if(state.equals("recording")) {
                new AlertDialog.Builder(activity,
                        AlertDialog.THEME_HOLO_DARK)
                        .setTitle("信息")
                        .setMessage("上次视频录制未正常退出，是否继续上次视频录制？")
                        .setPositiveButton("继续",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {

//										Intent intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
//										Bundle bundle = new Bundle();
                                        bundle.clear();
                                        bundle.putString("record_sec", videosec);
                                        bundle.putString("preVideo", state);
                                        if(videoUrl.equals("")){
                                            bundle.putString("ref_state", ref_state);
                                            if(ref_state.equals("new_ref")){
                                                String picPath = sharedPreferences.getString("picPath", "");
                                                bundle.putString("ref_path", picPath);

                                            } else if(ref_state.equals("s_ref")){
                                                int position = sharedPreferences.getInt("pic_index", -1);
                                                bundle.putInt("pic_index", position);
                                            }

                                            intent.putExtras(bundle);
                                            startActivityForResult(intent,606);
                                        }else {
                                            Intent intent1 = new Intent(getContext(),VideoAndRecording.class);
                                            bundle.putString("videoUrl", videoUrl);
                                            intent1.putExtras(bundle);
                                            startActivityForResult(intent1,606);
                                        }

                                    }
                                }).setNegativeButton("放弃",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                FileUtils.deleteDirectoryContent(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + SystemValue.tempFilePath);
                                editor.putString("state", "finished");
                                editor.apply();
                            }
                        }).create().show();
            }

            if(state.equals("convertion")) {
                new AlertDialog.Builder(activity,
                        AlertDialog.THEME_HOLO_DARK)
                        .setTitle("信息")
                        .setMessage("上次视频压缩出错，是否继续压缩？")
                        .setPositiveButton("继续",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {

//										Intent intent = new Intent(VideoSelectionActivity2.this,VideoActivity.class);
//										Bundle bundle = new Bundle();
                                        bundle.clear();
                                        bundle.putString("record_sec", videosec);
                                        bundle.putString("preVideo", state);

                                        if(videoUrl.equals("")){
                                            bundle.putString("ref_state", ref_state);
                                            if(ref_state.equals("new_ref")){
                                                String picPath = sharedPreferences.getString("picPath", "");
                                                bundle.putString("ref_path", picPath);
                                            }
                                            else if(ref_state.equals("s_ref")){
                                                int position = sharedPreferences.getInt("pic_index", -1);
                                                bundle.putInt("pic_index", position);
                                            }
                                            intent.putExtras(bundle);
                                            startActivityForResult(intent,606);
                                        }
                                        else {
                                            Intent intent1 = new Intent(getContext(),VideoAndRecording.class);
                                            bundle.putString("videoUrl", videoUrl);
                                            intent1.putExtras(bundle);
                                            startActivityForResult(intent1,606);
                                        }

                                    }
                                }).setNegativeButton("放弃",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                FileUtils.deleteDirectoryContent(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + SystemValue.tempFilePath);
                                editor.putString("state", "finished");
                                editor.apply();
                            }
                        }).create().show();
            }
        }

    }

    @Override
    public void onClick(View view) {
        if(!initActivation()){
            return;
        }

        switch (view.getId()) {

            case R.id.buttons_rl:

                break;

            case R.id.album_ref_ibtn:
                getPic();
                break;

            case R.id.photograph_ref_ibtn:
                ContentValues values = new ContentValues();
                photoUri = activity.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                takePic(photoUri);
                break;

            case R.id.no_ref_ibtn:
                bundle.clear();
                bundle.putString("record_sec", videosec);
                bundle.putString("ref_state", "no_ref");
                bundle.putString("preVideo", "finished");
                showTimeSelection();
                break;
        }
    }

    public void takePic(Uri photouri) {// 拍照
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// "android.media.action.IMAGE_CAPTURE"

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photouri);
            SystemValue.per_fragment_index = SystemValue.cur_fragment_index;

            startActivityForResult(intent, SystemValue.TAKE_PICTURE);
        } else {
            Toast.makeText(activity, "sd卡不可用", Toast.LENGTH_LONG).show();
        }
    }

    public  void getPic() {
        SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, SystemValue.GET_PICTURE);
    }

    //进行激活
    private boolean initActivation() {

        SharedPreferences sharedPreferences = activity.getSharedPreferences("activation", Context.MODE_PRIVATE);
        boolean isActivation = sharedPreferences.getBoolean("isActivation", false);
        if (!isActivation) {//没有激活
            if (!isNetworkAvailable(getContext())) {//没有联网
                Toast.makeText(getContext(), "请联网进行激活", Toast.LENGTH_SHORT).show();
            }else {//联网
                goScan();//进行扫描激活
            }
        }
        return isActivation;
    }

    void showTimeSelection() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("mode", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if (dialog == null) {
            dialog = new Dialog(getContext(), R.style.PicDialogTheme);
        }

        if (timeSelectView == null) {
            timeSelectView = LayoutInflater.from(getActivity()).inflate(R.layout.time_selection_layout, null);
            ten_s_ibtn = (ImageButton) timeSelectView.findViewById(R.id.ten_s_ibtn);
            thirty_s_ibtn = (ImageButton) timeSelectView.findViewById(R.id.thirty_s_ibtn);
            sixty_s_ibtn = (ImageButton) timeSelectView.findViewById(R.id.sixty_s_ibtn);
            time_select_cancel = (ImageButton) timeSelectView.findViewById(R.id.cancel_ibtn);
            velocity_but = (ImageButton) timeSelectView.findViewById(R.id.velocity_but);
            quality_but = (ImageButton) timeSelectView.findViewById(R.id.quality_but);
            message_iv = (ImageView) timeSelectView.findViewById(R.id.message_iv);
            String ve_and_qu = sharedPreferences.getString("ve_and_qu", "");
            SystemValue.VE_AND_QU = ve_and_qu;
            if(ve_and_qu.equals("velocity")){
                velocity_but.setImageResource(R.drawable.velocity_selected);
                quality_but.setImageResource(R.drawable.quality_unselect);
                message_iv.setImageResource(R.drawable.velocity);
            }else if(ve_and_qu.equals("quality")){
                velocity_but.setImageResource(R.drawable.velocity_unselect);
                quality_but.setImageResource(R.drawable.quality_selected);
                message_iv.setImageResource(R.drawable.quality);
            }else {
                velocity_but.setImageResource(R.drawable.velocity_selected);
                quality_but.setImageResource(R.drawable.quality_unselect);
                message_iv.setImageResource(R.drawable.velocity);
            }

            velocity_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    velocity_but.setImageResource(R.drawable.velocity_selected);
                    quality_but.setImageResource(R.drawable.quality_unselect);
                    message_iv.setImageResource(R.drawable.velocity);
                    SystemValue.VE_AND_QU = "velocity";
                    editor.putString("ve_and_qu", "velocity");
                    editor.commit();
                }
            });

            quality_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    velocity_but.setImageResource(R.drawable.velocity_unselect);
                    quality_but.setImageResource(R.drawable.quality_selected);
                    message_iv.setImageResource(R.drawable.quality);
                    SystemValue.VE_AND_QU = "quality";
                    editor.putString("ve_and_qu", "quality");
                    editor.commit();
                }
            });

            ten_s_ibtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    videosec = "10s";
                    ten_s_ibtn.setImageResource(R.drawable.ten_s_selected);
                    thirty_s_ibtn.setImageResource(R.drawable.fifteen_s_unselect);
                    sixty_s_ibtn.setImageResource(R.drawable.thirty_s_unselect);
                    bundle.putString("record_sec", videosec);
                    intent.putExtras(bundle);
                    startActivityForResult(intent,606);
                }
            });
            thirty_s_ibtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    videosec = "15s";
                    ten_s_ibtn.setImageResource(R.drawable.ten_s_unselect);
                    thirty_s_ibtn.setImageResource(R.drawable.fifteen_s_select);
                    sixty_s_ibtn.setImageResource(R.drawable.thirty_s_unselect);
                    bundle.putString("record_sec", videosec);
                    intent.putExtras(bundle);
                    startActivityForResult(intent,606);
                }
            });
            sixty_s_ibtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    videosec = "30s";
                    ten_s_ibtn.setImageResource(R.drawable.ten_s_unselect);
                    thirty_s_ibtn.setImageResource(R.drawable.fifteen_s_unselect);
                    sixty_s_ibtn.setImageResource(R.drawable.thirty_s_selected);
                    bundle.putString("record_sec", videosec);
                    intent.putExtras(bundle);
                    startActivityForResult(intent,606);

                }
            });
            time_select_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            showDialog(timeSelectView);
        }else {
            ten_s_ibtn.setImageResource(R.drawable.ten_s_unselect);
            thirty_s_ibtn.setImageResource(R.drawable.fifteen_s_unselect);
            sixty_s_ibtn.setImageResource(R.drawable.thirty_s_unselect);
        }
        dialog.show();
    }

    private void showDialog(View view) {
        // 引入窗口配置文件

        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        window.getAttributes().alpha = 1.0f;
//        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null && !activity.isFinishing()) {
            dialog.dismiss();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.i("NetWorkState", "Unavailabel");
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.i("NetWorkState", "Availabel");
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
