package er.woinstaller.archiver;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XarEntry extends HashMap<String, Object> {
	private static final XPath xpath = XPathFactory.newInstance().newXPath();
	private static XPathExpression parentNodes, xpOffset, xpLength, xpName, xpType;
	private final Node data;
	private MessageDigest digest;

	static {
		try {
			parentNodes = xpath.compile("ancestor::node()/name[../type=\"directory\"]");
			xpOffset = xpath.compile("./data/offset/text()");
			xpLength = xpath.compile("./data/length/text()");
			xpName = xpath.compile("./name");
			xpType = xpath.compile("./type");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	public XarEntry(Node node) {
		data = node;
		try {
			NodeList nodes = (NodeList)parentNodes.evaluate(data, XPathConstants.NODESET);
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				s.append(n.getTextContent()).append(File.separatorChar);
			}
			String name = xpName.evaluate(data);
			String type = xpType.evaluate(data);
			s.append(name);
			this.put("name", s.toString());
			this.put("type", type);
			if (get("type").equals("file")) {				
				this.put("offset", Long.valueOf(xpOffset.evaluate(data)));
				this.put("length", Long.valueOf(xpLength.evaluate(data)));
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Map<String, XarEntry> getEntries(Document doc) {
		Map<String, XarEntry> result = new HashMap<String, XarEntry>();
		try {
			Object nodelist = xpath.evaluate("//file[type=\"file\"]", doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) nodelist;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				XarEntry entry = new XarEntry(node);
				result.put(entry.getName(), entry);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public String getName() {
		return (String) this.get("name");
	}

	public Long getOffset() {
		return (Long) this.get("offset");
	}
	
	public Long getLength() {
		return (Long) this.get("length");
	}
	
	public String getCompression() {
		String compression = (String)getValue("data.encoding.@style");
		if (compression == null) {
			return null;
		}
		if ("application/x-bzip2".equals(compression)) {
			return "bzip2";
		}
		if ("application/x-gzip".equals(compression)) {
			return "gzip";
		}
		return compression;
	}
	
	public String getExtractedChecksum() {
		return (String)getValue("data.extracted-checksum");
	}

	public long getSize() {
		return Long.valueOf((String) getValue("data.size"));
	}
	
	public boolean hasChecksum() {
		String checksum = getExtractedChecksum();
		return checksum != null && !"".equals(checksum);
	}

	private Object getValue(String key) {
		try {
			return xpath.evaluate("./" + key.replace(".", "/"), data);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public MessageDigest getMessageDigest(String checksumAlg) {
		if (checksumAlg == null) {
			return digest;
		}
		try {
			digest = MessageDigest.getInstance(checksumAlg);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return digest;
	}
}
