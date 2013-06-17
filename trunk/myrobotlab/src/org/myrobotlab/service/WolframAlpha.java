package org.myrobotlab.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.myrobotlab.framework.Service;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.LoggingFactory;
import org.slf4j.Logger;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

public class WolframAlpha extends Service {

	private static final long serialVersionUID = 1L;
	private static String AppID = "W6VGAJ-P4RA2HKTTH";
	public final static Logger log = LoggerFactory.getLogger(WolframAlpha.class
			.getCanonicalName());

	public WolframAlpha(String n) {
		super(n, WolframAlpha.class.getCanonicalName());
	}

	@Override
	public String getToolTip() {
		return "Wolfram Alpha Service";
	}

	@Override
	public void stopService() {
		super.stopService();
	}

	@Override
	public void releaseService() {
		super.releaseService();
	}

	public void setAppID(String id) {
		AppID = id;
	}

	public WAQueryResult getQueryResult(String query) {
		String url;
		try {
			url = "http://www.wolframalpha.com/input/?i="
					+ URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
		}
		// openUrl(url);
		// The WAEngine is a factory for creating WAQuery objects,
		// and it also used to perform those queries. You can set properties of
		// the WAEngine (such as the desired API output format types) that will
		// be inherited by all WAQuery objects created from it. Most
		// applications
		// will only need to crete one WAEngine object, which is used throughout
		// the life of the application.
		WAEngine engine = new WAEngine();

		// These properties will be set in all the WAQuery objects created from
		// this WAEngine.
		engine.setAppID(AppID);
		engine.addFormat("plaintext");
		engine.addFormat("image");

		// Create the query.
		WAQuery waquery = engine.createQuery();

		// Set properties of the query.
		waquery.setInput(query);

		try {
			// For educational purposes, print out the URL we are about to send:
			// System.out.println("Query URL:");
			// System.out.println(engine.toURL(waquery));
			// System.out.println("");

			// This sends the URL to the Wolfram|Alpha server, gets the XML
			// result
			// and parses it into an object hierarchy held by the WAQueryResult
			// object.
			return engine.performQuery(waquery);
		} catch (Exception e) {
		}
		return null;
	}

	// pod is the Category string you want returned
	public String wolframAlpha(String query, String pod) {
		WAQueryResult queryResult = getQueryResult(query);
		String acc = null;
		for (WAPod ppod : queryResult.getPods()) {
			if (!ppod.isError()) {
				for (WASubpod subpod : ppod.getSubpods()) {
					for (Object element : subpod.getContents()) {
						if (ppod.getTitle().equals(pod)
								&& element instanceof WAPlainText) {
							if (acc != null)
								acc += " ; ";
							else
								acc = "";
							acc += ((WAPlainText) element).getText();
						}
					}
				}
			}
		}
		return acc;
	}

	public String[] wolframAlphaSolution(String query) {

		String get = wolframAlpha(query, "Solutions");
		return parseString(get);

	}

	public String[] wolframAlphaSolution(String query, String pod) {

		String get = wolframAlpha(query, "Solutions");
		return parseString(get);
	}

	private String[] parseString(String get) {
		get = get.replaceAll(" ", "");
		String[] ret = get.split("[=;,]");
		System.out.println(Arrays.toString(ret));
		return ret;
	}

	/**
	 * Query Wolfram Alpha for an answer
	 * 
	 * @param query
	 * @return
	 */
	public String wolframAlpha(String query) {
		return wolframAlpha(query, false);
	}

	public String wolframAlpha(String query, boolean html) {

		WAQueryResult queryResult = getQueryResult(query);
		String full = html ? "<html><body>" : "";

		if (queryResult.isError()) {
			return "Query error (" + query + "( " + query + ")"
					+ (html ? "<br>" : "\n") + "Error code: "
					+ queryResult.getErrorCode() + (html ? "<br>" : "\n")
					+ "Error message: " + queryResult.getErrorMessage();

		} else if (!queryResult.isSuccess()) {
			return ("Query (" + query + ") was not understood; no results available.");
		} else {
			// Got a result.

			for (WAPod pod : queryResult.getPods()) {
				if (!pod.isError()) {
					full += (html ? "<br><b>" : "") + pod.getTitle()
							+ (html ? "</b><br>" : "");
					// try {
					// pod.acquireImages();
					// } catch (WAException e) {
					// // TODO Auto-generated catch block
					// }
					for (WASubpod subpod : pod.getSubpods()) {
						for (Object element : subpod.getContents()) {
							// System.out.println(element.getClass());
							if (html && element instanceof WAPlainText) {
								System.out.println(pod.getTitle() + " "
										+ ((WAPlainText) element).getText());
							}
							if (!html && element instanceof WAPlainText) {
								full += ((WAPlainText) element).getText()
										+ (html ? "<br>" : "\n");
							}
							if (html && element instanceof WAImage) {
								full += "<img src=\""
										+ ((WAImage) element).getURL() + "\">"
										+ (html ? "<br>" : "\n");
							}
						}
					}
				}
			}
			return full + (html ? "</body><html>" : "");
		}

	}

	public static void main(String[] args) {
		LoggingFactory.getInstance().configure();
		LoggingFactory.getInstance().setLevel(Level.WARN);

		WolframAlpha template = new WolframAlpha("wolfram");
		template.startService();

		Runtime.createAndStart("gui", "GUIService");
		/*
		 * GUIService gui = new GUIService("gui"); gui.startService();
		 * gui.display();
		 */
	}

}
