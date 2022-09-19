package er.xiss;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.CharEncoding;
import org.joda.time.LocalDate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.webobjects.foundation.NSTimestamp;

/**
 * XML is the container class for all of the XISS XML builder classes and methods.
 * 
 * @author mschrag
 * 
 * http://github.com/mschrag/xiss
 */
public class ERXML {

  /** Version ID */
  public static final NSTimestamp versionDate = new NSTimestamp(new LocalDate(2009, 11, 24).toDate());

  /**
   * Item is the base class of everything that can appear in an XML document. 
   * 
   * @author mschrag
   */
  public static abstract class Item {
    private Item _parent;

    /**
     * Constructs a new Item.
     */
    public Item() {
    }

    /**
     * Sets the parent of the this item.
     * 
     * @param parent the parent of the this item
     */
    protected void setParent(Item parent) {
      _parent = parent;
    }

    /**
     * Returns the parent of this item, or null if there isn't one.
     * 
     * @return the parent of this item, or null if there isn't one
     */
    public Item parent() {
      return _parent;
    }

    /**
     * Returns the XML document, or null if it isn't in a document.
     *  
     * @return the XML document, or null if it isn't in a document
     */
    public ERXML.Doc doc() {
      ERXML.Item item = this;
      ERXML.Item parent = null;
      while ((parent = item.parent()) != null) {
        item = parent;
      }
      ERXML.Doc doc = null;
      if (item instanceof ERXML.Doc) {
        doc = (ERXML.Doc) item;
      }
      return doc;
    }

    /**
     * Writes this item to the given writer with a certain indentation. All
     * items are pretty printed.
     * 
     * @param writer the writer to write to
     * @param indent the current indentation
     */
    public abstract void write(PrintWriter writer, int indent);

    /**
     * Visits this item and any of its children (if the visitor allows).
     * 
     * @param visitor the visitor to visit with
     */
    public abstract void visit(ERXML.Visitor visitor);

    /**
     * Writes the given escaped String to the writer.
     * 
     * @param value the string value to escape
     * @param writer the writer to write to
     */
    protected void writeEscapedString(String value, PrintWriter writer) {
      if (value != null) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
          char c = value.charAt(i);
          switch (c) {
          case '<':
            writer.print("&lt;");
            break;
          case '>':
            writer.print("&gt;");
            break;
          case '&':
            writer.print("&amp;");
            break;
          case '"':
            writer.print("&quot;");
            break;
          default:
            writer.print(c);
          }
        }
      }
    }

    /**
     * Writes an indentation to the writer.
     * 
     * @param indent the indentation to write
     * @param writer the writer to write to
     */
    protected void writeIndent(int indent, PrintWriter writer) {
      for (int i = 0; i < indent; i++) {
        writer.print("  ");
      }
    }

    /**
     * Generates a formatted representation of this item.
     * 
     * @return a formatted representation of this item
     */
    @Override
    public String toString() {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      write(pw, 0);
      return sw.toString();
    }
  }

  /**
   * Declaration represents an XML declaration (the &lt;? ... ?&gt; part of the XML document).
   * 
   * @author mschrag
   */
  public static class Declaration extends Item {
    private String _version;
    private String _encoding;

    /**
     * Constructs a new Declaration.
     * 
     * @param version the version of XML used by this document
     * @param encoding the encoding used by this document
     */
    public Declaration(String version, String encoding) {
      _version = version;
      _encoding = encoding;
    }

    /**
     * Returns the version of this declaration.
     * 
     * @return the version of this declaration
     */
    public String version() {
      return _version;
    }

    /**
     * Sets the version of this document.
     * 
     * @param version the version of this document
     */
    public void setVersion(String version) {
      _version = version;
    }

    /**
     * Returns the encoding for this document.
     * 
     * @return the encoding for this document
     */
    public String encoding() {
      return _encoding;
    }

    /**
     * Sets the encoding for this document.
     * 
     * @param encoding the encoding for this document
     */
    public void setEncoding(String encoding) {
      _encoding = encoding;
    }

    @Override
    public void visit(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public void write(PrintWriter writer, int indent) {
      writer.print("<?xml");
      if (_version != null) {
        writer.print(" version=\"");
        writer.print(_version);
        writer.print("\"");
      }
      if (_encoding != null) {
        writer.print(" encoding=\"");
        writer.print(_encoding);
        writer.print("\"");
      }
      writer.print("?>");
      writer.println();
    }
  }

  /**
   * <p>
   * Doc represents the top level XML Document object, and is typically the first object you will make when building.
   * </p>
   * 
   * <pre>
   * XML.Doc doc = XML.doc();
   * XML.E person = doc.root("person");
   * </pre>
   * 
   * @author mschrag
   */
  public static class Doc extends Item {
    private ERXML.E _root;
    private ERXML.Declaration _declaration;
    private List<ERXML.Item> _children;

    /**
     * Constructs a new Document.
     */
    public Doc() {
      _children = new LinkedList<>();
      setDeclaration(new ERXML.Declaration("1.0", CharEncoding.UTF_8));
    }

    /**
     * Checks if there is already a root element and throws if there is.
     */
    protected void checkNullRoot() {
      if (_root != null) {
        throw new IllegalStateException("There is already a root node for this document.");
      }
    }

    /**
     * Returns the declaration for this document, or null if there isn't one. Every Document
     * gets a version="1.0" encoding="UTF-8" declaration by default, so you should call
     * setDeclaration(null) if you want to remove this default.
     * 
     * @return the declaration for this document
     */
    public ERXML.Declaration declaration() {
      return _declaration;
    }

    /**
     * Sets the declaration for this document.
     * 
     * @param declaration the declaration for this document
     */
    public void setDeclaration(ERXML.Declaration declaration) {
      if (_declaration != null) {
        remove(_declaration);
      }
      _declaration = declaration;
      if (_declaration != null) {
        _declaration.setParent(this);
        _children.add(0, _declaration);
      }
    }

    /**
     * Creates a new root element with the given name and returns it. If there is already
     * a root element, this will throw an exception.
     * 
     * @param name the name of the new root element
     * 
     * @return the new root element
     */
    public ERXML.E root(String name) {
      checkNullRoot();
      ERXML.E root = ERXML.e(name);
      setRoot(root);
      return root;
    }

    /**
     * Creates a new root element with the given name and initial text contents and returns 
     * it. If there is already a root element, this will throw an exception.
     * 
     * @param name the name of the new root element
     * @param value the initial text value of this element
     * 
     * @return the new root element
     */
    public ERXML.E root(String name, String value) {
      checkNullRoot();
      ERXML.E root = ERXML.e(name, value);
      setRoot(root);
      return root;
    }

    /**
     * Returns the root element (or null if there isn't one). Documents do not start out
     * with a root element, so this might be null.
     *
     * @return the root element
     */
    public ERXML.E root() {
      return _root;
    }

    /**
     * Sets the root element of this document.
     * 
     * @param root the new root element
     */
    public void setRoot(ERXML.E root) {
      if (_root != null) {
        if (root != null) {
          int rootIndex = _children.indexOf(_root);
          _children.set(rootIndex, root);
        }
        else {
          _children.remove(_root);
        }
        _root = root;
      }
      else if (root != null) {
        _root = root;
        _add(root);
      }
    }

    /**
     * Creates and returns a new comment for this document. If you want comments above
     * the root element, you should call doc.comment(..) prior to calling .root(..).
     * 
     * @param comment a new comment for this document
     * 
     * @return this document
     */
    public ERXML.Doc comment(String comment) {
      _add(ERXML.comment(comment));
      return this;
    }

    /**
     * Removes the given child from this document.
     * 
     * @param child the child to remove
     */
    public void remove(ERXML.Item child) {
      _children.remove(child);
    }

    /**
     * Adds a new child to this document.
     * 
     * @param child the child to add
     */
    protected void _add(ERXML.Item child) {
      child.setParent(this);
      _children.add(child);
    }

    /**
     * Adds a new child to this document. No validation is performed of these items.
     * 
     * @param <T> the type of the item
     * @param child the child to add
     * @return the newly added item
     */
    public <T extends ERXML.Item> T add(T child) {
      if (_root == null && child instanceof ERXML.E) {
        setRoot((ERXML.E) child);
      }
      else if (child instanceof ERXML.Declaration) {
        setDeclaration((ERXML.Declaration) child);
      }
      else {
        _add(child);
      }
      return child;
    }

    /**
     * Returns the children of this document.
     * 
     * @return the children of this document
     */
    public List<ERXML.Item> children() {
      return _children;
    }

    @Override
    public void write(PrintWriter writer, int indent) {
      for (ERXML.Item item : _children) {
        item.write(writer, indent);
      }
    }

    @Override
    public void visit(ERXML.Visitor visitor) {
      if (visitor.visit(this)) {
        for (ERXML.Item item : _children) {
          item.visit(visitor);
        }
      }
    }

    public org.w3c.dom.Document w3c() {
      try {
        org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        if (_children != null) {
          for (ERXML.Item child : _children) {
            if (child instanceof ERXML.Node) {
              org.w3c.dom.Node childNode = ((ERXML.Node) child).w3c(doc);
              doc.appendChild(childNode);
            }
          }
        }
        return doc;
      }
      catch (Throwable t) {
        throw new IllegalArgumentException("Failed to create a W3C Document from the this Doc.", t);
      }
    }
  }

  /**
   * Node is the abstract superclass of all Node items in a Document.
   * 
   * @author mschrag
   */
  public static abstract class Node extends ERXML.Item {
    public abstract org.w3c.dom.Node w3c(Document doc);
  }

  /**
   * Content is the abstract superclass of all Nodes that have text content.
   * 
   * @author mschrag
   */
  public static abstract class Content extends ERXML.Node {
    private String _text;

    /**
     * Constructs a new Content.
     * 
     * @param text the text of the node
     */
    public Content(String text) {
      _text = text;
    }

    /**
     * Returns the text of this node.
     * 
     * @return the text of this node
     */
    public String text() {
      return _text;
    }

    /**
     * Sets text text of this node.
     * 
     * @param text text text of this node
     */
    public void setText(String text) {
      _text = text;
    }

    @Override
    public void visit(Visitor visitor) {
      visitor.visit(this);
    }

    /**
     * Writes the text of this node to the writer.
     * 
     * @param writer the writer to write to
     */
    protected abstract void writeText(PrintWriter writer);

    @Override
    public void write(PrintWriter writer, int indent) {
      if (_text != null) {
        writeIndent(indent, writer);
        writeText(writer);
      }
    }
  }

  /**
   * Text represents a bare text node.
   * 
   * @author mschrag
   */
  public static class Text extends Content {
    /**
     * Creates a new text node.
     *  
     * @param text the text of the node
     */
    public Text(String text) {
      super(text);
    }

    @Override
    protected void writeText(PrintWriter writer) {
      writeEscapedString(text(), writer);
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      org.w3c.dom.Text text = doc.createTextNode(text());
      return text;
    }
  }

  /**
   * CDATA represents a CDATA section of your document.
   * 
   * @author mschrag
   */
  public static class CDATA extends Content {
    public CDATA(String text) {
      super(text);
    }

    @Override
    protected void writeText(PrintWriter writer) {
      writer.print("<![CDATA[");
      writer.print(text());
      writer.println("]]>");
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      org.w3c.dom.CDATASection cdata = doc.createCDATASection(text());
      return cdata;
    }
  }

  /**
   * Comment represents a Comment section of the document.
   * 
   * @author mschrag
   */
  public static class Comment extends Content {
    public Comment(String text) {
      super(text);
    }

    @Override
    protected void writeText(PrintWriter writer) {
      writer.print("<!-- ");
      writer.print(text());
      writer.println(" -->");
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      org.w3c.dom.Comment comment = doc.createComment(text());
      return comment;
    }
  }

  /**
   * Attr represents a key-value pair attribute of an element.
   * 
   * @author mschrag
   */
  public static class Attr {
    private String _name;
    private String _value;

    /**
     * Constructs a new attribute.
     * 
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    public Attr(String name, String value) {
      _name = name;
      _value = value;
    }

    /**
     * Returns the name of this attribute.
     * 
     * @return the name of this attribute
     */
    public String name() {
      return _name;
    }

    /**
     * Sets the name of this attribute.
     * 
     * @param name the name of this attribute
     */
    public void setName(String name) {
      _name = name;
    }

    /**
     * Returns the value of this attribute.
     * 
     * @return the value of this attribute
     */
    public String value() {
      return _value;
    }

    /**
     * Sets the value of this attribute.
     * 
     * @param value the value of this attribute
     */
    public void setValue(String value) {
      _value = value;
    }
  }

  /**
   * E represents an element of the XML document, which can
   * have attributes and children nodes.
   * 
   * @author mschrag
   */
  public static class E extends ERXML.Node {
    private String _name;
    private List<Node> _children;
    private List<Attr> _attributes;

    /**
     * Constructs a new element.
     * 
     * @param name the name of this element
     */
    public E(String name) {
      _name = name;
    }

    /**
     * Constructs a new element.
     * 
     * @param name the name of this element
     * @param text the initial text of this element
     */
    public E(String name, String text) {
      this(name);
      text(text);
    }

    /**
     * Sets the name of this element.
     * 
     * @param name the name of this element
     */
    public void setName(String name) {
      _name = name;
    }

    /**
     * Returns the name of this element.
     * 
     * @return the name of this element
     */
    public String name() {
      return _name;
    }

    @Override
    public void visit(ERXML.Visitor visitor) {
      if (visitor.visit(this) && _children != null) {
        for (ERXML.Node node : _children) {
          node.visit(visitor);
        }
      }
    }

    /**
     * Sets the attribute of the given name to the given value. If there is
     * already an attribute with this name, it is replaced.
     * 
     * @param name the name of the attribute
     * @param value the value of the attribute
     * 
     * @return this element
     */
    public ERXML.E set(String name, String value) {
      remove(name);
      add(new ERXML.Attr(name, value));
      return this;
    }

    /**
     * Sets a series of attributes in the format "name1","value1", "name2","value2", ... If any
     * name already exists, it will be replaced with the value you provide.
     * 
     * @param nvPairs an array of name-value pairs
     * 
     * @return this element
     */
    public ERXML.E set(String... nvPairs) {
      for (int i = 0; i < nvPairs.length; i += 2) {
        String name = nvPairs[i];
        String value = nvPairs[i + 1];
        set(name, value);
      }
      return this;
    }

    /**
     * Adds the given attribute to this element. This does
     * not check for duplicates.
     * 
     * @param attribute the attribute to add
     * 
     * @return this element
     */
    public ERXML.E add(ERXML.Attr attribute) {
      if (_attributes == null) {
        _attributes = new LinkedList<>();
      }
      _attributes.add(attribute);
      return this;
    }

    /**
     * Returns the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to look up
     * 
     * @return the attribute with the given name, or null if there isn't one
     */
    public ERXML.Attr getAttr(String attributeName) {
      if (_attributes != null) {
        for (ERXML.Attr attribute : _attributes) {
          if (attribute._name.equals(attributeName)) {
            return attribute;
          }
        }
      }
      return null;
    }

    /**
     * Removes the attribute with the given name.
     * 
     * @param attributeName the name of the attribute to remove
     * 
     * @return the removed attribute, or null if there wasn't one
     */
    public ERXML.Attr remove(String attributeName) {
      ERXML.Attr attribute = getAttr(attributeName);
      if (attribute != null) {
        remove(attribute);
      }
      return attribute;
    }

    /**
     * Removes the given attribute from this element.
     * 
     * @param attribute the attribute to remove
     */
    public void remove(ERXML.Attr attribute) {
      _attributes.remove(attribute);
      if (_attributes.size() == 0) {
        _attributes = null;
      }
    }

    /**
     * Returns the value of the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to look up
     * 
     * @return the value of the attribute with the given name, or null if there isn't one
     */
    public String get(String attributeName) {
      ERXML.Attr attribute = getAttr(attributeName);
      String value = null;
      if (attribute != null) {
        value = attribute.value();
      }
      return value;
    }

    /**
     * Returns the int variant of the value of the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to lookup
     * @param defaultValue the default value to return if there is no attribute, or if the attribute value is null
     * 
     * @return the int variant of the value of the attribute with the given name, or null if there isn't one.
     */
    public int getInt(String attributeName, int defaultValue) {
      int value = defaultValue;
      String text = get(attributeName);
      if (text != null) {
        value = Integer.parseInt(text);
      }
      return value;
    }

    /**
     * Returns the float variant of the value of the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to lookup
     * @param defaultValue the default value to return if there is no attribute, or if the attribute value is null
     * 
     * @return the float variant of the value of the attribute with the given name, or null if there isn't one.
     */
    public float getFloat(String attributeName, float defaultValue) {
      float value = defaultValue;
      String text = get(attributeName);
      if (text != null) {
        value = Float.parseFloat(text);
      }
      return value;
    }

    /**
     * Returns the boolean variant of the value of the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to lookup
     * @param defaultValue the default value to return if there is no attribute, or if the attribute value is null
     * 
     * @return the boolean variant of the value of the attribute with the given name, or null if there isn't one.
     */
    public boolean getBoolean(String attributeName, boolean defaultValue) {
      boolean value = defaultValue;
      String text = get(attributeName);
      if (text != null) {
        value = Boolean.parseBoolean(text);
      }
      return value;
    }

    /**
     * Returns the enum variant of the value of the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to lookup
     * @param enumClass the class of the enum to return
     * @param defaultValue the default value to return if there is no attribute, or if the attribute value is null
     * 
     * @return the enum variant of the value of the attribute with the given name, or null if there isn't one.
     */
    public <T extends Enum<T>> T getEnum(String attributeName, Class<T> enumClass, T defaultValue) {
      T value = defaultValue;
      String text = get(attributeName);
      if (text != null) {
        value = Enum.valueOf(enumClass, text);
      }
      return value;
    }

    /**
     * Returns the attributes for this element.
     * 
     * @return the attributes for this element
     */
    public List<ERXML.Attr> attributes() {
      return _attributes;
    }

    /**
     * Sets the text value of this element. This will replace an existing Text child with
     * the provided string, or create a new one if one doesn't exist. If there is already
     * a child in this element that is not a Text node, this will throw an exception.
     * 
     * @param text the new text value for this node
     * 
     * @return this element
     */
    public ERXML.E setText(String text) {
      if (_children != null) {
        if (_children.size() == 1) {
          ERXML.Node node = _children.get(0);
          if (node instanceof ERXML.Text) {
            ((ERXML.Text) node).setText(text);
          }
          else {
            throw new IllegalStateException("There was already a non-text child of this element: " + node);
          }
        }
        else {
          throw new IllegalStateException("There was more than one child of this element: " + this);
        }
      }
      else {
        text(text);
      }
      return this;
    }

    /**
     * Returns the text value of this element, or null if there isn't one. If there is a non-text
     * child of this element, this will throw an exception.
     * 
     * @return the text value of this element
     */
    public ERXML.Text textNode() {
      ERXML.Text textNode = null;
      if (_children != null) {
        if (_children.size() == 1) {
          ERXML.Node node = _children.get(0);
          if (node instanceof ERXML.Text) {
            textNode = (ERXML.Text) node;
          }
          else {
            throw new IllegalStateException("There was only a non-text child of this element: " + node);
          }
        }
        else {
          throw new IllegalStateException("There was more than one child of this element: " + this);
        }
      }
      return textNode;
    }

    /**
     * Returns the text value of this element, or null if there isn't one. If there is a non-text
     * child of this element, this will throw an exception.
     * 
     * @return the text value of this element
     */
    public String text() {
      ERXML.Text textNode = textNode();
      String text = (textNode != null) ? textNode.text() : null;
      return text;
    }

    /**
     * Returns the children nodes of this element.
     * 
     * @return the children nodes of this element
     */
    public List<ERXML.Node> children() {
      return _children;
    }

    /**
     * Returns the text of the child element with the given name from this element, or null if there isn't one.
     * 
     * @param name the name of the element to look up
     * 
     * @return the text of the child element with the given name from this element, or null if there isn't one
     */
    public String childText(String name) {
      ERXML.E child = child(name);
      return (child != null) ? child.text() : null;
    }

    /**
     * Returns the int variant of the child element with the given name from this element, or defaultValue if there isn't one.
     * 
     * @param name the name of the element to look up
     * @param defaultValue the default value to return if there is no child node, or if the child node is empty
     * 
     * @return the int variant of the child element with the given name from this element, or defaultValue if there isn't one.
     */
    public int childInt(String name, int defaultValue) {
      int value = defaultValue;
      ERXML.E child = child(name);
      if (child != null) {
        String text = child.text();
        if (text != null) {
          value = Integer.parseInt(text);
        }
      }
      return value;
    }

    /**
     * Returns the float variant of the child element with the given name from this element, or defaultValue if there isn't one.
     * 
     * @param name the name of the element to look up
     * @param defaultValue the default value to return if there is no child node, or if the child node is empty
     * 
     * @return the float variant of the child element with the given name from this element, or defaultValue if there isn't one.
     */
    public float childFloat(String name, float defaultValue) {
      float value = defaultValue;
      ERXML.E child = child(name);
      if (child != null) {
        String text = child.text();
        if (text != null) {
          value = Float.parseFloat(text);
        }
      }
      return value;
    }

    /**
     * Returns the boolean variant of the child element with the given name from this element, or defaultValue if there isn't one.
     * 
     * @param name the name of the element to look up
     * @param defaultValue the default value to return if there is no child node, or if the child node is empty
     * 
     * @return the boolean variant of the child element with the given name from this element, or defaultValue if there isn't one.
     */
    public boolean childBoolean(String name, boolean defaultValue) {
      boolean value = defaultValue;
      ERXML.E child = child(name);
      if (child != null) {
        String text = child.text();
        if (text != null) {
          value = Boolean.parseBoolean(text);
        }
      }
      return value;
    }

    /**
     * Returns the enum variant of the child element with the given name from this element, or defaultValue if there isn't one.
     * 
     * @param name the name of the element to look up
     * @param enumClass the class of the enum to return
     * @param defaultValue the default value to return if there is no child node, or if the child node is empty
     * @return the enum variant of the child element with the given name from this element, or defaultValue if there isn't one.
     * 
     */
    public <T extends Enum<T>> T childEnum(String name, Class<T> enumClass, T defaultValue) {
      T value = defaultValue;
      ERXML.E child = child(name);
      if (child != null) {
        String text = child.text();
        if (text != null) {
          value = Enum.valueOf(enumClass, text);
        }
      }
      return value;
    }

    /**
     * Returns the child element with the given name from this element, or null if there isn't one.
     * 
     * @param name the name of the element to look up
     * 
     * @return the child element with the given name from this element, or null if there isn't one
     */
    public ERXML.E child(String name) {
      ERXML.E matchingElement = null;
      if (_children != null) {
        for (ERXML.Node node : _children) {
          if (node instanceof ERXML.E && ((ERXML.E) node)._name.equals(name)) {
            if (matchingElement == null) {
              matchingElement = (ERXML.E) node;
            }
            else {
              throw new IllegalStateException("There was more than one child named '" + name + "'.");
            }
          }
        }
      }
      return matchingElement;
    }

    /**
     * Returns a list of direct children of this element that have the given name, or an empty
     * list if there aren't any.
     *  
     * @param name the name of the children elements to look up
     * 
     * @return a list of direct children of this element that have the given name, or an empty
     * list if there aren't any
     */
    public List<ERXML.E> children(String name) {
      List<ERXML.E> children = new LinkedList<>();
      if (_children != null) {
        for (ERXML.Node node : _children) {
          if (node instanceof ERXML.E && ((ERXML.E) node)._name.equals(name)) {
            children.add((ERXML.E) node);
          }
        }
      }
      return children;
    }

    /**
     * Returns a set of the text of the descendent elements of this element that have the given name, or an empty
     * list if there aren't any.
     *  
     * @param name the name of the descendent elements to look up
     * 
     * @return a set of the text of the descendents of this element that have the given name, or an empty
     * set if there aren't any
     */
    public Set<String> descendentsText(String name) {
      Set<ERXML.E> descendents = descendents(name);
      Set<String> descendentsText = new LinkedHashSet<>();
      for (ERXML.E descendent : descendents) {
        descendentsText.add(descendent.text());
      }
      return descendentsText;
    }

    /**
     * Returns a set of descendent elements of this element that have the given name, or an empty
     * list if there aren't any.
     *  
     * @param name the name of the descendent elements to look up
     * 
     * @return a set of descendents of this element that have the given name, or an empty
     * set if there aren't any
     */
    public Set<ERXML.E> descendents(final String name) {
      final Set<ERXML.E> descendents = new LinkedHashSet<>();
      if (_children != null) {
        ERXML.Visitor visitor = new ERXML.Visitor() {
          public boolean visit(Item item) {
            if (item instanceof ERXML.E && ((ERXML.E) item)._name.equals(name)) {
              descendents.add((ERXML.E) item);
            }
            return true;
          }
        };

        for (ERXML.Node node : _children) {
          node.visit(visitor);
        }
      }
      return descendents;
    }

    /**
     * Creates and appends a new element to this element.
     *  
     * @param name the name of the new element
     * 
     * @return the new element
     */
    public ERXML.E e(String name) {
      return add(ERXML.e(name));
    }

    /**
     * Creates and appends a new element to this element.
     *  
     * @param name the name of the new element
     * @param value the text value of the new element
     * 
     * @return the new element
     */
    public ERXML.E e(String name, String value) {
      return add(ERXML.e(name, value));
    }

    /**
     * Creates and appends a new text node to this element.
     *  
     * @param text the text value to append
     * 
     * @return the new text node
     */
    public ERXML.Text text(String text) {
      return add(ERXML.text(text));
    }

    /**
     * Creates and appends a new CDATA node to this element.
     * 
     * @param cdata the cdata value to append
     * 
     * @return the new cdata node
     */
    public ERXML.CDATA cdata(String cdata) {
      return add(ERXML.cdata(cdata));
    }

    /**
     * Creates and appends a new comment node to this element.
     * 
     * @param comment the comment value to append
     * 
     * @return the new comment node
     */
    public ERXML.Comment comment(String comment) {
      return add(ERXML.comment(comment));
    }

    /**
     * Removes the given node from this element.
     * 
     * @param child the child node to remove
     */
    public void remove(ERXML.Node child) {
      if (_children != null) {
        _children.remove(child);
        if (_children.size() == 0) {
          _children = null;
        }
      }
    }

    /**
     * Adds a new node to this element.
     * 
     * @param <T> the type of the node to add
     * @param child the child to add
     * 
     * @return the added child
     */
    public <T extends ERXML.Node> T add(T child) {
      child.setParent(this);
      if (_children == null) {
        _children = new LinkedList<>();
      }
      _children.add(child);
      return child;
    }

    /**
     * Writes the attributes of this element to the writer.
     * 
     * @param writer the writer to write attributes to
     */
    protected void writeAttributes(PrintWriter writer) {
      if (_attributes != null) {
        for (ERXML.Attr attribute : _attributes) {
          writer.print(" ");
          writer.print(attribute.name());
          writer.print("=\"");
          writeEscapedString(attribute.value(), writer);
          writer.print("\"");
        }
      }
    }

    @Override
    public void write(PrintWriter writer, int indent) {
      writeIndent(indent, writer);
      if (_children != null && _children.size() > 0) {
        writer.print("<");
        writer.print(_name);
        writeAttributes(writer);
        writer.print(">");
        if (_children.size() == 1 && _children.get(0) instanceof ERXML.Text) {
          _children.get(0).write(writer, 0);
        }
        else {
          writer.println();
          for (ERXML.Node node : _children) {
            node.write(writer, indent + 1);
          }
          writeIndent(indent, writer);
        }
        writer.print("</");
        writer.print(_name);
        writer.println(">");
      }
      else {
        writer.print("<");
        writer.print(_name);
        writeAttributes(writer);
        writer.println(" />");
      }
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      Element e = doc.createElement(_name);
      if (_attributes != null) {
        for (ERXML.Attr attribute : _attributes) {
          e.setAttribute(attribute.name(), attribute.value());
        }
      }
      if (_children != null) {
        for (ERXML.Node child : _children) {
          org.w3c.dom.Node childNode = child.w3c(doc);
          e.appendChild(childNode);
        }
      }
      return e;
    }
  }

  /**
   * Visitor is an interface that can be passed to the visit
   * method of any XML.Item to walk the DOM.
   * 
   * @author mschrag
   */
  public static interface Visitor {
    /**
     * Called by the visit method of each item.
     * 
     * @param item the item being visited
     * 
     * @return true if the children of the given item should be visited, false if it should skip the children
     */
    public boolean visit(ERXML.Item item);
  }

  /**
   * Creates and returns a new Document.
   * 
   * @return a new document
   */
  public static ERXML.Doc doc() {
    return new ERXML.Doc();
  }

  /**
   * Creates and return a document parsed from the given string.
   * 
   * @param documentString the string to parse as XML
   * 
   * @return a new parsed document
   */
  public static ERXML.Doc doc(String documentString) {
    try {
      ERXML.Doc doc;
      if (documentString == null || documentString.trim().length() == 0) {
        doc = ERXML.doc();
      }
      else {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(documentString)));
        doc = ERXML.doc(document);
      }
      return doc;
    }
    catch (Throwable t) {
      throw new IllegalArgumentException("Failed to parse a document from the provided string.", t);
    }
  }

  /**
   * Creates and return a document parsed from the given reader.
   * 
   * @param reader the reader to parse from
   * 
   * @return a new parsed document
   */
  public static ERXML.Doc doc(Reader reader) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(reader));
      return ERXML.doc(document);
    }
    catch (Throwable t) {
      throw new IllegalArgumentException("Failed to parse a document from the provided reader.", t);
    }
  }

  /**
   * Creates and return a document parsed from the given file.
   * 
   * @param file the file to parse from
   * 
   * @return a new parsed document
   */
  public static ERXML.Doc doc(File file) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
      return ERXML.doc(document);
    }
    catch (Throwable t) {
      throw new IllegalArgumentException("Failed to parse a document from the provided file.", t);
    }
  }

  /**
   * Converts a W3C Element into an XML.E.
   * 
   * @param w3cElement the W3C Element
   * 
   * @return the equivalent XML.E
   */
  public static ERXML.E e(Element w3cElement) {
    ERXML.E e = ERXML.e(w3cElement.getNodeName());
    org.w3c.dom.NamedNodeMap attributes = w3cElement.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      org.w3c.dom.Node w3cAttribute = attributes.item(i);
      String attributeName = w3cAttribute.getNodeName();
      String attributeValue = w3cAttribute.getNodeValue();
      e.set(attributeName, attributeValue);
    }
    org.w3c.dom.NodeList w3cChildren = w3cElement.getChildNodes();
    for (int i = 0; i < w3cChildren.getLength(); i++) {
      org.w3c.dom.Node w3cChild = w3cChildren.item(i);
      if (w3cChild instanceof org.w3c.dom.Text) {
        e.text(((org.w3c.dom.Text) w3cChild).getNodeValue());
      }
      else if (w3cChild instanceof org.w3c.dom.CDATASection) {
        e.cdata(((org.w3c.dom.CDATASection) w3cChild).getNodeValue());
      }
      else if (w3cChild instanceof org.w3c.dom.Comment) {
        e.comment(((org.w3c.dom.Comment) w3cChild).getNodeValue());
      }
      else if (w3cChild instanceof org.w3c.dom.Element) {
        e.add(ERXML.e((org.w3c.dom.Element) w3cChild));
      }
      else {
        throw new IllegalArgumentException("Unable to handle nodes of type '" + w3cChild + "'.");
      }
    }
    return e;
  }

  /**
   * Converts a W3C Document into an XML.Doc.
   * 
   * @param w3cDocument the W3C Document
   * 
   * @return the equivalent XML.Doc
   */
  public static ERXML.Doc doc(org.w3c.dom.Document w3cDocument) {
    org.w3c.dom.Element w3cElement = w3cDocument.getDocumentElement();
    Doc doc = ERXML.doc();
    doc.setRoot(ERXML.e(w3cElement));
    return doc;
  }

  /**
   * Creates and returns a new Document. The first item that is an element
   * will be set as the root element of the document.
   * 
   * @param children the children to add to the document
   * 
   * @return a new element
   */
  public static ERXML.Doc doc(ERXML.Item... children) {
    ERXML.Doc doc = ERXML.doc();
    for (ERXML.Item child : children) {
      doc.add(child);
    }
    return doc;
  }

  /**
   * Creates and returns a new Declaration.
   * 
   * @param version the version of the declaration
   * @param encoding the encoding of the declaration
   * 
   * @return a new declaration
   */
  public static ERXML.Declaration declaration(String version, String encoding) {
    return new ERXML.Declaration(version, encoding);
  }

  /**
   * Creates and returns a new Element.
   * 
   * @param name the name of the element
   * 
   * @return a new element
   */
  public static ERXML.E e(String name) {
    return new ERXML.E(name);
  }

  /**
   * Creates and returns a new Element.
   * 
   * @param name the name of the element
   * @param children the children to add to this element (String, XML.Node, or XML.Attr)
   * 
   * @return a new element
   */
  public static ERXML.E e(String name, Object... children) {
    ERXML.E e = ERXML.e(name);
    for (Object child : children) {
      if (child instanceof String) {
        e.text((String) child);
      }
      else if (child instanceof ERXML.Node) {
        e.add((ERXML.Node) child);
      }
      else if (child instanceof ERXML.Attr) {
        e.add((ERXML.Attr) child);
      }
      else {
        throw new IllegalArgumentException("Unable to add the object '" + child + "' to an XML element.");
      }
    }
    return e;
  }

  /**
   * Creates and returns a new Element.
   * 
   * @param name the name of the element
   * @param text the text of the element
   * 
   * @return a new element
   */
  public static ERXML.E e(String name, String text) {
    return new ERXML.E(name, text);
  }

  /**
   * Creates and returns a new attribute.
   * 
   * @param name the name of the attribute
   * @param value the value of the attribute
   * 
   * @return a new attribute
   */
  public static ERXML.Attr a(String name, String value) {
    return new ERXML.Attr(name, value);
  }

  /**
   * Creates and returns a new text node.
   * 
   * @param text the text of the node
   * 
   * @return a new text node
   */
  public static ERXML.Text text(String text) {
    return new ERXML.Text(text);
  }

  /**
   * Creates and returns a new cdata node.
   * 
   * @param cdata the text of the cdata node
   * 
   * @return a new cdata node
   */
  public static ERXML.CDATA cdata(String cdata) {
    return new ERXML.CDATA(cdata);
  }

  /**
   * Creates and returns a new comment node.
   * 
   * @param comment the text of the comment node
   * 
   * @return a new comment node
   */
  public static ERXML.Comment comment(String comment) {
    return new ERXML.Comment(comment);
  }
}
