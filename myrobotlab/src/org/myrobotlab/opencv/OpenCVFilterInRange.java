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

import static com.googlecode.javacv.cpp.opencv_core.cvAnd;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageCOI;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.myrobotlab.logging.LoggerFactory;

import org.myrobotlab.service.OpenCV;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class OpenCVFilterInRange extends OpenCVFilter {
	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(OpenCVFilterInRange.class.getCanonicalName());
	// http://cgi.cse.unsw.edu.au/~cs4411/wiki/index.php?title=OpenCV_Guide#Calculating_color_histograms
	
	int useMask = 0;

	public final static int HUE_MASK = 1;
	public final static int VALUE_MASK = 2;
	public final static int SATURATION_MASK = 4;

	public boolean useHue = false;
	public float hueMinValue = 0.0f;
	public float hueMaxValue = 256.0f;

	public boolean useValue = false;
	public float valueMinValue = 0.0f;
	public float valueMaxValue = 256.0f;

	public boolean useSaturation = false;
	public float saturationMinValue = 0.0f;
	public float saturationMaxValue = 256.0f;
	
	transient IplImage hsv = null;

	transient IplImage hue = null;
	transient IplImage hueMask = null;

	transient IplImage value = null;
	transient IplImage valueMask = null;

	transient IplImage saturation = null;
	transient IplImage saturationMask = null;
	transient IplImage temp = null;

	transient IplImage mask = null;

	transient IplImage ret = null;

	transient BufferedImage frameBuffer = null;
	
	transient CvScalar hueMin = cvScalar(hueMinValue, 0.0, 0.0, 0.0);
	transient CvScalar hueMax = cvScalar(hueMaxValue, 0.0, 0.0, 0.0);
	transient CvScalar valueMin = cvScalar(valueMinValue, 0.0, 0.0, 0.0);
	transient CvScalar valueMax = cvScalar(valueMaxValue, 0.0, 0.0, 0.0);
	transient CvScalar saturationMin = cvScalar(saturationMinValue, 0.0, 0.0, 0.0);
	transient CvScalar saturationMax = cvScalar(saturationMaxValue, 0.0, 0.0, 0.0);

	public OpenCVFilterInRange(OpenCV service, String name) {
		super(service, name);
	}

	@Override
	public BufferedImage display(IplImage image, Object[] data) {

		return ret.getBufferedImage(); // TODO - ran out of memory here
	}

	public void samplePoint(Integer x, Integer y) {

		frameBuffer = hsv.getBufferedImage();
		int rgb = frameBuffer.getRGB(x, y);
		Color c = new Color(rgb);
		log.error(x + "," + y + " h " + c.getRed() + " s " + c.getGreen() + " v " + c.getBlue());
	}

	@Override
	public IplImage process(IplImage image) {

		ret = image;

		if (hsv == null) {
			hsv = cvCreateImage(cvGetSize(image), 8, 3);
			hue = cvCreateImage(cvGetSize(image), 8, 1);
			hueMask = cvCreateImage(cvGetSize(image), 8, 1);
			value = cvCreateImage(cvGetSize(image), 8, 1);
			valueMask = cvCreateImage(cvGetSize(image), 8, 1);
			saturation = cvCreateImage(cvGetSize(image), 8, 1);
			saturationMask = cvCreateImage(cvGetSize(image), 8, 1);
			temp = cvCreateImage(cvGetSize(image), 8, 1);
			mask = cvCreateImage(cvGetSize(image), 8, 1);
		}

		// load up desired mask case
		useMask = useSaturation ? 1 : 0;
		useMask = useMask << 1;
		useMask = useMask | (useValue ? 1 : 0);
		useMask = useMask << 1;
		useMask = useMask | (useHue ? 1 : 0);

		if (image == null) {
			log.error("image is null");
		}

		// convert to more stable HSV
		// cvCvtColor(image, hsv, CV_RGB2HSV); // # 41
		// #define CV_BGR2HSV 40 - not defined in javacv
		// cvResetImageCOI(image);// added reset - still get Input COI is not
		// supported
		cvSetImageCOI(hsv, 0); // added reset - still get Input COI is not
								// supported
		cvCvtColor(image, hsv, CV_BGR2HSV);

		if ((useMask & HUE_MASK) == 1) {
			// copy out hue
			cvSetImageCOI(hsv, 1);
			cvCopy(hsv, hue);

			// cfg values if changed
			if (hueMin.magnitude() != hueMinValue || hueMax.magnitude() != hueMaxValue) {
				hueMin = cvScalar(hueMinValue, 0.0, 0.0, 0.0);
				hueMax = cvScalar(hueMaxValue, 0.0, 0.0, 0.0);
			}

			// create hue mask
			cvInRangeS(hue, hueMin, hueMax, hueMask);
		}

		if ((useMask & VALUE_MASK) == 2) {
			// copy out value
			cvSetImageCOI(hsv, 3);
			cvCopy(hsv, value);

			// look for changed config - update if changed
			if (valueMin.magnitude() != valueMinValue || valueMax.magnitude() != valueMaxValue) {
				valueMin = cvScalar(valueMinValue, 0.0, 0.0, 0.0);
				valueMax = cvScalar(valueMaxValue, 0.0, 0.0, 0.0);
			}

			// create value mask
			cvInRangeS(value, valueMin, valueMax, valueMask);
		}

		if ((useMask & SATURATION_MASK) == 4) {
			// copy out saturation
			cvSetImageCOI(hsv, 2);
			cvCopy(hsv, saturation);

			// look for changed config - update if changed
			if (saturationMin.magnitude() != saturationMinValue || saturationMax.magnitude() != saturationMaxValue) {
				saturationMin = cvScalar(saturationMinValue, 0.0, 0.0, 0.0);
				saturationMax = cvScalar(saturationMaxValue, 0.0, 0.0, 0.0);
			}

			// create saturation mask
			cvInRangeS(saturation, saturationMin, saturationMax, saturationMask);
		}

		switch (useMask) {
		case 0: // !hue !value !sat
			ret = image;
			break;

		case 1: // hue !value !sat
			ret = hueMask;
			break;

		case 2: // !hue value !sat
			ret = valueMask;
			break;

		case 3: // hue value !sat
			cvAnd(hueMask, valueMask, mask, null);
			ret = mask;
			break;

		case 4: // !hue !value sat
			ret = saturationMask;
			break;

		case 5: // hue !value sat
			cvAnd(hueMask, saturationMask, mask, null);
			// cvAnd(saturationMask, hueMask, mask, null);
			ret = mask;
			break;

		case 6: // !hue value sat
			cvAnd(valueMask, saturationMask, mask, null);
			ret = mask;
			break;

		case 7: // hue value sat
			cvAnd(hueMask, valueMask, temp, null);
			cvAnd(temp, saturationMask, mask, null); // ??
			ret = mask;
			break;

		default:
			log.error("unknown useMask " + useMask);
			break;
		}

		return ret;

	}

	@Override
	public void imageChanged(IplImage frame) {
		// TODO Auto-generated method stub
		
	}

}
