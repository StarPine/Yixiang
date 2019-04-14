package entity;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DrawingDBHelper extends SQLiteOpenHelper{
	
	private static final String TAG = "DrawingSQLite"; 
	private static final String DATABASENAME = "localdrawing.db";  
    private static final int DATABASE_VERSION = 1;  
	
	public DrawingDBHelper(Context context) {
		super(context, DATABASENAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	// 当第一次创建数据库的时候，调用该方法  
	@Override
	public void onCreate(SQLiteDatabase db) {

		String sql = "CREATE TABLE IF NOT EXISTS local_drawing(id INTEGER PRIMARY KEY AUTOINCREMENT,local_video_path varchar(100),local_video_img varchar(100),local_image_path varchar(100), create_date varchar(45), content varchar(100))";  
		//输出创建数据库的日志信息  
		Log.i(TAG, "create Database------------->");  
		//execSQL函数用于执行SQL语句  
		db.execSQL(sql); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

		
	}

	

}
