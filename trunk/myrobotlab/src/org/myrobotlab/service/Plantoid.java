package org.myrobotlab.service;

import org.myrobotlab.framework.Service;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.LoggingFactory;
import org.slf4j.Logger;

public class Plantoid extends Service {

	// tracking ? pan tilt ?

	private static final long serialVersionUID = 1L;

	// peer services
	/**
	 * Leg servos and pan tilt kit
	 */
	transient public Servo leg1, leg2, leg3, leg4, pan, tilt;
	transient public Arduino arduino;
	// video0 = rgbpilot cam
	// video1 = pink plant static NIR - 
	// Imaged from there should be taken and put through the infrapix, then opencv Nope static camera view of the braaaains
	// video2 = NIR pilot cam
	
	/**
	 * future services
	 */
	transient public OpenCV IRCamera, camera;
	transient public Keyboard keyboard;
	transient public WebGUI webgui;
	transient public JFugue jfugue;
	transient public Speech speech;
	transient public AudioFile audioFile;
		
	// system specific data
	/**
	 * default port of the Arduino mega
	 */
	public String port = "/dev/ttyACM0";
	
	/**
	 * default pin of legs leg1 through leg4
	 */
	public int leg1Pin = 2;
	public int leg2Pin = 3;
	public int leg3Pin = 4;
	public int leg4Pin = 5;
	
	/**
	 * default pins for pan tilt kit
	 */
	public int panPin = 6;
	public int tiltPin = 7;
	
	/**
	 * analog read pins
	 */
	public int soildMoisture = 0;
	public int tempHumidity = 2;
	public int leftLight = 4;
	public int rightLight = 6;
	public int airQuality = 10;

	private int sampleRate = 8000;

	public final static Logger log = LoggerFactory.getLogger(Plantoid.class.getCanonicalName());

	/**
	 * Plantoid Service - this service controls all peer services.  It is a OrbousMundus Genus and
	 * flagship of the BEPSL Republic.
	 * 
	 *  Its mission is to go forth explore and be one with nature in alien environments while reporting
	 *  telemetry back to BEPSL control
	 * @param n
	 */
	public Plantoid(String n) {
		super(n, Plantoid.class.getCanonicalName());

		reserve("arduino", "Arduino", "Plantoid has one arduino controlling all the servos and sensors");
		reserve("leg1", "Servo", "one of the driving servos");
		reserve("leg2", "Servo", "one of the driving servos");
		reserve("leg3", "Servo", "one of the driving servos");
		reserve("leg4", "Servo", "one of the driving servos");

		reserve("pan", "Servo", "pan servo");
		reserve("tilt", "Servo", "tilt servo");
		
		reserve("keyboard", "Keyboard", "for keyboard control");

		reserve("webgui", "WebGUI", "plantoid gui");

		// reserve("pilotcam","OpenCV", "One of the cameras");
		// reserve("ircamera","OpenCV", "One of the cameras");
		
		// TODO - reserve future services

	}

	@Override
	public String getDescription() {
		return "the plantoid service";
	}

	@Override
	public void startService() {
		super.startService();

		try {
			webgui = (WebGUI) startReserved("webgui");
			arduino = (Arduino) startReserved("arduino");
			arduino.connect(port);

			leg1 = (Servo) startReserved("leg1");
			leg2 = (Servo) startReserved("leg2");
			leg3 = (Servo) startReserved("leg3");
			leg4 = (Servo) startReserved("leg4");

			
			pan = (Servo) startReserved("pan");
			tilt = (Servo) startReserved("tilt");

			startPolling();
			attachServos();
			detachLegs(); // at the moment detach legs
			stop();
			
		} catch (Exception e) {
			error(e);
		}
	}

	/**
	 * Connects the plantoid server's Arduino service to the appropriate serial port.
	 * This is automatically called when the Plantoid service starts.
	 * Default is /dev/ttyACM0
	 * @param port
	 * @return
	 * true if connected false otherwise
	 */
	public boolean connect(String port) {
		this.port = port;
		arduino = (Arduino) startReserved("arduino");
		arduino.connect(port);
		arduino.broadcastState();
		return true;
	}
	
	/**
	 * This begins polling of the various analog senesors of the 
	 * Plantoid server.  It is automatically started when the Plantoid
	 * service is started. Soil, temperature, left and right light sensors
	 * and air quality are all polled
	 */
	public void startPolling()
	{
		arduino.setSampleRate(sampleRate);
		arduino.analogReadPollingStart(soildMoisture);
		arduino.analogReadPollingStart(tempHumidity);
		arduino.analogReadPollingStart(leftLight);
		arduino.analogReadPollingStart(rightLight);
		arduino.analogReadPollingStart(airQuality);
	}
	
	/**
	 * shut down polling of analog sensors
	 */
	public void stopPolling(){
		arduino.analogReadPollingStop(soildMoisture);
		arduino.analogReadPollingStop(tempHumidity);
		arduino.analogReadPollingStop(leftLight);
		arduino.analogReadPollingStop(rightLight);
		arduino.analogReadPollingStop(airQuality);
	}

	// ------- servos begin -----------
	
	/**
	 * Spin spins the plantoid server
	 * @param power
	 * power range is from 
	 *         -90 (spin full clock wise)
	 *         0 (stop)
	 *         90 (spin full counter clockwise)
	 */
	public void spin(Integer power) {
		int s = 90 - power;
		leg2.moveTo(s);
		leg3.moveTo(s);
		leg4.moveTo(s);
		leg1.moveTo(s);
	}
	
	/**
	 * moveY moves the plantoid on the Y axis
	 * @param power
	 *         -90 - down the Y axis
	 *         0 (stop)
	 *         90 - up the Y axis
	 */
	public void moveY(Integer power){
		int s = 90 - power;
		leg2.moveTo(90);
		leg3.moveTo(s);
		leg4.moveTo(90);
		leg1.moveTo(s);
	}

	/**
	 * moveX moves the plantoid on the X axis
	 * @param power
	 *         -90 - down the X axis
	 *         0 (stop)
	 *         90 - up the X axis
	 */	public void moveX(Integer power){
		int s = 90 - power;
		leg1.moveTo(s);
		leg2.moveTo(90);
		leg3.moveTo(s);
		leg4.moveTo(90);
	}
	
	/**
	 * square dance "should" make a square by moving the plantoid
	 * up and down the X and Y axis's
	 * @param power
	 * power applied to legs
	 * @param time
	 * run time on each axis
	 */
	public void squareDance(Integer power, Integer time)
	{
		int s = 90 - power;
		moveX(s);
		sleep(time);
		moveY(s);
		sleep(time);
		moveX(-s);
		sleep(time);
		moveX(-s);
		sleep(time);
		stop();
	}
	
	/**
	 * stops all legs
	 */
	public void stop() {
		leg1.moveTo(90);
		leg2.moveTo(90);
		leg3.moveTo(90);
		leg4.moveTo(90);
	}

	/**
	 * attaches all the servos 
	 * legs and pan tilt kit
	 */
	public void attachServos()
	{
		attachPanTilt();
		attachLegs();
	}
	
	/**
	 * detaches all servos
	 */
	public void detachServos()
	{
		detachPanTilt();
		detachLegs();
	}
	
	/**
	 * attaches only the pan tilt
	 */
	public void attachPanTilt() {
		arduino.servoAttach(pan.getName(), panPin);
		arduino.servoAttach(tilt.getName(), tiltPin);
	}	
	
	/**
	 * detaches the pan tilt only
	 */
	public void detachPanTilt() {
		pan.detach();
		tilt.detach();
	}	
	
	/**
	 * attaches the legs only
	 */
	public void attachLegs() {
		arduino.servoAttach(leg1.getName(), leg1Pin);
		arduino.servoAttach(leg2.getName(), leg2Pin);
		arduino.servoAttach(leg3.getName(), leg3Pin);
		arduino.servoAttach(leg4.getName(), leg4Pin);
	}
	
	/**
	 * detaches the legs only
	 */
	public void detachLegs() {
		leg1.detach();
		leg2.detach();
		leg3.detach();
		leg4.detach();
	}
	// ------- servos begin -----------
	
	/**
	 * shuts down the planoid server
	 */
	public void shutdown() {
		detachServos();
		Runtime.releaseAll();
	}
	
	/**
	 * current uptime of the plantoid server
	 * this represents the longevity and quality
	 * of our plantoid craft
	 * LONG LIVE BEPSL !!
	 * @return
	 * the uptime
	 */
	public String getUptime()
	{
		return Runtime.getUptime();
	}

	public static void main(String[] args) {
		LoggingFactory.getInstance().configure();
		LoggingFactory.getInstance().setLevel(Level.DEBUG);

		Plantoid plantoid = (Plantoid) Runtime.create("plantoid", "Plantoid");
		plantoid.connect("COM9");
		plantoid.startService();
		Runtime.createAndStart("python", "Python");
		Runtime.createAndStart("webgui", "WebGUI");
		/*
		 * GUIService gui = new GUIService("gui"); gui.startService();
		 * gui.display();
		 */
	}

}
