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
 * Enjoy.
 * 
 * */

package org.myrobotlab.service;

import java.awt.Dimension;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.myrobotlab.framework.Service;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;

public class FaceTracking extends Service {

	private static final long serialVersionUID = 1L;

	public final static Logger LOG = Logger.getLogger(FaceTracking.class.getCanonicalName());

	/*
	 *  TODO - dead zone - scan / search
	 */
	Servo tilt = new Servo("tilt");
	Servo pan = new Servo("pan");
	OpenCV camera = new OpenCV("camera");
	
	Arduino arduino = new Arduino("arduino");
	
	Logging logger = new Logging("logger");
	
	PID xpid = new PID();
	PID ypid = new PID();
	
	public FaceTracking(String n) {
		super(n, FaceTracking.class.getCanonicalName());
		
		tilt.startService();
		pan.startService();
		camera.startService();
		logger.startService();
		arduino.startService();
		//pan.attach(arduino.name, 13);

		camera.notify("publish", name, "input");
		camera.notify("sizeChange", name, "sizeChange", Dimension.class);
		//notify("pan", "logger", "log");
		//notify("tilt", "logger", "log");
		notify("pan", "pan", "move");
		notify("tilt", "tilt", "move");
		
		xpid.SetTunings(0.1f, 0f, 0.5f);
		xpid.Setpoint = 80;

		ypid.SetTunings(0.1f, 0f, 0.5f);
		ypid.Setpoint = 60;
		
	}
	
	@Override
	public void loadDefaultConfiguration() {
	}
	
	public void startTracking()
	{
	}
	
	public void stopTracking()
	{
	}

	// TODO - put cfg
	int width = 160;
	int height = 120;
	int centerX = width/2;
	int centerY = height/2;
	int errorX = 0;
	int errorY = 0;
	
	public void sizeChange(Dimension d)
	{
		width = d.width;
		height = d.height;
	}
	
	public class PID {
		
		/*working variables*/
		public long lastTime;
		public float Input, Output, Setpoint;
		public float errSum, lastErr;
		public float kp, ki, kd;
		
		void Compute()
		{
		   /*How long since we last calculated*/
		   long now = System.currentTimeMillis();
		   float timeChange = (float)(now - lastTime);
		 
		   /*Compute all the working error variables*/
		   float error = Setpoint - Input;
		   errSum += (error * timeChange);
		   float dErr = (error - lastErr) / timeChange;
		 
		   /*Compute PID Output*/
		   Output = kp * error + ki * errSum + kd * dErr;
		 
		   /*Remember some variables for next time*/
		   lastErr = error;
		   lastTime = now;
		}
		 
		void SetTunings(float Kp, float Ki, float Kd)
		{
		   kp = Kp;
		   ki = Ki;
		   kd = Kd;
		}	
	}
	
	public void input (CvPoint point)
	{
		// TODO - handle multiple resolutions
		// currently I am only going to do 160 x 120
		//errorX = (point.x() - centerX)/2 * -1;
		//errorY = (point.y() - centerY)/2 * -1;
		
		xpid.Input = point.x();
		xpid.Compute();
		
		ypid.Input = point.y();
		ypid.Compute();
		
		//LOG.error(xpid.Output);
		
		/*
		if (point.x() > centerX)
		{
			errorX = -2;
		} else {
			errorX = 2;			
		}

		if (point.y() > centerY)
		{
			errorY = -2;
		} else {
			errorY = 2;			
		}
		
		
		LOG.info(point);
		
		invoke("pan", errorX);
		invoke("tilt", errorY);
		*/
		/*
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		invoke("pan", (int)xpid.Output);
		invoke("tilt", (int)ypid.Output);
	}
	
	public Integer tilt (Integer position)
	{
		return position;
	}

	public Integer pan (Integer position)
	{
		return position;
	}
	
	public FaceTracking publishState()
	{
		return this;
	}

	// TODO - reflectively do it in Service? !?
	// No - the overhead of a Service warrants a data only proxy - so to
	// a single container class "ClockData data = new ClockData()" could allow
	// easy maintenance and extensibility - possibly even reflective sync if names are maintained   
	public FaceTracking setState(FaceTracking o)
	{
		return o;
	}
	
	public static void main(String[] args) throws ClassNotFoundException {
		org.apache.log4j.BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);
				
		
		FaceTracking ft = new FaceTracking("face tracker");
		ft.startService();

		ft.camera.addFilter("PyramidDown1", "PyramidDown");
		ft.camera.addFilter("PyramidDown2", "PyramidDown");
		//ft.camera.addFilter("PyramidDown2", "PyramidDown");
		ft.camera.capture();
		
		//ft.arduino.se - TODO setPort("/dev/ttyUSB0");
		
		//ft.tilt.attach(ft.arduino.name, 12);
		//ft.pan.attach(ft.arduino.name, 13);
		
		GUIService gui = new GUIService("gui");
		gui.startService();
		gui.display();

/*		
		RuntimeEnvironment.releaseAll();
		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			
			
		       fos = new FileOutputStream("test.backup");
		       out = new ObjectOutputStream(fos);
		       //out.writeObject(remote);
		       out.writeObject(ft);
		       //out.writeObject(clock);
		       //out.writeObject(gui);
		       out.close();
		      
			
		       FileInputStream fis = new FileInputStream("test.backup");
		       ObjectInputStream in = new ObjectInputStream(fis);
		       Logging log1 = (Logging)in.readObject();
		       Clock clock1 = (Clock)in.readObject();
		       GUIService gui1 = (GUIService)in.readObject();
		       in.close();
		       
		       RuntimeEnvironment.register(null,ft);
		       //RuntimeEnvironment.register(null,clock);
		       //RuntimeEnvironment.register(null,gui);
		       
		       log1.startService();
		       clock1.startService();
		       //clock.startClock();		       
		       gui1.startService();
		       gui1.display();
		    
		       
		} catch (Exception e)
		{
			LOG.error(e.getMessage());
			LOG.error(stackToString(e));
		}
		
		/*
		OpenCV camera = new OpenCV("camera");
		camera.startService();
		
		Logging log = new Logging("log");
		log.startService();
		*/
		//clock.notify("pulse", "log", "log", Integer.class);

	}
	
	@Override
	public String getToolTip() {
		return "used to generate pulses";
	}


}
