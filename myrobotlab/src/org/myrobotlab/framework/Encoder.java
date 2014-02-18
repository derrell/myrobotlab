package org.myrobotlab.framework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.soap.SOAPBodyElement;

import org.apache.commons.codec.binary.Base64;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * handles all encoding and decoding of MRL messages or api(s) assumed context -
 * services can add an assumed context as a prefix
 * /api/returnEncoding/inputEncoding/service/method/param1/param2/ ...
 * 
 * xmpp for example assumes (/api/string/gson)/service/method/param1/param2/ ...
 * 
 * scheme = alpha *( alpha | digit | "+" | "-" | "." )
 * Components of all URIs: [<scheme>:]<scheme-specific-part>[#<fragment>]
 * http://stackoverflow.com/questions/3641722/valid-characters-for-uri-schemes
 */
public class Encoder {

	public final static Logger log = LoggerFactory.getLogger(Encoder.class);

	public final static String SCHEME_MRL = "mrl";
	public final static String SCHEME_BASE64 = "base64";

	// disableHtmlEscaping to prevent encoding or "=" -
	public final transient static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").setPrettyPrinting().disableHtmlEscaping().create();
	public final static String API_REST_PREFIX = "/api";

	public static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
			Float.class, Double.class, Void.class));

	public static final Set<String> WRAPPER_TYPES_CANONICAL = new HashSet<String>(Arrays.asList(Boolean.class.getCanonicalName(), Character.class.getCanonicalName(),
			Byte.class.getCanonicalName(), Short.class.getCanonicalName(), Integer.class.getCanonicalName(), Long.class.getCanonicalName(), Float.class.getCanonicalName(),
			Double.class.getCanonicalName(), Void.class.getCanonicalName()));

	final static HashMap<String, Method> methodCache = new HashMap<String, Method>();

	/**
	 * a method signature map based on name and number of methods - the String[]
	 * will be the keys into the methodCache A method key is generated by input
	 * from some encoded protocol - the method key is object name + method name
	 * + parameter number - this returns a full method signature key which is
	 * used to look up the method in the methodCache
	 */
	final static HashMap<String, ArrayList<Method>> methodOrdinal = new HashMap<String, ArrayList<Method>>();

	final static HashSet<String> objectsCached = new HashSet<String>();

	public static boolean isWrapper(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
	}

	public static boolean isWrapper(String className) {
		return WRAPPER_TYPES_CANONICAL.contains(className);
	}

	/**
	 * most lossy protocols need conversion of parameters into correctly typed
	 * elements this method is used to query a candidate method to see if a
	 * simple conversion is possible
	 * 
	 * @return
	 */
	public static boolean isSimpleType(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz) || clazz == String.class;
	}

	public static Message decodeURI(URI uri) {
		log.info(String.format("authority %s", uri.getAuthority())); // gperry:blahblah@localhost:7777
		log.info(String.format("     host %s", uri.getHost())); // localhost
		log.info(String.format("     port %d", uri.getPort())); // 7777
		log.info(String.format("     path %s", uri.getPath()));
		log.info(String.format("    query %s", uri.getQuery())); // /api/string/gson/runtime/getUptime
		log.info(String.format("   scheme %s", uri.getScheme())); // http
		log.info(String.format(" userInfo %s", uri.getUserInfo())); // gperry:blahblah

		Message msg = decodePathInfo(uri.getPath());

		return msg;
	}

	// TODO optimization of HashSet combinations of supported encoding instead
	// of parsing...
	// e.g. HashMap<String> supportedEncoding.containsKey(
	public static Message decodePathInfo(String pathInfo) {

		if (pathInfo == null) {
			log.error("pathInfo is null");
			return null;
		}

		if (!pathInfo.startsWith(API_REST_PREFIX)) {
			log.error(String.format("pathInfo [%s] needs to start with [%s]", pathInfo, API_REST_PREFIX));
			return null;
		}

		int p0 = API_REST_PREFIX.length() + 1; // "/api/"
		int p1 = pathInfo.indexOf("/", p0);

		String responseEncoding = pathInfo.substring(p0, p1);

		p0 = p1 + 1;
		p1 = pathInfo.indexOf("/", p0);

		String inputEncoding = pathInfo.substring(p0, p1);

		p0 = p1 + 1;
		p1 = pathInfo.indexOf("/", p0);

		String serviceName = pathInfo.substring(p0, p1);

		p0 = p1 + 1;
		p1 = pathInfo.indexOf("/", p0);

		String method = null;
		String[] params = null;

		if (p1 != -1) {
			// there are parameters
			method = pathInfo.substring(p0, p1);
			params = pathInfo.substring(++p1).split("/");

			// param conversion via inputEncoding
		} else {
			method = pathInfo.substring(p0, p1);
		}

		// FIXME INVOKING VS PUTTING A MESSAGE ON THE BUS
		Message msg = new Message();
		msg.name = serviceName;
		msg.method = method;

		return msg;
	}

	// TODO
	// public static Object encode(Object, encoding) - dispatches appropriately

	public static String msgToGson(Message msg) {
		return gson.toJson(msg, Message.class);
	}

	public static Message gsonToMsg(String gsonData) {
		return (Message) gson.fromJson(gsonData, Message.class);
	}

	public static final Message base64ToMsg(String base64) {
		String data = base64;
		if (base64.startsWith(String.format("%s://", SCHEME_BASE64))) {
			data = base64.substring(SCHEME_BASE64.length() + 3);
		}
		final ByteArrayInputStream dataStream = new ByteArrayInputStream(Base64.decodeBase64(data));
		try {
			final ObjectInputStream objectStream = new ObjectInputStream(dataStream);
			Message msg = (Message) objectStream.readObject();
			return msg;
		} catch (Exception e) {
			Logging.logException(e);
			return null;
		}
	}

	public static final String msgToBase64(Message msg) {
		final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream objectStream = new ObjectOutputStream(dataStream);
			objectStream.writeObject(msg);
			objectStream.close();
			dataStream.close();
			String base64 = String.format("%s://%s", SCHEME_BASE64, new String(Base64.encodeBase64(dataStream.toByteArray())));
			return base64;
		} catch (Exception e) {
			log.error(String.format("couldnt seralize %s", msg));
			Logging.logException(e);
			return null;
		}
	}

	static final public String getParameterSignature(Object[] data) {
		if (data == null) {
			// return "null";
			return "";
		}

		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < data.length; ++i) {
			if (data[i] != null) {
				Class<?> c = data[i].getClass(); // not all data types are safe
													// toString() e.g.
													// SerializableImage
				if (c == String.class || c == Integer.class || c == Boolean.class || c == Float.class || c == MRLListener.class) {
					ret.append(data[i].toString());
				} else {
					String type = data[i].getClass().getCanonicalName();
					String shortTypeName = type.substring(type.lastIndexOf(".") + 1);
					ret.append(shortTypeName);
				}

				if (data.length != i + 1) {
					ret.append(",");
				}
			} else {
				ret.append("null");
			}

		}
		return ret.toString();

	}

	public static String type(String type) {
		int pos0 = type.indexOf(".");
		if (pos0 > 0) {
			return type;
		}
		return String.format("org.myrobotlab.service.%s", type);
	}

	public static boolean tryParseInt(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception e) {

		}
		return false;
	}

	static public String makeMethodKey(String fullObjectName, String methodName, Class<?>[] paramTypes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < paramTypes.length; ++i) {
			sb.append("/");
			sb.append(paramTypes[i].getCanonicalName());
		}
		return String.format("%s/%s%s", fullObjectName, methodName, sb.toString());
	}

	static public String makeMethodOrdinalKey(String fullObjectName, String methodName, int paramCount) {
		return String.format("%s/%s/%d", fullObjectName, methodName, paramCount);
	}

	// LOSSY Encoding (e.g. xml & gson - which do not encode type information)
	// can possibly
	// give us the parameter count - from the parameter count we can grab method
	// candidates
	// @return is a arraylist of keys !!!

	static public ArrayList<Method> getMethodCandidates(String serviceType, String methodName, int paramCount) {
		if (!objectsCached.contains(serviceType)) {
			loadObjectCache(serviceType);
		}

		String ordinalKey = makeMethodOrdinalKey(serviceType, methodName, paramCount);
		if (!methodOrdinal.containsKey(ordinalKey)) {
			log.error(String.format("cant find matching method candidate for %s.%s %d params", serviceType, methodName, paramCount));
			return null;
		}
		return methodOrdinal.get(ordinalKey);
	}

	/**
	 * a CodeBlock is an execution unit ready to be invoked with converted
	 * parameters and data
	 * 
	 */
	static public class CodeBlock {
		public Method method;
		public Object[] params;

		CodeBlock(Method method, Object[] params) {
			this.method = method;
			this.params = params;
		}
	}

	// TODO - this is a specific xml decode - e.g. Jaxb versus simplexml
	static public CodeBlock getCodeBlockFromXML(String serviceType, String methodName, ArrayList<SOAPBodyElement> parms) {
		ArrayList<Method> candidates = getMethodCandidates(serviceType, methodName, parms.size());
		Object[] params = new Object[parms.size()];
		for (int i = 0; i < candidates.size(); ++i) {
			Method m = candidates.get(i);
			Class<?>[] mParms = m.getParameterTypes();
			boolean converted = false;
			// parameter converter
			try {
				for (int j = 0; j < parms.size(); ++j) {
					SOAPBodyElement parm = parms.get(j);
					String value = parm.getValue();
					// if the parm has a value it is a simple type
					// cuz that's the way my xml rolls ;)
					if (value != null) {
						// instead of reflectively invoking a converter
						// i chose manual construction - for possible
						// performance gain
						Class<?> c = mParms[j];
						if (c == Integer.class || c == int.class) {
							params[j] = Integer.parseInt(value);
						} else if (c == String.class) {
							params[j] = value;
						} else if (c == Float.class || c == float.class) {
							params[j] = Float.parseFloat(value);
						} else if (c == Boolean.class || c == boolean.class) {
							params[j] = Boolean.parseBoolean(value);
						} else if (c == Byte.class || c == byte.class) {
							params[j] = Byte.parseByte(value);
						} else if (c == Double.class || c == double.class) {
							params[j] = Double.parseDouble(value);
						} else if (c == Long.class || c == long.class) {
							params[j] = Long.parseLong(value);
						} else if (c == Short.class || c == short.class) {
							params[j] = Short.parseShort(value);
						} else if (c == Character.class || c == char.class) {
							// FIXME - if value.length > 1 - ERROR !! ABORT
							if (value.length() > 1) {
								throw new Exception(String.format("conversion to char - incorrect size of string %s", value));
							}
							params[j] = value.charAt(0);
						}
					}
				} // for each parameter
				converted = true;
			} catch (Exception e) {
				Logging.logException(e);
				converted = false;
			}
			
			if (converted){
				return new CodeBlock(m, params);
			}

		}

		log.error(String.format("could not make CodeBlock for %s.%s.%d", serviceType, methodName, parms.size()));
		return null;
	}

	// FIXME - axis's Method cache - loads only requested methods
	// this would probably be more gracefull than batch loading as I am doing..
	// http://svn.apache.org/repos/asf/webservices/axis/tags/Version1_2RC2/java/src/org/apache/axis/utils/cache/MethodCache.java
	static public void loadObjectCache(String serviceType) {
		try {
			objectsCached.add(serviceType);
			Class<?> clazz = Class.forName(serviceType);
			Method[] methods = clazz.getMethods();
			for (int i = 0; i < methods.length; ++i) {
				Method m = methods[i];
				Class<?>[] types = m.getParameterTypes();

				String ordinalKey = makeMethodOrdinalKey(serviceType, m.getName(), types.length);
				String methodKey = makeMethodKey(serviceType, m.getName(), types);

				if (!methodOrdinal.containsKey(ordinalKey)) {
					ArrayList<Method> keys = new ArrayList<Method>();
					keys.add(m);
					methodOrdinal.put(ordinalKey, keys);
				} else {
					methodOrdinal.get(ordinalKey).add(m);
				}

				if (log.isDebugEnabled()) {
					log.debug(String.format("loading %s into method cache", methodKey));
				}
				methodCache.put(methodKey, m);
			}
		} catch (Exception e) {
			Logging.logException(e);
		}
	}

	// concentrator data coming from decoder
	static public Method getMethod(String serviceType, String methodName, Object[] params) {
		return getMethod("org.myrobotlab.service", serviceType, methodName, params);
	}

	// --- xml codec begin ------------------
	// inbound parameters are probably strings or xml bits encoded in some way -
	// need to match
	// ordinal first

	// real encoded data ??? getMethodFromXML getMethodFromJson - all resolve to
	// this getMethod with class form
	// encoded data.. YA !
	static public Method getMethod(String pkgName, String objectName, String methodName, Object[] params) {
		// try {
		String fullObjectName = String.format("%s.%s", pkgName, objectName);
		// TODO - is param number incorrect? should the params
		// String key = makeMethodKey(fullObjectName, methodName,
		// params.length);
		/*
		 * Class<?>[] paramTypes = new Class<?>[params.length]; for (int i = 0;
		 * i < params.length; ++i) { paramTypes[i] = params[i].getClass(); }
		 * String methodKey = makeMethodKey(fullObjectName, methodName,
		 * paramsTypes);
		 */
		/*
		 * 
		 * if (!objectsCached.contains(fullObjectName)){
		 * objectsCached.add(fullObjectName); // first time for this object type
		 * - we will cache all its methods Class<?> clazz =
		 * Class.forName(String.format("%s.%s", pkgName, objectName)); Method[]
		 * methods = clazz.getMethods(); for (int i = 0; i < methods.length;
		 * ++i) { Method m = methods[i]; Class<?>[] types =
		 * m.getParameterTypes();
		 * 
		 * String ordinalKey = makeMethodOrdinalKey(fullObjectName, methodName,
		 * types.length); String methodKey = makeMethodKey(fullObjectName,
		 * methodName, types);
		 * 
		 * 
		 * if (!methodOrdinal.containsKey(ordinalKey)){
		 * methodOrdinal.put(ordinalKey, m); }
		 * 
		 * methodCache.put(methodKey, m);
		 * 
		 * 
		 * } }
		 */

		/*
		 * 
		 * if (!methodCache.containsKey(methodKey)) {
		 * 
		 * Class<?> clazz = Class.forName(String.format("%s.%s", pkgName,
		 * objectName)); Method[] methods = clazz.getMethods(); for (int i = 0;
		 * i < methods.length; ++i) { Method m = methods[i]; Class<?>[] types =
		 * m.getParameterTypes();
		 * 
		 * if (!methodOrdinal.containsKey(ordinalKey)) {
		 * methodOrdinal.put(ordinalKey, m); }
		 * 
		 * methodCache.put(methodKey, m);
		 * 
		 * } }
		 * 
		 * } catch (Exception e) { Logging.logException(e); return null; }
		 */
		return null;

	}
	/*
	 * // --- xml codec end ------------------ public static void main(String[]
	 * args) { LoggingFactory.getInstance().configure();
	 * LoggingFactory.getInstance().setLevel(Level.INFO);
	 * 
	 * try {
	 * 
	 * Encoder.getMethod("Clock", "setInterval", new Object[] { new Integer(6)
	 * });
	 * 
	 * String user = null; String group = null;
	 * 
	 * HashMap<String, String> userGroup = new HashMap<String, String>();
	 * userGroup.put(String.format("%s", group), "ALLOW");
	 * userGroup.put(String.format("%s.%s", user, group), "ALLOW");
	 * 
	 * String x = userGroup.get("null.null");
	 * 
	 * String url =
	 * "http://gperry:blahblah@localhost:7777/api/string/gson/runtime/getUptime"
	 * ; log.info(url.substring(5)); url = "mrl://remote/tcp://blah.com"; URI
	 * uri = new URI(url);
	 * 
	 * log.info(uri.getHost()); log.info(uri.getScheme());
	 * log.info(uri.getPath());
	 * 
	 * Message msg = decodeURI(uri);
	 * 
	 * decodePathInfo("/api"); decodePathInfo(null);
	 * decodePathInfo("  /api/  ");
	 * 
	 * // REST rest = new REST(); } catch (Exception e) {
	 * Logging.logException(e); }
	 * 
	 * }
	 */
}
