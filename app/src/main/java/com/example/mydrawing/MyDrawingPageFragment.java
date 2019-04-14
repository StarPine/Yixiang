package com.example.mydrawing;

import java.util.ArrayList;

import util.SystemValue;

import android.R.integer;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class MyDrawingPageFragment extends Fragment{
	
	final int LOCAL_TAB = 0,CLOUD_TAB = 1;
	int curr_tab = 0;
	
	@ViewInject(R.id.top_ibtn)
	ImageButton top_ibtn;
	
	@ViewInject(R.id.local_tab_iv)
	ImageView local_tab_iv;
	@ViewInject(R.id.cloud_tab_iv)
	ImageView cloud_tab_iv;
	
	@ViewInject(R.id.local_cloud_vp)
	ViewPager local_cloud_vp;
	

	private ViewPagerAdapter fPagerAdapter;
	private ArrayList<Fragment> pagerFargmentList;
	
	HomeLocalFragment homeLocalFragment;
	HomeMeFragment homeMeFragment;
	Fragment currFragment;
	
	boolean isEditStatu = false;
	
	Handler mHandler = new Handler(Looper.getMainLooper());
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.my_drawing_page_layout, container,false);
		ViewUtils.inject(this,view);
		
		
		initView();
		return view;
	}
	
	void initView(){
		pagerFargmentList = new ArrayList<Fragment>();
		homeLocalFragment = new HomeLocalFragment();
		homeMeFragment = new HomeMeFragment();		
		pagerFargmentList.add(homeLocalFragment);
		pagerFargmentList.add(homeMeFragment);
		
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {

				fPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), pagerFargmentList);
				local_cloud_vp.setAdapter(fPagerAdapter);
				setTab(LOCAL_TAB);
			}
		});
		
		
		
		local_cloud_vp.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(final int index) {

//				clearTab();
//				if(index==LOCAL_TAB){
//					local_tab_iv.setImageResource(R.drawable.local_tab_icon_s);
//				}
//				else {
//					cloud_tab_iv.setImageResource(R.drawable.cloud_tab_icon_s);
//				}
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {

						setTab(index);
					}
				});
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

				
			}
			
			@Override
			public void onPageScrollStateChanged(int index) {

//				
			}
		});
		
	}
	
	@OnClick({R.id.local_tab_iv,R.id.cloud_tab_iv,R.id.top_ibtn})
	private void onClick(View v){
		switch (v.getId()) {
		case R.id.local_tab_iv:
			setTab(LOCAL_TAB);
			break;
		case R.id.cloud_tab_iv:
			setTab(CLOUD_TAB);
			break;
		case R.id.top_ibtn:
			if(isEditStatu){
				isEditStatu = false;
				top_ibtn.setImageResource(R.drawable.edit_icon);
				
			}else {
				isEditStatu = true;
				top_ibtn.setImageResource(R.drawable.finish_edit_icon);
			}
			if(curr_tab==LOCAL_TAB){
				homeLocalFragment.isEditStatu = isEditStatu;
				homeLocalFragment.lvAdapter.notifyDataSetChanged();
			}
			else {				
				homeMeFragment.isEditStatu = isEditStatu;
				homeMeFragment.lvAdapter.notifyDataSetChanged();
			}
			break;

		default:
			break;
		}
	}
	
	void setTab(int index){
		
		switch (index) {
		case LOCAL_TAB:
			clearTab();
			curr_tab = index;
			local_cloud_vp.setCurrentItem(index);
			currFragment = homeLocalFragment;
			local_tab_iv.setImageResource(R.drawable.local_tab_icon_s);
			break;
		case CLOUD_TAB:
			if(SystemValue.curUserInfo==null||SystemValue.curUserInfo.getUserId()==null){
				Intent intent = new Intent(getActivity(),LoginActivity.class);
				intent.putExtra("isFromRegister", false);
				startActivity(intent);
				local_cloud_vp.setCurrentItem(LOCAL_TAB);
				
			}
			else {
				clearTab();
				curr_tab = index;
				local_cloud_vp.setCurrentItem(index);
				currFragment = homeMeFragment;
				cloud_tab_iv.setImageResource(R.drawable.cloud_tab_icon_s);
			}
			
			break;

		default:
			break;
		}
	}
	
	void clearTab(){
		local_tab_iv.setImageResource(R.drawable.local_tab_icon);
		cloud_tab_iv.setImageResource(R.drawable.cloud_tab_icon);
		isEditStatu = false;	
		homeLocalFragment.isEditStatu = isEditStatu;	
		homeMeFragment.isEditStatu = isEditStatu;
		if(homeLocalFragment.lvAdapter!=null){
			homeLocalFragment.lvAdapter.notifyDataSetChanged();
		}
		if(homeMeFragment.lvAdapter!=null){
			homeMeFragment.lvAdapter.notifyDataSetChanged();
		}
		
		top_ibtn.setImageResource(R.drawable.edit_icon);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden){
//		Toast.makeText(getActivity(), "MYDrawingfrag:hidden "+hidden+" pre:cur "+SystemValue.per_fragment_index+":"+SystemValue.cur_fragment_index, 300).show();
		super.onHiddenChanged(hidden);
		if(!hidden&&SystemValue.per_fragment_index!=SystemValue.cur_fragment_index){
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {

					homeLocalFragment.getAction();
					homeMeFragment.getAction(1, homeMeFragment.PAGE_SIZE, homeMeFragment.HEAD);
					
				}
			});
		}
		if(hidden){
			isEditStatu = false;	
			homeLocalFragment.isEditStatu = isEditStatu;	
			homeMeFragment.isEditStatu = isEditStatu;
			if(homeLocalFragment.lvAdapter!=null){
				homeLocalFragment.lvAdapter.notifyDataSetChanged();
			}
			if(homeMeFragment.lvAdapter!=null){
				homeMeFragment.lvAdapter.notifyDataSetChanged();
			}
			
			top_ibtn.setImageResource(R.drawable.edit_icon);
		}
		
	}
	
	class ViewPagerAdapter extends FragmentStatePagerAdapter {

		ArrayList<Fragment> list;

		public ViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {
			super(fm);
			// TODO Auto-generated constructor stub
			this.list = list;
		}

		@Override
		public Fragment getItem(int arg0) {

			return list.get(arg0);
		}

		@Override
		public int getCount() {

			return list.size();
		}
		@Override  
        public void destroyItem(View container, int position, Object object) { 
        }  
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		currFragment.onActivityResult(requestCode, resultCode, data);
	}
}
