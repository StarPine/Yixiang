package com.liimou.artdrawing;

import util.SystemValue;
import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.widget.ImageView;

public class DetectEdges {
	
	ImageView imageView;
	

	private Bitmap mOriginalBitmap;
	private Bitmap mEdgeBitmap;

	private int canny1=10;
	private int canny2=100;
	
	private int imageWidth;
	private int imageHeight;
	private int[] pixels;
	private int[] edgePixels;
	
	public DetectEdges(ImageView imageView,String oriPath,int width, int height){
		this.imageView = imageView;
		mOriginalBitmap = SystemValue.lessenUriImage(oriPath, width, height);
		imageWidth = mOriginalBitmap.getWidth();  
		imageHeight =  mOriginalBitmap.getHeight();
		pixels = new int[imageWidth*imageHeight];
        mOriginalBitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);        
        edgePixels = new int[imageWidth*imageHeight];
        mEdgeBitmap=Bitmap.createBitmap(imageWidth, imageHeight, Config.ARGB_8888); 
        
        
        GetEdges();
	}
	
	public void changeCanny2(int canny2){
		this.canny2 = canny2;
		GetEdges();
	}
	
	private void GetEdges()
	{
		double thread1=canny1;
		if(thread1==0)thread1=1;
		double thread2=canny2;
		if(thread2==0)thread2=1;
		ArtDrawingLib.DetectEdges(imageWidth, imageHeight, pixels, edgePixels, thread1, thread2,0, 0, 255, 255,
				0, 0, 0, 0);
        mEdgeBitmap.setPixels(edgePixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);
        imageView.setImageBitmap(mEdgeBitmap);        
	}
}
