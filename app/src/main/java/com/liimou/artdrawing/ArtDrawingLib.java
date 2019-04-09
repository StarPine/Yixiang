package com.liimou.artdrawing;

public class ArtDrawingLib {

	static 
    {
        System.loadLibrary("ArtDrawing");
    }
	
	//
	// Native JNI 
	//
    
	//图像校正接口
	//参数设置
    public native static void SetAffineCorrection(float[] srcPts,float[] dstPts);    
    //图像校正
    public native static void AffineCorrectionImage(int width, int height, byte[] yuvData, int [] pixels);
    
    //边缘检测
    public native static void DetectEdges(int width, int height,int [] srcData, int [] dstData,double thread1,double thread2,
    		int e_b, int e_g, int e_r, int e_a,
    		int b_b, int b_g, int b_r, int b_a);
}
