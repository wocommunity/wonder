package er.rest;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.webobjects.appserver.WORequest;

public class ERXXmlRestRequestParser implements IERXRestRequestParser {
	protected ERXRestRequestNode createRequestNodeForElement(Element element) {
		String name = element.getTagName();
		String value = element.getNodeValue();
		ERXRestRequestNode requestNode = new ERXRestRequestNode(name);
		NamedNodeMap attributeNodes = element.getAttributes();
		for (int attributeNum = 0; attributeNum < attributeNodes.getLength(); attributeNum++) {
			Node attribute = attributeNodes.item(attributeNum);
			requestNode.setAttributeForKey(attribute.getNodeValue(), attribute.getNodeName());
		}
		NodeList childNodes = element.getChildNodes();
		for (int childNodeNum = 0; childNodeNum < childNodes.getLength(); childNodeNum++) {
			Node childNode = childNodes.item(childNodeNum);
			if (childNode instanceof Element) {
				Element childElement = (Element) childNode;
				ERXRestRequestNode childRequestNode = createRequestNodeForElement(childElement);
				requestNode.addChild(childRequestNode);
			}
			else if (childNode instanceof Text) {
				String text = childNode.getNodeValue();
				if (text != null) {
					if (!(childNode instanceof CDATASection)) {
						text = text.trim();
					}
					if (value == null) {
						value = text;
					}
					else {
						value += text;
					}
				}
			}
		}
		requestNode.setValue(value);
		return requestNode;
	}

	public ERXRestRequest parseRestRequest(ERXRestContext context, WORequest request, String requestPath) throws ERXRestException, ERXRestNotFoundException {
		ERXRestKey requestKey = ERXRestKey.parse(context, requestPath);

		ERXRestRequestNode rootRequestNode = null;

		String contentStr = request.contentString();
		if (contentStr != null && contentStr.length() > 0) {
			Document document;
			try {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contentStr)));
				document.normalize();
				Element rootElement = document.getDocumentElement();
				rootRequestNode = createRequestNodeForElement(rootElement);
			}
			catch (Exception e) {
				throw new ERXRestException("Failed to parse request document.", e);
			}
		}

		ERXRestRequest restRequest = new ERXRestRequest(requestKey, rootRequestNode);
		return restRequest;
	}
}
