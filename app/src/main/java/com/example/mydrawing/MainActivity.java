package com.example.mydrawing;

import util.SystemValue;
import util.UpdateVersion;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydrawing2.VideoSelectionActivity2;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import entity.Advertisement;
import entity.MyApplication;

@ContentView(R.layout.activity_main)
public class MainActivity extends FragmentActivity {
	
	@ViewInject(R.id.advertise_rl)
	RelativeLayout advertise_rl;
	
	@ViewInject(R.id.start_loading_rl)
	RelativeLayout start_loading_rl;
	
	@ViewInject(R.id.skip_ibtn)
	ImageButton skip_ibtn;
	
	@ViewInject(R.id.advertiseimg_iv)
	ImageView advertise_iv;

	@ViewInject(R.id.home_page_iv)
	ImageView home_page_iv;

	@ViewInject(R.id.home_drawing_iv)
	ImageView home_drawing_iv;


	@ViewInject(R.id.home_me_iv)
	ImageView home_me_iv;
	
	@ViewInject(R.id.dowload_rl)
	RelativeLayout dowload_rl;
	
	@ViewInject(R.id.pic_show_rl)
	public RelativeLayout pic_show_rl;
	@ViewInject(R.id.pic_show_iv)
	public ImageView pic_show_iv;

	HomePageFragment homePageFragment;
	HomeDrawingFragment homeDrawingFragment;
	MyDrawingPageFragment myDrawingPageFragment;

	private static FragmentManager fMgr;
	private Fragment curFragment;
	private int cur_fragment_index = 0;
	
	final int has_version_to_update = 401;
	final int download_finished =402;
	final int close_advertise =403;
	final int get_advertise =404;
	UpdateVersion updateVersion;
	Advertisement advertisement;
	
	LayoutInflater inflater;
	RelativeLayout videoGenLayout;
	ProgressBar video_gen_pb;
	TextView loading_tv;
	int back_times = 0;
	int back_times1 = 0;
	//测试加干辣椒两个加两个

	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case has_version_to_update:
				new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_DARK)
				.setTitle("信息")
				.setMessage("艺享发布新版本啦，是否更新新版本？")
				.setPositiveButton("更新",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(
									DialogInterface arg0, int arg1) {

								dowload_rl.setVisibility(View.VISIBLE);
								updateVersion.downloadLastetVersion(video_gen_pb, mHandler);
							}
						}).setNegativeButton("取消", null).create().show();
				break;
			case download_finished:
				dowload_rl.setVisibility(View.GONE);
				break;
			case close_advertise:
				advertise_rl.setVisibility(View.GONE);
				initFragments();
				setTabSelection(SystemValue.HOME_PAGE);
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {

						initDownloadView();
						updateVersion.getLatestVersion(mHandler);
					}
				});
				break;
			case get_advertise:
				advertisement = (Advertisement) msg.obj;
				if(advertisement!=null){
					setAdvertise();
				}
				else{
					advertise_rl.setVisibility(View.GONE);
					initFragments();
					setTabSelection(SystemValue.HOME_PAGE);
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {

							initDownloadView();
							updateVersion.getLatestVersion(mHandler);
						}
					});
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ViewUtils.inject(this);
		// 获取FragmentManager实例
		fMgr = getSupportFragmentManager();
//		initFragments();

//		SystemValue.curUserInfo = new UserInfo();
//		SystemValue.curUserInfo.setUserId("9794415541e744488cdea0d61a82496c");
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.introduction)
				.showImageOnFail(R.drawable.introduction).cacheInMemory(true)
				.cacheOnDisc(true).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				.discCacheSize(50 * 1024 * 1024)//
				.discCacheFileCount(100)// 缓存一百张图片
				.writeDebugLogs().build();
		ImageLoader.getInstance().init(config);
//		setTabSelection(SystemValue.HOME_PAGE);
		
		updateVersion = new UpdateVersion(this);
		
//		mHandler.post(new Runnable() {
//			
//			@Override
//			public void run() {
//
//				initDownloadView();
//				updateVersion.getLatestVersion(mHandler);
//			}
//		});
		advertise_rl.setVisibility(View.VISIBLE);
		updateVersion.getAdvertisement(mHandler);
		start_loading_rl.setVisibility(View.VISIBLE);
		start_loading_rl.setOnClickListener(null);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {

				start_loading_rl.setVisibility(View.GONE);
			}
		}, 1000);

	}
	
	void setAdvertise(){
		ImageLoader.getInstance().displayImage(SystemValue.basic_url+advertisement.getImage(), advertise_iv);
		advertise_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				String address = advertisement.getAddress();
				if(address!=null&&!address.equals("")){
					Uri uri = Uri.parse(address);  
					Intent it = new Intent(Intent.ACTION_VIEW, uri);  
					startActivity(it);
				}
				
			}
		});
		Message msg = new Message();
		msg.what = close_advertise;
		mHandler.sendMessageDelayed(msg, 3000);
	}
	
	@Override  
    public void onBackPressed() { 
		back_times++;
		if(back_times>=2){
//			super.onBackPressed();
//			finish();
//			moveTaskToBack(false);
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

			back_times=0;
		}
	};
	void initDownloadView(){
		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		videoGenLayout = (RelativeLayout) inflater.inflate(
				R.layout.video_gen_layout, null);
		video_gen_pb = (ProgressBar) videoGenLayout
				.findViewById(R.id.video_gen_pb);
		video_gen_pb.setProgress(0);
		loading_tv = (TextView) videoGenLayout.findViewById(R.id.loading_tv);
		loading_tv.setText("艺享新版本正在下载，请耐心等候...");
		dowload_rl.removeAllViews();
		dowload_rl.addView(videoGenLayout);
		
	}

	private void initFragments() {

		Log.d(this.toString(), "initFragments()");
		FragmentTransaction transaction = fMgr.beginTransaction();

		if (homePageFragment == null) {
			homePageFragment = new HomePageFragment();
			transaction.add(R.id.content, homePageFragment);
		}
		if (homeDrawingFragment == null) {
			homeDrawingFragment = new HomeDrawingFragment();
			transaction.add(R.id.content, homeDrawingFragment);
		}
		
		if (myDrawingPageFragment == null) {
			myDrawingPageFragment = new MyDrawingPageFragment();
			transaction.add(R.id.content, myDrawingPageFragment);
		}
		transaction.commitAllowingStateLoss();
		
		
	}

	private void hideFragments() {

		Log.d(this.toString(), "hideFragments()");
		FragmentTransaction transaction = fMgr.beginTransaction();

		if (homePageFragment != null) {
			transaction.hide(homePageFragment);
		}
		if (homeDrawingFragment != null) {
			transaction.hide(homeDrawingFragment);
		}
		if (myDrawingPageFragment != null) {
			transaction.hide(myDrawingPageFragment);
		}
		transaction.commitAllowingStateLoss();
	}

	@OnClick({ R.id.home_page_iv, R.id.home_drawing_iv, 
			R.id.home_me_iv,R.id.skip_ibtn })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.skip_ibtn:
			Message msg = new Message();
			msg.what = close_advertise;
			mHandler.sendMessage(msg);
			break;

		case R.id.home_page_iv:
			setTabSelection(SystemValue.HOME_PAGE);

			break;
		case R.id.home_drawing_iv:
			// setTabSelection(2);
			clearSelected();
			home_drawing_iv.setImageResource(R.drawable.home_drawing_icon_s);
			Intent intent = new Intent(MainActivity.this,
					VideoSelectionActivity2.class);
			startActivity(intent);
			SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
			SystemValue.cur_fragment_index = -1;
			
//			setTabSelection(SystemValue.per_fragment_index);
			break;
		case R.id.home_me_iv:
			setTabSelection(SystemValue.HOME_ME);

			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(SystemValue.per_fragment_index!=SystemValue.cur_fragment_index){
			if(SystemValue.cur_fragment_index==-1){
				setTabSelection(SystemValue.per_fragment_index);
			}
			
		}
	}
	
	

	void clearSelected() {
		home_page_iv.setImageResource(R.drawable.home_page_icon);
		home_drawing_iv.setImageResource(R.drawable.home_drawing_icon);
		home_me_iv.setImageResource(R.drawable.home_me_icon);
	}

	private void setTabSelection(int index) {

		// Toast.makeText(this, "index:"+index, Toast.LENGTH_SHORT).show();
		SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
		SystemValue.cur_fragment_index = index;
		hideFragments();
		clearSelected();
		FragmentTransaction transaction = fMgr.beginTransaction();
		switch (index) {
		case SystemValue.HOME_PAGE:
			transaction.show(homePageFragment);
			curFragment = homePageFragment;
			home_page_iv.setImageResource(R.drawable.home_page_icon_s);
			break;

		case SystemValue.HOME_ME:
			transaction.show(myDrawingPageFragment);
			curFragment = myDrawingPageFragment;
			home_me_iv.setImageResource(R.drawable.home_me_icon_s);
			 
			break;
		}

		transaction.commitAllowingStateLoss();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		curFragment.onActivityResult(requestCode, resultCode, data);
//		setTabSelection(SystemValue.per_fragment_index);
	}

}
