package org.myrobotlab.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.myrobotlab.framework.Service;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.serial.SerialDevice;
import org.myrobotlab.serial.SerialDeviceEvent;
import org.myrobotlab.serial.SerialDeviceEventListener;
import org.myrobotlab.serial.SerialDeviceFactory;
import org.myrobotlab.serial.SerialDeviceService;
import org.slf4j.Logger;

public class Serial extends Service implements SerialDeviceService, SerialDeviceEventListener {

	private static final long serialVersionUID = 1L;

	public final static Logger log = LoggerFactory.getLogger(Serial.class.getCanonicalName());

	private transient SerialDevice serialDevice;
	public ArrayList<String> portNames = new ArrayList<String>();

	int BUFFER_SIZE = 8192;
	byte[] buffer = new byte[BUFFER_SIZE];
	BlockingQueue<Byte> blockingData = new LinkedBlockingQueue<Byte>();

	private int recievedByteCount = 0;

	boolean publish = true;
	boolean blocking = true;

	private boolean connected = false;
	private String portName = "";

	public static final int PUBLISH_BYTE = 0;
	public static final int PUBLISH_LONG = 1;
	public static final int PUBLISH_INT = 2;
	public static final int PUBLISH_CHAR = 3;
	public static final int PUBLISH_BYTE_ARRAY = 3;
	public static final int PUBLISH_STRING = 4;
	public static final int PUBLISH_MESSAGE = 5;

	public boolean useFixedWidth = false;
	public int msgWidth = 10;
	public char delimeter = '\n';

	public int publishType = PUBLISH_BYTE;

	// Arduino micro-controller specific at the moment
	public int BYTE_SIZE_LONG = 4;
	public int BYTE_SIZE_INT = 2;

	public Serial(String n) {
		super(n, Serial.class.getCanonicalName());
	}

	public void capacity(int size) {
		buffer = new byte[size];
	}

	@Override
	public String getToolTip() {
		return "used as a general template";
	}

	public String getPortName() {
		return portName;
	}

	public void publishType(Integer type) {
		publishType = type;
	}

	public void publish(Boolean b) {
		publish = b;
	}

	public void publishInt() {
		publishType = PUBLISH_INT;
	}

	@Override
	public void serialEvent(SerialDeviceEvent event) {
		switch (event.getEventType()) {
		case SerialDeviceEvent.BI:
		case SerialDeviceEvent.OE:
		case SerialDeviceEvent.FE:
		case SerialDeviceEvent.PE:
		case SerialDeviceEvent.CD:
		case SerialDeviceEvent.CTS:
		case SerialDeviceEvent.DSR:
		case SerialDeviceEvent.RI:
		case SerialDeviceEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialDeviceEvent.DATA_AVAILABLE:
			try {

				byte newByte;

				while (serialDevice.isOpen() && (newByte = (byte) serialDevice.read()) >= 0) {
					++recievedByteCount;

					if (blocking) {
						if (blockingData.size() < BUFFER_SIZE) {
							blockingData.add(newByte);
						} else {
							warn(String.format("overrun data > %d", BUFFER_SIZE));
						}
					}

					if (publish) {
						switch (publishType) {

						case PUBLISH_LONG: {
							buffer[recievedByteCount - 1] = newByte;
							if (recievedByteCount % BYTE_SIZE_LONG == 0) {
								long value = 0;
								for (int i = 0; i < BYTE_SIZE_LONG; i++) {
									value = (value << 8) + (buffer[i] & 0xff);
								}

								invoke("publishLong", value);
								recievedByteCount = 0;
							}
							break;
						}
						case PUBLISH_INT: {
							buffer[recievedByteCount - 1] = newByte;
							if (recievedByteCount % BYTE_SIZE_LONG == 0) {
								long value = 0;
								for (int i = 0; i < BYTE_SIZE_LONG; i++) {
									value = (value << 8) + (buffer[i] & 0xff);
								}

								invoke("publishInt", value);
								recievedByteCount = 0;
							}
							break;
						}

						case PUBLISH_BYTE: {
							invoke("publishByte", newByte);
							break;
						}
						} 
					} // if publish
				}
			} catch (IOException e) {
				Logging.logException(e);
			}

			break;
		}

	}

	@Override
	public ArrayList<String> getPortNames() {
		return SerialDeviceFactory.getSerialDeviceNames();
	}

	@Override
	public SerialDevice getSerialDevice() {
		return serialDevice;
	}

	@Override
	public boolean connect(String name, int rate, int databits, int stopbits, int parity) {
		if (name == null || name.length() == 0) {
			log.info("got emtpy connect name - disconnecting");
			return disconnect();
		}
		try {
			serialDevice = SerialDeviceFactory.getSerialDevice(name, rate, databits, stopbits, parity);
			if (serialDevice != null) {
				if (!serialDevice.isOpen()) {
					serialDevice.open();
					serialDevice.addEventListener(this); // TODO - only add if
															// "publishing" ?
					serialDevice.notifyOnDataAvailable(true);
					sleep(1000);
				}

				serialDevice.setParams(rate, databits, stopbits, parity);
				portName = serialDevice.getName();
				connected = true;
				save(); // successfully bound to port - saving
				broadcastState(); // state has changed let everyone know
				return true;

			} else {
				log.error("could not get serial device");
			}
		} catch (Exception e) {
			logException(e);
		}
		return false;
	}

	@Override
	public boolean connect(String name) {
		return connect(name, 57600, 8, 1, 0);
	}

	/**
	 * ------ publishing points begin -------
	 */

	// FIXME - fixed width and message delimeter
	// FIXME - block read(until block size)

	public byte publishByte(Byte data) {
		return data;
	}

	public char publishChar(Character data) {
		return data;
	}

	public int publishInt(Integer data) {
		return data;
	}

	public long publishLong(Long data) {
		return data;
	}

	public byte[] publishByteArray(byte[] data) {
		return data;
	}

	public String publishString(String data) {
		return data;
	}

	/**
	 * ------ publishing points end -------
	 */

	/**
	 * -------- blocking reads begin --------
	 * 
	 * @throws IOException
	 * 
	 * @throws InterruptedException
	 */

	// http://stackoverflow.com/questions/11805300/rxtx-java-inputstream-does-not-return-all-the-buffer
	public byte readByte() throws InterruptedException {
		return blockingData.take().byteValue();
	}

	public char readChar() throws InterruptedException {
		return (char) blockingData.take().byteValue();
	}

	public int readInt() throws InterruptedException {
		int count = 0;
		int value = 0;
		byte newByte = -1;
		while ((newByte = blockingData.take().byteValue()) > 0 && count < BYTE_SIZE_INT) {
			++count;
			value = (value << 8) + (newByte & 0xff);
		}
		return value;
	}

	public long readLong() throws InterruptedException {
		int count = 0;
		long value = -1;
		byte newByte = -1;
		while ((newByte = blockingData.take().byteValue()) > 0 && count < BYTE_SIZE_LONG) {
			++count;
			value = (value << 8) + (newByte & 0xff);
		}
		return value;
	}

	public byte[] readByteArray(int length) throws InterruptedException {
		int count = 0;
		byte[] value = new byte[length];
		byte newByte = -1;
		while (count < length && (newByte = blockingData.take().byteValue()) > 0) {
			value[count] = newByte;
			++count;
		}
		return value;
	}

	public String readString(char delimeter) throws InterruptedException {
		StringBuffer value = new StringBuffer();
		byte newByte = -1;
		while ((newByte = blockingData.take().byteValue()) > 0 && newByte != delimeter) {
			value.append(newByte);
		}
		return value.toString();
	}

	/**
	 * -------- blocking reads begin --------
	 */

	public boolean isConnected() {
		// I know not normalized
		// but we have to do this - since
		// the SerialDevice is transient
		return connected;
	}

	@Override
	public void write(String data) throws IOException {
		write(data.getBytes());
	}

	@Override
	public void write(byte[] data) throws IOException {
		for (int i = 0; i < data.length; ++i) {
			serialDevice.write(data[i]);
		}
	}

	@Override
	public void write(char data) throws IOException {
		serialDevice.write(data);
	}

	@Override
	public void write(int data) throws IOException {
		serialDevice.write(data);
	}

	@Override
	public boolean disconnect() {
		if (serialDevice == null) {
			connected = false;
			portName = "";
			return false;
		}

		serialDevice.close();
		connected = false;
		portName = "";

		broadcastState();
		return true;

	}

	public boolean isBlocking() {
		return blocking;
	}

	public void blocking(boolean b) {
		blocking = b;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		LoggingFactory.getInstance().configure();
		LoggingFactory.getInstance().setLevel(Level.INFO);

		Serial serial = new Serial("serial");
		serial.startService();

		serial.connect("COM9", 57600, 8, 1, 0);

		for (int i = 0; i < 10; ++i) {
			log.info("here {}", serial.readByte());
		}
		for (int i = 0; i < 10; ++i) {
			log.info("here {}", serial.readInt());
		}
		for (int i = 0; i < 10; ++i) {
			log.info("here {}", serial.readByteArray(10));
		}
		/*
		 * GUIService gui = new GUIService("gui"); gui.startService();
		 * gui.display();
		 */
	}

}
