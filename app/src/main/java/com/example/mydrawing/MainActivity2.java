package com.example.mydrawing;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by Jerry on 2019/4/6.
 */

public class MainActivity2 extends FragmentActivity {

    private FrameLayout contentFrame;
    private RelativeLayout start_loading_rl;
    private ImageView home_drawing_iv;
    private boolean isCreation = true;
    int back_times = 0;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private HomeCreationFragment homeCreationFragment;
    private HomeRecordFragment homeRecordFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initview();
        intiData();
        initFragment();
    }

    private void intiData() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                start_loading_rl.setVisibility(View.GONE);
            }
        }, 1000);

    }

    private void initFragment() {
        homeCreationFragment = new HomeCreationFragment();
        homeRecordFragment = new HomeRecordFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content,homeRecordFragment);
        fragmentTransaction.add(R.id.content,homeCreationFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        back_times++;
        if(back_times>=2){
            backToHome();
        }else {
            Toast.makeText(this, "再按一次退出艺享", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(backRunnable, 3000);
        }
    }

    void backToHome(){
        PackageManager pm = getPackageManager();
        ResolveInfo homeInfo =
                pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        ActivityInfo ai = homeInfo.activityInfo;
        Intent startIntent = new Intent(Intent.ACTION_MAIN);
        startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
        startActivitySafely(startIntent);
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "null",
                    Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "null",
                    Toast.LENGTH_SHORT).show();
        }
    }

    Runnable backRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            back_times=0;
        }
    };

    private void initview() {

        contentFrame = (FrameLayout) findViewById(R.id.content);
        start_loading_rl = (RelativeLayout) findViewById(R.id.start_loading_rl);
        home_drawing_iv = (ImageView) findViewById(R.id.home_drawing_iv);
        home_drawing_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                hidtFragment(ft);
                if(isCreation){
                    isCreation = false;
                    home_drawing_iv.setImageResource(R.drawable.home_jilu);
                    ft.show(homeRecordFragment);
                    ft.commit();
                }else {
                    isCreation = true;
                    home_drawing_iv.setImageResource(R.drawable.home_one);
                    ft.show(homeCreationFragment);
                    ft.commit();
                }
            }
        });
    }

    //隐藏所有的fragment
    private void hidtFragment(FragmentTransaction fragmentTransaction){
        if (homeCreationFragment != null){
            fragmentTransaction.hide(homeCreationFragment);
        }
        if (homeRecordFragment != null){
            fragmentTransaction.hide(homeRecordFragment);
        }
    }
}
