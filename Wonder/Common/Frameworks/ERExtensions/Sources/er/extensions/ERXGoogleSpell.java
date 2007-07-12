package er.extensions;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ERXGoogleSpell provides a simple API to spell checking with Google's web service.
 * 
 * This code is based on the work from the blog post
 * http://immike.net/blog/2007/04/07/hacking-google-spell-checker-for-fun-and-profit/.
 * 
 * @author mschrag
 */
public class ERXGoogleSpell {
	public static void main(String[] args) throws CorrectionException {
		String str = "gogle spel";
		System.out.println("ERXGoogleSpell.correct: " + ERXGoogleSpell.correct(str));
	}

	/**
	 * Corrects the spelling of the given text (language = "en").
	 * 
	 * @param text
	 *            the misspelled text
	 * @return the corrected text
	 * @throws CorrectionException
	 *             if there is a problem correcting the text
	 */
	public static String correct(String text) throws CorrectionException {
		return ERXGoogleSpell.correct(text, "en");
	}

	/**
	 * Corrects the spelling of the given text.
	 * 
	 * @param text
	 *            the misspelled text
	 * @param lang
	 *            the language of the text
	 * @return the corrected text
	 * @throws CorrectionException
	 *             if there is a problem correcting the text
	 */
	public static String correct(String text, String lang) throws CorrectionException {
		return ERXGoogleSpell.correct(text, lang, lang);
	}

	/**
	 * Corrects the spelling of the given text.
	 * 
	 * @param text
	 *            the misspelled text
	 * @param lang
	 *            the language of the text
	 * @param hl
	 *            the human interface language
	 * @return the corrected text
	 * @throws CorrectionException
	 *             if there is a problem correcting the text
	 */
	public static String correct(String text, String lang, String hl) throws CorrectionException {
		Correction[] corrections = ERXGoogleSpell.suggestions(text, lang, hl);
		int lastOffset = 0;
		StringBuffer buffer = new StringBuffer();
		for (int correctionNum = 0; correctionNum < corrections.length; correctionNum++) {
			Correction correction = corrections[correctionNum];
			String[] suggestions = correction.suggestions();
			if (suggestions.length > 0) {
				String suggestion = suggestions[0];
				int offset = correction.offset();
				buffer.append(text.substring(lastOffset, offset));
				buffer.append(suggestion);
				lastOffset = offset + correction.length();
			}
		}
		buffer.append(text.substring(lastOffset));
		return buffer.toString();
	}

	/**
	 * Returns possible spelling corrections of the given text (language = "en").
	 * 
	 * @param text
	 *            the misspelled text
	 * @return the list of suggested corrections
	 * @throws CorrectionException
	 *             if there is a problem correcting the text
	 */
	public static Correction[] suggestions(String text) throws CorrectionException {
		return ERXGoogleSpell.suggestions(text, "en");
	}

	/**
	 * Returns possible spelling corrections of the given text.
	 * 
	 * @param text
	 *            the misspelled text
	 * @param lang
	 *            the language of the text
	 * @return the list of suggested corrections
	 * @throws CorrectionException
	 *             if there is a problem correcting the text
	 */
	public static Correction[] suggestions(String text, String lang) throws CorrectionException {
		return ERXGoogleSpell.suggestions(text, lang, lang);
	}

	/**
	 * Returns possible spelling corrections of the given text.
	 * 
	 * @param text
	 *            the misspelled text
	 * @param lang
	 *            the language of the text
	 * @param hl
	 *            the human interface language
	 * @return the list of suggested corrections
	 * @throws CorrectionException
	 *             if there is a problem correcting the text
	 */
	public static Correction[] suggestions(String text, String lang, String hl) throws CorrectionException {
		try {
			StringBuffer request = new StringBuffer();
			request.append("<spellrequest textalreadyclipped=\"0\" ignoredups=\"1\" ignoredigits=\"1\" ignoreallcaps=\"0\"><text>");
			request.append(ERXStringUtilities.escapeNonXMLChars(text));
			request.append("</text></spellrequest>");

			URL url = new URL("https://www.google.com/tbproxy/spell?lang=" + lang + "&hl=" + lang);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(request.toString());
			out.close();

			Correction[] corrections;
			InputStream in = connection.getInputStream();
			try {
				Document responseDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
				responseDocument.normalize();
				NodeList correctionNodes = responseDocument.getElementsByTagName("c");
				int correctionCount = correctionNodes.getLength();
				corrections = new Correction[correctionCount];
				for (int correctionNum = 0; correctionNum < correctionCount; correctionNum++) {
					Node correctionNode = correctionNodes.item(correctionNum);
					if (correctionNode instanceof Element) {
						Element correctionElement = (Element) correctionNode;
						String correctionsStr = correctionElement.getChildNodes().item(0).getNodeValue();
						int offset = Integer.parseInt(correctionElement.getAttribute("o"));
						int length = Integer.parseInt(correctionElement.getAttribute("l"));
						int confidence = Integer.parseInt(correctionElement.getAttribute("s"));
						String[] correctionStrs = correctionsStr.split("\t");
						corrections[correctionNum] = new Correction(offset, length, confidence, correctionStrs);
					}
				}
			}
			finally {
				in.close();
			}
			return corrections;
		}
		catch (Exception e) {
			throw new CorrectionException("Failed to correct spelling of '" + text + "'.", e);
		}
	}

	/**
	 * Correction encapsulates a suggested spelling correction for a word in a string of text.
	 * 
	 * @author mschrag
	 */
	public static class Correction {
		private int _offset;
		private int _length;
		private int _confidence;
		private String[] _suggestions;

		/**
		 * Creates a new correction.
		 * 
		 * @param offset
		 *            the offset of the misspelled in the original text
		 * @param length
		 *            the length of the misspelled word in the original text
		 * @param confidence
		 *            the confidence of correction (0 or 1)
		 * @param suggestions
		 *            the list of suggested corrections
		 */
		public Correction(int offset, int length, int confidence, String[] suggestions) {
			_offset = offset;
			_length = length;
			_confidence = confidence;
			_suggestions = suggestions;
		}

		/**
		 * Returns the offset of the misspelled word in the original text.
		 */
		public int offset() {
			return _offset;
		}

		/**
		 * Returns the length of the misspelled word in the original text.
		 */
		public int length() {
			return _length;
		}

		/**
		 * Returns the confidence of the correction (0 or 1).
		 */
		public int confidence() {
			return _confidence;
		}

		/**
		 * Returns an ordered list of suggested spelling corrections.
		 */
		public String[] suggestions() {
			return _suggestions;
		}
	}

	/**
	 * CorrectionException is thrown if anything fails during the correction process.
	 * 
	 * @author mschrag
	 */
	public static class CorrectionException extends Exception {
		/**
		 * Creates a new CorrectionException.
		 * 
		 * @param message
		 *            the exception message
		 * @param cause
		 *            the root cause
		 */
		public CorrectionException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Creates a new CorrectionException.
		 * 
		 * @param message
		 *            the exception message
		 */
		public CorrectionException(String message) {
			super(message);
		}
	}
}
