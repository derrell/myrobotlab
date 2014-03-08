package org.myrobotlab.framework;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Status extends Exception {

	private static final long serialVersionUID = 1L;

	public final static String DEBUG = "debug";
	public final static String INFO = "info";
	public final static String WARN = "warn";
	public final static String ERROR = "error";

	public String name;
	public String level;
	public String key;
	public String detail;

	public Status(String name, String level, String key, String detail) {
		this.name = name;
		this.level = level;
		this.key = key;
		this.detail = detail;
	}
	
	public Status(Status s){
		this.name = s.name;
		this.level  = s.level;
		this.key = s.key;
		this.detail = s.detail;
	}

	public Status(String detail) {
		this.level = ERROR;
		this.detail = detail;
	}

	public Status(Exception e) {
		super(e);
	}

	public final String stackToString() {
		StringWriter sw;
		sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		this.printStackTrace(pw);
		return "------\r\n" + sw.toString() + "------\r\n";
	}

	public boolean isDebug() {
		return DEBUG.equals(level);
	}

	public boolean isInfo() {
		return INFO.equals(level);
	}

	public boolean isWarn() {
		return WARN.equals(level);
	}

	public boolean isError() {
		return ERROR.equals(level);
	}

	public static void throwError(String msg) throws Status {
		throw new Status(msg);
	}

	public static Status error(Exception e) {
		Status s = new Status(e);
		s.level = ERROR;
		return s;
	}

	public static Status info(String msg) {
		Status s = new Status(msg);
		s.level = INFO;
		return s;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		if (name != null){
			sb.append(name);
			sb.append(" ");
		}
		if (level != null){
			sb.append(level);
			sb.append(" ");
		}
		if (key != null){
			sb.append(key);
			sb.append(" ");
		}
		if (detail != null){
			sb.append(detail);
		}
		
		return sb.toString();
	}
}
