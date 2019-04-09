package util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.ImageColumns;

public class ImageUtil {

	/** 
	08.     * 旋转Bitmap 
	09.     * @param b 
	10.     * @param rotateDegree 
	11.     * @return 
	12.     */  
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){  
	    Matrix matrix = new Matrix();  
	    matrix.postRotate((float)rotateDegree);  
	    Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);  
	    return rotaBitmap;  
	}  
	
	public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
	{
		byte [] yuv = new byte[imageWidth*imageHeight*3/2];
		// Rotate the Y luma
		int i = 0;
		for(int x = 0;x < imageWidth;x++)
		{
			for(int y = imageHeight-1;y >= 0;y--)
			{
				yuv[i] = data[y*imageWidth+x];
				i++;
			}
		}
		// Rotate the U and V color components
		i = imageWidth*imageHeight*3/2-1;
		for(int x = imageWidth-1;x > 0;x=x-2)
		{
			for(int y = 0;y < imageHeight/2;y++)
			{
				yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
				i--;
				yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
				i--;
			}
		}
		return yuv;
	}
	

	public static void rotateYUV240SP(byte[] src,byte[] des,int width,int height)
	{
	   
		int wh = width * height;
		//旋转Y
		int k = 0;
		for(int i=0;i<width;i++) {
			for(int j=0;j<height;j++) 
			{
	              des[k] = src[width*j + i];			
			      k++;
			}
		}
		
		for(int i=0;i<width;i+=2) {
			for(int j=0;j<height/2;j++) 
			{	
	              des[k] = src[wh+ width*j + i];	
	              des[k+1]=src[wh + width*j + i+1];
			      k+=2;
			}
		}
		
		
	}
	
	/**

	 * 读取图片的旋转的角度

	 *

	 * @param path

	 *            图片绝对路径

	 * @return 图片的旋转角�?

	 */

	private int getBitmapDegree(String path) {

	    int degree = 0;

	    try {

	        // 从指定路径下读取图片，并获取其EXIF信息

	        ExifInterface exifInterface = new ExifInterface(path);

	        // 获取图片的旋转信�?

	        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,

	                ExifInterface.ORIENTATION_NORMAL);

	        switch (orientation) {

	        case ExifInterface.ORIENTATION_ROTATE_90:

	            degree = 90;

	            break;

	        case ExifInterface.ORIENTATION_ROTATE_180:

	            degree = 180;

	            break;

	        case ExifInterface.ORIENTATION_ROTATE_270:

	            degree = 270;

	            break;

	        }

	    } catch (IOException e) {

	        e.printStackTrace();

	    }

	    return degree;

	}
	
	public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree)
	{

		 Matrix m = new Matrix();
         m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

         try {

       Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

           return bm1;

          } catch (OutOfMemoryError ex) {
                 }

           return null;
    }

	public static Bitmap adjustPhotoRotation1(Bitmap bm, final int orientationDegree)
	{

	        Matrix m = new Matrix();
	        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
	        float targetX, targetY;
	        if (orientationDegree == 90) {
	        targetX = bm.getHeight();
	        targetY = 0;
	        } else {
	        targetX = bm.getHeight();
	        targetY = bm.getWidth();
	  }

	    final float[] values = new float[9];
	    m.getValues(values);

	    float x1 = values[Matrix.MTRANS_X];
	    float y1 = values[Matrix.MTRANS_Y];

	    m.postTranslate(targetX - x1, targetY - y1);

	    Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
	Paint paint = new Paint();
	    Canvas canvas = new Canvas(bm1);
	    canvas.drawBitmap(bm, m, paint);

	    return bm1;
	  }
	
	/**
	 * Try to return the absolute file path from the given Uri
	 *
	 * @param context
	 * @param uri
	 * @return the file path or null
	 */
	public static String getRealFilePath( final Context context, final Uri uri ) {
	    if ( null == uri ) return null;
	    final String scheme = uri.getScheme();
	    String data = null;
	    if ( scheme == null )
	        data = uri.getPath();
	    else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
	        data = uri.getPath();
	    } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
	        Cursor cursor = context.getContentResolver().query( uri, new String[] { ImageColumns.DATA }, null, null, null );
	        if ( null != cursor ) {
	            if ( cursor.moveToFirst() ) {
	                int index = cursor.getColumnIndex( ImageColumns.DATA );
	                if ( index > -1 ) {
	                    data = cursor.getString( index );
	                }
	            }
	            cursor.close();
	        }
	    }
	    return data;
	}
	
	/*
	ѹ��ͼƬ������ĳЩ�ֻ����սǶ���ת������
	*/
	public static String compressImage(Context context,String filePath,boolean isCover)  {

		//ͼƬ�������ռ�   ��λ��KB
        double maxSize =100.00;
        int q = 100;
        File file = new File(filePath);
        String fileName = file.getName();
        if(isCover){
        	fileName = "cover"+fileName;
        }
        fileName = System.currentTimeMillis()+fileName;
	        Bitmap bm = getSmallBitmap(filePath);

	        int degree = readPictureDegree(filePath);

	        if(degree!=0){//��ת��Ƭ�Ƕ�
	            bm=rotateBitmap(bm,degree);
	        }
	        try {
	        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+SystemValue.localImagePath;
	        new FileUtils().creatSDDir("MyDrawings");
	        new FileUtils().creatSDDir(SystemValue.localImagePath);
	        File imageDir = new File(path);
	        
	        File outputFile=new File(imageDir,fileName);
	        
	        FileOutputStream out = new FileOutputStream(outputFile);
	      //��bitmap���������У�����bitmap�Ĵ�С����ʵ�ʶ�ȡ��ԭ�ļ�Ҫ��  
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        do{
	        	baos.reset();
	        	bm.compress(CompressFormat.JPEG, q, baos);
	        	q -= 10;
	        }while (baos.toByteArray().length > 100 * 1024);

	       
				baos.writeTo(out);
				return outputFile.getPath();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return null;
	    }
	// ����·�����ͼƬ��ѹ��������bitmap������ʾ
	public static Bitmap getSmallBitmap(String filePath) {
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(filePath, options);

	        // Calculate inSampleSize
	    options.inSampleSize = SystemValue.calculateInSampleSize(options, 480, 800);

	        // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;

	    return BitmapFactory.decodeFile(filePath, options);
	    }
	public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
	public static Bitmap rotateBitmap(Bitmap bitmap,int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress); 
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }
	// ˮƽ�Գ�--ͼƬ����X��Գ�
		public static Bitmap testSymmetryX(Bitmap bitmap) {
			Matrix matrix = new Matrix();
			int height =bitmap.getHeight();
			float matrixValues[] = { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
			matrix.setValues(matrixValues);
			//����matrix.postTranslate(0, height);//��ʾ��ͼƬ���µ���
			matrix.postTranslate(0, height*2);
			Bitmap rotaBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);  
		    return rotaBitmap;  
		}
		
		// ��ֱ�Գ�--ͼƬ����Y���
		public static Bitmap testSymmetryY(Bitmap bitmap) {
			Matrix matrix = new Matrix();
			int width=bitmap.getWidth();
			float matrixValues[] = {-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
			matrix.setValues(matrixValues);
			//����matrix.postTranslate(width,0);//��ʾ��ͼƬ���ҵ���
			matrix.postTranslate(width*2, 0);
			Bitmap rotaBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);  
		    return rotaBitmap; 
		}
		
		public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			bmp.compress(CompressFormat.PNG, 100, output);
			int index = 90;
			while(output.size()>30000&&index>0){
				bmp.compress(CompressFormat.PNG, index, output);
				index -= 10;
			}
//			if (needRecycle) {
//				bmp.recycle();
//			}
			
			byte[] result = output.toByteArray();
			try {
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return result;
		}
}
