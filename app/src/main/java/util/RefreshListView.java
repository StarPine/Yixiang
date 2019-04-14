package util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydrawing.R;

public class RefreshListView extends ListView implements OnScrollListener {
	
	 static final int STATE_PULL_REFRESH = 0;// 下拉刷新
	 static final int STATE_RELEASE_REFRESH = 1;// 松开刷新
	 static final int STATE_REFRESHING = 2;// 正在刷新
	
	int curr_state = STATE_PULL_REFRESH;//当前状态 
	
	View headerView,footerView;
	LayoutInflater inflater;
	
	ImageView headerTip;
	ImageView headerArrow;
	ProgressBar headerBar;
	int headerHeight,footerHeight;
	
	 RotateAnimation animUp;
	 RotateAnimation animDown;
	 int startY = -1;//滑动起点的y坐标
	 int endY;
	 
	 boolean isLoadingMore;
	 OnRefreashListener listener;

	public RefreshListView(Context context) {
		super(context);
		inflater = LayoutInflater.from(context);
		initView();
		// TODO Auto-generated constructor stub
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
		initView();

	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflater = LayoutInflater.from(context);
		initView();

	}
	
	/**
	 * 初始化头脚布局
	 */
	void initView(){
		initHeaderView();
		initFooterView();
		this.setOnScrollListener(this);
	}
	
	void initHeaderView(){
		headerView =inflate(getContext(), R.layout.header_lv_layout, null);
		this.addHeaderView(headerView);
		
		headerTip = (ImageView) headerView.findViewById(R.id.header_tip_tv);
		headerArrow = (ImageView) headerView.findViewById(R.id.header_arrow_iv);
		headerBar = (ProgressBar) headerView.findViewById(R.id.header_pb);
		
		headerView.measure(0, 0);//先测量再拿到它的高度
		headerHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerHeight*2, 0, 0);
		
		initArrowAnim();
	}
	
	void initFooterView(){
		footerView = inflate(getContext(), R.layout.footer_lv_layout, null);
		this.addFooterView(footerView);
		footerView.measure(0, 0);
		footerHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerHeight*2, 0, 0);//默认隐藏脚布局
	}
	
	void initArrowAnim(){
		//初始化箭头动画
		animUp = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animUp.setDuration(500);
		animUp.setFillAfter(true);//保持状态
		
		//箭头向下动画
		animDown = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animDown.setDuration(500);
		animDown.setFillAfter(true);
	}
	

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {


	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

		if(scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_IDLE){
			if(getLastVisiblePosition() == getCount() - 1 && !isLoadingMore){//滑到最后了
				footerView.setPadding(0, 0, 0, 0);
				setSelection(getCount() - 1);//改变ListView的显示位置
				isLoadingMore = true;
				if(listener != null){
					listener.onLoadMore();
				}
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startY = (int)event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			if(startY == -1){
				//有时候不会响应MotionEvent.ACTION_DOWN事件，这里重新获取startY坐标
				startY = (int)event.getRawY();
			}
			//当正在刷新时，跳出循环，不再执行以下步骤
			if(curr_state == STATE_REFRESHING){
				break;
			}
			
			endY = (int) event.getRawY();
			int dy = endY - startY;//计算手指滑动距离
			if(dy > 0 && getFirstVisiblePosition() == 0){
				//只有下拉且当前是第一个item时才允许下拉刷新
				int padding = dy - headerHeight;
				headerView.setPadding(0, padding, 0, 0);
				
				if(padding > 0 && curr_state != STATE_RELEASE_REFRESH){
					curr_state = STATE_RELEASE_REFRESH;
					refreshState();
				}
				else if(padding < 0 && curr_state != STATE_PULL_REFRESH){
					//改为下拉刷新状态
					curr_state = STATE_PULL_REFRESH;
					refreshState();
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			startY = -1;//手指抬起重置
			//当状态是松开刷新时抬起了手指，正在刷新
			if(curr_state == STATE_RELEASE_REFRESH){
				curr_state = STATE_REFRESHING;
				headerView.setPadding(0, 0, 0, 0);
				refreshState();
			}
			else if (curr_state == STATE_PULL_REFRESH) {
				headerView.setPadding(0, -headerHeight, 0, 0);
				
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	void refreshState(){
		switch (curr_state) {
		case STATE_PULL_REFRESH:
			headerTip.setImageResource(R.drawable.refresh_pre_tv_icon);
			headerArrow.setVisibility(View.VISIBLE);
			headerBar.setVisibility(View.INVISIBLE);
			headerArrow.startAnimation(animDown);
			break;
		case STATE_RELEASE_REFRESH:
			headerTip.setImageResource(R.drawable.refresh_tv_icon);
			headerArrow.setVisibility(View.VISIBLE);
			headerBar.setVisibility(View.INVISIBLE);
			headerArrow.startAnimation(animUp);
			break;
		case STATE_REFRESHING:
			headerTip.setImageResource(R.drawable.refresh_aft_tv_icon);
			headerArrow.clearAnimation();//必须先清除动画才能隐藏
			headerArrow.setVisibility(View.INVISIBLE);
			headerBar.setVisibility(View.VISIBLE);
			if(listener != null){
				listener.onRefreash();
			}
			break;

		default:
			break;
		}
	}
	
	/*
	 * 收起下拉刷新控件
	 */
	public void onRefreadshComplete(){
		if(isLoadingMore){
			footerView.setPadding(0, -footerHeight*2, 0, 0);
			isLoadingMore = false;
		}
		else {
			curr_state = STATE_PULL_REFRESH;

			headerTip.setImageResource(R.drawable.refresh_pre_tv_icon);
			headerArrow.setVisibility(View.VISIBLE);
			headerBar.setVisibility(View.INVISIBLE);
			
			headerView.setPadding(0, -headerHeight*2, 0, 0);//隐藏
		}
	}
	
	public void noFooterView(){
		footerView.setPadding(0, -footerHeight*2, 0, 0);
	}
	
	public void setOnRefreashListener(OnRefreashListener listener){
		this.listener = listener;
	}
	
	public interface OnRefreashListener {
        void onRefreash();

        void onLoadMore();
    }

}
