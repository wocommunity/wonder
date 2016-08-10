package er.extensions.components.javascript;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Provides an interface to the Yahoo Content Analysis Service.
 * 
 * @author mschrag
 */
public class ERXYahooContentAnalysisService {
	/**
	 * Returns a term extraction of significant words or phrases from the given
	 * content using the Yahoo Term Extraction service:
	 * http://developer.yahoo.com/search/content/V1/termExtraction.html.
	 * 
	 * @param appid
	 *            your Yahoo application ID (see
	 *            https://developer.yahoo.com/wsregapp/ )
	 * @param content
	 *            the content to extract terms from
	 * @param context
	 *            an optional search phrase that can provide context for the
	 *            term extraction
	 * @param maxTerms
	 *            the maximum number of terms to return, or null for unlimited
	 * @return an array of extract terms
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public static NSArray<String> termExtraction(String appid, String content, String context, Integer maxTerms) throws IOException, SAXException, ParserConfigurationException, FactoryConfigurationError {
		if (content == null || content.trim().length() == 0) {
			return NSArray.<String> emptyArray();
		}

		ERXMutableURL queryParameters = new ERXMutableURL();
		queryParameters.setQueryParameter("appid", appid);
		queryParameters.setQueryParameter("context", ERXStringUtilities.stripHtml(content, false));
		queryParameters.setQueryParameter("output", "xml");
		if (context != null) {
			queryParameters.setQueryParameter("context", ERXStringUtilities.stripHtml(context, false));
		}
		String postData = queryParameters.toExternalForm();

		HttpURLConnection conn = (HttpURLConnection) new URL("http://search.yahooapis.com/ContentAnalysisService/V1/termExtraction").openConnection();
		NSMutableArray<String> terms = new NSMutableArray<String>();
		try {
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Host", "api.search.yahoo.com");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()), true);
			pw.print(postData);
			pw.close();

			Document resultsDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(conn.getInputStream());
			resultsDoc.normalize();
			NodeList resultNodes = resultsDoc.getDocumentElement().getElementsByTagName("Result");
			for (int i = 0; i < resultNodes.getLength() && (maxTerms == null || terms.count() <= maxTerms.intValue()); i++) {
				Node resultNode = resultNodes.item(i);
				String result = ((Element) resultNode).getChildNodes().item(0).getNodeValue();
				if (result != null && result.length() > 0) {
					terms.addObject(result);
				}
			}
		}
		finally {
			conn.disconnect();
		}

		return terms;
	}
}
