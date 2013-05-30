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

import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.OpenCV;
import org.slf4j.Logger;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class OpenCVFilterFindContours extends OpenCVFilter {

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(OpenCVFilterFindContours.class.getCanonicalName());

	// FIXME - ok - use awt - use mrl pojo's if expected to serialize to Android
	// TODO - publish MRL objects - TODO make SerializableImage bundle with
	// Object map for data publishing
	// Stabalize with attributes of Object Map defined in OpenCV

	// BufferedImage frameBuffer = null;

	boolean useMinArea = true;

	public boolean publishBoundingBox = true;
	public boolean publishPolygon = false;

	boolean useMaxArea = false;
	int minArea = 150;
	int maxArea = -1;

	int thresholdValue = 0;
	boolean addPolygon = false;

	boolean isMinArea;
	boolean isMaxArea;

	transient IplImage grey = null;
	// transient IplImage display = null;
	transient IplImage dst = null;
	transient CvSeq contourPointer = new CvSeq();
	transient CvPoint drawPoint0 = new CvPoint(0, 0);
	transient CvPoint drawPoint1 = new CvPoint(0, 0);
	transient CvMemStorage cvStorage = null;

	public OpenCVFilterFindContours(String name) {
		super(name);
	}

	@Override
	public BufferedImage display(IplImage image, OpenCVData data) {

		BufferedImage frameBuffer = image.getBufferedImage();
		Graphics2D g = frameBuffer.createGraphics();
		g.setColor(Color.green);
		if (data != null) {
			ArrayList<Rectangle> boxes = data.getBoundingBoxArray();
			if (boxes != null) {
				for (Rectangle box : boxes) {
					g.drawRect(box.x, box.y, box.width, box.height);
				}
				g.drawString(String.format("cnt %d", boxes.size()), 10, 10);
			} else {
				g.drawString("null", 10, 10);
			}
		}
		return frameBuffer;
	}

	@Override
	public IplImage process(IplImage image, OpenCVData data) {

		// FIXME 3 channel search ???
		if (image.nChannels() == 3) {
			cvCvtColor(image, grey, CV_BGR2GRAY);
		} else {
			grey = image.clone();
		}

		cvFindContours(grey, cvStorage, contourPointer, Loader.sizeof(CvContour.class), 0, CV_CHAIN_APPROX_SIMPLE);
		CvSeq contour = contourPointer;

		while (contour != null && !contour.isNull()) {
			if (contour.elem_size() > 0) { // TODO - limit here for
											// "TOOOO MANY !!!!"

				// FIXME - mark centeroid
				CvRect rect = cvBoundingRect(contour, 0);

				minArea = 600;

				// find all the avg color of each polygon
				// cxcore.cvZero(polyMask);
				// cvDrawContours(polyMask, points, CvScalar.WHITE,
				// CvScalar.BLACK, -1, cxcore.CV_FILLED, CV_AA);

				// publish polygons
				// CvScalar avg = cxcore.cvAvg(image, polyMask); - good idea -
				// but not implemented

				// log.error("{}", rect);
				// size filter
				if (useMinArea) {
					isMinArea = (rect.width() * rect.height() > minArea) ? true : false;
					// log.error("{} {}", isMinArea, rect.width() *
					// rect.height());
				} else {
					useMinArea = true;
				}

				if (useMaxArea) {
					isMaxArea = (rect.width() * rect.height() < maxArea) ? true : false;
				} else {
					isMaxArea = true;
				}

				if (isMinArea && isMaxArea) {

					// data = new OpenCVData();

					if (publishBoundingBox) {

						Rectangle boundingBox = OpenCV.cvToAWT(rect);
						data.add(boundingBox);
					}

					if (publishPolygon) {
						CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), cvStorage, CV_POLY_APPROX_DP, cvContourPerimeter(contour) * 0.02, 1);
					}
					// Polygon polygon = new Polygon();
					// iterate through points - points.total() build awt Polygon
					// polygons.add(polygon);

					// polygons.add(new Polygon(rect, null,
					// (cvCheckContourConvexity(points) == 1) ? true : false,
					// cvPoint(rect.x() + rect.width() / 2, rect.y() +
					// rect.height() / 2), points.total()));

					/*
					 * WRONG FIXME - post processing should be done in Java on
					 * the buffered image !!!!S cvPutText(display, " " +
					 * points.total() + " " + (rect.x() + rect.width() / 2) +
					 * "," + (rect.y() + rect.height() / 2) + " " + rect.width()
					 * + "x" + rect.height() + "=" + (rect.width() *
					 * rect.height()) + " " + " " +
					 * cvCheckContourConvexity(points), cvPoint(rect.x() +
					 * rect.width() / 2, rect.y()), font, CvScalar.WHITE);
					 */
				}

				// cvPutText(display, " " + points.total() + " " + (rect.x() *
				// rect.height()) + "c" + (int)avg.getRed() + "," +
				// (int)avg.getGreen() + "," + (int)avg.getBlue(),
				// cvPoint(rect.x() + rect.width()/2,rect.y()), font,
				// CV_RGB(255, 0, 0));
				// cvPutText(display, " " + points.total() + " " + rect.x() +
				// "," +
				// rect.y() + " "+ (rect.x() * rect.height()/1000) + " " +
				// OpenCVFilterAverageColor.getColorName2(avg) + " " +
				// cv.cvCheckContourConvexity(points), cvPoint(rect.x() +
				// rect.width()/2,rect.y()), font, CvScalar.WHITE);

				/*
				 * drawPoint0.x(rect.x()); drawPoint0.y(rect.y());
				 * 
				 * drawPoint1.x(rect.x() + rect.width()); drawPoint1.y(rect.y()
				 * + rect.height());
				 * 
				 * cvDrawRect(display, drawPoint0, drawPoint1, CvScalar.RED, 1,
				 * 8, 0);
				 */

				// invoke("publish", (Object)rectangle);

			}
			contour = contour.h_next();
		}

		// if (publishOpenCVData) invoke("publishOpenCVData", data);

		// cvPutText(display, " " + cnt, cvPoint(10, 14), font, CvScalar.RED);
		// log.error("x");
		cvClearMemStorage(cvStorage);
		return image;
	}

	@Override
	public void imageChanged(IplImage image) {
		if (cvStorage == null) {
			cvStorage = cvCreateMemStorage(0);
		}

		grey = cvCreateImage(cvGetSize(image), 8, 1);
		// display = cvCreateImage(cvGetSize(frame), 8, 3);

	}

}
