package com.example.mydrawing.second;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;
import com.example.mydrawing.R;
import com.lidroid.xutils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import entity.DrawingDBOp;
import entity.DrawingInfo;
import util.FileUtils;
import util.SelectPhotoUtil;
import util.SystemValue;
import util.VideoUpload;

/**
 * 记录fragment
 * Created by Jerry on 2019/4/6.
 */


public class HomeRecordFragment extends Fragment {


    Activity activity;
    int windowWidth = 0, windowHeight = 0;
    private GridView gridView;
    private RelativeLayout no_data_rl;
    private ImageButton top_ibtn;
    public boolean isEditStatu = false;
    private int cur_drawing_to_change_index;
    DrawingInfo curDrawingInfo;
    DrawingDBOp dbOp;
    // 数据
    private List<DrawingInfo> drawingInfos = new ArrayList<DrawingInfo>();
    private VideoAdapter videoAdapter;

    public void setActivity(Activity mainActivity2) {
        activity = mainActivity2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.record_fragment, null);
        initViewAndData(view);
        videoAdapter = new VideoAdapter(getActivity(), drawingInfos);
        getAction();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Rect frame = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

        windowWidth = display.getWidth();
        windowHeight = display.getHeight();

        gridView.setAdapter(videoAdapter);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        getAction();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {//隐藏

        } else {//显示
            Log.e("Starpine", "刷新数据");
            isEditStatu = false;
            top_ibtn.setImageResource(R.drawable.edit_icon);
            getAction();
        }
    }

    private void initViewAndData(View view) {
        gridView = view.findViewById(R.id.video_grodview);
        no_data_rl = view.findViewById(R.id.no_data_rl);
        top_ibtn = view.findViewById(R.id.top_ibtn);
        top_ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditStatu) {
                    isEditStatu = false;
                    top_ibtn.setImageResource(R.drawable.edit_icon);

                } else {
                    isEditStatu = true;
                    top_ibtn.setImageResource(R.drawable.finish_edit_icon);
                }
                isEditStatu = isEditStatu;
                videoAdapter.notifyDataSetChanged();
            }
        });
        if (dbOp == null) {
            dbOp = new DrawingDBOp(getActivity());
        }
    }

    // 从本地数据库得到数据
    public void getAction() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {


                List<DrawingInfo> temps = dbOp.queryDrawings();
                if (temps != null && temps.size() > 0) {
                    drawingInfos.clear();
                    drawingInfos.addAll(temps);
                } else {

                }
                videoAdapter.notifyDataSetChanged();
            }
        });
    }

    public class VideoAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater;
        private List<DrawingInfo> list;
        private String avatar;
        private String userName;

        public VideoAdapter(Context context, List<DrawingInfo> list) {
            this.mContext = context;
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.list = list;
        }

        @Override
        public int getCount() {
            if (list.size() == 0) {
                no_data_rl.setVisibility(View.VISIBLE);
            } else {
                no_data_rl.setVisibility(View.GONE);
            }
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            final DrawingInfo drawingInfo = list.get(position);

            convertView = inflater.inflate(R.layout.drawing_item_second, null);
            // 显示控件
            RelativeLayout show_rl = (RelativeLayout) convertView
                    .findViewById(R.id.show_rl);
            TextView content_tv = (TextView) convertView
                    .findViewById(R.id.content_tv);
            TextView date_tv = (TextView) convertView
                    .findViewById(R.id.date_tv);
            ImageView video_iv = (ImageView) convertView
                    .findViewById(R.id.video_iv);
            ImageView video_play_iv = (ImageView) convertView
                    .findViewById(R.id.video_play_iv);
            // 编辑控件
            RelativeLayout edit_rl = (RelativeLayout) convertView
                    .findViewById(R.id.edit_rl);
            ImageButton delete_video_ibtn = (ImageButton) convertView
                    .findViewById(R.id.delete_video_ibtn);

            // 显示内容
            if (isEditStatu) {
                edit_rl.setVisibility(View.VISIBLE);
            } else {
                edit_rl.setVisibility(View.GONE);
            }

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) show_rl.getLayoutParams();
            params.height = windowWidth / 2 / 3 * 4;
            show_rl.setLayoutParams(params);
            edit_rl.setLayoutParams(params);


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dateString = "";
            if (drawingInfo.getCreateDate() != null) {
                dateString = sdf.format(drawingInfo.getCreateDate());
            }

            date_tv.setText(dateString);
            String content = drawingInfo.getDescription();
            if (content == null || content.equals("") || content.equals("null")) {
                content = SystemValue.default_drawing_content;
            }
            content_tv.setText(content);

            Bitmap bitmapvid = SystemValue.lessenUriImage(drawingInfo.getVideoCover(), windowWidth / 2, windowWidth / 2 / 3 * 4);
            video_iv.setImageBitmap(bitmapvid);

            final int p = position;
            delete_video_ibtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                            .setTitle("删除作品")
                            .setMessage("确定删除该作品，删除后不可恢复！")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            cur_drawing_to_change_index = p;
                                            deleteVideo(p);
                                        }
                                    }).setNegativeButton("取消", null).show();

                }
            });
            video_play_iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String type = "video/mp4";
                    Uri uri = Uri.parse(drawingInfo.getDrawingVideo());
                    intent.setDataAndType(uri, type);
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    private void deleteVideo(int position) {
        curDrawingInfo = drawingInfos.get(position);
        int id = Integer.parseInt(curDrawingInfo.getDrawingId());
        dbOp.deleteDrawing(id);
        drawingInfos.remove(position);
        videoAdapter.notifyDataSetChanged();

        FileUtils.deleteFile(curDrawingInfo.getDrawingImg());
        FileUtils.deleteFile(curDrawingInfo.getDrawingVideo());
        FileUtils.deleteFile(curDrawingInfo.getVideoCover());

    }
}
