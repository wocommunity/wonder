/*
 * Copyright (c) 2007 Design Maximum - http://www.designmaximum.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package er.selenium.io;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.webobjects.foundation.NSMutableArray;

import er.selenium.SeleniumTest;

public class SeleniumXHTMLImporter implements SeleniumTestImporter {
	private static final String TEST_NAME_XPATH = "//thead/tr/td/text()";
	private static final String ROOT_ELEMENTS_XPATH = "//tbody/child::node()[self::tr or self::comment()]";
	private static final String COMMAND_XPATH = "self::node()/child::td/text()";
	
	protected Document parseDocument(String contents) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(contents)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	public String name() {
		return "xhtml";
	}
	
	public SeleniumTest process(String contents) {
		String name;
		NSMutableArray elements = new NSMutableArray();
		Document document = parseDocument(contents);
		
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			
			name = (String)path.evaluate(TEST_NAME_XPATH, document, XPathConstants.STRING);
			NodeList rootElements = (NodeList)path.evaluate(ROOT_ELEMENTS_XPATH, document, XPathConstants.NODESET);
			
			for (int i = 0; i < rootElements.getLength(); ++i) {
				Node node = rootElements.item(i);
				
				switch (node.getNodeType()) {
				case Node.COMMENT_NODE:
					String commentValue = node.getNodeValue().trim();
					if (commentValue.startsWith("@")) {
						SeleniumTest.MetaCommand metaCommand = SeleniumTest.MetaCommand.metaCommandFromString(commentValue.substring(1));
						elements.add(metaCommand);
					} else {
						elements.add(new SeleniumTest.Comment(commentValue));
					}
					break;
				case Node.ELEMENT_NODE:
					if (!node.getNodeName().equalsIgnoreCase("tr")) {
						throw new RuntimeException("Can't find expected 'tr' tag ('" + node.getNodeName() + "' was found instead");
					}
				
					NodeList commandNodes = (NodeList)path.evaluate(COMMAND_XPATH, node, XPathConstants.NODESET);
					int cmdLength = commandNodes.getLength();
					if (cmdLength > 3 || cmdLength == 0) {
						throw new RuntimeException("Invalid command structure - expected 1-3 'td' tags (only " + commandNodes.getLength() + " were found)");
					}
					
					elements.add(new SeleniumTest.Command(commandNodes.item(0).getNodeValue(), cmdLength >= 2 ? commandNodes.item(1).getNodeValue() : "", cmdLength == 3 ? commandNodes.item(2).getNodeValue() : ""));
					break;
				default:
					throw new RuntimeException("Unexpected node: " + node.getNodeName());
				}

			}
		} catch (Exception e) {
			throw new RuntimeException("Error parsing document.", e);
		}
		
		SeleniumTest test = new SeleniumTest(name, elements);
		test.dump();
		return test;
	}
}