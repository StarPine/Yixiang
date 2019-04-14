package util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.mydrawing.DrawingActivity;

public class SelectPhotoUtil {
	
	Activity activity;
	Uri photoUri;
	
	public SelectPhotoUtil(Activity mActivity){
		this.activity = mActivity; 
		
	}
	
	@SuppressLint("NewApi")
	public void selectPhoto(final Uri photoUri){
		this.photoUri = photoUri;
		new AlertDialog.Builder(activity,AlertDialog.THEME_HOLO_DARK)
		.setTitle("信息")
		.setMessage("请选择图片获取方式")
		.setPositiveButton("拍照",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(
							DialogInterface arg0, int arg1) {

						takePic(photoUri);
					}
				})
		.setNegativeButton("相册",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(
							DialogInterface arg0, int arg1) {

						getPic();
					}
				}).create().show();
	}

	public void takePic(Uri photouri) {// 拍照
		String SDState = Environment.getExternalStorageState();
		if (SDState.equals(Environment.MEDIA_MOUNTED)) {

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// "android.media.action.IMAGE_CAPTURE"

//			ContentValues values = new ContentValues();
//			photoUri = activity.getContentResolver().insert(
//					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, photouri);
			SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
			
			activity.startActivityForResult(intent, SystemValue.TAKE_PICTURE);
		} else {
			Toast.makeText(activity, "sd卡不可用", Toast.LENGTH_LONG).show();
		}
	}

	public  void getPic() {
		SystemValue.per_fragment_index = SystemValue.cur_fragment_index;
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		activity.startActivityForResult(intent, SystemValue.GET_PICTURE);
	}
}
