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

package org.myrobotlab.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.myrobotlab.control.widget.MemoryWidget;
import org.myrobotlab.control.widget.NodeGUI;
import org.myrobotlab.image.SerializableImage;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.memory.Node;
import org.myrobotlab.opencv.OpenCVData;
import org.myrobotlab.service.MouthControl;
import org.myrobotlab.service.interfaces.GUI;
import org.myrobotlab.service.interfaces.MemoryDisplay;
import org.slf4j.Logger;

public class MouthControlGUI extends ServiceGUI implements ActionListener {

	static final long serialVersionUID = 1L;
	public final static Logger log = LoggerFactory.getLogger(MouthControlGUI.class.getCanonicalName());

	public MouthControlGUI(final String boundServiceName, final GUI myService) {
		super(boundServiceName, myService);
	}

	public void init() {
		display.add(new JLabel("no gui"));
	}

	public void getState(MouthControl mouth) {
	
	}

	@Override
	public void attachGUI() {

	}

	@Override
	public void detachGUI() {
	
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}
	

}
