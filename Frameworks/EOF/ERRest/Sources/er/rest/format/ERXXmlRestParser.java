package er.rest.format;

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

import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;

/**
 * ERXXmlRestRequestParser is an implementation of the IERXRestRequestParser interface that supports XML document
 * requests.
 * 
 * @author mschrag
 */
public class ERXXmlRestParser implements IERXRestParser {
	protected ERXRestRequestNode createRequestNodeForElement(Element element, boolean rootNode, ERXRestFormat.Delegate delegate) {
		String name = element.getTagName();
		ERXRestRequestNode requestNode = new ERXRestRequestNode(name, rootNode);

		String value = element.getNodeValue();

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
				ERXRestRequestNode childRequestNode = createRequestNodeForElement(childElement, false, delegate);
				if (childRequestNode != null) {
					String childRequestNodeName = childRequestNode.name();
					// MS: this is a huge hack, but it turns out that it's surprinsingly tricky to
					// identify an array in XML ... I'm cheating here and just saying that if the
					// node name is uppercase, it represents a new object and not an attribute ...
					// this will totally break on rails-style lowercase class names, but for now
					// this flag is just a heuristic
					if (childRequestNodeName == null || Character.isUpperCase(childRequestNodeName.charAt(0))) {
						childRequestNode.setRootNode(true);
						requestNode.setArray(true);
					}
					requestNode.addChild(childRequestNode);
				}
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

		delegate.nodeDidParse(requestNode);

		return requestNode;
	}

	public ERXRestRequestNode parseRestRequest(WORequest request, ERXRestFormat.Delegate delegate) {
		return parseRestRequest(request.contentString(), delegate);
	}

	public ERXRestRequestNode parseRestRequest(String contentStr, ERXRestFormat.Delegate delegate) {
		ERXRestRequestNode rootRequestNode = null;

		if (contentStr != null && contentStr.length() > 0) {
			// MS: Support direct updating of primitive type keys -- so if you don't want to
			// wrap your request in XML, this will allow it
			if (!contentStr.trim().startsWith("<")) {
				contentStr = "<FakeWrapper>" + contentStr.trim() + "</FakeWrapper>";
			}

			Document document;
			try {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contentStr)));
				document.normalize();
				Element rootElement = document.getDocumentElement();
				rootRequestNode = createRequestNodeForElement(rootElement, true, delegate);
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Failed to parse request document.", e);
			}
		}

		return rootRequestNode;
	}

	public static void main(String[] args) {
		String str = "<Company><id>100</id><type>Company</type><name>mDT</name><firstName nil=\"true\"/><employees><Employee id=\"101\" type=\"Employee\"/><Employee id=\"102\"><name>Mike</name></Employee></employees></Company>";
		//String str = "<Employees><Employee id=\"101\" type=\"Employee\"/><Employee id=\"102\"><name>Mike</name></Employee></Employees>";
		ERXRestNameRegistry.registry().setExternalNameForInternalName("Super", "Company");
		ERXRestNameRegistry.registry().setExternalNameForInternalName("Super2", "Employee");
		ERXRestRequestNode n = new ERXXmlRestParser().parseRestRequest(str, new ERXRestFormatDelegate());
		ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
		new ERXXmlRestWriter().appendToResponse(n, response, new ERXRestFormatDelegate());
		System.out.println("ERXXmlRestParser.main: " + response);
	}
}
