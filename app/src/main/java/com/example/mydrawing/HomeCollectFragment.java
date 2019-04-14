package com.example.mydrawing;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import util.PullToRefreshView;
import util.PullToRefreshView.OnFooterRefreshListener;
import util.PullToRefreshView.OnHeaderRefreshListener;
import util.RefreshReceiver.Refresh;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeCollectFragment extends Fragment implements
OnHeaderRefreshListener, OnFooterRefreshListener, Refresh{
	
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.me_layout, container, false);

		ViewUtils.inject(this, view);
		initView();
		return view;
	}
	
	void initView(){
		
	}


	@Override
	public void toRefresh() {

		
	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {

		
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {

		
	}

}
