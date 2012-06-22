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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.myrobotlab.framework.Service;

public class TestThrower extends Service {

	private static final long serialVersionUID = 1L;
	public final static Logger log = Logger.getLogger(TestThrower.class
			.getCanonicalName());
	public int cnt = 0;
	public int pulseLimit = 20;
	public int pitchCnt = 0;
	public ArrayList<Integer> catcher = new ArrayList<Integer>();
	public ArrayList<Integer> catchList = new ArrayList<Integer>();

	public ArrayList<RapidThrower> pitchers = new ArrayList<RapidThrower>();

	// TODO bury this in Service??
	public TestThrower(String n) {
		this(n, null);
	}

	public TestThrower(String n, String serviceDomain) {
		super(n, TestThrower.class.getCanonicalName(), serviceDomain);
	}

	@Override
	public void loadDefaultConfiguration() {
		cfg.set("throwInterval", 100);
		cfg.set("throwType", "throwInteger");
	}

	public Integer catchInteger(Integer count) {
		log.info("***THROWER CATCH*** catchInteger " + count);
		synchronized (catchList) {
			catchList.add(count);
			catchList.notify();
		}
		return count;
	}

	public void throwNothing() {
		log.info("throwNothing");
	}

	public Integer throwInteger(Integer count) {
		log.info("throwInteger " + count);
		return count;
	}

	public Integer highPitchInteger(Integer count) {
		++pitchCnt;
		log.info("highPitchInteger " + pitchCnt);
		return count;
	}

	public Integer lowPitchInteger(Integer count) {
		++pitchCnt;
		log.info("lowPitchInteger " + pitchCnt);
		return count;
	}

	public Integer noPitchInteger(Integer count) {
		++pitchCnt;
		log.info("noPitchInteger null ");
		return 0;
	}

	public class RapidThrower implements Runnable {
		Service myService = null;
		public boolean running = false;
		Integer count = new Integer(0);

		RapidThrower(Service myService) {
			this.myService = myService;
		}

		@Override
		public void run() {
			running = true;
			while (running) {
				int sleep = Integer.parseInt(myService.getCFG("throwInterval"));
				try {
					++count;
					invoke("throwInteger", count);
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logException(e);
					running = false;
				}
			}

		}
	}

	public void setNumberOfPitchers(Integer num) {
		if (pitchers.size() < num) {
			for (int i = 0; i < num; ++i) {
				RapidThrower pitcher = new RapidThrower(this);
				Thread t = new Thread(pitcher);
				t.start();
				pitchers.add(pitcher);
			}
		} else {
			for (int i = num; i >= num; --i) {
				RapidThrower pitcher = pitchers.get(i);
				pitcher.running = false;
				pitchers.remove(i);
			}
		}
	}

	@Override
	public String getToolTip() {
		return "<html>service for junit tests</html>";
	}
	
}
