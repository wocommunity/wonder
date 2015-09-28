package er.xiss.test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.lang3.CharEncoding;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import er.xiss.ERXML;
import er.xiss.ERXML.Item;
import er.xiss.ERXML.Visitor;


public class DocTest extends TestCase {

  public void testBlank() {
    ERXML.Doc doc = ERXML.doc();
    assertNull(doc.root());
    assertNotNull(doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
  }

  public void testDeclaration() {
    ERXML.Doc doc = ERXML.doc();
    assertNotNull(doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));

    doc.setDeclaration(null);
    assertNull(doc.declaration());
    assertEquals(0, doc.children().size());

    ERXML.Declaration declaration = ERXML.declaration("1.0", CharEncoding.UTF_8);
    doc.setDeclaration(declaration);
    assertNotNull(doc.declaration());
    assertEquals(declaration, doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(declaration, doc.children().get(0));

    doc.setDeclaration(null);
    doc.root("root");
    ERXML.Declaration declaration2 = ERXML.declaration("1.0", CharEncoding.UTF_8);
    doc.setDeclaration(declaration2);
    assertNotNull(doc.declaration());
    assertEquals(declaration2, doc.declaration());
    assertEquals(2, doc.children().size());
    assertEquals(declaration2, doc.children().get(0));
    assertEquals(doc.root(), doc.children().get(1));

    ERXML.Declaration declaration3 = ERXML.declaration("1.0", CharEncoding.UTF_8);
    doc.setDeclaration(declaration3);
    assertNotNull(doc.declaration());
    assertEquals(declaration3, doc.declaration());
    assertEquals(2, doc.children().size());
    assertEquals(declaration3, doc.children().get(0));
    assertEquals(doc.root(), doc.children().get(1));

    doc.setDeclaration(null);
    assertNull(doc.declaration());
    assertEquals(null, doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(doc.root(), doc.children().get(0));
  }

  public void testRoot() {
    ERXML.Doc doc = ERXML.doc();
    assertNull(doc.root());
    ERXML.E root = doc.root("test");
    assertEquals(root, doc.root());
    assertEquals(2, doc.children().size());
    assertEquals(root, doc.children().get(1));
    try {
      doc.root("test2");
      throw new AssertionFailedError("should have failed");
    }
    catch (IllegalStateException e) {
      // EXPECTED
    }
  }

  public void testSetRoot() {
    ERXML.Doc doc = ERXML.doc();
    ERXML.E root = ERXML.e("test");
    doc.setRoot(root);
    assertEquals("test", root.name());
    assertEquals(root, doc.root());
    assertEquals(2, doc.children().size());

    ERXML.E root2 = ERXML.e("test2");
    doc.setRoot(root2);
    assertEquals(root2, doc.root());
    assertEquals(2, doc.children().size());
    assertEquals(root2, doc.children().get(1));

    doc.setRoot(null);
    assertEquals(null, doc.root());
    assertEquals(1, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
  }

  public void testComment() {
    ERXML.Doc doc = ERXML.doc();
    doc.comment("This is a comment");
    assertEquals(2, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
    assertEquals(ERXML.Comment.class, doc.children().get(1).getClass());
    assertEquals("This is a comment", ((ERXML.Comment) doc.children().get(1)).text());
  }

  public void testAdd() {
    ERXML.Doc doc = ERXML.doc();
    doc.comment("comment1");
    doc.comment("comment2");
    doc.comment("comment3");

    assertEquals(doc.declaration(), doc.children().get(0));
    assertEquals("comment1", ((ERXML.Comment) doc.children().get(1)).text());
    assertEquals("comment2", ((ERXML.Comment) doc.children().get(2)).text());
    assertEquals("comment3", ((ERXML.Comment) doc.children().get(3)).text());
  }

  public void testRemove() {
    ERXML.Doc doc = ERXML.doc();
    doc.comment("comment1");
    doc.comment("comment2");
    doc.comment("comment3");

    assertEquals(4, doc.children().size());
    ERXML.Comment comment = (ERXML.Comment) doc.children().get(2);
    assertEquals("comment2", comment.text());

    doc.remove(comment);
    assertEquals(3, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
    assertEquals("comment1", ((ERXML.Comment) doc.children().get(1)).text());
    assertEquals("comment3", ((ERXML.Comment) doc.children().get(2)).text());
  }

  public void testChildren() {
    ERXML.Doc doc = ERXML.doc();
    doc.comment("comment1");
    doc.comment("comment2");
    doc.comment("comment3");
    assertNotNull(doc.children());
    assertEquals(4, doc.children().size());
  }

  public void testWrite() {
    ERXML.Doc doc = ERXML.doc();
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", doc.toString());

    doc.comment("comment1");
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- comment1 -->\n", doc.toString());

    doc.root("parent");
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- comment1 -->\n<parent />\n", doc.toString());
  }

  public void testVisit() {
    ERXML.Doc doc = ERXML.doc();
    doc.comment("comment1");
    doc.root("parent");

    final Set<ERXML.Item> shouldVisit = new HashSet<ERXML.Item>();
    shouldVisit.add(doc);
    shouldVisit.addAll(doc.children());
    final Set<ERXML.Item> visited = new HashSet<ERXML.Item>();
    doc.visit(new Visitor() {
      public boolean visit(Item item) {
        visited.add(item);
        return true;
      }
    });
    assertEquals(shouldVisit, visited);
  }

  public void testW3CToXISS() throws SAXException, IOException, ParserConfigurationException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader("<person><first-name>Mike</first-name><last-name>Schrag</last-name><addresses><address location=\"home\"><address>100 Main St</address><city>Richmond</city></address></addresses></person>")));
    ERXML.Doc doc = ERXML.doc(document);
    assertEquals("person", doc.root().name());
    assertEquals(((ERXML.E) doc.root().children().get(0)).name(), "first-name");
    assertEquals(((ERXML.E) doc.root().children().get(0)).text(), "Mike");
    assertEquals(((ERXML.E) doc.root().children().get(2)).name(), "addresses");
    assertEquals(((ERXML.E) ((ERXML.E) doc.root().children().get(2)).children().get(0)).name(), "address");
    assertEquals(((ERXML.E) ((ERXML.E) doc.root().children().get(2)).children().get(0)).get("location"), "home");
  }

  public void testXISSToW3C() throws SAXException, IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
    org.w3c.dom.Document document1 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader("<person><first-name>Mike</first-name><last-name>Schrag</last-name><addresses><address location=\"home\"><address>100 Main St</address><city>Richmond</city></address></addresses></person>")));
    ERXML.Doc doc = ERXML.doc(document1);
    org.w3c.dom.Document document2 = doc.w3c();

    Transformer transformer = TransformerFactory.newInstance().newTransformer();

    StreamResult result1 = new StreamResult(new StringWriter());
    transformer.transform(new DOMSource(document1), result1);
    String xmlString1 = result1.getWriter().toString();

    StreamResult result2 = new StreamResult(new StringWriter());
    transformer.transform(new DOMSource(document2), result2);
    String xmlString2 = result2.getWriter().toString();

    assertEquals(xmlString1, xmlString2);
  }

  public void testEscapedValues() {
    ERXML.Doc doc = ERXML.doc();
    doc.root("person").set("name", "<M & M>");
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<person name=\"&lt;M &amp; M&gt;\" />\n", doc.toString());
  }
}
