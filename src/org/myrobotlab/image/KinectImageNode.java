package org.myrobotlab.image;

import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;

import java.io.Serializable;
import java.util.Date;

import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class KinectImageNode implements Serializable {
	private static final long serialVersionUID = 1L;
	public int ID = 0;
	public Date timestamp = new Date();

	// won't serialize - need type conversion
	public transient IplImage cvCameraFrame = null; 
	public transient IplImage cvMask = null; 
	//public transient IplImage cvGrayFrame = null; 
		
	public SerializableImage cameraFrame = null;
	public SerializableImage mask = null;
	//public Rectangle boudingBox = null;
	public CvRect boudingBox = null;
	public SerializableImage template = null;
	public String imageFilePath = null;

	public IplImage getTemplate()
	{
		cvSetImageROI(cvMask, boudingBox); // 615-8 = to remove right hand band
		IplImage template = cvMask.clone(); // 
		cvResetImageROI(cvMask);
		return template;
	}
	
	public void convertToSerializableTypes()
	{
		cameraFrame = new SerializableImage(cvCameraFrame.getBufferedImage());
		mask  = new SerializableImage(cvMask.getBufferedImage());
	}
	
	
}
