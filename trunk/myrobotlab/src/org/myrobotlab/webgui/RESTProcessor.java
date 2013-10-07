package org.myrobotlab.webgui;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.net.NanoHTTPD.Response;
import org.myrobotlab.service.interfaces.HTTPProcessor;
import org.myrobotlab.service.interfaces.ServiceInterface;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class RESTProcessor implements HTTPProcessor {

	public final static Logger log = LoggerFactory.getLogger(RESTProcessor.class.getCanonicalName());

	private HashSet<String> uris = new HashSet<String>();

	transient private Serializer serializer = new Persister();

	@Override
	public Response serve(String uri, String method, Properties header, Properties parms, Socket socket) {

		String returnFormat = "gson";

		// TODO top level is return format /html /text /soap /xml /gson /json a
		// default could exist - start with SOAP response
		// default is not specified but adds {/rest/xml} /services ...
		// TODO - custom display /displays
		// TODO - structured rest fault responses

		String[] keys = uri.split("/");

		// decode everything
		for (int i = 0; i < keys.length; ++i) {
			keys[i] = decodePercent(keys[i], true);
		}

		if ("/services".equals(uri)) {
			// get runtime list
			log.info("services request");
			REST rest = new REST();
			String services = rest.getServices();

			Response response = new Response("200 OK", "text/html", services);
			return response;

		} else if (keys.length > 3) {
			// get a specific service instance - execute method --with
			// parameters--
			String serviceName = keys[2];
			String fn = keys[3];
			Object[] typedParameters = null;

			ServiceInterface si = org.myrobotlab.service.Runtime.getService(serviceName);

			// get parms
			if (keys.length > 4) {
				// copy paramater part of rest uri
				String[] stringParams = new String[keys.length - 4];
				for (int i = 0; i < keys.length - 4; ++i) {
					stringParams[i] = keys[i + 4];
				}

				// FIXME FIXME FIXME !!!!
				// this is an input format decision !!! .. it "SHOULD" be determined based on inbound uri format
				
				TypeConverter.getInstance();
				typedParameters = TypeConverter.getTypedParams(si.getClass(), fn, stringParams);
			}

			// TODO - handle return type -
			// TODO top level is return format /html /text /soap /xml /gson
			// /json /base16 a default could exist - start with SOAP response
			Object returnObject = si.invoke(fn, typedParameters);
			String xml = null;

			// Message msg = new Message();
			// FIXME FIXME FIXME - put in a ResponseHanlder object with simple
			// imputs/outputs String InputStream / OutputStream !!!!
			if ("xml".equals(returnFormat)) {

				if (returnObject != null) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					// if (returnObject)
					try {

						ReturnType returnType = new ReturnType();

						// FIXME - handle
						if (returnObject.getClass() == ArrayList.class) {
							returnType.arrayList = (ArrayList<Object>) returnObject;
						} else if (returnObject.getClass() == Array.class) {
							returnType.array = (Object[]) returnObject;
						} else if (returnObject.getClass() == HashMap.class) {
							returnType.map = (HashMap<String, Object>) returnObject;
						} else {
							returnType.returnObject = returnObject;
						}

						serializer.write(returnType, out);

					} catch (Exception e) {
						// TODO Auto-generated catch block SOAP FAULT ??
						e.printStackTrace();
					}
					xml = String.format("<%sResponse>%s</%sResponse>", fn, new String(out.toByteArray()), fn);
				} else {
					xml = String.format("<%sResponse />", fn);
				}

				Response response = new Response("200 OK", "text/xml", xml);
				return response;

			} else if ("gson".equals(returnFormat)) {
				ByteArrayOutputStream out = null;
				
				// FIXME - a "response" of some sort is important - even if the returned object is null
				// the signal which you get from a event can be significant - the quetion arrises
				// gson is to encode the returned data - but if there is no data - it still
				// should return ... "blank" ?
				
				String encodedResponse = "null"; // Hah .. dorky yet appropriate?
				
				try {

					if (returnObject != null) {
						Gson gson = new Gson(); // FIXME - threadsafe? singeton?
						out = new ByteArrayOutputStream();
						JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
						writer.setIndent("  ");

						writer.beginArray();
						gson.toJson(returnObject, returnObject.getClass(), writer);
						// for (Message message : messages) {
						// gson.toJson(message, Message.class, writer);
						// }
						writer.endArray();
						writer.close();
						
						encodedResponse = new String(out.toByteArray());
						
					}
				} catch (Exception e) {
					Logging.logException(e);
				}
				
				Response response = new Response("200 OK", "text/text", encodedResponse);
				
				return response;
			}

			// handle response depending on type
			// TODO - make structured !!!
			// Right now just return string object
			// Response response = new Response("200 OK", "text/xml",
			// (returnObject == null)?String.format("<%sResponse></%sResponse>",
			// method, method):returnObject.toString());
			Response response = new Response("200 OK", "text/xml", (returnObject == null) ? String.format("<%sResponse></%sResponse>", method, method) : xml);
			return response;
		}
		return null;
	}

	@Override
	public HashSet<String> getURIs() {
		return uris;
	}

	public void addURI(String uri) {
		uris.add(uri);
	}

	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" -> "an example string"
	 */
	private String decodePercent(String str, boolean decodeForwardSlash) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			switch (c) {
			case '+':
				sb.append(' ');
				break;
			case '%':
				if ("2F".equalsIgnoreCase(str.substring(i + 1, i + 3)) && !decodeForwardSlash) {
					log.info("found encoded / - leaving");
					sb.append("%2F");
				} else {
					sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
				}
				i += 2;
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return new String(sb.toString().getBytes());

	}

}
