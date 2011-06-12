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

package org.myrobotlab.service;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.myrobotlab.framework.Service;
import org.simpleframework.xml.Root;

@Root
/* Finite State Machine Service */
public class FSM extends Service {

	private static final long serialVersionUID = 1L;

	public final static Logger LOG = Logger.getLogger(FSM.class.getCanonicalName());

	
	HashMap<String, EventData> transistionStates = new HashMap<String, EventData>();
	
	// TODO - subsumption
	public class EventData
	{
		String name;
		String method;
		Object[] data;
		HashMap<String, Boolean> supressedStates;
	}
	
	public FSM(String n) {
		super(n, FSM.class.getCanonicalName());
	}
	
	public String inState(String newState)
	{
		if (transistionStates.containsKey(newState))
		{
			EventData ed = transistionStates.get(newState);
			send(ed.name, ed.method, ed.data);
		}
		return newState;
	}
	
	@Override
	public void loadDefaultConfiguration() {
		
	}
	
	@Override
	public String getToolTip() {
		return "used to generate pulses";
	}
	
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.WARN);
		
		Invoker invoker = new Invoker("invoker");
		invoker.startService();

		FSM fsm = new FSM("fsm");
		fsm.startService();
		
		GUIService gui = new GUIService("gui");
		gui.startService();
		gui.display();
	}


}
