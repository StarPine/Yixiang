package entity;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Contacts.Intents.Insert;

public class DrawingDBOp {
	
	DrawingDBHelper dbHelper;
	Context context;
	SQLiteDatabase db;
	
	public DrawingDBOp(Context context){
		this.context = context;
		//创建StuDBHelper对象   
		dbHelper = new DrawingDBHelper(context);  
	}
	
	public int insertDrawing(DrawingInfo drawingInfo){
		//得到一个可写的数据库  
		db =dbHelper.getWritableDatabase();  
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		 
		 
		//生成ContentValues对象 //key:列名，value:想插入的值   
		ContentValues cv = new ContentValues();  
		//往ContentValues对象存放数据，键-值对模式  
		cv.put("miboo_video_path", drawingInfo.getDrawingVideo());
		cv.put("miboo_video_img", drawingInfo.getVideoCover());
		cv.put("local_image_path", drawingInfo.getDrawingImg());  
		cv.put("create_date", sdf.format(drawingInfo.getCreateDate()));
		cv.put("content", drawingInfo.getDescription());  
		//调用insert方法，将数据插入数据库  
		db.insert("local_drawing", null, cv);  
		
		Cursor cursor = db.rawQuery("select last_insert_rowid() from local_drawing",null);            
		int strid = -1;      
		if(cursor.moveToFirst())    
		   strid = cursor.getInt(0); 
		
		//关闭数据库  
		db.close();  
		return strid;
	}
	
	public void deleteDrawing(int id){
		//得到一个可写的数据库  
		db =dbHelper.getWritableDatabase();  
		String whereClauses = "id=?";  
		String [] whereArgs = {String.valueOf(id)};  
		//调用delete方法，删除数据   
		db.delete("local_drawing", whereClauses, whereArgs);  
	}
	
//	public void updateDrawing(int id,String imgPath,String content){
//		//得到一个可写的数据库  
//		db =dbHelper.getWritableDatabase();  
//		ContentValues cv = new ContentValues(); 
//		if(imgPath!=null){ 
//			cv.put("local_image_path", imgPath); 			
//		}
//		if(content!=null&&!content.equals("")&&!content.equals("null")){
//			cv.put("content", content);  
//			
//		}
//		
//		//where 子句 "?"是占位符号，对应后面的"1",  
//		String whereClause="id=?";  
//		String [] whereArgs = {String.valueOf(id)};  
//		//参数1 是要更新的表名  
//		//参数2 是一个ContentValeus对象  
//		//参数3 是where子句  
//		db.update("local_drawing", cv, whereClause, whereArgs);  
//	}
	public void updateDrawingImgPath(int id,String imgPath){
		//得到一个可写的数据库  
		db =dbHelper.getWritableDatabase();  
		String sql = "UPDATE local_drawing set local_image_path = '"+imgPath+"' where id="+id+";";
		
		db.execSQL(sql);
	}
	public void updateDrawingContent(int id,String content){
		//得到一个可写的数据库  
		db =dbHelper.getWritableDatabase();  
		String sql = "UPDATE local_drawing set content = '"+content+"' where id="+id+";";
		
		db.execSQL(sql);
	}
//	
	public List<DrawingInfo> queryDrawings(){
		
		List<DrawingInfo> drawingInfos = new ArrayList<DrawingInfo>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		
		//得到一个可写的数据库  
		db =dbHelper.getReadableDatabase();  
		//参数1：表名   
		//参数2：要想显示的列   
		//参数3：where子句   
		//参数4：where子句对应的条件值   
		//参数5：分组方式   
		//参数6：having条件   
		//参数7：排序方式   
		Cursor cursor = db.query("local_drawing", new String[]{"id","miboo_video_path",
				"miboo_video_img","local_image_path","create_date","content"}, null, null, null, null, "create_date desc");
		while(cursor.moveToNext()){ 
			DrawingInfo drawingInfo = new DrawingInfo();
			drawingInfo.setDrawingId(cursor.getInt(cursor.getColumnIndex("id"))+"");
			drawingInfo.setDrawingVideo(cursor.getString(cursor.getColumnIndex("miboo_video_path")));
			drawingInfo.setVideoCover(cursor.getString(cursor.getColumnIndex("miboo_video_img")));
			drawingInfo.setDrawingImg(cursor.getString(cursor.getColumnIndex("local_image_path")));
			try {
				drawingInfo.setCreateDate(new Timestamp(sdf.parse(cursor.getString(cursor.getColumnIndex("create_date"))).getTime()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			drawingInfo.setDescription(cursor.getString(cursor.getColumnIndex("content")));
			
			drawingInfos.add(drawingInfo);
		}  
		//关闭数据库  
		db.close(); 
		return drawingInfos;
	}

}
