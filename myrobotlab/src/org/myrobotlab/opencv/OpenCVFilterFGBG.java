/**
 *                    
 * @author greg (at) myrobotlab.org
 *  
 * This file is part of MyRobotLab (http://myrobotlab.org).
 *
 * MyRobotLab is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * MyRobotLab is distributed in the hope that it will be useful or fun,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * All libraries in thirdParty bundle are subject to their own license
 * requirements - please refer to http://myrobotlab.org/libraries for 
 * details.
 * 
 * Enjoy !
 * 
 * */

package org.myrobotlab.opencv;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.OpenCV;
import org.slf4j.Logger;

import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;

public class OpenCVFilterFGBG extends OpenCVFilter {

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(OpenCVFilterFGBG.class.getCanonicalName());

	CvMemStorage storage = null;
	BackgroundSubtractorMOG2 bg_model = null;
	boolean update_bg_model = true;
	IplImage fgmask, fgimg;

	public OpenCVFilterFGBG(VideoProcessor vp, String name, HashMap<String, IplImage> source,  String sourceKey)  {
		super(vp, name, source, sourceKey);
	}

	@Override
	public BufferedImage display(IplImage image) {

		return fgimg.getBufferedImage();
	}

	
	@Override
	public IplImage process(IplImage image, OpenCVData data) {

		if (fgimg == null) {
			fgimg = cvCreateImage(cvGetSize(image), 8, 3);
		}

		bg_model.apply(image, fgmask, update_bg_model ? -1 : 0);

		// cvCopy(src, dst)
		// fgimg = Scalar::all(0);
		// img.copyTo(fgimg, fgmask);

		IplImage bgimg = null;
		bg_model.getBackgroundImage(bgimg);

		/*
		 * imshow("image", img); imshow("foreground mask", fgmask);
		 * imshow("foreground image", fgimg);
		 */

		return fgimg;
	}

	@Override
	public void imageChanged(IplImage image) {
		// TODO Auto-generated method stub
		
	}

}