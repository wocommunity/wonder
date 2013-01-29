package er.pdf.builder;

import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFCreationListener;

import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;

/**
 * <p>
 * Implements {@link PDFCreationListener} to extract metadata values from the
 * input XHTML, and inserts those values into the resulting PDF. Specifically,
 * XHTML {@code meta} tags with the following {@code name} attributes will have
 * their {@code content} attributes inserted as the corresponding PDF metadata
 * keys:
 * </p>
 * 
 * <ul>
 * <li>title</li>
 * <li>author</li>
 * <li>subject</li>
 * <li>creator</li>
 * <li>descriptions</li>
 * <li>keywords</li>
 * </ul>
 * 
 * <p>
 * For example, the following XHTML fragment would set the {@code title},
 * {@code author} and {@code subject} metadata keys in the PDF output:
 * </p>
 * 
 * <pre>
 * <code>
 * &lt;meta name="title" content="Foo Sales" /&gt;
 * &lt;meta name="author" content="John Doe" /&gt;
 * &lt;meta name="subject" content="Monthly sales of Foo" /&gt;
 * </code>
 * </pre>
 * 
 * <p>
 * Further, in the absence of a "title" {@code meta} element, the content of the
 * {@code title} element will be used instead. That is, the following fragment
 * would produce the same output:
 * </p>
 * 
 * <pre>
 * <code>
 * &lt;title&gt;Foo Sales&lt;/title&gt;
 * &lt;meta name="author" content="John Doe" /&gt;
 * &lt;meta name="subject" content="Monthly sales of Foo" /&gt;
 * </code>
 * </pre>
 * 
 * <p>
 * This class is based on code posted to the <a href=
 * "http://code.google.com/p/flying-saucer/wiki/HowTo_PDF_metadata_from_xhtml_meta_tags"
 * >Flying Saucer project wiki</a>, and is assumed to be in the public domain.
 * </p>
 * 
 * @author paulhoadley
 */
public class FlyingSaucerMetadataCreationListener extends DefaultPDFCreationListener {
	/**
	 * Collection of XHTML {@code meta} tag keys and their values
	 */
	Properties headMetaTags = new Properties();

	/**
	 * Parses the XHTML {@code meta} tags into a property list.
	 * 
	 * @param sourceXHTML
	 *            XHTML source
	 */
	public void parseMetaTags(Document sourceXHTML) {
		Element headTag =
				(Element) sourceXHTML.getDocumentElement()
						.getElementsByTagName("head").item(0);
		NodeList metaTags = headTag.getElementsByTagName("meta");

		for (int i = 0; i < metaTags.getLength(); ++i) {
			Element thisNode = (Element) metaTags.item(i);
			String name = thisNode.getAttribute("name");
			String content = thisNode.getAttribute("content");
			if (name.length() != 0 && content.length() != 0) {
				headMetaTags.setProperty(name, content);
			}
		}

		// No title meta tag given --> take it from title tag
		if (headMetaTags.getProperty("title") == null) {
			Element titleTag =
					(Element) headTag.getElementsByTagName("title").item(0);
			if (titleTag != null) {
				headMetaTags.setProperty("title", titleTag.getTextContent());
			}
		}

		return;
	}

	@Override
	public void preOpen(ITextRenderer iTextRenderer) {
		@SuppressWarnings("unchecked")
		Enumeration<String> e =
				(Enumeration<String>) headMetaTags.propertyNames();

		while (e.hasMoreElements()) {
			String key = e.nextElement();
			PdfString val =
					new PdfString(headMetaTags.getProperty(key),
							PdfObject.TEXT_UNICODE);
			iTextRenderer.getWriter().setViewerPreferences(
					PdfWriter.DisplayDocTitle);
			if (key == null ? "title" == null : key.equals("title")) {
				iTextRenderer.getWriter().getInfo().put(PdfName.TITLE, val);
			} else if (key == null ? "author" == null : key.equals("author")) {
				iTextRenderer.getWriter().getInfo().put(PdfName.AUTHOR, val);
			} else if (key == null ? "subject" == null : key.equals("subject")) {
				iTextRenderer.getWriter().getInfo().put(PdfName.SUBJECT, val);
			} else if (key == null ? "creator" == null : key.equals("creator")) {
				iTextRenderer.getWriter().getInfo().put(PdfName.CREATOR, val);
			} else if (key == null ? "description" == null : key
					.equals("description")) {
				iTextRenderer.getWriter().getInfo().put(PdfName.DESC, val);
			} else if (key == null ? "keywords" == null : key
					.equals("keywords")) {
				iTextRenderer.getWriter().getInfo().put(PdfName.KEYWORDS, val);
			}
		}
	}
}
