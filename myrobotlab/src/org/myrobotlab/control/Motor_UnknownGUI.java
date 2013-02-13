package org.myrobotlab.control;

import org.slf4j.Logger;
import org.myrobotlab.logging.LoggerFactory;


public class Motor_UnknownGUI extends MotorControllerPanel {

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(MotorControllerPanel.class.getCanonicalName());

	Object[] data = null;

	@Override
	public void setData(Object[] data) {
		log.warn("setData on an unknown MotorGUI Panel :P");
		this.data = data;
	}

	@Override
	void setAttached(boolean state) {
		// TODO Auto-generated method stub

	}

}
