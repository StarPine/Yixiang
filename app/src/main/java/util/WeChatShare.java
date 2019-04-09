package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;

import com.example.mydrawing.R;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class WeChatShare {
	
	public final static String APPID = "wx2d57f482e50f38db"; 
	IWXAPI apiIwxapi;
	Context context;
	
	public static final int WXSceneSession = SendMessageToWX.Req.WXSceneSession;
	public static final int WXSceneTimeline = SendMessageToWX.Req.WXSceneTimeline;
	
	public static boolean isWXAppInstalledAndSupported = true;

	public WeChatShare(Context context) {
		// TODO Auto-generated constructor stub
		apiIwxapi = WXAPIFactory.createWXAPI(context, APPID, true);
		apiIwxapi.registerApp(APPID);
		this.context = context;
		checkWC();
	}
	
	public void checkWC(){ 
		isWXAppInstalledAndSupported = apiIwxapi.isWXAppInstalled()&& apiIwxapi.isWXAppSupportAPI();
		
	}
	
	public void share(String drawingid,int share_type,Bitmap thumb,String description){
		
		String url = SystemValue.basic_url+"VideoInfoPage.jsp?drawingid='"+drawingid+"'";
		
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = url;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "艺享-人人都可以是艺术家";
		msg.description = description;
		
//		thumb = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap();
		
		thumb = ThumbnailUtils.extractThumbnail(thumb, 108, 108);
		msg.thumbData = ImageUtil.bmpToByteArray(thumb, true);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = share_type;
		apiIwxapi.sendReq(req);
	}
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}
