package org.myrobotlab.framework;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.myrobotlab.logging.Logging;
import org.myrobotlab.runtime.ProcParser;

public class Platform implements Serializable {

	private static final long serialVersionUID = 1L;

	// VM Names
	public final static String VM_DALVIK = "dalvik";
	public final static String VM_HOTSPOT = "hotspot";

	// OS Names
	public final static String OS_LINUX = "linux";
	public final static String OS_MAC = "mac";
	public final static String OS_WINDOWS = "windows";

	public final static String UNKNOWN = "unknown";

	// arch names
	public final static String ARCH_X86 = "x86";
	public final static String ARCH_ARM = "arm";

	private String os;
	private String arch;
	private int bitness;
	private String vmName;
	private String mrlVersion;

	static Platform localInstance = getLocalInstance();

	public Platform() {
	}

	// -------------pass through begin -------------------
	public static Platform getLocalInstance() {
		if (localInstance == null) {
			Platform platform = new Platform();

			// os
			platform.os = System.getProperty("os.name").toLowerCase();
			if (platform.os.indexOf("win") >= 0) {
				platform.os = OS_WINDOWS;
			}

			platform.vmName = System.getProperty("java.vm.name").toLowerCase();

			// bitness
			String model = System.getProperty("sun.arch.data.model");
			if ("64".equals(model)) {
				platform.bitness = 64;
			} else {
				platform.bitness = 32;
			}

			// arch
			String arch = System.getProperty("os.arch").toLowerCase();
			if ("i386".equals(arch) || "i686".equals(arch) || "i586".equals(arch) || "amd64".equals(arch) || arch.startsWith("x86")) {
				platform.arch = "x86"; // don't care at the moment
			}

			if ("arm".equals(arch)) {
				// FIXME - procparser is unsafe and borked !!
				//Integer armv = ProcParser.getArmInstructionVersion();
				Integer armv = 6;
				if (armv != null) {
					platform.arch = String.format("armv%d", armv);
				}
				// arch = "armv6"; // assume its version 6 instruction set

			}

			if (platform.arch == null) {
				platform.arch = arch;
			}

			// REMOVED EVIL RECURSION - you can't call a file which has static
			// logging !!
			// logging calls -> platform calls a util class -> calls logging --
			// infinite loop
			// platform.mrlVersion = FileIO.getResourceFile("version.txt");
			StringBuffer sb = new StringBuffer();

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(Platform.class.getResourceAsStream("/resource/version.txt"), "UTF-8"));
				for (int c = br.read(); c != -1; c = br.read())
					sb.append((char) c);
			} catch (Exception e) {
				Logging.logException(e);
			}
			
			// TODO - ProcParser

			System.out.println(sb.toString());

			localInstance = platform;
		}

		return localInstance;
	}

	static public String getMRLVersion() {
		Platform p = getLocalInstance();
		return p.mrlVersion;
	}

	static public String getOS() {
		Platform p = getLocalInstance();
		return p.os;
	}

	static public String getVMName() {
		Platform p = getLocalInstance();
		return p.vmName;
	}

	static public boolean isDavlik() {
		Platform p = getLocalInstance();
		return VM_DALVIK.equals(p.vmName);
	}

	static public int getBitness() {
		Platform p = getLocalInstance();
		return p.bitness;
	}

	/**
	 * Returns only the bitness of the JRE hooked here in-case we need to
	 * normalize
	 * 
	 * @return hardware architecture
	 */
	static public String getArch() {
		return localInstance.arch;
	}

	static public boolean isMac() {
		return OS_MAC.equals(localInstance.arch);
	}

	static public boolean isLinux() {
		return OS_LINUX.equals(localInstance.arch);
	}

	static public boolean isWindows() {
		return OS_WINDOWS.equals(localInstance.arch);
	}

	static public String getClassPathSeperator() {
		if (isWindows()) {
			return ";";
		} else {
			return ":";
		}
	}

	static public boolean isArm() {
		return getArch().startsWith(ARCH_ARM);
	}

	static public boolean isX86() {
		return getArch().equals(ARCH_X86);
	}

	public String toString() {
		return String.format("%s.%d.%s", arch, bitness, os);
	}

}
