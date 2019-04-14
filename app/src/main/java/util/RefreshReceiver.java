package util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshReceiver extends BroadcastReceiver {
	Refresh refresh;

	@Override
	public void onReceive(Context context, Intent intent) {

		refresh.toRefresh();
	}

	public void setRefresh(Refresh refresh) {
		this.refresh = refresh;
	}

	public interface Refresh {
		public void toRefresh();
	}
}
