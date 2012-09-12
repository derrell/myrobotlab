package org.myrobotlab.service;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceWrapper;

/**
 * @author GroG
 * 
 * TODO - implement Servo interface or Servo Shield 
 * 		create a GUI for it
 * 
 *
 */
public class ACEduinoMotorShield extends Service {

	private static final long serialVersionUID = 1L;

	public final static Logger log = Logger.getLogger(ACEduinoMotorShield.class.getCanonicalName());
	
	// name of the Arduino
	String controllerName;

	public ACEduinoMotorShield(String n) {
		super(n, ACEduinoMotorShield.class.getCanonicalName());
	}
	
	@Override
	public void loadDefaultConfiguration() {
		
	}
	
	@Override
	public String getToolTip() {
		return "used as a general template";
	}
	
	
	public void setPosition(int servo, int position)
	{		
		send(controllerName, "sendCommand", Arduino.ACEDUINO_MOTOR_SHIELD_SERVO_SET_POSITION, servo, position);
	}
	
	public void setBounds(int servo, int minposition, int maxposition)
	{
		// FIXME
		// 3 parameter method have to be decomposed into 
		// 2 methods of 1 parameter until the Arduino
		// either accepts multiple parameters
		// not multi-threaded safe - ie get Servo then setServo Position
		send(controllerName, "sendCommand", Arduino.ACEDUINO_MOTOR_SHIELD_SERVO_SET_MIN_BOUNDS, servo, minposition);
		send(controllerName, "sendCommand",  Arduino.ACEDUINO_MOTOR_SHIELD_SERVO_SET_MAX_BOUNDS, servo, maxposition);
		
	}
	
	/*	
	public void getposition(int Servo)
	{
		return 1;
	}
	*/
	
	public void start()
	{
		send(controllerName, "sendCommand", Arduino.ACEDUINO_MOTOR_SHIELD_START, 0, 0);
	}
	
	public void stop()
	{
		send(controllerName, "sendCommand", Arduino.ACEDUINO_MOTOR_SHIELD_STOP, 0, 0);
	}
	

	public Object getControllerName() {
		return controllerName;
	}
	
	public boolean attach(String controllerName) {
		ServiceWrapper sw = Runtime.getService(controllerName);
		if (sw != null)
		{
			log.info(String.format("%s controller set to %s", getName(), controllerName));
			this.controllerName = controllerName;
			return true;
		} 
		
		log.error(String.format("controller %s does not exits", controllerName));
		return false;
	}

	
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.WARN);
		
		Arduino arduino = new Arduino("arduino");
		arduino.startService();
		
		ACEduinoMotorShield aceduinoShield = new ACEduinoMotorShield("aceduinoShield");
		aceduinoShield.startService();
		
		GUIService gui = new GUIService("gui");
		gui.startService();
		gui.display();
		
		/*
		GUIService gui = new GUIService("gui");
		gui.startService();
		gui.display();
		*/
	}


}