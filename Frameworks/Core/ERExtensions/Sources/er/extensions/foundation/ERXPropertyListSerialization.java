//Copyright (c) 1999, Apple Computer, Inc. All rights reserved.

package er.extensions.foundation;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.CharEncoding;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSBase64;
import com.webobjects.foundation._NSStreamingOutputData;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.foundation._NSUtilities;

/**
 * <p>
 * This class provides static methods that convert between property lists and their string representations, which can be either strings or NSData objects. A property list is a structure that represents organized data. It can be built from a combination of NSArray, NSDictionary, String, and NSData
 * objects.
 * </p>
 * <p>
 * The string representation can be in XML or the ASCII plist format. To distinguish between the two formats, the parser that converts strings to property lists finds out whether the string starts with <code>&lt;?xml</code>. A discussion of the ASCII plist format,
 * <em>A Primer on ASCII Property Lists</em>, is available in the Mac OS X section of the Apple Developer Connection website. A discussion of XML property lists, <em>Property List Services</em>, is also available in the same area of the Apple Developer Connection website.
 * </p>
 * Some methods do not support XML property list representations, specifically <code>booleanForString</code> and <code>intForString</code>. Also note that XML property lists de-serialize 'integer' value types to java.math.BigInteger and 'real' value types ot java.math.BigDecimal.
 * <p>
 * The ERXPropertyListSerialization class cannot be instantiated. There is an alternative Binary plist format.
 * </p></br>
 *JSON Serialization Example:
 *
 * <pre>
 * NSDictionary dict<String,Object> = new NSDictionary<String,Object>(new String[] { "one", "two" }, new Object[] {Integer.valueOf(1), Integer.valueOf(2)});
 * String jsonString = ERXPropertyListSerialization.jsonStringFromPropertyList(dict);
 * </pre>
 *
 * JSON Deserialization Example:
 *
 * <pre>
 * NSDictionary&lt;String, Object&gt;	result	= ERXPropertyListSerialization.&lt;String, Object&gt; dictionaryForJSONString(jsonString);
 * </pre>
 *
 * If you know that you are recieving a JSON array, you can use the convenience API:
 *
 * <pre>
 * NSArray	result	= ERXPropertyListSerialization.arrayForJSONString(jsonString);
 * </pre>
 *
 * Binary PList Example:
 *
 * <pre>
 * try {
 * 	URLConnection conn = url.openConnection();
 * 	InputStream is = conn.getInputStream();
 * 	NSDictionary plist = ERXPropertyListSerialization.dictionaryForBinaryStream(is);
 * } catch (RuntimeException e) {
 * 	e.printStackTrace();
 * } catch (IOException e) {
 * 	e.printStackTrace();
 * }
 * </pre>
 *
 * Serialization to an OutputStream:
 *
 * <pre>
 * File tempFile = File.createTempFile(&quot;myPlist&quot;, &quot;plist&quot;);
 * FileOutputStream out = null;
 * try {
 * 	out = new FileOutputStream(tempFile);
 * 	ERXPropertyListSerialization.propertyListWriteToStream(plist, out, ERXPropertyListSerialization.PListFormat.NSPropertyListXMLFormat_v1_0);
 * } catch (Exception e) {
 * 	e.printStackTrace();
 * } finally {
 * 	if (out != null) {
 * 		out.close();
 * 	}
 * }
 * </pre>
 *
 * @see #booleanForString
 * @see #intForString
 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
 * @see PListFormat#NSPropertyListXMLFormat_v1_0
 */
public class ERXPropertyListSerialization {
	static org.apache.log4j.Logger	logger							= org.apache.log4j.Logger.getLogger(ERXPropertyListSerialization.class);

	/**
	 *
	 */
	public static final Class<?>	_CLASS							= _NSUtilities._classWithFullySpecifiedName("er.extensions.foundation.ERXPropertyListSerialization");

	private final static int		EOT								= -1;

	/**
	 * Convenience for methods to convert to plist. Returns true..
	 */
	public static final boolean		Indents							= true;

	/**
	 * Convenience for methods to convert to plist. Returns false.
	 */
	public static final boolean		NoIndents						= false;

	/**
	 * Convenience for methods to convert to xml plist. Returns true..
	 */
	public static final boolean		ForceXML						= true;

	/**
	 * Null value
	 */
	public static final String		NULL							= "NULL";

	/** yyyy-MM-dd'T'HH:mm:ss'Z' */
	protected static final String	DefaultSimpleDateFormatPattern	= "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	protected static final double kCFAbsoluteTimeIntervalSince1970 = 978307200L;

	/**
	 * Types of property lists (as specified in CoreFoundation)
	 */
	public enum PListFormat {
		/** Legacy plist format */
		NSPropertyListOpenStepFormat,
		/** XML plist v 1.0 */
		NSPropertyListXMLFormat_v1_0,
		/** Binary formatted plist */
		NSPropertyListBinaryFormat_v1_0,
		/** Jason formatted plist */
		NSPropertyListJsonFormat_v1_0
	}

	/**
	 * Data types found in property lists (as specified in CoreFoundation)
	 */
	public enum PListType {
		ARRAY, SET, BOOLEAN, DATA, DATE, DICTIONARY, FLOAT, INTEGER, STRING, @SuppressWarnings("hiding")
		NULL, FILL, UUID, UNKNOWN
	}

	/**
	 * This class is intentionally undocumented
	 */
	@SuppressWarnings("unqualified-field-access")
	public static abstract class _PListParser {
		private boolean	_indents;

		/**
		 * @param indents
		 */
		public _PListParser(boolean indents) {
			_indents = indents;
		}

		/**
		 * @param buffer
		 * @param i
		 */
		protected void _appendIndentationToStringBuffer(StringBuffer buffer, int i) {
			if (_indents) {
				for (int j = 0; j < i; j++)
					buffer.append('\t');
			}
		}

		/**
		 * @param buffer
		 * @param i
		 */
		protected void _appendNewLineToStringBuffer(StringBuffer buffer, int i) {
			if (_indents) {
				buffer.append('\n');
			}
		}

		/**
		 * @param string
		 * @return Object
		 */
		public abstract Object parseStringIntoPlist(String string);

	}

	/**
	 * This class is intentionally undocumented
	 */
	public static class _XML extends _PListParser {
		private static org.apache.log4j.Logger	logger2	= org.apache.log4j.Logger.getLogger(_XML.class);

		/**
		 *
		 */
		protected static SAXParserFactory		_parserFactory;

		/**
		 *
		 */
		protected SimpleDateFormat				_dateFormat;

		/**
		 * This class is intentionally undocumented
		 */
		@SuppressWarnings("unqualified-field-access")
		public static class DictionaryParser extends DefaultHandler {
			private static org.apache.log4j.Logger	logger1							= org.apache.log4j.Logger.getLogger(DictionaryParser.class);

			static String							PUBLIC_APPLE_COMPUTER_PLIST_1_0	= "-//Apple Computer//DTD PLIST 1.0//EN";

			static String							PUBLIC_APPLE_PLIST_1_0			= "-//Apple//DTD PLIST 1.0//EN";

			/**
			 *
			 */
			protected SimpleDateFormat				_dateFormat;

			/**
			 *
			 */
			protected Stack<XMLNode>				_stack;

			/**
			 *
			 */
			protected Object						_plist;

			/**
			 *
			 */
			protected StringBuffer					_curChars;

			/**
			 * This class is intentionally undocumented
			 */
			public static class XMLNode {

				/**
				 * Enum for all valid data types within a Apple XML plist
				 */
				public enum Type {
					/**
					 *
					 */
					PLIST("plist", true),
					/**
					 *
					 */
					STRING("string", true),
					/**
					 *
					 */
					KEY("key", true),
					/**
					 *
					 */
					DATA("data", true),
					/**
					 *
					 */
					DATE("date", true),
					/**
					 *
					 */
					INTEGER("integer", true),
					/**
					 *
					 */
					REAL("real", true),
					/**
					 *
					 */
					BOOLEAN("boolean", true),
					/**
					 *
					 */
					ARRAY("array", true),
					/**
					 *
					 */
					DICTIONARY("dict", true),
					/**
					 *
					 */
					TRUE("true", false),
					/**
					 *
					 */
					FALSE("false", false),
					/**
					 *
					 */
					UNKNOWN("unknown", true);

					String	_qName;

					String	_openTag;

					String	_closeTag;

					Type(String qName, boolean content) {
						_qName = qName;
						if (content) {
							_openTag = "<" + qName + ">";
							_closeTag = "</" + qName + ">";
						} else {
							_openTag = "<" + qName + "/>";
							_closeTag = "";
						}
					}

					/**
					 * @return qName
					 */
					public String qName() {
						return _qName;
					}

					/**
					 * @return open tag for the type
					 */
					public String openTag() {
						return _openTag;
					}

					/**
					 * @return close tag for the type
					 */
					public String closeTag() {
						return _closeTag;
					}

					/**
					 * @param qName
					 * @return node type for the name
					 */
					public static Type typeForName(String qName) {
						if (qName != null) {
							String aQName = qName.toLowerCase();
							for (Type aType : Type.values()) {
								if (aType._qName.equals(aQName))
									return aType;
							}
						}
						return UNKNOWN;
					}

					@Override
					public String toString() {
						return _qName;
					}
				}

				/**
				 *
				 */
				protected Type		_type;

				/**
				 *
				 */
				protected Object	_value;

				/**
				 *
				 */
				protected boolean	_tag_open;

				/**
				 * @param type
				 * @param value
				 * @param tag_open
				 */
				public XMLNode(Type type, Object value, boolean tag_open) {
					super();
					_type = type;
					_value = value;
					_tag_open = tag_open;
				}

				/**
				 * @param type
				 * @param value
				 */
				public XMLNode(Type type, Object value) {
					this(type, value, true);
				}

				/**
				 * @param type
				 */
				public XMLNode(Type type) {
					this(type, null, true);
				}

				/**
				 * @param type
				 * @param value
				 * @param tag_open
				 */
				public XMLNode(String type, Object value, boolean tag_open) {
					this(Type.typeForName(type), value, tag_open);
				}

				/**
				 * @param type
				 * @param value
				 */
				public XMLNode(String type, Object value) {
					this(type, value, true);
				}

				/**
				 * @param type
				 */
				public XMLNode(String type) {
					this(type, null, true);
				}

				/**
				 * @return type
				 */
				public Type type() {
					return _type;
				}

				/**
				 * @return value
				 */
				public Object value() {
					return _value;
				}

				/**
				 * @param value
				 */
				public void setValue(Object value) {
					_value = value;
				}

				/**
				 * @return true if the tag is open
				 */
				public boolean tagOpen() {
					return _tag_open;
				}

				/**
				 * @param value
				 */
				public void setTagOpen(boolean value) {
					_tag_open = value;
				}

				@Override
				public String toString() {
					return "type = " + _type + "; object = " + _value + "; open = " + _tag_open;
				}
			}

			/**
			 *
			 */
			public DictionaryParser() {
				super();
				_dateFormat = new SimpleDateFormat(DefaultSimpleDateFormatPattern);
				_dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				_dateFormat.setLenient(true);
			}

			/**
			 * @return parsed plist
			 */
			public Object plist() {
				return _plist;
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				_curChars.append(ch, start, length);
			}

			@Override
			public void endDocument() throws SAXException {
				if (!_stack.empty()) {
					if (_stack.size() == 1) {
						XMLNode lastNode = _stack.pop();
						if (!lastNode.tagOpen()) {
							_plist = lastNode.value();
						} else {
							throw new SAXException("Starting <" + lastNode.type() + "> node was never ended.");
						}
					} else {
						throw new SAXException("A plist may only contain one top-level node.");
					}
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				XMLNode.Type aType = XMLNode.Type.typeForName(qName);

				if (XMLNode.Type.PLIST.equals(aType)) {
					if (_stack.size() != 2)
						throw new SAXException("A plist may only contain one top-level node, and all tags must have end tags.");
					XMLNode lastNode = _stack.pop();
					if (!lastNode.tagOpen()) {
						_plist = lastNode.value();
						_stack.pop();
					} else {
						throw new SAXException("Starting <" + lastNode.type() + "> tag was never ended.");
					}
					return;
				}
				saveCharContent();
				switch (aType) {
					case PLIST:
						// This has been taken care of before
						break;
					case ARRAY: {
						NSMutableArray<Object> array = new NSMutableArray<Object>();
						boolean foundOpenTag = false;
						while (!_stack.isEmpty()) {
							XMLNode currentNode = _stack.peek();
							if (currentNode.tagOpen()) {
								if (XMLNode.Type.ARRAY.equals(currentNode.type())) {
									foundOpenTag = true;
									currentNode.setValue(array);
									currentNode.setTagOpen(false);
								} else {
									throw new SAXException("Ending <" + qName + "> tag does not match starting <" + currentNode.type() + "> tag.");
								}
								break;
							}
							if (currentNode.value() != null)
								array.insertObjectAtIndex(currentNode.value(), 0);
							_stack.pop();
						}
						if (!foundOpenTag)
							throw new SAXException("No starting <array> tag.");
						break;
					}
					case DICTIONARY: {
						NSMutableDictionary<Object, Object> dictionary = new NSMutableDictionary<Object, Object>();
						boolean foundOpenTag = false;
						while (!_stack.isEmpty()) {
							XMLNode currentNode = _stack.peek();
							if (currentNode.tagOpen()) {
								if (XMLNode.Type.DICTIONARY.equals(currentNode.type())) {
									foundOpenTag = true;
									currentNode.setValue(dictionary);
									currentNode.setTagOpen(false);
								} else {
									throw new SAXException("Ending <" + qName + "> tag does not match starting <" + currentNode.type() + "> tag.");
								}
								break;
							}
							if (_stack.size() > 1) {
								Object obj = currentNode.value();
								_stack.pop();
								currentNode = _stack.peek();
								if (XMLNode.Type.KEY.equals(currentNode.type())) {
									if ((obj != null) && (currentNode.value() != null))
										dictionary.setObjectForKey(obj, currentNode.value());
									_stack.pop();
								} else {
									throw new SAXException("Key must be before the value.");
								}
							} else {
								throw new SAXException("All values in a dictionary must have corresponding keys.");
							}
						}
						if (!foundOpenTag)
							throw new SAXException("No starting <" + qName + "> tag.");
						break;
					}
					case UNKNOWN:
						break;
					case STRING: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue("");
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case KEY: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue("");
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case DATE: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue(new NSTimestamp());
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case INTEGER: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue(BigInteger.ZERO);
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case REAL: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue(BigDecimal.ZERO);
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case BOOLEAN: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue(Boolean.FALSE);
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case TRUE: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue(Boolean.TRUE);
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case FALSE: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue(Boolean.FALSE);
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
					case DATA: {
						XMLNode lastNode = _stack.peek();
						if (aType.equals(lastNode.type())) {
							if (lastNode.value() == null) {
								lastNode.setValue(new NSData());
								lastNode.setTagOpen(false);
							}
						} else {
							throw new SAXException("Ending <" + qName + "> tag does not match starting <" + lastNode.type() + "> tag.");
						}
						break;
					}
				}
			}

			@Override
			public void error(SAXParseException exception) throws SAXException {
				logger1.error("Parse error : ", exception);
				// throw exception;
			}

			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				logger1.error("Parse fatal error : ", exception);
				throw exception;
			}

			@Override
			public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {/* Not used */}

			@Override
			public void processingInstruction(String target, String data) throws SAXException {/* Not used */}

			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
				InputSource inputsource = null;
				// We are not resolving any external entities in
				// http://www.apple.com/DTDs/PropertyList-1.0.dtd, so there is really no need
				// to waste bandwidth and upset customers. However, we leave the flexibilty
				// for future versions.
				if (PUBLIC_APPLE_PLIST_1_0.equals(publicId) || PUBLIC_APPLE_COMPUTER_PLIST_1_0.equals(publicId))
					inputsource = new InputSource(new StringReader(""));
				return inputsource;
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) {
				XMLNode.Type aType = XMLNode.Type.typeForName(qName);
				switch (aType) {
					case TRUE:
						_stack.push(new XMLNode(aType, Boolean.TRUE, false));
						break;
					case FALSE:
						_stack.push(new XMLNode(aType, Boolean.FALSE, false));
						break;
					default:
						_stack.push(new XMLNode(aType));
						break;

				}
				_curChars = new StringBuffer();
			}

			@Override
			public void startDocument() throws SAXException {
				_stack = new Stack<XMLNode>();
				_plist = null;
				_curChars = new StringBuffer();
			}

			private void saveCharContent() throws SAXException {
				if (_curChars.length() == 0)
					return;
				XMLNode lastNode = _stack.peek();
				if (lastNode.tagOpen()) {
					switch (lastNode.type()) {
						case PLIST:
							break;
						case ARRAY:
							break;
						case DICTIONARY:
							break;
						case UNKNOWN:
							lastNode.setTagOpen(false);
							break;
						case STRING:
							lastNode.setValue(unescapeString(_curChars.toString()));
							lastNode.setTagOpen(false);
							break;
						case KEY:
							lastNode.setValue(unescapeString(_curChars.toString()));
							lastNode.setTagOpen(false);
							break;
						case DATE:
							try {
								lastNode.setValue(new NSTimestamp(_dateFormat.parse(_curChars.toString())));
							} catch (Exception exception) {
								throw new SAXException("Unable to convert value <" + _curChars.toString() + "> to timestamp.");
							}
							lastNode.setTagOpen(false);
							break;
						case INTEGER:
							try {
								lastNode.setValue(new BigInteger(_curChars.toString()));
							} catch (Exception exception) {
								throw new SAXException("Unable to convert value <" + _curChars.toString() + "> to integer.");
							}
							lastNode.setTagOpen(false);
							break;
						case REAL:
							try {
								lastNode.setValue(new BigDecimal(_curChars.toString()));
							} catch (Exception exception) {
								throw new SAXException("Unable to convert value <" + _curChars.toString() + "> to float.");
							}
							lastNode.setTagOpen(false);
							break;
						case BOOLEAN:
							lastNode.setValue(Boolean.valueOf(_curChars.toString()));
							lastNode.setTagOpen(false);
							break;
						case TRUE:
							lastNode.setValue(Boolean.TRUE);
							lastNode.setTagOpen(false);
							break;
						case FALSE:
							lastNode.setValue(Boolean.FALSE);
							lastNode.setTagOpen(false);
							break;
						case DATA:
							try {
								StringBuilder stringbuffer = new StringBuilder(_curChars.length());
								for (int i = 0; i < _curChars.length(); i++)
									if (!Character.isWhitespace(_curChars.charAt(i)))
										stringbuffer.append(_curChars.charAt(i));

								byte abyte0[] = stringbuffer.toString().getBytes(CharEncoding.US_ASCII);
								byte abyte64[] = _NSBase64.decode(abyte0);
								if (abyte64 != null && abyte64.length > 0) {
									lastNode.setValue(new NSData(abyte64));
								} else {
									lastNode.setValue(new NSData()); // assume empty data
								}
								lastNode.setTagOpen(false);
							} catch (UnsupportedEncodingException unsupportedencodingexception) {
								throw new SAXException(unsupportedencodingexception.getMessage());
							}
							lastNode.setTagOpen(false);
							break;
					}
				}
			}

			@Override
			public void warning(SAXParseException exception) throws SAXException {
				logger1.warn("Parse warning : ", exception);
				// throw exception;
			}

			/**
			 * @param toRestore
			 * @return result string
			 */
			protected String unescapeString(String toRestore) {
				String result = toRestore.replace("&amp;", "&");
				result = toRestore.replace("&lt;", "<");
				result = toRestore.replace("&gt;", ">");
				result = toRestore.replace("&apos;", "'");
				result = toRestore.replace("&quot;", "\"");
				result = toRestore.replace("&lt;", "<");
				return result;
			}

		}

		/**
		 *
		 */
		public _XML() {
			this(true);
		}

		/**
		 * @param indents
		 *            true if the result string must be indented
		 */
		public _XML(boolean indents) {
			super(indents);
			// http://www.w3.org/TR/NOTE-datetime
			_dateFormat = new SimpleDateFormat(DefaultSimpleDateFormatPattern);
			_dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			_dateFormat.setLenient(true);
		}

		/**
		 * @return SAX parser factory
		 */
		public static SAXParserFactory parserFactory() {
			if (_parserFactory == null) {
				try {
					_parserFactory = SAXParserFactory.newInstance();
				} catch (Exception exception) {
					logger2.warn("Exception ", exception);
				}
			}
			return _parserFactory;
		}

		/**
		 * @return SAX parser
		 */
		public SAXParser newSAXParser() {
			if (_XML.parserFactory() != null) {
				try {
					return _XML.parserFactory().newSAXParser();
				} catch (Exception exception) {
					logger2.warn("Exception ", exception);
				}
			}
			return null;
		}

		@Override
		public Object parseStringIntoPlist(String string) {
			DictionaryParser dictionaryParser = new DictionaryParser();
			try {
				SAXParser parser = newSAXParser();
				if (parser != null)
					parser.parse(new InputSource(new StringReader(string)), dictionaryParser);
			} catch (SAXException exception) {
				logger2.warn("Exception ", exception);
				if (exception instanceof SAXParseException) {
					throw new RuntimeException("Parsing failed in line " + ((SAXParseException) exception).getLineNumber() + ", column "
							+ ((SAXParseException) exception).getColumnNumber(), exception);
				}
				throw new RuntimeException(exception);
			} catch (IOException ioexception) {
				throw NSForwardException._runtimeExceptionForThrowable(ioexception);
			}
			return dictionaryParser.plist();
		}

		/**
		 * @param plist
		 * @return xml encoded property list
		 */
		public String stringFromPropertyList(Object plist) {
			if (plist == null)
				return null;
			StringBuffer stringbuffer = new StringBuffer(128);
			stringbuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			_appendNewLineToStringBuffer(stringbuffer, 0);
			stringbuffer.append("<!DOCTYPE plist PUBLIC \"" + DictionaryParser.PUBLIC_APPLE_PLIST_1_0 + "\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
			_appendNewLineToStringBuffer(stringbuffer, 0);
			stringbuffer.append("<plist version=\"1.0\">");
			_appendNewLineToStringBuffer(stringbuffer, 0);
			_appendObjectToStringBuffer(plist, stringbuffer, 1);
			stringbuffer.append("</plist>");
			return stringbuffer.toString();
		}

		private void _appendObjectToStringBuffer(Object obj, StringBuffer stringbuffer, int i) {
			if (obj instanceof String || obj instanceof StringBuffer) {
				_appendStringToStringBuffer(obj.toString(), stringbuffer, i);
			} else if (obj instanceof Integer || obj instanceof Long || obj instanceof BigInteger) {
				_appendIntegerToStringBuffer((Number) obj, stringbuffer, i);
			} else if (obj instanceof Float || obj instanceof Double || obj instanceof BigDecimal) {
				_appendFloatToStringBuffer((Number) obj, stringbuffer, i);
			} else if (obj instanceof Date) {
				_appendDateToStringBuffer((Date) obj, stringbuffer, i);
			} else if (obj instanceof Boolean) {
				_appendBooleanToStringBuffer((Boolean) obj, stringbuffer, i);
			} else if (obj instanceof NSData) {
				_appendDataToStringBuffer((NSData) obj, stringbuffer, i);
			} else if (obj instanceof List<?>) {
				_appendArrayToStringBuffer((List<?>) obj, stringbuffer, i);
			} else if (obj instanceof Map<?, ?>) {
				_appendDictionaryToStringBuffer((Map<?, ?>) obj, stringbuffer, i);
            } else if (obj instanceof NSArray) {
                _appendNSArrayToStringBuffer((NSArray) obj, stringbuffer, i);
            } else if (obj instanceof NSDictionary) {
                _appendNSDictionaryToStringBuffer((NSDictionary) obj, stringbuffer, i);
			} else {
				_appendStringToStringBuffer(obj.toString(), stringbuffer, i);
			}
		}

		private void _appendStringToStringBuffer(String s, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.STRING.openTag());
			stringbuffer.append(escapeString(s));
			stringbuffer.append(DictionaryParser.XMLNode.Type.STRING.closeTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

		private void _appendIntegerToStringBuffer(Number s, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.INTEGER.openTag());
			stringbuffer.append(s.toString());
			stringbuffer.append(DictionaryParser.XMLNode.Type.INTEGER.closeTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

		private void _appendFloatToStringBuffer(Number s, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.REAL.openTag());
			stringbuffer.append(s.toString());
			stringbuffer.append(DictionaryParser.XMLNode.Type.REAL.closeTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

		private void _appendBooleanToStringBuffer(Boolean s, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			if (s.booleanValue()) {
				stringbuffer.append(DictionaryParser.XMLNode.Type.TRUE.openTag());
				stringbuffer.append(DictionaryParser.XMLNode.Type.TRUE.closeTag());
			} else {
				stringbuffer.append(DictionaryParser.XMLNode.Type.FALSE.openTag());
				stringbuffer.append(DictionaryParser.XMLNode.Type.FALSE.closeTag());
			}
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

		private void _appendDateToStringBuffer(Date s, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.DATE.openTag());
			stringbuffer.append(_dateFormat.format(s));
			stringbuffer.append(DictionaryParser.XMLNode.Type.DATE.closeTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

		private void _appendDataToStringBuffer(NSData s, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.DATA.openTag());
			try {
				stringbuffer.append(new String(_NSBase64.encode(s.bytes()), _NSStringUtilities.UTF8_ENCODING));
			}
			catch (UnsupportedEncodingException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			stringbuffer.append(DictionaryParser.XMLNode.Type.DATA.closeTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

		private void _appendArrayToStringBuffer(List<?> vector, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.ARRAY.openTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
			for (Iterator<?> iterator = vector.iterator(); iterator.hasNext();) {
				_appendObjectToStringBuffer(iterator.next(), stringbuffer, i + 1);
			}
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.ARRAY.closeTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

        private void _appendNSArrayToStringBuffer(NSArray vector, StringBuffer stringbuffer, int i) {
            _appendIndentationToStringBuffer(stringbuffer, i);
            stringbuffer.append(DictionaryParser.XMLNode.Type.ARRAY.openTag());
            _appendNewLineToStringBuffer(stringbuffer, i);
            for (Enumeration iterator = vector.objectEnumerator(); iterator.hasMoreElements();) {
                _appendObjectToStringBuffer(iterator.nextElement(), stringbuffer, i + 1);
            }
            _appendIndentationToStringBuffer(stringbuffer, i);
            stringbuffer.append(DictionaryParser.XMLNode.Type.ARRAY.closeTag());
            _appendNewLineToStringBuffer(stringbuffer, i);
        }

		private void _appendDictionaryToStringBuffer(Map<?, ?> table, StringBuffer stringbuffer, int i) {
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.DICTIONARY.openTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
			for (Iterator<?> iterator = table.keySet().iterator(); iterator.hasNext();) {
				Object key = iterator.next();

				// Can encounter null keys in Maps, therefore put placeholder
				if (key == null) {
					key = NULL;
				}

				_appendIndentationToStringBuffer(stringbuffer, i + 1);
				stringbuffer.append(DictionaryParser.XMLNode.Type.KEY.openTag());
				stringbuffer.append(escapeString(key.toString()));
				stringbuffer.append(DictionaryParser.XMLNode.Type.KEY.closeTag());
				_appendNewLineToStringBuffer(stringbuffer, i + 1);
				_appendObjectToStringBuffer((key.equals(NULL) ? table.get(null) : table.get(key)), stringbuffer, i + 1);
				_appendNewLineToStringBuffer(stringbuffer, i + 1);
			}
			_appendIndentationToStringBuffer(stringbuffer, i);
			stringbuffer.append(DictionaryParser.XMLNode.Type.DICTIONARY.closeTag());
			_appendNewLineToStringBuffer(stringbuffer, i);
		}

        private void _appendNSDictionaryToStringBuffer(NSDictionary table, StringBuffer stringbuffer, int i) {
            _appendIndentationToStringBuffer(stringbuffer, i);
            stringbuffer.append(DictionaryParser.XMLNode.Type.DICTIONARY.openTag());
            _appendNewLineToStringBuffer(stringbuffer, i);
            for (Enumeration iterator = table.keyEnumerator(); iterator.hasMoreElements();) {
                Object key = iterator.nextElement();

                // Can encounter null keys in Maps, therefore put placeholder
                if (key == null) {
                    key = NULL;
                }

                _appendIndentationToStringBuffer(stringbuffer, i + 1);
                stringbuffer.append(DictionaryParser.XMLNode.Type.KEY.openTag());
                stringbuffer.append(escapeString(key.toString()));
                stringbuffer.append(DictionaryParser.XMLNode.Type.KEY.closeTag());
                _appendNewLineToStringBuffer(stringbuffer, i + 1);
                _appendObjectToStringBuffer((key.equals(NULL) ? table.objectForKey(null) : table.objectForKey(key)), stringbuffer, i + 1);
                _appendNewLineToStringBuffer(stringbuffer, i + 1);
            }
            _appendIndentationToStringBuffer(stringbuffer, i);
            stringbuffer.append(DictionaryParser.XMLNode.Type.DICTIONARY.closeTag());
            _appendNewLineToStringBuffer(stringbuffer, i);
        }

		/**
		 * Validate the string. We need to watch out for the entity references &, <, >, ' and ";
		 *
		 * @param toValidate
		 * @return result string
		 */
		protected String escapeString(String toValidate) {
			int length = toValidate.length();
			StringBuilder cleanString = new StringBuilder(length);
			char currentChar;

			for (int i = 0; i < length; i++) {
				currentChar = toValidate.charAt(i);
				switch (currentChar) {
					case '&':
						cleanString.append("&amp;");
						break;
					case '<':
						cleanString.append("&lt;");
						break;
					case '>':
						cleanString.append("&gt;");
						break;
					case '\'':
						cleanString.append("&apos;");
						break;
					case '"':
						cleanString.append("&quot;");
						break;
					default:
						cleanString.append(currentChar);
						break;
				}
			}
			return cleanString.toString();
		}

	}

	/**
	 *
	 */
	public static class _JSONPList extends _PListParser {
		private int					_lineNumber;

		private int					_startOfLineCharIndex;

		private int					_savedIndex;

		private int					_savedLineNumber;

		private int					_savedStartOfLineCharIndex;

		private SimpleDateFormat	_dateFormat;

		// private static final Pattern JSON_DATE_PATTERN = Pattern.compile("^(\\d\\{4\\})-(\\d\\{2\\})-(\\d\\{2\\})T(\\d\\{2\\}):(\\d\\{2\\}):(\\d\\{2\\}(?:\\.\\d*)?)Z$");

		private static final int	_C_NON_COMMENT_OR_SPACE		= 1;

		private static final int	_C_WHITESPACE				= 2;

		private static final int	_C_SINGLE_LINE_COMMENT		= 3;

		private static final int	_C_MULTI_LINE_COMMENT		= 4;

		private static final int	NSToPrecompUnicodeTable[]	= {
																/* NextStep Encoding Unicode */
																/* 128 figspace */0x00a0, /* 0x2007 is fig space */
																/* 129 Agrave */0x00c0,
																/* 130 Aacute */0x00c1,
																/* 131 Acircumflex */0x00c2,
																/* 132 Atilde */0x00c3,
																/* 133 Adieresis */0x00c4,
																/* 134 Aring */0x00c5,
																/* 135 Ccedilla */0x00c7,
																/* 136 Egrave */0x00c8,
																/* 137 Eacute */0x00c9,
																/* 138 Ecircumflex */0x00ca,
																/* 139 Edieresis */0x00cb,
																/* 140 Igrave */0x00cc,
																/* 141 Iacute */0x00cd,
																/* 142 Icircumflex */0x00ce,
																/* 143 Idieresis */0x00cf,
																/* 144 Eth */0x00d0,
																/* 145 Ntilde */0x00d1,
																/* 146 Ograve */0x00d2,
																/* 147 Oacute */0x00d3,
																/* 148 Ocircumflex */0x00d4,
																/* 149 Otilde */0x00d5,
																/* 150 Odieresis */0x00d6,
																/* 151 Ugrave */0x00d9,
																/* 152 Uacute */0x00da,
																/* 153 Ucircumflex */0x00db,
																/* 154 Udieresis */0x00dc,
																/* 155 Yacute */0x00dd,
																/* 156 Thorn */0x00de,
																/* 157 mu */0x00b5,
																/* 158 multiply */0x00d7,
																/* 159 divide */0x00f7,
																/* 160 copyright */0x00a9,
																/* 161 exclamdown */0x00a1,
																/* 162 cent */0x00a2,
																/* 163 sterling */0x00a3,
																/* 164 fraction */0x2044,
																/* 165 yen */0x00a5,
																/* 166 florin */0x0192,
																/* 167 section */0x00a7,
																/* 168 currency */0x00a4,
																/* 169 quotesingle */0x2019,
																/* 170 quotedblleft */0x201c,
																/* 171 guillemotleft */0x00ab,
																/* 172 guilsinglleft */0x2039,
																/* 173 guilsinglright */0x203a,
																/* 174 fi */0xFB01,
																/* 175 fl */0xFB02,
																/* 176 registered */0x00ae,
																/* 177 endash */0x2013,
																/* 178 dagger */0x2020,
																/* 179 daggerdbl */0x2021,
																/* 180 periodcentered */0x00b7,
																/* 181 brokenbar */0x00a6,
																/* 182 paragraph */0x00b6,
																/* 183 bullet */0x2022,
																/* 184 quotesinglbase */0x201a,
																/* 185 quotedblbase */0x201e,
																/* 186 quotedblright */0x201d,
																/* 187 guillemotright */0x00bb,
																/* 188 ellipsis */0x2026,
																/* 189 perthousand */0x2030,
																/* 190 logicalnot */0x00ac,
																/* 191 questiondown */0x00bf,
																/* 192 onesuperior */0x00b9,
																/* 193 grave */0x02cb,
																/* 194 acute */0x00b4,
																/* 195 circumflex */0x02c6,
																/* 196 tilde */0x02dc,
																/* 197 macron */0x00af,
																/* 198 breve */0x02d8,
																/* 199 dotaccent */0x02d9,
																/* 200 dieresis */0x00a8,
																/* 201 twosuperior */0x00b2,
																/* 202 ring */0x02da,
																/* 203 cedilla */0x00b8,
																/* 204 threesuperior */0x00b3,
																/* 205 hungarumlaut */0x02dd,
																/* 206 ogonek */0x02db,
																/* 207 caron */0x02c7,
																/* 208 emdash */0x2014,
																/* 209 plusminus */0x00b1,
																/* 210 onequarter */0x00bc,
																/* 211 onehalf */0x00bd,
																/* 212 threequarters */0x00be,
																/* 213 agrave */0x00e0,
																/* 214 aacute */0x00e1,
																/* 215 acircumflex */0x00e2,
																/* 216 atilde */0x00e3,
																/* 217 adieresis */0x00e4,
																/* 218 aring */0x00e5,
																/* 219 ccedilla */0x00e7,
																/* 220 egrave */0x00e8,
																/* 221 eacute */0x00e9,
																/* 222 ecircumflex */0x00ea,
																/* 223 edieresis */0x00eb,
																/* 224 igrave */0x00ec,
																/* 225 AE */0x00c6,
																/* 226 iacute */0x00ed,
																/* 227 ordfeminine */0x00aa,
																/* 228 icircumflex */0x00ee,
																/* 229 idieresis */0x00ef,
																/* 230 eth */0x00f0,
																/* 231 ntilde */0x00f1,
																/* 232 Lslash */0x0141,
																/* 233 Oslash */0x00d8,
																/* 234 OE */0x0152,
																/* 235 ordmasculine */0x00ba,
																/* 236 ograve */0x00f2,
																/* 237 oacute */0x00f3,
																/* 238 ocircumflex */0x00f4,
																/* 239 otilde */0x00f5,
																/* 240 odieresis */0x00f6,
																/* 241 ae */0x00e6,
																/* 242 ugrave */0x00f9,
																/* 243 uacute */0x00fa,
																/* 244 ucircumflex */0x00fb,
																/* 245 dotlessi */0x0131,
																/* 246 udieresis */0x00fc,
																/* 247 yacute */0x00fd,
																/* 248 lslash */0x0142,
																/* 249 oslash */0x00f8,
																/* 250 oe */0x0153,
																/* 251 germandbls */0x00df,
																/* 252 thorn */0x00fe,
																/* 253 ydieresis */0x00ff,
																/* 254 .notdef */0xFFFD,
																/* 255 .notdef */0xFFFD };

		/**
		 *
		 */
		public _JSONPList() {
			super(true);
			_init();
		}

		/**
		 * @param indents
		 *            true if the result string must be indented
		 */
		public _JSONPList(boolean indents) {
			super(indents);
			_init();
		}

		private void _init() {
			_dateFormat = new SimpleDateFormat(DefaultSimpleDateFormatPattern);
			_dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			_dateFormat.setLenient(true);
		}

		private void _saveIndexes(int i, int j, int k) {
			_savedIndex = i;
			_savedLineNumber = j;
			_savedStartOfLineCharIndex = k;
		}

		private String _savedIndexesAsString() {
			return "line number: " + _savedLineNumber + ", column: " + (_savedIndex - _savedStartOfLineCharIndex);
		}

		/**
		 * @param obj
		 * @param obj1
		 * @return true is the two list are equals
		 */
		public static boolean propertyListsAreEqual(Object obj, Object obj1) {
			if (obj == null && obj1 == null)
				return true;
			if (((obj instanceof String) || (obj instanceof StringBuffer) || (obj instanceof StringBuilder))
					&& ((obj1 instanceof String) || (obj1 instanceof StringBuffer) || (obj1 instanceof StringBuilder)))
				return obj.toString().equals(obj1.toString());
			if ((obj instanceof NSData) && (obj1 instanceof NSData))
				return ((NSData) obj).isEqualToData((NSData) obj1);
			if ((obj instanceof NSArray<?>) && (obj1 instanceof NSArray<?>)) {
				NSArray<?> nsarray = (NSArray<?>) obj;
				NSArray<?> nsarray1 = (NSArray<?>) obj1;
				int i = nsarray.count();
				int k = nsarray1.count();
				if (i != k)
					return false;
				for (int i1 = 0; i1 < i; i1++)
					if (!propertyListsAreEqual(nsarray.objectAtIndex(i1), nsarray1.objectAtIndex(i1)))
						return false;

				return true;
			}
			if ((obj instanceof NSDictionary<?, ?>) && (obj1 instanceof NSDictionary<?, ?>)) {
				NSDictionary<?, ?> nsdictionary = (NSDictionary<?, ?>) obj;
				NSDictionary<?, ?> nsdictionary1 = (NSDictionary<?, ?>) obj1;
				int j = nsdictionary.count();
				int l = nsdictionary1.count();
				if (j != l)
					return false;
				for (Enumeration<?> enumeration = nsdictionary.keyEnumerator(); enumeration.hasMoreElements();) {
					Object obj2 = enumeration.nextElement();
					Object obj3 = nsdictionary1.objectForKey(obj2);
					if (obj3 == null)
						return false;
					Object obj4 = nsdictionary.objectForKey(obj2);
					if (!propertyListsAreEqual(obj4, obj3))
						return false;
				}

				return true;
			}
			return false;
		}

		/**
		 * @param obj
		 * @return copy of the pList
		 */
		public static Object copyPropertyList(Object obj) {
			if (obj == null)
				return null;
			if (obj instanceof String)
				return obj;
			if (obj instanceof StringBuffer)
				return ((StringBuffer) obj).toString();
			if (obj instanceof NSData)
				return ((NSData) obj).clone();
			if (obj instanceof NSArray<?>) {
				NSArray<?> array = (NSArray<?>) obj;
				int i = array.count();
				NSMutableArray<Object> newArray = new NSMutableArray<Object>(i);
				for (int j = 0; j < i; j++) {
					newArray.addObject(copyPropertyList(array.objectAtIndex(j)));
				}
				return newArray;
			}
			if (obj instanceof NSDictionary<?, ?>) {
				NSDictionary<?, ?> dictionary = (NSDictionary<?, ?>) obj;
				NSMutableDictionary<Object, Object> newDictionary = new NSMutableDictionary<Object, Object>(dictionary.count());
				Object key = null;
				Object value = null;
				for (Enumeration<?> enumeration = dictionary.keyEnumerator(); enumeration.hasMoreElements(); newDictionary.setObjectForKey(copyPropertyList(value), copyPropertyList(key))) {
					key = enumeration.nextElement();
					value = dictionary.objectForKey(key);
				}

				return newDictionary;
			}
			throw new IllegalArgumentException("Property list copying failed while attempting to copy non property list type: " + obj.getClass().getName());
		}

		/**
		 * @param plist
		 * @param suppressWhitespace
		 * @return encoded property list
		 */
		public String stringFromPropertyList(Object plist, boolean suppressWhitespace) {
			if (plist == null)
				return null;
			StringBuffer buffer = new StringBuffer(128);
			_appendObjectToStringBuffer(plist, buffer, 0, suppressWhitespace);
			return buffer.toString();
		}

		@Override
		public Object parseStringIntoPlist(String string) {
			if (string == null)
				return null;
			char[] charArray = string.toCharArray();
			Object aobj[] = new Object[1];
			_lineNumber = 1;
			_startOfLineCharIndex = 0;
			aobj[0] = null;
			int i = 0;
			i = _readObjectIntoObjectReference(charArray, i, aobj);
			i = _skipWhitespaceAndComments(charArray, i);
			if (i != EOT) {
				throw new IllegalArgumentException("parseStringIntoPlist parsed an object, but there's still more text in the string. A plist should contain only one top-level object. Line number: "
						+ _lineNumber + ", column: " + (i - _startOfLineCharIndex) + ".");
			}
			return aobj[0];
		}

		private void _appendObjectToStringBuffer(Object obj, StringBuffer stringbuffer, int i, boolean suppressWhitespace) {
			if (obj instanceof String) {
				_appendStringToStringBuffer((String) obj, stringbuffer, i);
			} else if (obj instanceof StringBuffer) {
				_appendStringToStringBuffer(((StringBuffer) obj).toString(), stringbuffer, i);
			} else if (obj instanceof StringBuilder) {
				_appendStringToStringBuffer(((StringBuilder) obj).toString(), stringbuffer, i);
			} else if (obj instanceof NSData) {
				_appendDataToStringBuffer((NSData) obj, stringbuffer, i);
			} else if (obj instanceof List<?>) {
				_appendArrayToStringBuffer((List<?>) obj, stringbuffer, i, suppressWhitespace);
			} else if (obj instanceof Map<?, ?>) {
				_appendDictionaryToStringBuffer((Map<?, ?>) obj, stringbuffer, i, suppressWhitespace);
            } else if (obj instanceof NSArray) {
                _appendNSArrayToStringBuffer((NSArray) obj, stringbuffer, i, suppressWhitespace);
            } else if (obj instanceof NSDictionary) {
                _appendNSDictionaryToStringBuffer((NSDictionary) obj, stringbuffer, i, suppressWhitespace);
			} else if (obj instanceof Boolean) {
				stringbuffer.append(((Boolean) obj).booleanValue() ? "true" : "false");
			} else if (obj instanceof BigDecimal) {
				stringbuffer.append(((BigDecimal) obj).toPlainString());
			} else if (obj instanceof Number) {
				stringbuffer.append(((Number) obj).toString());
			} else if (obj instanceof NSTimestamp) {
				_appendDateToStringBuffer(((Date) obj), stringbuffer);
			} else {
				if (obj != null) {
					_appendStringToStringBuffer(obj.toString(), stringbuffer, i);
				} else {
					stringbuffer.append("null");
				}
			}
		}

		private void _appendStringToStringBuffer(String s, StringBuffer stringbuffer, @SuppressWarnings("unused") int i) {
			stringbuffer.append('"');
			char ac[] = s.toCharArray();
			for (int j = 0; j < ac.length; j++) {
				if (ac[j] < '\200') {
					if (ac[j] == '\n') {
						stringbuffer.append("\\n");
						continue;
					} else if (ac[j] == '\r') {
						stringbuffer.append("\\r");
						continue;
					} else if (ac[j] == '\t') {
						stringbuffer.append("\\t");
						continue;
					} else if (ac[j] == '"') {
						stringbuffer.append('\\');
						stringbuffer.append('"');
						continue;
					} else if (ac[j] == '\\') {
						stringbuffer.append("\\\\");
						continue;
					} else if (ac[j] == '\f') {
						stringbuffer.append("\\f");
						continue;
					} else if (ac[j] == '\b') {
						stringbuffer.append("\\b");
						continue;
					} else if (ac[j] == '\007') {
						stringbuffer.append("\\a");
						continue;
					} else if (ac[j] == '\013') {
						stringbuffer.append("\\v");
					} else {
						stringbuffer.append(ac[j]);
					}
				} else {
					char c = ac[j];
					byte byte0 = (byte) (c & 0xf);
					c >>= '\004';
					byte byte1 = (byte) (c & 0xf);
					c >>= '\004';
					byte byte2 = (byte) (c & 0xf);
					c >>= '\004';
					byte byte3 = (byte) (c & 0xf);
					c >>= '\004';
					stringbuffer.append("\\u");
					stringbuffer.append(_hexDigitForNibble(byte3));
					stringbuffer.append(_hexDigitForNibble(byte2));
					stringbuffer.append(_hexDigitForNibble(byte1));
					stringbuffer.append(_hexDigitForNibble(byte0));
				}
			}
			stringbuffer.append('"');
		}

		private void _appendDataToStringBuffer(NSData nsdata, StringBuffer stringbuffer, @SuppressWarnings("unused") int i) {
			stringbuffer.append('"');
			stringbuffer.append('<');
			byte abyte0[] = nsdata.bytes();
			for (int j = 0; j < abyte0.length; j++) {
				byte byte0 = abyte0[j];
				byte byte1 = (byte) (byte0 & 0xf);
				byte0 >>= 4;
				byte byte2 = (byte) (byte0 & 0xf);
				stringbuffer.append(_hexDigitForNibble(byte2));
				stringbuffer.append(_hexDigitForNibble(byte1));
			}

			stringbuffer.append('>');
			stringbuffer.append('"');
		}

		/*
		 * There are a couple of popular ways to serialize dates. This adheres http://www.json.org/json.js For example, this would serialize Dates as ISO strings. Date.prototype.toJSON = function (key) { function f(n) { // Format integers to have at least two digits. return n < 10 ? '0' + n : n; }
		 * return getUTCFullYear() + '-' + f(getUTCMonth() + 1) + '-' + f(getUTCDate()) + 'T' + f(getUTCHours()) + ':' + f(getUTCMinutes()) + ':' + f(getUTCSeconds()) + 'Z'; };
		 */
		private void _appendDateToStringBuffer(Date date, StringBuffer stringbuffer) {
			stringbuffer.append('"');
			stringbuffer.append(_dateFormat.format(date));
			stringbuffer.append('"');
		}

		private void _appendArrayToStringBuffer(List<?> nsarray, StringBuffer stringbuffer, int i, boolean suppressWhitespace) {
			stringbuffer.append('[');
			int j = nsarray.size();
			if (j > 0) {
				for (int k = 0; k < j; k++) {
					if (k > 0)
						stringbuffer.append(',');
					if (!suppressWhitespace) {
						_appendNewLineToStringBuffer(stringbuffer, i);
						_appendIndentationToStringBuffer(stringbuffer, i + 1);
					}
					_appendObjectToStringBuffer(nsarray.get(k), stringbuffer, i + 1, suppressWhitespace);
				}

				if (!suppressWhitespace) {
					_appendNewLineToStringBuffer(stringbuffer, i);
					_appendIndentationToStringBuffer(stringbuffer, i);
				}
			}
			stringbuffer.append(']');
		}

        private void _appendNSArrayToStringBuffer(NSArray nsarray, StringBuffer stringbuffer, int i, boolean suppressWhitespace) {
            stringbuffer.append('[');
            int j = nsarray.count();
            if (j > 0) {
                for (int k = 0; k < j; k++) {
                    if (k > 0)
                        stringbuffer.append(',');
                    if (!suppressWhitespace) {
                        _appendNewLineToStringBuffer(stringbuffer, i);
                        _appendIndentationToStringBuffer(stringbuffer, i + 1);
                    }
                    _appendObjectToStringBuffer(nsarray.objectAtIndex(k), stringbuffer, i + 1, suppressWhitespace);
                }

                if (!suppressWhitespace) {
                    _appendNewLineToStringBuffer(stringbuffer, i);
                    _appendIndentationToStringBuffer(stringbuffer, i);
                }
            }
            stringbuffer.append(']');
        }

		private void _appendDictionaryToStringBuffer(Map<?, ?> nsdictionary, StringBuffer stringbuffer, int i, boolean suppressWhitespace) {
			stringbuffer.append('{');
			int j = nsdictionary.size();
			if (j > 0) {
				for (Iterator<?> iteration = nsdictionary.keySet().iterator(); iteration.hasNext();) {
					Object obj = iteration.next();
					if (!(obj instanceof String))
						throw new IllegalArgumentException(
								"JSON Property list generation failed while attempting to write hashtable. Non-String key found in Hashtable. Property list dictionaries must have String's as keys.");
					if (!suppressWhitespace) {
						_appendNewLineToStringBuffer(stringbuffer, i);
						_appendIndentationToStringBuffer(stringbuffer, i + 1);
					}
					_appendStringToStringBuffer((String) obj, stringbuffer, i + 1);
					stringbuffer.append(" : ");
					_appendObjectToStringBuffer(nsdictionary.get(obj), stringbuffer, i + 1, suppressWhitespace);

					if (iteration.hasNext()) {
						stringbuffer.append(',');
					}
				}

				if (!suppressWhitespace) {
					_appendNewLineToStringBuffer(stringbuffer, i);
					_appendIndentationToStringBuffer(stringbuffer, i);
				}
			}

			stringbuffer.append('}');
		}

        private void _appendNSDictionaryToStringBuffer(NSDictionary nsdictionary, StringBuffer stringbuffer, int i, boolean suppressWhitespace) {
            stringbuffer.append('{');
            int j = nsdictionary.count();
            if (j > 0) {
                for (Enumeration<?> iteration = nsdictionary.keyEnumerator(); iteration.hasMoreElements();) {
                    Object obj = iteration.nextElement();
                    if (!(obj instanceof String))
                        throw new IllegalArgumentException(
                                "JSON Property list generation failed while attempting to write hashtable. Non-String key found in Hashtable. Property list dictionaries must have String's as keys.");
                    if (!suppressWhitespace) {
                        _appendNewLineToStringBuffer(stringbuffer, i);
                        _appendIndentationToStringBuffer(stringbuffer, i + 1);
                    }
                    _appendStringToStringBuffer((String) obj, stringbuffer, i + 1);
                    stringbuffer.append(" : ");
                    _appendObjectToStringBuffer(nsdictionary.objectForKey(obj), stringbuffer, i + 1, suppressWhitespace);

                    if (iteration.hasMoreElements()) {
                        stringbuffer.append(',');
                    }
                }

                if (!suppressWhitespace) {
                    _appendNewLineToStringBuffer(stringbuffer, i);
                    _appendIndentationToStringBuffer(stringbuffer, i);
                }
            }

            stringbuffer.append('}');
        }

		private final char _hexDigitForNibble(byte nibble) {
			char c = '\0';
			if (nibble >= 0 && nibble <= 9) {
				c = (char) (48 + (char) nibble);
			} else if (nibble >= 10 && nibble <= 15) {
				c = (char) (97 + (char) (nibble - 10));
			}
			return c;
		}

		private int _readObjectIntoObjectReference(char ac[], int index, Object aobj[]) {
			int aBufferIndex = index;
			aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
			if (aBufferIndex == EOT || aBufferIndex >= ac.length)
				aobj[0] = null;
			else if (ac[aBufferIndex] == '"') {
				StringBuffer buffer = new StringBuffer(64);
				aBufferIndex = _readQuotedStringIntoStringBuffer(ac, aBufferIndex, buffer);
				aobj[0] = buffer.toString();
				// // detect date strings
				// String theString = buffer.toString();
				// Matcher matcher = JSON_DATE_PATTERN.matcher(theString);
				// if (matcher.matches()) {
				// NSTimestamp ts;
				// try {
				// ts = new NSTimestamp(_dateFormat.parse(theString));
				// aobj[0] = ts;
				// } catch (ParseException e) {
				// logger.error("Failed to parse JSON date string " + theString + " returning raw string instead.", e);
				// aobj[0] = theString;
				// }
				// } else {
				// // return raw string
				// aobj[0] = theString;
				// }
			} else if (ac[aBufferIndex] == '<') {
				NSMutableData data = new NSMutableData(_lengthOfData(ac, aBufferIndex));
				aBufferIndex = _readDataContentsIntoData(ac, aBufferIndex, data);
				aobj[0] = data;
			} else if (ac[aBufferIndex] == '[') {
				NSMutableArray<Object> array = new NSMutableArray<Object>();
				aBufferIndex = _readArrayContentsIntoArray(ac, aBufferIndex, array);
				aobj[0] = array;
			} else if (ac[aBufferIndex] == '{') {
				NSMutableDictionary<Object, Object> dictionary = new NSMutableDictionary<Object, Object>();
				aBufferIndex = _readDictionaryContentsIntoDictionary(ac, aBufferIndex, dictionary);
				aobj[0] = dictionary;
			} else {
				StringBuffer buffer = new StringBuffer(64);
				aBufferIndex = _readUnquotedStringIntoStringBuffer(ac, aBufferIndex, buffer);
				String theString = buffer.toString();
				if ("true".equals(theString)) {
					aobj[0] = Boolean.TRUE;
				} else if ("false".equals(theString)) {
					aobj[0] = Boolean.FALSE;
				} else if ("null".equals(theString)) {
					aobj[0] = NULL;
				} else {
					try {
						if (theString.indexOf(".") >= 0) {
							aobj[0] = new BigDecimal(theString);
						} else {
							aobj[0] = new BigInteger(theString);
						}
					} catch (Exception exception) {
						logger.error("Exception ", exception);
						aobj[0] = theString;
					}
				}
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readUnquotedStringIntoStringBuffer(char ac[], int index, StringBuffer buffer) {
			int aBufferIndex = index;
			int j = aBufferIndex;
			buffer.setLength(0);
			for (; aBufferIndex < ac.length
					&& (ac[aBufferIndex] >= 'a' && ac[aBufferIndex] <= 'z' || ac[aBufferIndex] >= 'A' && ac[aBufferIndex] <= 'Z' || ac[aBufferIndex] >= '0' && ac[aBufferIndex] <= '9'
							|| ac[aBufferIndex] == '_' || ac[aBufferIndex] == '$' || ac[aBufferIndex] == '.' || ac[aBufferIndex] == '/' || ac[aBufferIndex] == '-'); aBufferIndex++) {/**/}
			if (j < aBufferIndex)
				buffer.append(ac, j, aBufferIndex - j);
			else
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read unquoted string. No allowable characters were found. At line number: " + _lineNumber
						+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".  Current char is " + ac[j] + ".");
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readQuotedStringIntoStringBuffer(char ac[], int index, StringBuffer stringbuffer) {
			int aBufferIndex = index;
			_saveIndexes(aBufferIndex, _lineNumber, _startOfLineCharIndex);
			int j = ++aBufferIndex;
			while (aBufferIndex < ac.length && ac[aBufferIndex] != '"')
				if (ac[aBufferIndex] == '\\') {
					if (j < aBufferIndex)
						stringbuffer.append(ac, j, aBufferIndex - j);
					if (++aBufferIndex >= ac.length)
						throw new IllegalArgumentException(
								"JSON Property list parsing failed while attempting to read quoted string. Input exhausted before closing quote was found. Opening quote was at "
										+ _savedIndexesAsString() + ".");
					if (ac[aBufferIndex] == 'n') {
						stringbuffer.append('\n');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'r') {
						stringbuffer.append('\r');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 't') {
						stringbuffer.append('\t');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'f') {
						stringbuffer.append('\f');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'b') {
						stringbuffer.append('\b');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'a') {
						stringbuffer.append('\007');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'v') {
						stringbuffer.append('\013');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'u' || ac[aBufferIndex] == 'U') {
						if (aBufferIndex + 4 >= ac.length)
							throw new IllegalArgumentException(
									"Property list parsing failed while attempting to read quoted string. Input exhausted before escape sequence was completed. Opening quote was at "
											+ _savedIndexesAsString() + ".");
						aBufferIndex++;
						if (!_isHexDigit(ac[aBufferIndex]) || !_isHexDigit(ac[aBufferIndex + 1]) || !_isHexDigit(ac[aBufferIndex + 2]) || !_isHexDigit(ac[aBufferIndex + 3]))
							throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read quoted string. Improperly formed \\U type escape sequence. At line number: "
									+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
						byte byte0 = _nibbleForHexDigit(ac[aBufferIndex]);
						byte byte1 = _nibbleForHexDigit(ac[aBufferIndex + 1]);
						byte byte2 = _nibbleForHexDigit(ac[aBufferIndex + 2]);
						byte byte3 = _nibbleForHexDigit(ac[aBufferIndex + 3]);
						stringbuffer.append((char) ((byte0 << 12) + (byte1 << 8) + (byte2 << 4) + byte3));
						aBufferIndex += 4;
					} else if (ac[aBufferIndex] >= '0' && ac[aBufferIndex] <= '7') {
						int k = 0;
						int l = 1;
						int ai[] = new int[3];
						ai[0] = ac[aBufferIndex] - 48;
						for (aBufferIndex++; l < 3 && aBufferIndex < ac.length && ac[aBufferIndex] >= '0' && ac[aBufferIndex] <= '7'; aBufferIndex++)
							ai[l++] = ac[aBufferIndex] - 48;

						if (l == 3 && ai[0] > 3)
							throw new IllegalArgumentException(
									"JSON Property list parsing failed while attempting to read quoted string. Octal escape sequence too large (bigger than octal 377). At line number: " + _lineNumber
											+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
						for (int i1 = 0; i1 < l; i1++) {
							k *= 8;
							k += ai[i1];
						}

						stringbuffer.append(_nsToUnicode(k));
					} else {
						stringbuffer.append(ac[aBufferIndex]);
						if (ac[aBufferIndex] == '\n') {
							_lineNumber++;
							_startOfLineCharIndex = aBufferIndex + 1;
						}
						aBufferIndex++;
					}
					j = aBufferIndex;
				} else {
					if (ac[aBufferIndex] == '\n') {
						_lineNumber++;
						_startOfLineCharIndex = aBufferIndex + 1;
					}
					aBufferIndex++;
				}
			if (j < aBufferIndex)
				stringbuffer.append(ac, j, aBufferIndex - j);
			if (aBufferIndex >= ac.length) {
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read quoted string. Input exhausted before closing quote was found. Opening quote was at "
						+ _savedIndexesAsString() + ".");
			}
			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _lengthOfData(char ac[], int index) {
			int aBufferIndex = index;
			int j = 0;
			boolean isHexDigit;
			for (aBufferIndex++; aBufferIndex < ac.length && ((isHexDigit = _isHexDigit(ac[aBufferIndex])) || _isWhitespace(ac[aBufferIndex])); aBufferIndex++)
				if (isHexDigit)
					j++;

			if (aBufferIndex >= ac.length)
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read data. Input exhausted before data was terminated with '>'. At line number: "
						+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			if (ac[aBufferIndex] != '>')
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read data. Illegal character encountered in data: '" + ac[aBufferIndex]
						+ "'. At line number: " + _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			if (j % 2 != 0)
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read data. An odd number of half-bytes were specified. At line number: " + _lineNumber
						+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			return j / 2;
		}

		private int _readDataContentsIntoData(char ac[], int index, NSMutableData nsmutabledata) {
			int aBufferIndex = index;
			aBufferIndex++;
			do {
				if (ac[aBufferIndex] == '>')
					break;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (ac[aBufferIndex] == '>')
					break;
				byte byte0 = _nibbleForHexDigit(ac[aBufferIndex]);
				aBufferIndex++;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				byte byte1 = _nibbleForHexDigit(ac[aBufferIndex]);
				aBufferIndex++;
				nsmutabledata.appendByte((byte) ((byte0 << 4) + byte1));
			} while (true);
			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readArrayContentsIntoArray(char ac[], int index, NSMutableArray<Object> nsmutablearray) {
			int aBufferIndex = index;
			Object aobj[] = new Object[1];
			aBufferIndex++;
			nsmutablearray.removeAllObjects();
			aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
			do {
				if (aBufferIndex == EOT || ac[aBufferIndex] == ']') {
					break;
				}
				if (nsmutablearray.count() > 0) {
					if (ac[aBufferIndex] != ',') {
						throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read array. No comma found between array elements. At line number: " + _lineNumber
								+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
					}
					aBufferIndex++;
					aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
					if (aBufferIndex == EOT) {
						throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read array. Input exhausted before end of array was found. At line number: "
								+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
					}
				}
				if (ac[aBufferIndex] != ']') {
					aobj[0] = null;
					aBufferIndex = _readObjectIntoObjectReference(ac, aBufferIndex, aobj);
					if (aobj[0] == null)
						throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read array. Failed to read content object. At line number: " + _lineNumber
								+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
					aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
					nsmutablearray.addObject(aobj[0]);
				}
			} while (true);
			if (aBufferIndex == EOT) {
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read array. Input exhausted before end of array was found. At line number: " + _lineNumber
						+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			}
			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readDictionaryContentsIntoDictionary(char ac[], int index, NSMutableDictionary<Object, Object> nsmutabledictionary) {
			int aBufferIndex = index;
			Object aobj[] = new Object[1];
			Object aobj1[] = new Object[1];
			aBufferIndex++;
			if (nsmutabledictionary.count() != 0) {
				for (Enumeration<?> enumeration = nsmutabledictionary.keyEnumerator(); enumeration.hasMoreElements(); nsmutabledictionary.removeObjectForKey(enumeration.nextElement())) {/**/}
			}

			for (aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex); aBufferIndex != EOT && ac[aBufferIndex] != '}';) {
				aBufferIndex = _readObjectIntoObjectReference(ac, aBufferIndex, aobj); // key
				if (aobj[0] == null || !(aobj[0] instanceof String))
					throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read dictionary. Failed to read key or key is not a String. At line number: "
							+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (aBufferIndex == EOT || ac[aBufferIndex] != ':') {
					logger.info("Exception for key=" + aobj[0] + " with unparsed values=" + new StringBuilder().append(ac, aBufferIndex, ac.length - aBufferIndex));

					throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read dictionary. Read key " + aobj[0] + " with no value. At line number: " + _lineNumber
							+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".  Parsed '" + ac[aBufferIndex] + "' instead.");
				}
				aBufferIndex++;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (aBufferIndex == EOT) {
					throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read dictionary. Encountered unexpected end of file while reading key " + aobj[0]
							+ " with no value. At line number: " + _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				}
				aBufferIndex = _readObjectIntoObjectReference(ac, aBufferIndex, aobj1); // value
				if (aobj1[0] == null)
					throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read dictionary. Failed to read value. At line number: " + _lineNumber + ", column: "
							+ (aBufferIndex - _startOfLineCharIndex) + ".");
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (aBufferIndex == EOT) {
					throw new IllegalArgumentException("Unexpected end of JSON string");
				}

				// JSON dictionaries don't need terminators
				if (ac[aBufferIndex] == '}') {
					nsmutabledictionary.setObjectForKey(aobj1[0], aobj[0]);
					break;
				}

				aBufferIndex++;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				nsmutabledictionary.setObjectForKey(aobj1[0], aobj[0]);
			}

			if (aBufferIndex >= ac.length) {
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to read dictionary. Exhausted input before end of dictionary was found. At line number: "
						+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			}

			if (aBufferIndex == EOT) {
				return aBufferIndex;
			}

			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _checkForWhitespaceOrComment(char ac[], int index) {
			if (index == EOT || index >= ac.length)
				return _C_NON_COMMENT_OR_SPACE;
			if (_isWhitespace(ac[index]))
				return _C_WHITESPACE;
			if (index + 1 < ac.length) {
				if (ac[index] == '/' && ac[index + 1] == '/')
					return _C_SINGLE_LINE_COMMENT;
				if (ac[index] == '/' && ac[index + 1] == '*')
					return _C_MULTI_LINE_COMMENT;
			}
			return _C_NON_COMMENT_OR_SPACE;
		}

		private int _skipWhitespaceAndComments(char ac[], int index) {
			int aBufferIndex = index;
			for (int j = _checkForWhitespaceOrComment(ac, aBufferIndex); j != _C_NON_COMMENT_OR_SPACE; j = _checkForWhitespaceOrComment(ac, aBufferIndex)) {
				switch (j) {
					case _C_WHITESPACE: // '\002'
						aBufferIndex = _processWhitespace(ac, aBufferIndex);
						break;

					case _C_SINGLE_LINE_COMMENT: // '\003'
						aBufferIndex = _processSingleLineComment(ac, aBufferIndex);
						break;

					case _C_MULTI_LINE_COMMENT: // '\004'
						aBufferIndex = _processMultiLineComment(ac, aBufferIndex);
						break;
				}
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _processWhitespace(char ac[], int index) {
			int aBufferIndex = index;
			for (; aBufferIndex < ac.length && _isWhitespace(ac[aBufferIndex]); aBufferIndex++) {
				if (ac[aBufferIndex] == '\n') {
					_lineNumber++;
					_startOfLineCharIndex = aBufferIndex + 1;
				}
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _processSingleLineComment(char ac[], int index) {
			int aBufferIndex = index;
			for (aBufferIndex += 2; aBufferIndex < ac.length && ac[aBufferIndex] != '\n'; aBufferIndex++) {/**/}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _processMultiLineComment(char ac[], int index) {
			int aBufferIndex = index;
			_saveIndexes(aBufferIndex, _lineNumber, _startOfLineCharIndex);
			for (aBufferIndex += 2; aBufferIndex + 1 < ac.length && (ac[aBufferIndex] != '*' || ac[aBufferIndex + 1] != '/'); aBufferIndex++) {
				if (ac[aBufferIndex] == '/' && ac[aBufferIndex + 1] == '*') {
					throw new IllegalArgumentException("JSON Property list parsing does not support embedded multi line comments.The first opening comment was at " + _savedIndexesAsString()
							+ ". A second opening comment was found at line " + _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				}
				if (ac[aBufferIndex] == '\n') {
					_lineNumber++;
					_startOfLineCharIndex = aBufferIndex + 1;
				}
			}

			if (aBufferIndex + 1 < ac.length && ac[aBufferIndex] == '*' && ac[aBufferIndex + 1] == '/') {
				aBufferIndex += 2;
			} else {
				throw new IllegalArgumentException("JSON Property list parsing failed while attempting to find closing to comment that began at " + _savedIndexesAsString() + ".");
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private final byte _nibbleForHexDigit(char c) {
			int i = 0;
			if (c >= '0' && c <= '9')
				i = (byte) (c - 48);
			else if (c >= 'a' && c <= 'f')
				i = (byte) ((c - 97) + 10);
			else if (c >= 'A' && c <= 'F')
				i = (byte) ((c - 65) + 10);
			else
				throw new IllegalArgumentException("JSON Property list parsing found non-hex digit passed to _nibbleForHexDigit()");
			return (byte) i;
		}

		private final boolean _isHexDigit(char c) {
			return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
		}

		private final boolean _isWhitespace(char c) {
			return Character.isWhitespace(c);
		}

		private char _nsToUnicode(int i) {
			return i >= 128 ? (char) NSToPrecompUnicodeTable[i - 128] : (char) i;
		}

	}

	/**
	 *
	 */
	public static class _ApplePList extends _PListParser {
		private int					_lineNumber;

		private int					_startOfLineCharIndex;

		private int					_savedIndex;

		private int					_savedLineNumber;

		private int					_savedStartOfLineCharIndex;

		private static final int	_C_NON_COMMENT_OR_SPACE		= 1;

		private static final int	_C_WHITESPACE				= 2;

		private static final int	_C_SINGLE_LINE_COMMENT		= 3;

		private static final int	_C_MULTI_LINE_COMMENT		= 4;

		private static final int	NSToPrecompUnicodeTable[]	= {
																/* NextStep Encoding Unicode */
																/* 128 figspace */0x00a0, /* 0x2007 is fig space */
																/* 129 Agrave */0x00c0,
																/* 130 Aacute */0x00c1,
																/* 131 Acircumflex */0x00c2,
																/* 132 Atilde */0x00c3,
																/* 133 Adieresis */0x00c4,
																/* 134 Aring */0x00c5,
																/* 135 Ccedilla */0x00c7,
																/* 136 Egrave */0x00c8,
																/* 137 Eacute */0x00c9,
																/* 138 Ecircumflex */0x00ca,
																/* 139 Edieresis */0x00cb,
																/* 140 Igrave */0x00cc,
																/* 141 Iacute */0x00cd,
																/* 142 Icircumflex */0x00ce,
																/* 143 Idieresis */0x00cf,
																/* 144 Eth */0x00d0,
																/* 145 Ntilde */0x00d1,
																/* 146 Ograve */0x00d2,
																/* 147 Oacute */0x00d3,
																/* 148 Ocircumflex */0x00d4,
																/* 149 Otilde */0x00d5,
																/* 150 Odieresis */0x00d6,
																/* 151 Ugrave */0x00d9,
																/* 152 Uacute */0x00da,
																/* 153 Ucircumflex */0x00db,
																/* 154 Udieresis */0x00dc,
																/* 155 Yacute */0x00dd,
																/* 156 Thorn */0x00de,
																/* 157 mu */0x00b5,
																/* 158 multiply */0x00d7,
																/* 159 divide */0x00f7,
																/* 160 copyright */0x00a9,
																/* 161 exclamdown */0x00a1,
																/* 162 cent */0x00a2,
																/* 163 sterling */0x00a3,
																/* 164 fraction */0x2044,
																/* 165 yen */0x00a5,
																/* 166 florin */0x0192,
																/* 167 section */0x00a7,
																/* 168 currency */0x00a4,
																/* 169 quotesingle */0x2019,
																/* 170 quotedblleft */0x201c,
																/* 171 guillemotleft */0x00ab,
																/* 172 guilsinglleft */0x2039,
																/* 173 guilsinglright */0x203a,
																/* 174 fi */0xFB01,
																/* 175 fl */0xFB02,
																/* 176 registered */0x00ae,
																/* 177 endash */0x2013,
																/* 178 dagger */0x2020,
																/* 179 daggerdbl */0x2021,
																/* 180 periodcentered */0x00b7,
																/* 181 brokenbar */0x00a6,
																/* 182 paragraph */0x00b6,
																/* 183 bullet */0x2022,
																/* 184 quotesinglbase */0x201a,
																/* 185 quotedblbase */0x201e,
																/* 186 quotedblright */0x201d,
																/* 187 guillemotright */0x00bb,
																/* 188 ellipsis */0x2026,
																/* 189 perthousand */0x2030,
																/* 190 logicalnot */0x00ac,
																/* 191 questiondown */0x00bf,
																/* 192 onesuperior */0x00b9,
																/* 193 grave */0x02cb,
																/* 194 acute */0x00b4,
																/* 195 circumflex */0x02c6,
																/* 196 tilde */0x02dc,
																/* 197 macron */0x00af,
																/* 198 breve */0x02d8,
																/* 199 dotaccent */0x02d9,
																/* 200 dieresis */0x00a8,
																/* 201 twosuperior */0x00b2,
																/* 202 ring */0x02da,
																/* 203 cedilla */0x00b8,
																/* 204 threesuperior */0x00b3,
																/* 205 hungarumlaut */0x02dd,
																/* 206 ogonek */0x02db,
																/* 207 caron */0x02c7,
																/* 208 emdash */0x2014,
																/* 209 plusminus */0x00b1,
																/* 210 onequarter */0x00bc,
																/* 211 onehalf */0x00bd,
																/* 212 threequarters */0x00be,
																/* 213 agrave */0x00e0,
																/* 214 aacute */0x00e1,
																/* 215 acircumflex */0x00e2,
																/* 216 atilde */0x00e3,
																/* 217 adieresis */0x00e4,
																/* 218 aring */0x00e5,
																/* 219 ccedilla */0x00e7,
																/* 220 egrave */0x00e8,
																/* 221 eacute */0x00e9,
																/* 222 ecircumflex */0x00ea,
																/* 223 edieresis */0x00eb,
																/* 224 igrave */0x00ec,
																/* 225 AE */0x00c6,
																/* 226 iacute */0x00ed,
																/* 227 ordfeminine */0x00aa,
																/* 228 icircumflex */0x00ee,
																/* 229 idieresis */0x00ef,
																/* 230 eth */0x00f0,
																/* 231 ntilde */0x00f1,
																/* 232 Lslash */0x0141,
																/* 233 Oslash */0x00d8,
																/* 234 OE */0x0152,
																/* 235 ordmasculine */0x00ba,
																/* 236 ograve */0x00f2,
																/* 237 oacute */0x00f3,
																/* 238 ocircumflex */0x00f4,
																/* 239 otilde */0x00f5,
																/* 240 odieresis */0x00f6,
																/* 241 ae */0x00e6,
																/* 242 ugrave */0x00f9,
																/* 243 uacute */0x00fa,
																/* 244 ucircumflex */0x00fb,
																/* 245 dotlessi */0x0131,
																/* 246 udieresis */0x00fc,
																/* 247 yacute */0x00fd,
																/* 248 lslash */0x0142,
																/* 249 oslash */0x00f8,
																/* 250 oe */0x0153,
																/* 251 germandbls */0x00df,
																/* 252 thorn */0x00fe,
																/* 253 ydieresis */0x00ff,
																/* 254 .notdef */0xFFFD,
																/* 255 .notdef */0xFFFD };

		/**
		 *
		 */
		public _ApplePList() {
			super(true);
		}

		/**
		 * @param indents
		 *            true if the result string must be indented
		 */
		public _ApplePList(boolean indents) {
			super(indents);
		}

		private void _saveIndexes(int i, int j, int k) {
			_savedIndex = i;
			_savedLineNumber = j;
			_savedStartOfLineCharIndex = k;
		}

		private String _savedIndexesAsString() {
			return "line number: " + _savedLineNumber + ", column: " + (_savedIndex - _savedStartOfLineCharIndex);
		}

		/**
		 * @param obj
		 * @param obj1
		 * @return true is the two list are equals
		 */
		public static boolean propertyListsAreEqual(Object obj, Object obj1) {
			if (obj == null && obj1 == null)
				return true;
			if (((obj instanceof String) || (obj instanceof StringBuffer) || (obj instanceof StringBuilder))
					&& ((obj1 instanceof String) || (obj1 instanceof StringBuffer) || (obj1 instanceof StringBuilder)))
				return obj.toString().equals(obj1.toString());
			if ((obj instanceof NSData) && (obj1 instanceof NSData))
				return ((NSData) obj).isEqualToData((NSData) obj1);
			if ((obj instanceof NSArray<?>) && (obj1 instanceof NSArray<?>)) {
				NSArray<?> nsarray = (NSArray<?>) obj;
				NSArray<?> nsarray1 = (NSArray<?>) obj1;
				int i = nsarray.count();
				int k = nsarray1.count();
				if (i != k)
					return false;
				for (int i1 = 0; i1 < i; i1++)
					if (!propertyListsAreEqual(nsarray.objectAtIndex(i1), nsarray1.objectAtIndex(i1)))
						return false;

				return true;
			}
			if ((obj instanceof NSDictionary<?, ?>) && (obj1 instanceof NSDictionary<?, ?>)) {
				NSDictionary<?, ?> nsdictionary = (NSDictionary<?, ?>) obj;
				NSDictionary<?, ?> nsdictionary1 = (NSDictionary<?, ?>) obj1;
				int j = nsdictionary.count();
				int l = nsdictionary1.count();
				if (j != l)
					return false;
				for (Enumeration<?> enumeration = nsdictionary.keyEnumerator(); enumeration.hasMoreElements();) {
					Object obj2 = enumeration.nextElement();
					Object obj3 = nsdictionary1.objectForKey(obj2);
					if (obj3 == null)
						return false;
					Object obj4 = nsdictionary.objectForKey(obj2);
					if (!propertyListsAreEqual(obj4, obj3))
						return false;
				}

				return true;
			}
			return false;
		}

		/**
		 * @param obj
		 * @return copy of the pList
		 */
		public static Object copyPropertyList(Object obj) {
			if (obj == null)
				return null;
			if (obj instanceof String)
				return obj;
			if (obj instanceof StringBuffer)
				return ((StringBuffer) obj).toString();
			if (obj instanceof NSData)
				return ((NSData) obj).clone();
			if (obj instanceof NSArray<?>) {
				NSArray<?> array = (NSArray<?>) obj;
				int i = array.count();
				NSMutableArray<Object> newArray = new NSMutableArray<Object>(i);
				for (int j = 0; j < i; j++) {
					newArray.addObject(copyPropertyList(array.objectAtIndex(j)));
				}
				return newArray;
			}
			if (obj instanceof NSDictionary<?, ?>) {
				NSDictionary<?, ?> dictionary = (NSDictionary<?, ?>) obj;
				NSMutableDictionary<Object, Object> newDictionary = new NSMutableDictionary<Object, Object>(dictionary.count());
				Object key = null;
				Object value = null;
				for (Enumeration<?> enumeration = dictionary.keyEnumerator(); enumeration.hasMoreElements(); newDictionary.setObjectForKey(copyPropertyList(value), copyPropertyList(key))) {
					key = enumeration.nextElement();
					value = dictionary.objectForKey(key);
				}

				return newDictionary;
			}
			throw new IllegalArgumentException("Property list copying failed while attempting to copy non property list type: " + obj.getClass().getName());
		}

		/**
		 * @param plist
		 * @return encoded property list
		 */
		public String stringFromPropertyList(Object plist) {
			if (plist == null)
				return null;
			StringBuffer buffer = new StringBuffer(128);
			_appendObjectToStringBuffer(plist, buffer, 0);
			return buffer.toString();
		}

		@Override
		public Object parseStringIntoPlist(String string) {
			if ((string == null) || (string.length() == 0))
				return null;
			String aString = string.trim();
			Object aobj[] = new Object[1];
			IllegalArgumentException originalException = null;
			try {
				char[] charArray = aString.toCharArray();
				_lineNumber = 1;
				_startOfLineCharIndex = 0;
				aobj[0] = null;
				int i = 0;
				i = _readObjectIntoObjectReference(charArray, i, aobj);
				i = _skipWhitespaceAndComments(charArray, i);
				if (i != EOT) {
					throw new IllegalArgumentException(
							"parseStringIntoPlist parsed an object, but there's still more text in the string. A plist should contain only one top-level object. Line number: " + _lineNumber
									+ ", column: " + (i - _startOfLineCharIndex) + ".");
				}
			} catch (Exception exception) {
				originalException = (IllegalArgumentException) exception;
			}
			if (originalException != null) {
				// We default the top level plist to be a dictionary
				aString = "{" + aString + "}";
				try {
					char[] charArray = aString.toCharArray();
					_lineNumber = 1;
					_startOfLineCharIndex = 0;
					aobj[0] = null;
					int i = 0;
					i = _readObjectIntoObjectReference(charArray, i, aobj);
					i = _skipWhitespaceAndComments(charArray, i);
					if (i != EOT) {
						throw new IllegalArgumentException(
								"parseStringIntoPlist parsed an object, but there's still more text in the string. A plist should contain only one top-level object. Line number: " + _lineNumber
										+ ", column: " + (i - _startOfLineCharIndex) + ".");
					}
					originalException = null;
				} catch (Exception exception) {
					// Nothing
				}
			}
			if (originalException != null) {
				// Still no luck we default to an array.
				aString = "(" + aString + ")";
				try {
					char[] charArray = aString.toCharArray();
					_lineNumber = 1;
					_startOfLineCharIndex = 0;
					aobj[0] = null;
					int i = 0;
					i = _readObjectIntoObjectReference(charArray, i, aobj);
					i = _skipWhitespaceAndComments(charArray, i);
					if (i != EOT) {
						throw new IllegalArgumentException(
								"parseStringIntoPlist parsed an object, but there's still more text in the string. A plist should contain only one top-level object. Line number: " + _lineNumber
										+ ", column: " + (i - _startOfLineCharIndex) + ".");
					}
					originalException = null;
				} catch (Exception exception) {
					// Nothing
				}
			}
			if (originalException != null) {
				throw originalException;
			}
			return aobj[0];
		}

		private void _appendObjectToStringBuffer(Object obj, StringBuffer stringbuffer, int i) {
			if (obj instanceof String) {
				_appendStringToStringBuffer((String) obj, stringbuffer, i);
			} else if (obj instanceof StringBuffer) {
				_appendStringToStringBuffer(((StringBuffer) obj).toString(), stringbuffer, i);
			} else if (obj instanceof NSData) {
				_appendDataToStringBuffer((NSData) obj, stringbuffer, i);
			} else if (obj instanceof List<?>) {
				_appendArrayToStringBuffer((List<?>) obj, stringbuffer, i);
			} else if (obj instanceof Map<?, ?>) {
				_appendDictionaryToStringBuffer((Map<?, ?>) obj, stringbuffer, i);
            } else if (obj instanceof NSArray) {
                _appendNSArrayToStringBuffer((NSArray) obj, stringbuffer, i);
            } else if (obj instanceof NSDictionary) {
                _appendNSDictionaryToStringBuffer((NSDictionary) obj, stringbuffer, i);
			} else if (obj instanceof Boolean) {
				String s = ((Boolean) obj).booleanValue() ? "true" : "false";
				_appendStringToStringBuffer(s, stringbuffer, i);
			} else {
				_appendStringToStringBuffer(obj.toString(), stringbuffer, i);
			}
		}

		private void _appendStringToStringBuffer(String s, StringBuffer stringbuffer, @SuppressWarnings("unused") int i) {
			stringbuffer.append('"');
			char ac[] = s.toCharArray();
			for (int j = 0; j < ac.length; j++) {
				if (ac[j] < '\200') {
					if (ac[j] == '\n') {
						stringbuffer.append("\\n");
						continue;
					} else if (ac[j] == '\r') {
						stringbuffer.append("\\r");
						continue;
					} else if (ac[j] == '\t') {
						stringbuffer.append("\\t");
						continue;
					} else if (ac[j] == '"') {
						stringbuffer.append('\\');
						stringbuffer.append('"');
						continue;
					} else if (ac[j] == '\\') {
						stringbuffer.append("\\\\");
						continue;
					} else if (ac[j] == '\f') {
						stringbuffer.append("\\f");
						continue;
					} else if (ac[j] == '\b') {
						stringbuffer.append("\\b");
						continue;
					} else if (ac[j] == '\007') {
						stringbuffer.append("\\a");
						continue;
					} else if (ac[j] == '\013') {
						stringbuffer.append("\\v");
					} else {
						stringbuffer.append(ac[j]);
					}
				} else {
					char c = ac[j];
					byte byte0 = (byte) (c & 0xf);
					c >>= '\004';
					byte byte1 = (byte) (c & 0xf);
					c >>= '\004';
					byte byte2 = (byte) (c & 0xf);
					c >>= '\004';
					byte byte3 = (byte) (c & 0xf);
					c >>= '\004';
					stringbuffer.append("\\U");
					stringbuffer.append(_hexDigitForNibble(byte3));
					stringbuffer.append(_hexDigitForNibble(byte2));
					stringbuffer.append(_hexDigitForNibble(byte1));
					stringbuffer.append(_hexDigitForNibble(byte0));
				}
			}
			stringbuffer.append('"');
		}

		private void _appendDataToStringBuffer(NSData nsdata, StringBuffer stringbuffer, @SuppressWarnings("unused") int i) {
			stringbuffer.append('<');
			byte abyte0[] = nsdata.bytes();
			for (int j = 0; j < abyte0.length; j++) {
				byte byte0 = abyte0[j];
				byte byte1 = (byte) (byte0 & 0xf);
				byte0 >>= 4;
				byte byte2 = (byte) (byte0 & 0xf);
				stringbuffer.append(_hexDigitForNibble(byte2));
				stringbuffer.append(_hexDigitForNibble(byte1));
			}

			stringbuffer.append('>');
		}

		private void _appendArrayToStringBuffer(List<?> nsarray, StringBuffer stringbuffer, int i) {
			stringbuffer.append('(');
			int j = nsarray.size();
			if (j > 0) {
				for (int k = 0; k < j; k++) {
					if (k > 0)
						stringbuffer.append(',');
					_appendNewLineToStringBuffer(stringbuffer, i);
					_appendIndentationToStringBuffer(stringbuffer, i + 1);
					_appendObjectToStringBuffer(nsarray.get(k), stringbuffer, i + 1);
				}

				_appendNewLineToStringBuffer(stringbuffer, i);
				_appendIndentationToStringBuffer(stringbuffer, i);
			}
			stringbuffer.append(')');
		}

        private void _appendNSArrayToStringBuffer(NSArray nsarray, StringBuffer stringbuffer, int i) {
            stringbuffer.append('(');
            int j = nsarray.count();
            if (j > 0) {
                for (int k = 0; k < j; k++) {
                    if (k > 0)
                        stringbuffer.append(',');
                    _appendNewLineToStringBuffer(stringbuffer, i);
                    _appendIndentationToStringBuffer(stringbuffer, i + 1);
                    _appendObjectToStringBuffer(nsarray.objectAtIndex(k), stringbuffer, i + 1);
                }

                _appendNewLineToStringBuffer(stringbuffer, i);
                _appendIndentationToStringBuffer(stringbuffer, i);
            }
            stringbuffer.append(')');
        }

		private void _appendDictionaryToStringBuffer(Map<?, ?> nsdictionary, StringBuffer stringbuffer, int i) {
			stringbuffer.append('{');
			int j = nsdictionary.size();
			if (j > 0) {
				for (Iterator<?> iteration = nsdictionary.keySet().iterator(); iteration.hasNext(); stringbuffer.append(';')) {
					Object obj = iteration.next();
					if (!(obj instanceof String))
						throw new IllegalArgumentException(
								"Property list generation failed while attempting to write hashtable. Non-String key found in Hashtable. Property list dictionaries must have String's as keys.");
					_appendNewLineToStringBuffer(stringbuffer, i);
					_appendIndentationToStringBuffer(stringbuffer, i + 1);
					_appendStringToStringBuffer((String) obj, stringbuffer, i + 1);
					stringbuffer.append(" = ");
					_appendObjectToStringBuffer(nsdictionary.get(obj), stringbuffer, i + 1);
				}

				_appendNewLineToStringBuffer(stringbuffer, i);
				_appendIndentationToStringBuffer(stringbuffer, i);
			}
			stringbuffer.append('}');
		}

        private void _appendNSDictionaryToStringBuffer(NSDictionary nsdictionary, StringBuffer stringbuffer, int i) {
            stringbuffer.append('{');
            int j = nsdictionary.count();
            if (j > 0) {
                for (Enumeration<?> iteration = nsdictionary.keyEnumerator(); iteration.hasMoreElements(); stringbuffer.append(';')) {
                    Object obj = iteration.nextElement();
                    if (!(obj instanceof String))
                        throw new IllegalArgumentException(
                                "Property list generation failed while attempting to write hashtable. Non-String key found in Hashtable. Property list dictionaries must have String's as keys.");
                    _appendNewLineToStringBuffer(stringbuffer, i);
                    _appendIndentationToStringBuffer(stringbuffer, i + 1);
                    _appendStringToStringBuffer((String) obj, stringbuffer, i + 1);
                    stringbuffer.append(" = ");
                    _appendObjectToStringBuffer(nsdictionary.objectForKey(obj), stringbuffer, i + 1);
                }

                _appendNewLineToStringBuffer(stringbuffer, i);
                _appendIndentationToStringBuffer(stringbuffer, i);
            }
            stringbuffer.append('}');
        }

		private final char _hexDigitForNibble(byte nibble) {
			char c = '\0';
			if (nibble >= 0 && nibble <= 9) {
				c = (char) (48 + (char) nibble);
			} else if (nibble >= 10 && nibble <= 15) {
				c = (char) (97 + (char) (nibble - 10));
			}
			return c;
		}

		private int _readObjectIntoObjectReference(char ac[], int index, Object aobj[]) {
			int aBufferIndex = index;
			aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
			if (aBufferIndex == EOT || aBufferIndex >= ac.length)
				aobj[0] = null;
			else if (ac[aBufferIndex] == '"') {
				StringBuffer buffer = new StringBuffer(64);
				aBufferIndex = _readQuotedStringIntoStringBuffer(ac, aBufferIndex, buffer);
				aobj[0] = buffer.toString();
			} else if (ac[aBufferIndex] == '<') {
				NSMutableData data = new NSMutableData(_lengthOfData(ac, aBufferIndex));
				aBufferIndex = _readDataContentsIntoData(ac, aBufferIndex, data);
				aobj[0] = data;
			} else if (ac[aBufferIndex] == '(') {
				NSMutableArray<Object> array = new NSMutableArray<Object>();
				aBufferIndex = _readArrayContentsIntoArray(ac, aBufferIndex, array);
				aobj[0] = array;
			} else if (ac[aBufferIndex] == '{') {
				NSMutableDictionary<Object, Object> dictionary = new NSMutableDictionary<Object, Object>();
				aBufferIndex = _readDictionaryContentsIntoDictionary(ac, aBufferIndex, dictionary);
				aobj[0] = dictionary;
			} else {
				StringBuffer buffer = new StringBuffer(64);
				aBufferIndex = _readUnquotedStringIntoStringBuffer(ac, aBufferIndex, buffer);
				aobj[0] = buffer.toString();
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readUnquotedStringIntoStringBuffer(char ac[], int index, StringBuffer buffer) {
			int aBufferIndex = index;
			int j = aBufferIndex;
			buffer.setLength(0);
			for (; aBufferIndex < ac.length
					&& (ac[aBufferIndex] >= 'a' && ac[aBufferIndex] <= 'z' || ac[aBufferIndex] >= 'A' && ac[aBufferIndex] <= 'Z' || ac[aBufferIndex] >= '0' && ac[aBufferIndex] <= '9'
							|| ac[aBufferIndex] == '_' || ac[aBufferIndex] == '$' || ac[aBufferIndex] == ':' || ac[aBufferIndex] == '.' || ac[aBufferIndex] == '/' || ac[aBufferIndex] == '-'); aBufferIndex++) {/**/}
			if (j < aBufferIndex)
				buffer.append(ac, j, aBufferIndex - j);
			else
				throw new IllegalArgumentException("Property list parsing failed while attempting to read unquoted string. No allowable characters were found. At line number: " + _lineNumber
						+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readQuotedStringIntoStringBuffer(char ac[], int index, StringBuffer stringbuffer) {
			int aBufferIndex = index;
			_saveIndexes(aBufferIndex, _lineNumber, _startOfLineCharIndex);
			int j = ++aBufferIndex;
			while (aBufferIndex < ac.length && ac[aBufferIndex] != '"')
				if (ac[aBufferIndex] == '\\') {
					if (j < aBufferIndex)
						stringbuffer.append(ac, j, aBufferIndex - j);
					if (++aBufferIndex >= ac.length)
						throw new IllegalArgumentException("Property list parsing failed while attempting to read quoted string. Input exhausted before closing quote was found. Opening quote was at "
								+ _savedIndexesAsString() + ".");
					if (ac[aBufferIndex] == 'n') {
						stringbuffer.append('\n');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'r') {
						stringbuffer.append('\r');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 't') {
						stringbuffer.append('\t');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'f') {
						stringbuffer.append('\f');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'b') {
						stringbuffer.append('\b');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'a') {
						stringbuffer.append('\007');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'v') {
						stringbuffer.append('\013');
						aBufferIndex++;
					} else if (ac[aBufferIndex] == 'u' || ac[aBufferIndex] == 'U') {
						if (aBufferIndex + 4 >= ac.length)
							throw new IllegalArgumentException(
									"Property list parsing failed while attempting to read quoted string. Input exhausted before escape sequence was completed. Opening quote was at "
											+ _savedIndexesAsString() + ".");
						aBufferIndex++;
						if (!_isHexDigit(ac[aBufferIndex]) || !_isHexDigit(ac[aBufferIndex + 1]) || !_isHexDigit(ac[aBufferIndex + 2]) || !_isHexDigit(ac[aBufferIndex + 3]))
							throw new IllegalArgumentException("Property list parsing failed while attempting to read quoted string. Improperly formed \\U type escape sequence. At line number: "
									+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
						byte byte0 = _nibbleForHexDigit(ac[aBufferIndex]);
						byte byte1 = _nibbleForHexDigit(ac[aBufferIndex + 1]);
						byte byte2 = _nibbleForHexDigit(ac[aBufferIndex + 2]);
						byte byte3 = _nibbleForHexDigit(ac[aBufferIndex + 3]);
						stringbuffer.append((char) ((byte0 << 12) + (byte1 << 8) + (byte2 << 4) + byte3));
						aBufferIndex += 4;
					} else if (ac[aBufferIndex] >= '0' && ac[aBufferIndex] <= '7') {
						int k = 0;
						int l = 1;
						int ai[] = new int[3];
						ai[0] = ac[aBufferIndex] - 48;
						for (aBufferIndex++; l < 3 && aBufferIndex < ac.length && ac[aBufferIndex] >= '0' && ac[aBufferIndex] <= '7'; aBufferIndex++)
							ai[l++] = ac[aBufferIndex] - 48;

						if (l == 3 && ai[0] > 3)
							throw new IllegalArgumentException(
									"Property list parsing failed while attempting to read quoted string. Octal escape sequence too large (bigger than octal 377). At line number: " + _lineNumber
											+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
						for (int i1 = 0; i1 < l; i1++) {
							k *= 8;
							k += ai[i1];
						}

						stringbuffer.append(_nsToUnicode(k));
					} else {
						stringbuffer.append(ac[aBufferIndex]);
						if (ac[aBufferIndex] == '\n') {
							_lineNumber++;
							_startOfLineCharIndex = aBufferIndex + 1;
						}
						aBufferIndex++;
					}
					j = aBufferIndex;
				} else {
					if (ac[aBufferIndex] == '\n') {
						_lineNumber++;
						_startOfLineCharIndex = aBufferIndex + 1;
					}
					aBufferIndex++;
				}
			if (j < aBufferIndex)
				stringbuffer.append(ac, j, aBufferIndex - j);
			if (aBufferIndex >= ac.length) {
				throw new IllegalArgumentException("Property list parsing failed while attempting to read quoted string. Input exhausted before closing quote was found. Opening quote was at "
						+ _savedIndexesAsString() + ".");
			}
			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _lengthOfData(char ac[], int index) {
			int aBufferIndex = index;
			int j = 0;
			boolean isHexDigit;
			for (aBufferIndex++; aBufferIndex < ac.length && ((isHexDigit = _isHexDigit(ac[aBufferIndex])) || _isWhitespace(ac[aBufferIndex])); aBufferIndex++)
				if (isHexDigit)
					j++;

			if (aBufferIndex >= ac.length)
				throw new IllegalArgumentException("Property list parsing failed while attempting to read data. Input exhausted before data was terminated with '>'. At line number: " + _lineNumber
						+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			if (ac[aBufferIndex] != '>')
				throw new IllegalArgumentException("Property list parsing failed while attempting to read data. Illegal character encountered in data: '" + ac[aBufferIndex] + "'. At line number: "
						+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			if (j % 2 != 0)
				throw new IllegalArgumentException("Property list parsing failed while attempting to read data. An odd number of half-bytes were specified. At line number: " + _lineNumber
						+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			return j / 2;
		}

		private int _readDataContentsIntoData(char ac[], int index, NSMutableData nsmutabledata) {
			int aBufferIndex = index;
			aBufferIndex++;
			do {
				if (ac[aBufferIndex] == '>')
					break;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (ac[aBufferIndex] == '>')
					break;
				byte byte0 = _nibbleForHexDigit(ac[aBufferIndex]);
				aBufferIndex++;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				byte byte1 = _nibbleForHexDigit(ac[aBufferIndex]);
				aBufferIndex++;
				nsmutabledata.appendByte((byte) ((byte0 << 4) + byte1));
			} while (true);
			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readArrayContentsIntoArray(char ac[], int index, NSMutableArray<Object> nsmutablearray) {
			int aBufferIndex = index;
			Object aobj[] = new Object[1];
			aBufferIndex++;
			nsmutablearray.removeAllObjects();
			aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
			do {
				if (aBufferIndex == EOT || ac[aBufferIndex] == ')')
					break;
				if (nsmutablearray.count() > 0) {
					if (ac[aBufferIndex] != ',')
						throw new IllegalArgumentException("Property list parsing failed while attempting to read array. No comma found between array elements. At line number: " + _lineNumber
								+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
					aBufferIndex++;
					aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
					if (aBufferIndex == EOT)
						throw new IllegalArgumentException("Property list parsing failed while attempting to read array. Input exhausted before end of array was found. At line number: " + _lineNumber
								+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				}
				if (ac[aBufferIndex] != ')') {
					aobj[0] = null;
					aBufferIndex = _readObjectIntoObjectReference(ac, aBufferIndex, aobj);
					if (aobj[0] == null)
						throw new IllegalArgumentException("Property list parsing failed while attempting to read array. Failed to read content object. At line number: " + _lineNumber + ", column: "
								+ (aBufferIndex - _startOfLineCharIndex) + ".");
					aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
					nsmutablearray.addObject(aobj[0]);
				}
			} while (true);
			if (aBufferIndex == EOT) {
				throw new IllegalArgumentException("Property list parsing failed while attempting to read array. Input exhausted before end of array was found. At line number: " + _lineNumber
						+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			}
			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _readDictionaryContentsIntoDictionary(char ac[], int index, NSMutableDictionary<Object, Object> nsmutabledictionary) {
			int aBufferIndex = index;
			Object aobj[] = new Object[1];
			Object aobj1[] = new Object[1];
			aBufferIndex++;
			if (nsmutabledictionary.count() != 0) {
				for (Enumeration<?> enumeration = nsmutabledictionary.keyEnumerator(); enumeration.hasMoreElements(); nsmutabledictionary.removeObjectForKey(enumeration.nextElement())) {/**/}
			}
			for (aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex); aBufferIndex != EOT && ac[aBufferIndex] != '}';) {
				aBufferIndex = _readObjectIntoObjectReference(ac, aBufferIndex, aobj);
				if (aobj[0] == null || !(aobj[0] instanceof String))
					throw new IllegalArgumentException("Property list parsing failed while attempting to read dictionary. Failed to read key or key is not a String. At line number: " + _lineNumber
							+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (aBufferIndex == EOT || ac[aBufferIndex] != '=')
					throw new IllegalArgumentException("Property list parsing failed while attempting to read dictionary. Read key " + aobj[0] + " with no value. At line number: " + _lineNumber
							+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				aBufferIndex++;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (aBufferIndex == EOT)
					throw new IllegalArgumentException("Property list parsing failed while attempting to read dictionary. Read key " + aobj[0] + " with no value. At line number: " + _lineNumber
							+ ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				aBufferIndex = _readObjectIntoObjectReference(ac, aBufferIndex, aobj1);
				if (aobj1[0] == null)
					throw new IllegalArgumentException("Property list parsing failed while attempting to read dictionary. Failed to read value. At line number: " + _lineNumber + ", column: "
							+ (aBufferIndex - _startOfLineCharIndex) + ".");
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				if (aBufferIndex == EOT || ac[aBufferIndex] != ';')
					throw new IllegalArgumentException("Property list parsing failed while attempting to read dictionary. Read key and value with no terminating semicolon. At line number: "
							+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				aBufferIndex++;
				aBufferIndex = _skipWhitespaceAndComments(ac, aBufferIndex);
				nsmutabledictionary.setObjectForKey(aobj1[0], aobj[0]);
			}

			if (aBufferIndex >= ac.length) {
				throw new IllegalArgumentException("Property list parsing failed while attempting to read dictionary. Exhausted input before end of dictionary was found. At line number: "
						+ _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
			}
			return ++aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _checkForWhitespaceOrComment(char ac[], int index) {
			if (index == EOT || index >= ac.length)
				return _C_NON_COMMENT_OR_SPACE;
			if (_isWhitespace(ac[index]))
				return _C_WHITESPACE;
			if (index + 1 < ac.length) {
				if (ac[index] == '/' && ac[index + 1] == '/')
					return _C_SINGLE_LINE_COMMENT;
				if (ac[index] == '/' && ac[index + 1] == '*')
					return _C_MULTI_LINE_COMMENT;
			}
			return _C_NON_COMMENT_OR_SPACE;
		}

		private int _skipWhitespaceAndComments(char ac[], int index) {
			int aBufferIndex = index;
			for (int j = _checkForWhitespaceOrComment(ac, aBufferIndex); j != _C_NON_COMMENT_OR_SPACE; j = _checkForWhitespaceOrComment(ac, aBufferIndex)) {
				switch (j) {
					case _C_WHITESPACE: // '\002'
						aBufferIndex = _processWhitespace(ac, aBufferIndex);
						break;

					case _C_SINGLE_LINE_COMMENT: // '\003'
						aBufferIndex = _processSingleLineComment(ac, aBufferIndex);
						break;

					case _C_MULTI_LINE_COMMENT: // '\004'
						aBufferIndex = _processMultiLineComment(ac, aBufferIndex);
						break;
				}
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _processWhitespace(char ac[], int index) {
			int aBufferIndex = index;
			for (; aBufferIndex < ac.length && _isWhitespace(ac[aBufferIndex]); aBufferIndex++) {
				if (ac[aBufferIndex] == '\n') {
					_lineNumber++;
					_startOfLineCharIndex = aBufferIndex + 1;
				}
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _processSingleLineComment(char ac[], int index) {
			int aBufferIndex = index;
			for (aBufferIndex += 2; aBufferIndex < ac.length && ac[aBufferIndex] != '\n'; aBufferIndex++) {/**/}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private int _processMultiLineComment(char ac[], int index) {
			int aBufferIndex = index;
			_saveIndexes(aBufferIndex, _lineNumber, _startOfLineCharIndex);
			for (aBufferIndex += 2; aBufferIndex + 1 < ac.length && (ac[aBufferIndex] != '*' || ac[aBufferIndex + 1] != '/'); aBufferIndex++) {
				if (ac[aBufferIndex] == '/' && ac[aBufferIndex + 1] == '*') {
					throw new IllegalArgumentException("Property list parsing does not support embedded multi line comments.The first opening comment was at " + _savedIndexesAsString()
							+ ". A second opening comment was found at line " + _lineNumber + ", column: " + (aBufferIndex - _startOfLineCharIndex) + ".");
				}
				if (ac[aBufferIndex] == '\n') {
					_lineNumber++;
					_startOfLineCharIndex = aBufferIndex + 1;
				}
			}

			if (aBufferIndex + 1 < ac.length && ac[aBufferIndex] == '*' && ac[aBufferIndex + 1] == '/') {
				aBufferIndex += 2;
			} else {
				throw new IllegalArgumentException("Property list parsing failed while attempting to find closing to comment that began at " + _savedIndexesAsString() + ".");
			}
			return aBufferIndex < ac.length ? aBufferIndex : EOT;
		}

		private final byte _nibbleForHexDigit(char c) {
			int i = 0;
			if (c >= '0' && c <= '9')
				i = (byte) (c - 48);
			else if (c >= 'a' && c <= 'f')
				i = (byte) ((c - 97) + 10);
			else if (c >= 'A' && c <= 'F')
				i = (byte) ((c - 65) + 10);
			else
				throw new IllegalArgumentException("Non-hex digit passed to _nibbleForHexDigit()");
			return (byte) i;
		}

		private final boolean _isHexDigit(char c) {
			return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
		}

		private final boolean _isWhitespace(char c) {
			return Character.isWhitespace(c);
		}

		private char _nsToUnicode(int i) {
			return i >= 128 ? (char) NSToPrecompUnicodeTable[i - 128] : (char) i;
		}

	}

	// Refer to CFBinaryPlist.c
	/***/

	/**
	 * Description of the binary plist format derived from http://cvs.opendarwin.org/cgi-bin/cvsweb.cgi/~checkout~/src/CoreFoundation/Parsing.subproj/CFBinaryPList.c?rev=1.1.1.3&content-type=text/plain EBNF description of the file format:
	 *
	 * <pre>
	 * bplist ::= header objectTable offsetTable trailer
	 * header ::= magicNumber fileFormatVersion
	 * magicNumber ::= "bplist"
	 * fileFormatVersion ::= "00"
	 * objectTable ::= { null | bool | fill | number | date | data |
	 *                 string | uid | array | dict }
	 * null  ::= 0b0000 0b0000
	 * bool  ::= false | true
	 * false ::= 0b0000 0b1000
	 * true  ::= 0b0000 0b1001
	 * fill  ::= 0b0000 0b1111         // fill byte
	 * number ::= int | real
	 * int    ::= 0b0001 0bnnnn byte*(2^nnnn)  // 2^nnnn big-endian bytes
	 * real   ::= 0b0010 0bnnnn byte*(2^nnnn)  // 2^nnnn big-endian bytes
	 * date   ::= 0b0011 0b0011 byte*8       // 8 byte float big-endian bytes
	 * data   ::= 0b0100 0bnnnn [int] byte*  // nnnn is number of bytes
	 *                                       // unless 0b1111 then a int
	 *                                       // variable-sized object follows
	 *                                       // to indicate the number of bytes
	 * string ::= asciiString | unicodeString
	 * asciiString   ::= 0b0101 0bnnnn [int] byte*
	 * unicodeString ::= 0b0110 0bnnnn [int] short*
	 *                                       // nnnn is number of bytes
	 *                                       // unless 0b1111 then a int
	 *                                       // variable-sized object follows
	 *                                       // to indicate the number of bytes
	 * uid ::= 0b1000 0bnnnn byte*           // nnnn+1 is # of bytes
	 * array ::= 0b1010 0bnnnn [int] objref* //
	 *                                       // nnnn is number of objref
	 *                                       // unless 0b1111 then a int
	 *                                       // variable-sized object follows
	 *                                       // to indicate the number of objref
	 * dict ::= 0b1110 0bnnnn [int] keyref* objref*
	 *                                       // nnnn is number of keyref and
	 *                                       // objref pairs
	 *                                       // unless 0b1111 then a int
	 *                                       // variable-sized object follows
	 *                                       // to indicate the number of pairs
	 * objref = byte | short                 // if refCount
	 *                                       // is less than 256 then objref is
	 *                                       // an unsigned byte, otherwise it
	 *                                       // is an unsigned big-endian short
	 * keyref = byte | short                 // if refCount
	 *                                       // is less than 256 then objref is
	 *                                       // an unsigned byte, otherwise it
	 *                                       // is an unsigned big-endian short
	 * unused ::= 0b0111 0bxxxx | 0b1001 0bxxxx |
	 *            0b1011 0bxxxx | 0b1100 0bxxxx |
	 *            0b1110 0bxxxx | 0b1111 0bxxxx
	 * offsetTable ::= { int }               // list of ints, byte size of which
	 *                                       // is given in trailer
	 *                                       // these are the byte offsets into
	 *                                       // the file
	 *                                       // number of these is in the trailer
	 * trailer ::= trailerUnused trailerSortVersion offsetIntSize objectRefSize objectCount theTopObject offsetTableOffset
	 * trailerUnused ::= byte*5             // 5 unused bytes
	 * trailerSortVersion ::= byte          // sortVersion 0x0, apparently not used in CF
	 * offsetIntSize ::= byte               // Size (in bytes) of the ints in the offsetTable
	 * objectRefSize ::= byte               // Size (in bytes) of the total number of objects references
	 * offsetCount ::= byte*8               // Object count, unsigned big-endian long
	 * theTopObject ::= byte*8              // Appears to be 0 in CF, unsigned big-endian long
	 * offsetTableOffset ::= byte*8         // Offset of the offset table, unsigned big-endian long
	 * </pre>
	 */
	public static class _BinaryPListParser {

		/**
		 *
		 */
		@SuppressWarnings("unused")
		private long	sortVersion;

		/**
		 * Total count of objrefs and keyrefs.
		 */
		private long	objectRefSize;

		/**
		 * Total count of ofsets.
		 */
		private long	offsetIntSize;

		/**
		 * Total count of objects.
		 */
		private long	numObjects;

		/**
		 *
		 */
		private long	topObject;

		/**
		 * Offset in file of top level offset in offset table.
		 */
		private long	offsetTableOffset;

		/**
		 * Object table. We gradually fill in objects from the binary PList object table into this list.
		 */
		List<Object>	objectTable;

		/**
		 * Object table. We gradually fill in objects from the binary PList object table into this list.
		 */
		List<Long>		objectIndexTable;

		/**
		 * Reference for a binary PList array element. Used by object table.
		 */
		protected static class BinaryArray {
			private final int[]		_objref;

			private final List<?>	_objectTable;

			public BinaryArray(List<?> objectTable, int count) {
				super();
				_objectTable = objectTable;
				_objref = new int[count];
			}

			public int size() {
				return _objref.length;
			}

			public void addValueRef(int index, long ref) {
				_objref[index] = (int) ref;
			}

			/**
			 * @param i
			 * @return refered object
			 */
			public Object getValue(int i) {
				return _objectTable.get(_objref[i]);
			}

			@Override
			public String toString() {
				StringBuffer buf = new StringBuffer("BinaryArray{");
				for (int i = 0; i < _objref.length; i++) {
					if (i > 0) {
						buf.append(',');
					}
					if (_objectTable.size() > _objref[i] && _objectTable.get(_objref[i]) != this) {
						buf.append(_objectTable.get(_objref[i]));
					} else {
						buf.append("*" + _objref[i]);
					}
				}
				buf.append('}');
				return buf.toString();
			}

			/**
			 * @return NSArray
			 */
			// Note: as of 10.6 GM, the mutability option was turned off in CF
			public NSArray<Object> toNSArray() {
				NSMutableArray<Object> anArray = new NSMutableArray<Object>();

				for (int i = 0; i < _objref.length; i++) {
					Object ref = _objectTable.get(_objref[i]);

					// TODO: what do we do if we encounter 'self' as a reference?
					if (ref instanceof BinaryArray) {
						anArray.addObject(((BinaryArray) ref).toNSArray());
					} else if (ref instanceof BinarySet) {
						anArray.addObject(((BinarySet) ref).toNSSet());
					} else if (ref instanceof BinaryDict) {
						anArray.addObject(((BinaryDict) ref).toNSDictionary());
					} else {
						anArray.addObject(ref);
					}
				}

				return anArray.immutableClone();
			}
		}

		protected static class BinarySet {
			private final int[]		_objref;

			private final List<?>	_objectTable;

			public BinarySet(List<?> objectTable, int count) {
				super();
				_objectTable = objectTable;
				_objref = new int[count];
			}

			public int size() {
				return _objref.length;
			}

			public void addValueRef(int index, long ref) {
				_objref[index] = (int) ref;
			}

			/**
			 * @param i
			 * @return refered object
			 */
			public Object getValue(int i) {
				return _objectTable.get(_objref[i]);
			}

			@Override
			public String toString() {
				StringBuffer buf = new StringBuffer("BinarySet{");
				for (int i = 0; i < _objref.length; i++) {
					if (i > 0) {
						buf.append(',');
					}
					if (_objectTable.size() > _objref[i] && _objectTable.get(_objref[i]) != this) {
						buf.append(_objectTable.get(_objref[i]));
					} else {
						buf.append("*" + _objref[i]);
					}
				}
				buf.append('}');
				return buf.toString();
			}

			/**
			 * @return NSSet
			 */
			// Note: as of 10.6 GM, the mutability option was turned off in CF
			public NSSet<Object> toNSSet() {
				NSMutableSet<Object> aSet = new NSMutableSet<Object>();

				for (int i = 0; i < _objref.length; i++) {
					Object ref = _objectTable.get(_objref[i]);

					// TODO: what do we do if we encounter 'self' as a reference?
					if (ref instanceof BinaryArray) {
						aSet.addObject(((BinaryArray) ref).toNSArray());
					} else if (ref instanceof BinarySet) {
						aSet.addObject(((BinarySet) ref).toNSSet());
					} else if (ref instanceof BinaryDict) {
						aSet.addObject(((BinaryDict) ref).toNSDictionary());
					} else {
						aSet.addObject(ref);
					}
				}

				return aSet.immutableClone();
			}
		}

		/**
		 * Holder for a binary PList dict element.
		 */
		protected static class BinaryDict {
			private final int[]		_keyref;

			private final int[]		_objref;

			private final List<?>	_objectTable;

			public BinaryDict(List<?> objectTable, int count) {
				super();
				_objectTable = objectTable;
				_keyref = new int[count];
				_objref = new int[count];
			}

			public int size() {
				return _objref.length;
			}

			public void addKeyRef(int index, long ref) {
				_keyref[index] = (int) ref;
			}

			public void addValueRef(int index, long ref) {
				_objref[index] = (int) ref;
			}

			/**
			 * @param i
			 * @return key
			 */
			public String getKey(int i) {
				return _objectTable.get(_keyref[i]).toString();
			}

			/**
			 * @param i
			 * @return value
			 */
			public Object getValue(int i) {
				return _objectTable.get(_objref[i]);
			}

			@Override
			public String toString() {
				StringBuffer buf = new StringBuffer("BinaryDict{");
				for (int i = 0; i < _keyref.length; i++) {
					if (i > 0) {
						buf.append(',');
					}
					if (_keyref[i] < 0 || _keyref[i] >= _objectTable.size()) {
						buf.append("#" + _keyref[i]);
					} else if (_objectTable.get(_keyref[i]) == this) {
						buf.append("*" + _keyref[i]);
					} else {
						buf.append(_objectTable.get(_keyref[i]));
					}
					buf.append(':');
					if (_objref[i] < 0 || _objref[i] >= _objectTable.size()) {
						buf.append("#" + _objref[i]);
					} else if (_objectTable.get(_objref[i]) == this) {
						buf.append("*" + _objref[i]);
					} else {
						buf.append(_objectTable.get(_objref[i]));
					}
				}
				buf.append('}');
				return buf.toString();
			}

			/**
			 * @return NSDictionary
			 */
			public NSDictionary<String, ?> toNSDictionary() {
				NSMutableDictionary<String, ?> aDict = new NSMutableDictionary<String, Object>(_keyref.length);
				for (int i = 0; i < _keyref.length; i++) {

					if (_keyref[i] < 0 || _keyref[i] >= _objectTable.size()) {
						logger.error("Object table is in illegal state.  The key reference " + i + " is larger than the object table size " + _objectTable.size());
					} else if (_objectTable.get(_keyref[i]) == this) {
						logger.warn("Encountered reference to 'self' in object table.");
					}
					String key = (String) _objectTable.get(_keyref[i]);
					if (_objref[i] < 0 || _objref[i] >= _objectTable.size()) {
						logger.error("Object table is in illegal state.  The object reference " + i + " is larger than the object table size " + _objectTable.size());
					}
					Object value = _objectTable.get(_objref[i]);

					// TODO: what do we do if we encounter 'self' as a reference, we should bail since it could cause infinite recursion?
					if (value instanceof BinaryArray) {
						aDict.takeValueForKey(((BinaryArray) value).toNSArray(), key);
					} else if (value instanceof BinarySet) {
						aDict.takeValueForKey(((BinarySet) value).toNSSet(), key);
					} else if (value instanceof BinaryDict) {
						aDict.takeValueForKey(((BinaryDict) value).toNSDictionary(), key);
					} else {
						aDict.takeValueForKey(value, key);
					}

				}
				return aDict.immutableClone();
			}
		}

		protected static class EncodedObject {

			protected byte[]	_bytes;

			private EncodedObject() {
				super();
			}

			public EncodedObject(NSData data) {
				this();
				_bytes = data.bytes();
			}

			public EncodedObject(byte[] data) {
				this();
				_bytes = data;
			}

			/**
			 * Appends the object to the data
			 *
			 * @param data
			 * @param objectRefSize
			 */
			public void appendToData(NSMutableData data, int objectRefSize) {
				data.appendBytes(_bytes);
			}

			protected static int refSizeForValue(long value) {
				int refsize = 0;
				if (value <= ByteMaxValue) {
					refsize = 1;
				} else if (value <= ShortMaxValue) {
					refsize = 2;
				} else if (value <= IntegerMaxValue) {
					refsize = 4;
				} else {
					refsize = 8;
				}
				return refsize;
			}

			protected static byte[] encodeRef(Long ref, int objectRefSize) {
				long aRef = ref.longValue();
				byte[] encodedValue = null;
				if (objectRefSize == 8) {
					// This is currently unsupported
					encodedValue = new byte[8];
					encodedValue[0] = (byte) ((aRef >>> 56) & 0xff);
					encodedValue[1] = (byte) ((aRef >>> 48) & 0xff);
					encodedValue[2] = (byte) ((aRef >>> 40) & 0xff);
					encodedValue[3] = (byte) ((aRef >>> 32) & 0xff);
					encodedValue[4] = (byte) ((aRef >>> 24) & 0xff);
					encodedValue[5] = (byte) ((aRef >>> 16) & 0xff);
					encodedValue[6] = (byte) ((aRef >>> 8) & 0xff);
					encodedValue[7] = (byte) ((aRef >>> 0) & 0xff);
				} else if (objectRefSize == 4) {
					// This is currently unsupported
					encodedValue = new byte[4];
					encodedValue[0] = (byte) ((aRef >>> 24) & 0xff);
					encodedValue[1] = (byte) ((aRef >>> 16) & 0xff);
					encodedValue[2] = (byte) ((aRef >>> 8) & 0xff);
					encodedValue[3] = (byte) ((aRef >>> 0) & 0xff);
				} else if (objectRefSize == 2) {
					encodedValue = new byte[2];
					encodedValue[0] = (byte) ((aRef >>> 8) & 0xff);
					encodedValue[1] = (byte) ((aRef >>> 0) & 0xff);
				} else if (objectRefSize == 1) {
					encodedValue = new byte[1];
					encodedValue[0] = (byte) (aRef & 0xff);
				}
				return encodedValue;
			}

			@Override
			public String toString() {
				return toString(2);
			}

			public String toString(int objectRefSize) {
				NSMutableData data = new NSMutableData(1024);
				appendToData(data, objectRefSize);
				return "[" + data.length() + "] " + data._hexString();
			}

		}

		protected static class EncodedDictionary extends EncodedObject {

			protected List<Long>	_keyRefs;

			protected List<Long>	_valueRefs;

			public EncodedDictionary(NSData data) {
				this(data.bytes());
			}

			public EncodedDictionary(byte[] data) {
				// The data is actually the marker and the object ref to follow.
				super(data);
				_keyRefs = new ArrayList<Long>();
				_valueRefs = new ArrayList<Long>();
			}

			public void addKeyRef(long ref) {
				_keyRefs.add(Long.valueOf(ref));
			}

			public void addValueRef(long ref) {
				_valueRefs.add(Long.valueOf(ref));
			}

			@Override
			public void appendToData(NSMutableData data, int objectRefSize) {
				super.appendToData(data, objectRefSize);

				for (Long ref : _keyRefs) {
					data.appendBytes(EncodedObject.encodeRef(ref, objectRefSize));
				}

				for (Long ref : _valueRefs) {
					data.appendBytes(EncodedObject.encodeRef(ref, objectRefSize));
				}
			}

		}

		protected static class EncodedArray extends EncodedObject {

			protected List<Long>	_valueRefs;

			public EncodedArray(NSData data) {
				this(data.bytes());
			}

			public EncodedArray(byte[] data) {
				// The data is actually the marker and the object ref to follow.
				super(data);
				_valueRefs = new ArrayList<Long>();
			}

			public void addValueRef(long ref) {
				_valueRefs.add(Long.valueOf(ref));
			}

			@Override
			public void appendToData(NSMutableData data, int objectRefSize) {
				super.appendToData(data, objectRefSize);

				for (Long ref : _valueRefs) {
					data.appendBytes(EncodedObject.encodeRef(ref, objectRefSize));
				}
			}

		}

		protected static class EncodedSet extends EncodedObject {

			protected List<Long>	_valueRefs;

			public EncodedSet(NSData data) {
				this(data.bytes());
			}

			public EncodedSet(byte[] data) {
				// The data is actually the marker and the object ref to follow.
				super(data);
				_valueRefs = new ArrayList<Long>();
			}

			public void addValueRef(long ref) {
				_valueRefs.add(Long.valueOf(ref));
			}

			@Override
			public void appendToData(NSMutableData data, int objectRefSize) {
				super.appendToData(data, objectRefSize);

				for (Long ref : _valueRefs) {
					data.appendBytes(EncodedObject.encodeRef(ref, objectRefSize));
				}
			}

		}

		/**
		 * Creates a new instance.
		 */
		public _BinaryPListParser() {
			super();
		}

		protected static long readByte(byte[] bytes, int startIndex) throws IOException {
			if (startIndex + 1 > bytes.length) {
				throw new EOFException("Premature end of file starting at byte index " + startIndex);
			}
			long ch1 = bytes[startIndex] & 0xffL;
			return ch1;
		}

		protected static long readShort(byte[] bytes, int startIndex) throws IOException {
			if (startIndex + 2 > bytes.length) {
				throw new EOFException("Premature end of file starting at byte index " + startIndex);
			}
			long ch1 = bytes[startIndex] & 0xffL;
			long ch2 = bytes[startIndex + 1] & 0xffL;
			return (ch1 << 8) | (ch2 << 0);
		}

		protected static long readInt(byte[] bytes, int startIndex) throws IOException {
			if (startIndex + 4 > bytes.length) {
				throw new EOFException("Premature end of file starting at byte index " + startIndex);
			}
			long ch1 = bytes[startIndex] & 0xffL;
			long ch2 = bytes[startIndex + 1] & 0xffL;
			long ch3 = bytes[startIndex + 2] & 0xffL;
			long ch4 = bytes[startIndex + 3] & 0xffL;

			return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0));
		}

		protected static long readLong(byte[] bytes, int startIndex) throws IOException {
			if (startIndex + 8 > bytes.length) {
				throw new EOFException("Premature end of file starting at byte index " + startIndex);
			}
			return (readInt(bytes, startIndex) << 32) | (readInt(bytes, startIndex + 4) & 0xFFFFFFFFL);
		}

		/**
		 * convert double to byte array
		 *
		 * @param d
		 * @param size
		 *            either 4 or 8
		 * @return byte array
		 */
		protected static byte[] doubleToByteArray(double d, int size) {
			long i = Double.doubleToRawLongBits(d);
			return longToByteArray2(i, size);
		}

		/**
		 * convert float to byte array
		 *
		 * @param f
		 * @param size
		 *            either 4 or 8
		 * @return byte array
		 */
		protected static byte[] floatToByteArray(float f, int size) {
			int i = Float.floatToRawIntBits(f);
			return intToByteArray(i, size);
		}

		/**
		 * convert int to byte array
		 *
		 * @param param
		 * @param size
		 *            either 4 or 8
		 * @return byte array
		 */
		protected static byte[] intToByteArray(int param, int size) {
			byte b[] = null;
			if (size == 4) {
				b = new byte[4];
			} else {
				b = new byte[size];
			}

			for (int i = 0; i < size; i++) {
				int offset = (b.length - 1 - i) * 8;
				b[i] = (byte) ((param >>> offset) & 0x0f);
			}
			return b;
		}

		protected static byte[] longToByteArray2(long l, int size) {
			byte b[] = null;
			if (size == 4) {
				b = new byte[4];
			} else {
				b = new byte[8];
			}
			//
			// ByteBuffer buf = ByteBuffer.wrap(b);
			// buf.putLong(l);
			// return b;

			ByteBuffer buf = ByteBuffer.wrap(b);
			LongBuffer lBuffer = buf.asLongBuffer();
			lBuffer.put(0, l);
			return b;
		}

		// Alternate byte conversion
		protected static byte[] intToByteArray2(int i, int size) {
			byte b[] = null;
			if (size == 4) {
				b = new byte[4];
			} else {
				b = new byte[8];
			}

			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.putInt(i);
			return b;
		}

		// Alternate for byte conversion
		protected static int byteArrayToInt2(byte[] b) {
			ByteBuffer buf = ByteBuffer.wrap(b);
			return buf.getInt();
		}

		// not exactly sure which cases require a fill byte
		protected static PListType typeForObject(Object object) {
			if (object == null) {
				return PListType.NULL;
			}

			if (object instanceof String) {
				return PListType.STRING;
			} else if (object instanceof Integer || object instanceof Long || object instanceof BigInteger) {
				return PListType.INTEGER;
			} else if (object instanceof Float || object instanceof Double || object instanceof BigDecimal) {
				return PListType.FLOAT;
			} else if (object instanceof Boolean) {
				return PListType.BOOLEAN;
			} else if (object instanceof byte[] || object instanceof NSData) {
				return PListType.DATA;
			} else if (object instanceof Date) {
				return PListType.DATE;
			} else if (object instanceof Map<?, ?>) {
				return PListType.DICTIONARY;
            } else if (object instanceof NSDictionary) {
                return PListType.DICTIONARY;
			} else if (object instanceof List<?>) {
				return PListType.ARRAY;
            } else if (object instanceof NSArray) {
                return PListType.ARRAY;
			} else if (object instanceof Set<?>) {
				return PListType.ARRAY;
            } else if (object instanceof NSSet) {
                return PListType.ARRAY;
			} else if (object instanceof UUID) {
				return PListType.UUID;
			}

			return PListType.UNKNOWN;
		}

		protected long encodeObject(Object object, List<EncodedObject> objectList, Map<Object, Long> uniquingTable) {
			if (uniquingTable.containsKey(object)) {
				// Check if we have already seen the object.
				return uniquingTable.get(object).longValue();
			}
			long objectIndex = objectList.size();
			// Register it so that we do not encode it twice.
			uniquingTable.put(object, Long.valueOf(objectIndex));
			PListType type = typeForObject(object);
			switch (type) {
				case STRING: {
					objectList.add(new EncodedObject(encodeString((String) object)));
					break;
				}
				case INTEGER: {
					objectList.add(new EncodedObject(encodeInt(((Number) object).longValue())));
					break;
				}
				case FLOAT: {
					objectList.add(new EncodedObject(encodeReal(((Number) object).doubleValue())));
					break;
				}
				case DATE: {
					objectList.add(new EncodedObject(encodeDate((Date) object)));
					break;
				}
				case BOOLEAN: {
					objectList.add(new EncodedObject(encodeBoolean(((Boolean) object).booleanValue())));
					break;
				}
				case DATA: {
					byte[] theBytes = null;
					if (object instanceof NSData) {
						theBytes = ((NSData) object).bytes();
					} else {
						theBytes = (byte[]) object;
					}
					objectList.add(new EncodedObject(encodeData(theBytes)));
					break;
				}
				case DICTIONARY: {
				    if (object instanceof Map) {
    					Map<?, ?> dict = (Map<?, ?>) object;
    
    					// 1. append dictionary markers here which includes the count
    					int count = dict.size();
    					EncodedDictionary dictionary = new EncodedDictionary(encodeCount(count, Type.kCFBinaryPlistMarkerDict));
    					objectList.add(dictionary);
    
    					// 2. write keys and values
    					for (Object aKey : dict.keySet()) {
    						dictionary.addKeyRef(encodeObject(aKey, objectList, uniquingTable));
    						dictionary.addValueRef(encodeObject(dict.get(aKey), objectList, uniquingTable));
    					}
				    }
				    else {
                        NSDictionary dict = (NSDictionary) object;
                        
                        // 1. append dictionary markers here which includes the count
                        int count = dict.count();
                        EncodedDictionary dictionary = new EncodedDictionary(encodeCount(count, Type.kCFBinaryPlistMarkerDict));
                        objectList.add(dictionary);
    
                        // 2. write keys and values
                        for (Enumeration keyEnum = dict.keyEnumerator(); keyEnum.hasMoreElements(); ) {
                            Object aKey = keyEnum.nextElement();
                            dictionary.addKeyRef(encodeObject(aKey, objectList, uniquingTable));
                            dictionary.addValueRef(encodeObject(dict.objectForKey(aKey), objectList, uniquingTable));
                        }
				    }
					break;
				}
				case ARRAY: {
				    if (object instanceof List) {
    					// 1. append array markers here
    					List<?> list = (List<?>) object;
    					int count = list.size();
    					EncodedArray array = new EncodedArray(encodeCount(count, Type.kCFBinaryPlistMarkerArray));
    					objectList.add(array);
    
    					// 2. write all values
    					for (Object aValue : list) {
    						array.addValueRef(encodeObject(aValue, objectList, uniquingTable));
    					}
				    }
				    else {
                        // 1. append array markers here
                        NSArray list = (NSArray) object;
                        int count = list.count();
                        EncodedArray array = new EncodedArray(encodeCount(count, Type.kCFBinaryPlistMarkerArray));
                        objectList.add(array);
    
                        // 2. write all values
                        for (Enumeration valuesEnum = list.objectEnumerator(); valuesEnum.hasMoreElements(); ) {
                            Object aValue = valuesEnum.nextElement();
                            array.addValueRef(encodeObject(aValue, objectList, uniquingTable));
                        }
				    }
					break;
				}
				case SET: {
				    if (object instanceof List) {
    					// 1. append set markers here
    					List<?> list = (List<?>) object;
    					int count = list.size();
    					EncodedSet set = new EncodedSet(encodeCount(count, Type.kCFBinaryPlistMarkerSet));
    					objectList.add(set);
    
    					// 2. write all values
    					for (Object aValue : list) {
    						set.addValueRef(encodeObject(aValue, objectList, uniquingTable));
    					}
				    }
				    else {
                        // 1. append set markers here
                        NSArray list = (NSArray) object;
                        int count = list.count();
                        EncodedSet set = new EncodedSet(encodeCount(count, Type.kCFBinaryPlistMarkerSet));
                        objectList.add(set);
    
                        // 2. write all values
                        for (Enumeration valuesEnum = list.objectEnumerator(); valuesEnum.hasMoreElements(); ) {
                            Object aValue = valuesEnum.nextElement();
                            set.addValueRef(encodeObject(aValue, objectList, uniquingTable));
                        }
				    }
					break;
				}
				case NULL: {
					objectList.add(new EncodedObject(encodeNull()));
					break;
				}
				case UUID: {
					objectList.add(new EncodedObject(encodeUUID((UUID) object)));
					break;
				}
				case FILL: {
					objectList.add(new EncodedObject(encodeFillByte()));
					break;
				}
				default: {
					objectList.add(new EncodedObject(encodeString(object.toString())));
					break;
				}
			}
			return objectIndex;
		}

		/**
		 *
		 */
		protected static final long	ByteMaxValue	= 0x00000000000000ffL;

		/**
		 *
		 */
		protected static final long	ShortMaxValue	= 0x000000000000ffffL;

		/**
		 *
		 */
		protected static final long	IntegerMaxValue	= 0x00000000ffffffffL;

		/**
		 * Write binary plist to stream
		 *
		 * @param plist
		 * @param out
		 * @see PListFormat
		 */
		public void writePropertyListToStream(Object plist, OutputStream out) {
			if (plist == null || out == null) {
				logger.warn("Encountered empty plist or null outputstream, returning");
				return;
			}

			NSMutableData theData = new NSMutableData();

			// write header
			theData.appendBytes("bplist00".getBytes());

			// add six padded bytes

			// do uniquing

			// flatten plist into object table
			List<EncodedObject> objectList = new ArrayList<EncodedObject>(512);
			Map<Object, Long> uniquingTable = new HashMap<Object, Long>(2048);
			long theTopObject = encodeObject(plist, objectList, uniquingTable);

			// determine ref size
			long numberOfObjects = objectList.size();
			int refsize = EncodedObject.refSizeForValue(numberOfObjects);

			List<Long> objectOffsets = new ArrayList<Long>(objectList.size());
			// write the byte
			for (EncodedObject object : objectList) {
				objectOffsets.add(Long.valueOf(theData.length()));
				object.appendToData(theData, refsize);
			}
			int offsetTableStart = theData.length();
			
			// CF expects intsize to be calculated based on the offset table start position
			// and not on the offset of the last object.
			int intsize = EncodedObject.refSizeForValue(offsetTableStart);

			for (Long offset : objectOffsets) {
				theData.appendBytes(EncodedObject.encodeRef(offset, intsize));
			}

			// add trailer calculations to data
			// typedef struct {
			// uint8_t _unused[5];
			// uint8_t _sortVersion;
			// uint8_t _offsetIntSize;
			// uint8_t _objectRefSize;
			// uint64_t _numObjects;
			// uint64_t _topObject;
			// uint64_t _offsetTableOffset;
			// } CFBinaryPlistTrailer;
			theData.appendBytes(new byte[5]);
			theData.appendByte((byte) 0x0); // _sortVersion which AFIK is not being used in CF
			theData.appendByte((byte) intsize); // _offsetIntSize which byte size for all ints in file
			theData.appendByte((byte) refsize); // _objectRefSize which is total # of objects value in bytes
			theData.appendBytes(longToByteArray2(numberOfObjects, 8)); // _numObjects which is object count
			theData.appendBytes(longToByteArray2(theTopObject, 8)); // _topObject appears to be set to 0 in CF
			theData.appendBytes(longToByteArray2(offsetTableStart, 8)); // _offsetTableOffset

			// write to stream
			try {
				theData.writeToStream(out);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Failed to write binary property list ", e);
			}
		}

		/**
		 * Returns the object represented by the given property list object loaded from the given URL.
		 * 
		 * @param url the URL to load
		 * @return the object represented by the given property list
		 * @throws IOException if the loading fails
		 */
		public Object propertyListWithURL(URL url) {
			try {
				 URLConnection conn = url.openConnection();
				 InputStream is = conn.getInputStream();
				 try {
					 return _propertyListWithStream(is);
				 }
				 finally {
					 is.close();
				 }
			} catch (RuntimeException e) {
				throw new RuntimeException("Failed to decode binary plist at " + url, e);
			} catch (IOException e) {
				throw new RuntimeException("Failed to decode binary plist at " + url, e);
			}
		}
		
		/**
		 * Returns the object represented by the given property list object loaded from the given stream.
		 * 
		 * @param is the InputStream to load
		 * @return the object represented by the given property list
		 * @throws IOException if the loading fails
		 */
		public Object propertyListWithStream(InputStream is) {
			try {
				return _propertyListWithStream(is);
			} catch (RuntimeException e) {
				throw new RuntimeException("Failed to decode binary plist from the provided stream.", e);
			} catch (IOException e) {
				throw new RuntimeException("Failed to decode binary plist from the provided stream.", e);
			}
		}
		
		/**
		 * Returns the object represented by the given property list object loaded from the given stream.
		 * 
		 * @param is the InputStream to load
		 * @return the object represented by the given property list
		 * @throws IOException if the loading fails
		 */
		protected Object _propertyListWithStream(InputStream is) throws IOException {
			if (is == null) {
				logger.error("The stream paramenter cannot be null.");
				return null;
			}

			// parse the binary data into object table
			_parseBinaryStream(is);

			Object binaryObject = objectTable.get(0);
			Object propertyListObject = null;
			if (binaryObject instanceof BinaryDict) {
				BinaryDict bDict = (BinaryDict)binaryObject; 
				propertyListObject = bDict.toNSDictionary();
			}
			else if (binaryObject instanceof BinaryArray) {
				BinaryArray barray = (BinaryArray) binaryObject;
				propertyListObject = barray.toNSArray();	
			}
			else if (binaryObject instanceof BinarySet) {
				BinarySet bset = (BinarySet) binaryObject;
				propertyListObject = bset.toNSSet();	
			}
			else {
				propertyListObject = binaryObject;
			}
			return propertyListObject;
		}

		public Document propertyListDocumentWithURL(URL url) {
			if (url == null) {
				logger.error("URL paramenter cannot be null");
				return null;
			}

			try {
				 URLConnection conn = url.openConnection();
				 InputStream is = conn.getInputStream();
				 try {
					 return propertyListDocumentWithStream(is);
				 }
				 finally {
					 is.close();
				 }
			} catch (RuntimeException e) {
				throw new RuntimeException("Failed to decode binary plist at " + url, e);
			} catch (IOException e) {
				throw new RuntimeException("Failed to decode binary plist at " + url, e);
			}
		}
		
		public Document propertyListDocumentWithStream(InputStream is) {
			if (is == null) {
				logger.error("InputStream paramenter cannot be null");
				return null;
			}

			try {
				// parse the binary data into object table
				_parseBinaryStream(is);
				
				return toPropertyListDocument();
			}
			catch (ParserConfigurationException e) {
				throw new RuntimeException("Failed to parse binary plist.", e);
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to parse binary plist.", e);
			}
		}
		
		protected Document toPropertyListDocument() throws ParserConfigurationException {
			// Convert the object table to XML and return it
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			Document root = null;
			builder = factory.newDocumentBuilder();
			root = builder.newDocument();
			root.setXmlStandalone(true);
			// TODO: Java 1.5/DOM level 3 does not support doctype node editing, add plist dtd when we can
			Element elem = root.createElement("plist");
			elem.setAttribute("version", "1.0");
			root.appendChild(elem);
			convertObjectTableToXML(elem, root, objectTable.get(0));
			return root;
		}

		/**
		 * @param theBytes
		 * @return true if binary plist header is identified, false otherwise.
		 */
		public static boolean isBinaryPList(byte[] theBytes) {
			try {
				long bpli = readInt(theBytes, 0);
				long st00 = readInt(theBytes, 4); // can this be st01?
				// Tiger and earlier will parse "bplist00"
				// Leopard will parse "bplist00" or "bplist01"
				// SnowLeopard will parse "bplist0?" where ? is any one character
				st00 = st00 & 0xFFFFFF00;
				if (bpli != 0x62706c69 || st00 != 0x73743000) {
					return false;
				}
			} catch (IOException e) {
				// swallow
				return false;
			}
			return true;
		}

		protected boolean _parseBinaryStream(InputStream is) throws IOException {
			if (is == null) {
				throw new IOException("Property list input stream cannot be null");
			}
			boolean parsed = false;
			BufferedInputStream bis = new BufferedInputStream(is);

			NSData data = new NSData(bis, -1); // bigger chunk size?
			byte[] theBytes = data.bytes();

			// Parse the HEADER
			// ----------------
			// magic number ("bplist")
			// file format version ("00")
			/*
			 * typedef struct { uint8_t _magic[6]; uint8_t _version[2]; } CFBinaryPlistHeader;
			 */
			long bpli = readInt(theBytes, 0);
			long st00 = readInt(theBytes, 4); // can this be st01?
			// Tiger and earlier will parse "bplist00"
			// Leopard will parse "bplist00" or "bplist01"
			// SnowLeopard will parse "bplist0?" where ? is any one character
			st00 = st00 & 0xFFFFFF00;
			if (bpli != 0x62706c69 || st00 != 0x73743000) {
				throw new IOException("Cannot parse binary property list.  Data does not start with 'bplist00' magic. Got " + bpli + " " + st00);
			}

			// Parse the TRAILER
			// ----------------
			// byte size of offset ints in offset table
			// byte size of object refs in arrays and dicts
			// number of offsets in offset table (also is number of objects)
			// element # in offset table which is top level object
			/*
			 * typedef struct { uint8_t _unused[6]; uint8_t _offsetIntSize; uint8_t _objectRefSize; uint64_t _numObjects; uint64_t _topObject; uint64_t _offsetTableOffset; } CFBinaryPlistTrailer;
			 */

			// In Leopard, the unused bytes in the trailer must be 0 or the parse will fail
			// This check is not present in Tiger and earlier or after Leopard
			// first six bytes should be unused
			// if (theBytes[theBytes.length - 32] != 0 || theBytes[theBytes.length - 31] != 0 || theBytes[theBytes.length - 30] != 0 || theBytes[theBytes.length - 29] != 0
			// || theBytes[theBytes.length - 28] != 0 || theBytes[theBytes.length - 27] != 0) {
			// throw new IOException("Bad trailer values detected");
			// }

			// typedef struct {
			// uint8_t _unused[5];
			// uint8_t _sortVersion;
			// uint8_t _offsetIntSize;
			// uint8_t _objectRefSize;
			// uint64_t _numObjects;
			// uint64_t _topObject;
			// uint64_t _offsetTableOffset;
			// } CFBinaryPlistTrailer;

			//
			sortVersion = readByte(theBytes, theBytes.length - 27);

			// count of offset ints in offset table
			offsetIntSize = readByte(theBytes, theBytes.length - 26);

			// count of object refs in arrays and dicts
			objectRefSize = readByte(theBytes, theBytes.length - 25);

			// count of offsets in offset table (also is number of objects)
			numObjects = readLong(theBytes, theBytes.length - 24);

			// appears to be set to 0 in CF
			topObject = readLong(theBytes, theBytes.length - 16);

			// element # in offset table which is top level object
			offsetTableOffset = readLong(theBytes, theBytes.length - 8);

			// validate trailer
			if (numObjects < 1) {
				logger.error("numObjects < 1");
				return false;
			}
			if (offsetTableOffset < 9) {
				logger.error("offsetTableOffset < 9");
				return false;
			}
			if (offsetIntSize < 1) {
				logger.error("offsetIntSize < 1");
				return false;
			}
			if (objectRefSize < 1) {
				logger.error("objectRefSize < 1");
				return false;
			}
			if (offsetTableOffset < 1) {
				logger.error("offsetTableOffset < 1");
				return false;
			}
			int numberOfBytes = (int) offsetIntSize;
			int expectedLength = (int) (offsetTableOffset + (numObjects * numberOfBytes) + 32);
			if (theBytes.length != expectedLength) {
				logger.error("bytes read do not correspond to the expected: " + expectedLength + " got: " + theBytes.length);
				return false;
			}

			// Parse the OBJECT INDEX TABLE
			// ----------------------------
			objectIndexTable = new ArrayList<Long>((int) numObjects);
			for (int i = 0; i < numObjects; i++) {
				if (offsetIntSize == 1) {
					objectIndexTable.add(Long.valueOf(readByte(theBytes, ((int) ((i * numberOfBytes) + offsetTableOffset)))));
				} else if (offsetIntSize == 2) {
					objectIndexTable.add(Long.valueOf(readShort(theBytes, ((int) ((i * numberOfBytes) + offsetTableOffset)))));
				} else if (offsetIntSize == 4) {
					objectIndexTable.add(Long.valueOf(readInt(theBytes, ((int) ((i * numberOfBytes) + offsetTableOffset)))));
				} else {
					objectIndexTable.add(Long.valueOf(readLong(theBytes, ((int) ((i * numberOfBytes) + offsetTableOffset)))));
				}
			}

			// Parse the OBJECT TABLE
			// ----------------------
			objectTable = new ArrayList<Object>((int) numObjects);
			for (Long index : objectIndexTable) {
				parseObject(theBytes, index.intValue());
			}

			return parsed;
		}

		/**
		 * Converts the object table in the binary PList into an XMLElement.
		 *
		 * @param parent
		 *            - parent node
		 * @param doc
		 *            - root Document object
		 * @param object
		 *            - object table
		 */
		private void convertObjectTableToXML(Element parent, Document doc, Object object) {
			Element elem = null;

			if (object instanceof BinaryDict) {
				BinaryDict dict = (BinaryDict) object;
				elem = doc.createElement("dict");
				for (int i = 0; i < dict.size(); i++) {
					Element key = doc.createElement("key");
					key.setTextContent(dict.getKey(i));
					elem.appendChild(key);
					convertObjectTableToXML(elem, doc, dict.getValue(i));
				}

			} else if (object instanceof BinaryArray) {
				BinaryArray arr = (BinaryArray) object;
				elem = doc.createElement("array");

				for (int i = 0; i < arr.size(); i++) {
					convertObjectTableToXML(elem, doc, arr.getValue(i));
				}
			} else if (object instanceof BinarySet) {
				// In 10.6 GM, the support for kCFBinaryPlistMarkerSet in xml isn't completely there, we'll dump it to 'array' for now
				BinarySet arr = (BinarySet) object;
				elem = doc.createElement("array");

				for (int i = 0; i < arr.size(); i++) {
					convertObjectTableToXML(elem, doc, arr.getValue(i));
				}
			} else if (object instanceof String) {
				elem = doc.createElement("string");
				elem.setTextContent((String) object);
			} else if (object instanceof Integer) {
				elem = doc.createElement("integer");
				elem.setTextContent(object.toString());
			} else if (object instanceof Long) {
				elem = doc.createElement("integer");
				elem.setTextContent(object.toString());
			} else if (object instanceof Float) {
				elem = doc.createElement("real");
				elem.setTextContent(object.toString());
			} else if (object instanceof Double) {
				elem = doc.createElement("real");
				elem.setTextContent(object.toString());
			} else if (object instanceof Boolean) {
				Boolean b = Boolean.valueOf(object.toString());
				if (b.booleanValue()) {
					elem = doc.createElement("true");
				} else {
					elem = doc.createElement("false");
				}
			} else if (object instanceof byte[]) {
				elem = doc.createElement("data");
				elem.setTextContent(new String(_NSBase64.encode((byte[]) object)));
			} else if (object instanceof NSData) {
				elem = doc.createElement("data");
				elem.setTextContent(new String(_NSBase64.encode(((NSData) object).bytes())));
			} else if (object instanceof Date) {
				DateFormat format = new SimpleDateFormat(DefaultSimpleDateFormatPattern);
				elem = doc.createElement("date");
				elem.setTextContent(format.format((Date) object));
			} else {
				elem = doc.createElement("unsupported");
				elem.setTextContent(object.toString());
			}

			parent.appendChild(elem);
		}

		// See CF/ForFoundationOnly.h
		/**
		 * Enum for binary property list types
		 */
		public static enum Type {
			/***/
			kCFBinaryPlistMarkerNull(0x00, "kCFBinaryPlistMarkerNull", true),
			/***/
			kCFBinaryPlistMarkerFalse(0x08, "kCFBinaryPlistMarkerFalse", true),
			/***/
			kCFBinaryPlistMarkerTrue(0x09, "kCFBinaryPlistMarkerTrue", true),
			/***/
			kCFBinaryPlistMarkerFill(0x0F, "kCFBinaryPlistMarkerFill", true),
			/***/
			kCFBinaryPlistMarkerInt(0x10, "kCFBinaryPlistMarkerInt", true),
			/***/
			kCFBinaryPlistMarkerReal(0x20, "kCFBinaryPlistMarkerReal", true),
			/***/
			kCFBinaryPlistMarkerDate(0x30, "kCFBinaryPlistMarkerDate", true),
			/***/
			kCFBinaryPlistMarkerData(0x40, "kCFBinaryPlistMarkerData", false),
			/***/
			kCFBinaryPlistMarkerASCIIString(0x50, "kCFBinaryPlistMarkerASCIIString", false),
			/***/
			kCFBinaryPlistMarkerUnicode16String(0x60, "kCFBinaryPlistMarkerUnicode16String", false),
			/***/
			kCFBinaryPlistMarkerUID(0x80, "kCFBinaryPlistMarkerUID", true),
			/***/
			kCFBinaryPlistMarkerArray(0xA0, "kCFBinaryPlistMarkerArray", false),
			/***/
			kCFBinaryPlistMarkerSet(0xC0, "kCFBinaryPlistMarkerSet", false),
			/***/
			kCFBinaryPlistMarkerDict(0xD0, "kCFBinaryPlistMarkerDict", false);

			String	_name;

			int		_value;

			boolean	_fixedLength;

			Type(int value, String name, boolean fixedLength) {
				_value = value;
				_name = name;
				_fixedLength = fixedLength;
			}

			/**
			 * Lookup Type for a given type constant
			 *
			 * @param type
			 * @return BinaryPlistParser.Type
			 */
			public static Type typeForValue(int type) {
				if (type > -1) {
					for (Type aType : Type.values()) {
						if (aType._value == type)
							return aType;
					}
				}
				return null;
			}

			/**
			 * @return value for type
			 */
			public int value() {
				return _value;
			}

			/**
			 * @return the fixedLength
			 */
			public boolean isFixedLength() {
				return _fixedLength;
			}

			@Override
			public String toString() {
				return _name;
			}
		}

		/**
		 * Object Formats (marker byte followed by additional info in some cases)<br/>
		 * <table>
		 * <tr>
		 * <td>null</td>
		 * <td>0000 0000</td>
		 * <td></td>
		 * </tr>
		 * <tr>
		 * <td>bool</td>
		 * <td>0000 1000</td>
		 * <td>// false</td>
		 * </tr>
		 * <tr>
		 * <td>bool</td>
		 * <td>0000 1001</td>
		 * <td>// true</td>
		 * </tr>
		 * <tr>
		 * <td>fill</td>
		 * <td>0000 1111</td>
		 * <td>// fill byte</td>
		 * </tr>
		 * <tr>
		 * <td>int</td>
		 * <td>0001 nnnn ...</td>
		 * <td>// # of bytes is 2^nnnn, big-endian bytes</td>
		 * </tr>
		 * <tr>
		 * <td>real</td>
		 * <td>0010 nnnn ...</td>
		 * <td>// # of bytes is 2^nnnn, big-endian bytes</td>
		 * </tr>
		 * <tr>
		 * <td>date</td>
		 * <td>0011 0011 ...</td>
		 * <td>// 8 byte float follows, big-endian bytes</td>
		 * </tr>
		 * <tr>
		 * <td>data</td>
		 * <td>0100 nnnn [int] ...</td>
		 * <td>// nnnn is number of bytes unless 1111 then int count follows, followed by bytes</td>
		 * </tr>
		 * <tr>
		 * <td>string</td>
		 * <td>0101 nnnn [int] ...</td>
		 * <td>// ASCII string, nnnn is # of chars, else 1111 then int count, then bytes</td>
		 * </tr>
		 * <tr>
		 * <td>string</td>
		 * <td>0110 nnnn [int] ...</td>
		 * <td>// Unicode string, nnnn is # of chars, else 1111 then int count, then big-endian 2-byte shorts</td>
		 * </tr>
		 * <tr>
		 * <td></td>
		 * <td>0111 xxxx</td>
		 * <td>// unused</td>
		 * </tr>
		 * <tr>
		 * <td>uid</td>
		 * <td>1000 nnnn ...</td>
		 * <td>// nnnn+1 is # of bytes</td>
		 * </tr>
		 * <tr>
		 * <td></td>
		 * <td>1001 xxxx</td>
		 * <td>// unused</td>
		 * </tr>
		 * <tr>
		 * <td>array</td>
		 * <td>1010 nnnn [int] objref*</td>
		 * <td>// nnnn is count, unless '1111', then int count follows</td>
		 * </tr>
		 * <tr>
		 * <td></td>
		 * <td>1011 xxxx</td>
		 * <td>// unused</td>
		 * </tr>
		 * <tr>
		 * <td></td>
		 * <td>1100 xxxx</td>
		 * <td>// unused</td>
		 * </tr>
		 * <tr>
		 * <td>dict</td>
		 * <td>1101 nnnn [int] keyref* objref*</td>
		 * <td>// nnnn is count, unless '1111', then int count follows</td>
		 * </tr>
		 * <tr>
		 * <td></td>
		 * <td>1110 xxxx</td>
		 * <td>// unused</td>
		 * </tr>
		 * <tr>
		 * <td></td>
		 * <td>1111 xxxx</td>
		 * <td>// unused</td>
		 * </tr>
		 * </table>
		 */
		private void parseObject(byte[] bytes, int startIndex) throws IOException {
			int marker = (int) readByte(bytes, startIndex);
			if (logger.isDebugEnabled()) {
				logger.debug("Marker=" + marker + " marker & 0xf0=" + (marker & 0xf0) + " (marker & 0xf0) >> 4)=" + ((marker & 0xf0) >> 4));
			}

			Type typeMarker = Type.typeForValue(marker & 0xf0);
			if (typeMarker == null) {
				logger.warn("Failed to translate binary plist marker " + marker);
				return;
			}
			long count = marker & 0x0f;
			int index = startIndex + 1;// skip the marker
			if (!typeMarker.isFixedLength()) {
				if (count == 0xf) { // 15
					int countMarker = (int) readByte(bytes, index);
					index++;

					if ((countMarker & 0xf0) != Type.kCFBinaryPlistMarkerInt.value()) {
						throw new IOException("variableLengthInt: Illegal marker " + Integer.toBinaryString(marker));
					}

					int countSize = (1 << (countMarker & 0xf));
					int countIndex = countSize + index;
					if (countIndex > bytes.length) {
						throw new IOException("variableLengthInt: Illegal count " + countSize + " for marker: " + marker + " of type: " + typeMarker);
					}
					long value = 0;
					for (; index < countIndex; index++) {
						value = (value << 8) | readByte(bytes, index);
					}
					count = value;
				}
			}

			switch (typeMarker) {
				// translates kCFBinaryPlistMarkerNull, kCFBinaryPlistMarkerFalse, kCFBinaryPlistMarkerTrue,kCFBinaryPlistMarkerFill
				case kCFBinaryPlistMarkerNull: {
					parsePrimitive(bytes, index, marker);
					break;
				}
				case kCFBinaryPlistMarkerInt: {
					parseInt(bytes, index, marker);
					break;
				}
				case kCFBinaryPlistMarkerReal: {
					parseReal(bytes, index, marker);
					break;
				}
					// See CFBinaryPlist.c
				case kCFBinaryPlistMarkerDate: {
					parseDate(bytes, index, marker);
					break;
				}
				case kCFBinaryPlistMarkerData: {
					parseData(bytes, index, (int) count);
					break;
				}
				case kCFBinaryPlistMarkerASCIIString: {
					parseAsciiString(bytes, index, (int) count);
					break;
				}
				case kCFBinaryPlistMarkerUnicode16String: {
					parseUnicodeString(bytes, index, (int) count);
					break;
				}
				case kCFBinaryPlistMarkerUID: {
					parseUUID(bytes, index, marker);
					break;
				}
				case kCFBinaryPlistMarkerArray: {
					parseArray(bytes, index, (int) count);
					break;
				}
				case kCFBinaryPlistMarkerSet: {
					parseSet(bytes, index, (int) count);
					break;
				}
				case kCFBinaryPlistMarkerDict: {
					parseDictionary(bytes, index, (int) count);
					break;
				}
				default: {
					logger.debug("Fall through: Marker=" + marker + " marker & 0xf0=" + (marker & 0xf0) + " (marker & 0xf0) >> 4)=" + ((marker & 0xf0) >> 4));
					logger.warn("Failed to translate binary plist marker " + typeMarker);
				}
			}
		}

		static void debugLog(String log) {
			logger.info(log);
		}

		/**
		 * null 0000 0000 bool 0000 1000 // false bool 0000 1001 // true fill 0000 1111 // fill byte
		 *
		 * @param bytes
		 * @param startIndex
		 * @param marker
		 * @throws IOException
		 */
		private void parsePrimitive(byte[] bytes, int startIndex, int marker) throws IOException {

			Type primitive = Type.typeForValue(marker);
			if (primitive == null) {
				throw new IOException("parsePrimitive: illegal primitive " + marker);
			}
			switch (primitive) {
				case kCFBinaryPlistMarkerNull:
					objectTable.add(null);
					break;
				case kCFBinaryPlistMarkerFalse:
					objectTable.add(Boolean.FALSE);
					break;
				case kCFBinaryPlistMarkerTrue:
					objectTable.add(Boolean.TRUE);
					break;
				case kCFBinaryPlistMarkerFill:
					// fill byte: don't add to object table
					break;
				default:
					throw new IOException("parsePrimitive: illegal primitive " + marker);
			}
		}

		/**
		 * array 1010 nnnn [int] objref* // nnnn is count, unless '1111', then int count follows
		 *
		 * @param bytes
		 * @param index
		 * @param count
		 * @throws IOException
		 */
		private void parseArray(byte[] bytes, int index, int count) throws IOException {
			BinaryArray arr = new BinaryArray(objectTable, count);

			int numberOfBytes = (int) objectRefSize;
			for (int i = 0; i < count; i++) {
				long ref = 0;
				if (objectRefSize == 1) {
					ref = readByte(bytes, index + (i * numberOfBytes));
				} else if (objectRefSize == 2) {
					ref = readShort(bytes, index + (i * numberOfBytes));
				} else if (objectRefSize == 3) {
					ref = readInt(bytes, index + (i * numberOfBytes));
				} else {
					ref = readLong(bytes, index + (i * numberOfBytes));
				}
				arr.addValueRef(i, ref);
			}
			objectTable.add(arr);
		}

		/**
		 * array 1100 nnnn [int] objref* // nnnn is count, unless '1111', then int count follows
		 *
		 * @param bytes
		 * @param index
		 * @param count
		 * @throws IOException
		 */
		private void parseSet(byte[] bytes, int index, int count) throws IOException {
			BinarySet arr = new BinarySet(objectTable, count);

			int numberOfBytes = (int) objectRefSize;
			for (int i = 0; i < count; i++) {
				long ref = 0;
				if (objectRefSize == 1) {
					ref = readByte(bytes, index + (i * numberOfBytes));
				} else if (objectRefSize == 2) {
					ref = readShort(bytes, index + (i * numberOfBytes));
				} else if (objectRefSize == 4) {
					ref = readInt(bytes, index + (i * numberOfBytes));
				} else {
					ref = readLong(bytes, index + (i * numberOfBytes));
				}
				arr.addValueRef(i, ref);
			}
			objectTable.add(arr);
		}

		/**
		 * short dict 1101 ffff int keyref objref // int is count
		 *
		 * @param in
		 * @param count
		 * @throws IOException
		 */
		private void parseDictionary(byte[] bytes, int index, int count) throws IOException {
			BinaryDict dict = new BinaryDict(objectTable, count);

			// Keys
			int numberOfBytes = (int) objectRefSize;
			for (int i = 0; i < count; i++) {
				long ref = 0;
				if (objectRefSize == 1) {
					ref = readByte(bytes, index + (i * numberOfBytes));
				} else if (objectRefSize == 2) {
					ref = readShort(bytes, index + (i * numberOfBytes));
				} else if (objectRefSize == 4) {
					ref = readInt(bytes, index + (i * numberOfBytes));
				} else {
					ref = readLong(bytes, index + (i * numberOfBytes));
				}
				dict.addKeyRef(i, ref);
			}
			// values
			int valueIndex = index + (count * numberOfBytes);
			for (int i = 0; i < count; i++) {
				long ref = 0;
				if (objectRefSize == 1) {
					ref = readByte(bytes, valueIndex + (i * numberOfBytes));
				} else if (objectRefSize == 2) {
					ref = readShort(bytes, valueIndex + (i * numberOfBytes));
				} else if (objectRefSize == 4) {
					ref = readInt(bytes, valueIndex + (i * numberOfBytes));
				} else {
					ref = readLong(bytes, valueIndex + (i * numberOfBytes));
				}
				dict.addValueRef(i, ref);
			}
			objectTable.add(dict);
		}

		/**
		 * data 0100 nnnn [int] ... // nnnn is number of bytes unless 1111 then int count follows, followed by bytes
		 *
		 * @param bytes
		 * @param index
		 * @param count
		 * @throws IOException
		 */
		private void parseData(byte[] bytes, int index, int count) throws IOException {
			objectTable.add(new NSData(bytes, index, count));
		}

		/**
		 * string 0101 nnnn [int] ... // ASCII string, nnnn is # of chars, else 1111 then int count, then bytes
		 *
		 * @param bytes
		 * @param index
		 * @param count
		 * @throws IOException
		 */
		private void parseAsciiString(byte[] bytes, int index, int count) throws IOException {
			String encoding = CharEncoding.UTF_8;
			if (Charset.isSupported("ASCII")) { // CHECKME isn't ASCII mandatory for any JVM?
				encoding = CharEncoding.US_ASCII;
			}
			objectTable.add(new String(bytes, index, count, encoding));
		}

		/**
		 * int 0001 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
		 *
		 * @param bytes
		 * @param startIndex
		 * @param marker
		 * @throws IOException
		 */
		private void parseInt(byte[] bytes, int startIndex, int marker) throws IOException {
			int count = 1 << (marker & 0x0f);
			if (count > 8) {
				throw new IllegalArgumentException("parseInt: unsupported byte count:" + count);
			}
			long value = 0;
			for (int i = 0; i < count; i++) {
				long b = bytes[startIndex + i];
				value = (value << 8) | (b & 0xffL);
			}
            objectTable.add(Long.valueOf(value));
		}

		/**
		 * real 0010 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
		 *
		 * @param bytes
		 * @param startIndex
		 * @param marker
		 * @throws IOException
		 */
		private void parseReal(byte[] bytes, int startIndex, int marker) throws IOException {
			int count = (marker & 0x0f);

			switch (count) {
				case 2:
					int intbits = (int) readInt(bytes, startIndex);
					objectTable.add(Float.valueOf(Float.intBitsToFloat(intbits)));
					break;
				case 3:
					long longbits = readLong(bytes, startIndex);
					objectTable.add(Double.valueOf(Double.longBitsToDouble(longbits)));
					break;
				default:
					throw new IllegalArgumentException("parseReal: unsupported byte count:" + count);
			}
		}

		/*
		 */
		// From NSDate.m
		/**
		 * date 0011 0011 ... // 8 byte float follows, big-endian bytes<br/>
		 * 16777216.000000 + 1 nanosecond = same (2001-07-14 04:20:16 +0000)<br/>
		 * 134217728.000000 + 10 nanoseconds = same (2005-04-03 10:42:08 +0000)<br/>
		 * 1073741824.000000 + 100 nanoseconds = same (2035-01-10 13:37:04 +0000)<br/>
		 * 17179869184.000000 + 1 microsecond = same (2545-05-30 01:53:04 +0000)<br/>
		 * 137438953472.000000 + 10 microseconds = same (6356-04-08 15:04:32 +0000)<br/>
		 *
		 * @param bytes
		 * @param startIndex
		 * @param marker
		 * @throws IOException
		 */
		private void parseDate(byte[] bytes, int startIndex, int marker) throws IOException {
			long longbits = readLong(bytes, startIndex);
			double date = Double.longBitsToDouble(longbits);
			NSTimestamp ts = new NSTimestamp((long)((date + kCFAbsoluteTimeIntervalSince1970) * 1000));
			// objectTable.add(new Date(ts.getTime()));
			objectTable.add(ts);
			if (logger.isDebugEnabled()) {
				logger.info("parseDate double=" + date + " long date=" + (long) date + " timestamp=" + ts + " converted=" + new Date(ts.getTime()));
			}
		}

		/**
		 * uid 1000 nnnn ... // nnnn+1 is # of bytes
		 *
		 * @param bytes
		 * @param startIndex
		 * @param marker
		 * @throws IOException
		 */
		private void parseUUID(byte[] bytes, int startIndex, int marker) throws IOException {
			int count = (marker & 0x0f) + 1;
			if (count > 16) {
				throw new IllegalArgumentException("parseUUID: unsupported byte count:" + count);
			}

			// FIXME [PJYF Mar 27 2010] This is very questionable and untested. A UUID should be 16 bytes (128 bits).
			long leastSigBits = 0;
			long mostSigBits = 0;
			if (count > 8) {
				for (int i = 0; i < 8; i++) {
					long b = bytes[startIndex + i];
					mostSigBits = (mostSigBits << 8) | (b & 0xffL);
				}
				for (int i = 8; i < count; i++) {
					long b = bytes[startIndex + i];
					leastSigBits = (leastSigBits << 8) | (b & 0xffL);
				}
			} else {
				for (int i = 0; i < count; i++) {
					long b = bytes[startIndex + i];
					mostSigBits = (mostSigBits << 8) | (b & 0xffL);
				}
			}
			objectTable.add(new UUID(mostSigBits, leastSigBits));
		}

		/**
		 * string 0110 nnnn [int] ... // Unicode string, nnnn is # of chars, else 1111 then int count, then big-endian 2-byte shorts
		 *
		 * @param in
		 * @param count
		 * @throws IOException
		 */
		private void parseUnicodeString(byte[] bytes, int index, int count) throws IOException {
			String encoding = CharEncoding.UTF_8;
			if (Charset.isSupported("UTF-16BE")) { // CHECKME isn't UTF-16BE mandatory for any JVM?
				encoding = CharEncoding.UTF_16BE;
			}
			// The count is teh number of char not the number of bytes. With UTF-16BE there is 2 bytes per char.
			objectTable.add(new String(bytes, index, count * 2, encoding));
		}

		private static double _convertDate(Date date) {
			NSTimestamp ts = null;
			if (date instanceof NSTimestamp) {
				ts = (NSTimestamp) date;
			} else {
				// assume java.util.Date or java.sql.Date
				ts = new NSTimestamp(date);
			}

      return (ts.getTime() / 1000) - kCFAbsoluteTimeIntervalSince1970;
		}

		/**
		 * Code an type byte, consisting of the type marker and the length of the type
		 *
		 * @param type
		 * @param length
		 * @return marker that encodes type and length
		 */
		private int typeMarker(Type type, int length) {
			return type.value() | (length < 15 ? length : 0xf);
		}

		/**
		 * bool 0000 1000 // false bool 0000 1001 // true
		 */
		private byte[] encodeBoolean(boolean b) {
			byte[] value = new byte[1];
			value[0] = (b ? (byte) Type.kCFBinaryPlistMarkerTrue.value() : (byte) Type.kCFBinaryPlistMarkerFalse.value());
			return value;
		}

		/**
		 * null 0000 0000
		 */
		private byte[] encodeNull() {
			byte[] value = new byte[1];
			value[0] = (byte) Type.kCFBinaryPlistMarkerNull.value();
			return value;
		}

		/**
		 * fill 0000 1111 // fill byte
		 */
		private byte[] encodeFillByte() {
			byte[] value = new byte[1];
			value[0] = (byte) Type.kCFBinaryPlistMarkerFill.value();
			return value;
		}

		/**
		 * int 0001 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
		 *
		 * @see offsetIntSize
		 */
		private byte[] encodeCount(long value, Type marker) {
			NSMutableData data = new NSMutableData(16);
			if (value > IntegerMaxValue) {
				data.appendByte((byte) typeMarker(marker, 0x0f));
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerInt, 3));
				data.appendByte((byte) ((value >>> 56) & 0xff));
				data.appendByte((byte) ((value >>> 48) & 0xff));
				data.appendByte((byte) ((value >>> 40) & 0xff));
				data.appendByte((byte) ((value >>> 32) & 0xff));
				data.appendByte((byte) ((value >>> 24) & 0xff));
				data.appendByte((byte) ((value >>> 16) & 0xff));
				data.appendByte((byte) ((value >>> 8) & 0xff));
				data.appendByte((byte) ((value >>> 0) & 0xff));
			} else if (value > ShortMaxValue) {
				data.appendByte((byte) typeMarker(marker, 0x0f));
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerInt, 2));
				data.appendByte((byte) ((value >>> 24) & 0xff));
				data.appendByte((byte) ((value >>> 16) & 0xff));
				data.appendByte((byte) ((value >>> 8) & 0xff));
				data.appendByte((byte) ((value >>> 0) & 0xff));
			} else if (value > ByteMaxValue) {
				data.appendByte((byte) typeMarker(marker, 0x0f));
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerInt, 1));
				data.appendByte((byte) ((value >>> 8) & 0xff));
				data.appendByte((byte) ((value >>> 0) & 0xff));
			} else if (value >= 15) {
				data.appendByte((byte) typeMarker(marker, 0x0f));
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerInt, 0));
				data.appendByte((byte) ((value >>> 0) & 0xff));
			} else {
				data.appendByte((byte) typeMarker(marker, (byte) (value & 0x0f)));
			}
			return data.bytes();
		}

		/**
		 * int 0001 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
		 *
		 * @see offsetIntSize
		 */
		private byte[] encodeInt(long value) {
			NSMutableData data = new NSMutableData();
			if (value > Integer.MAX_VALUE || value < 0) {
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerInt, 3));
				data.appendByte((byte) ((value >>> 56) & 0xff));
				data.appendByte((byte) ((value >>> 48) & 0xff));
				data.appendByte((byte) ((value >>> 40) & 0xff));
				data.appendByte((byte) ((value >>> 32) & 0xff));
				data.appendByte((byte) ((value >>> 24) & 0xff));
				data.appendByte((byte) ((value >>> 16) & 0xff));
				data.appendByte((byte) ((value >>> 8) & 0xff));
				data.appendByte((byte) ((value >>> 0) & 0xff));
			} else if (value > Short.MAX_VALUE || value < Short.MIN_VALUE) {
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerInt, 2));
				data.appendByte((byte) ((value >>> 24) & 0xff));
				data.appendByte((byte) ((value >>> 16) & 0xff));
				data.appendByte((byte) ((value >>> 8) & 0xff));
				data.appendByte((byte) ((value >>> 0) & 0xff));
			} else {
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerInt, 1));
				data.appendByte((byte) ((value >>> 8) & 0xff));
				data.appendByte((byte) ((value >>> 0) & 0xff));
			}
			return data.bytes();
		}

		/**
		 * uid 1000 nnnn ... // nnnn+1 is # of bytes
		 *
		 * @see offsetIntSize
		 */
		private byte[] encodeUUID(UUID value) {
			long mostSigBits = value.getMostSignificantBits();
			long leastSigBits = value.getLeastSignificantBits();

			NSMutableData data = new NSMutableData();
			data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerUID, 0x0f));
			data.appendByte((byte) ((mostSigBits >>> 56) & 0xff));
			data.appendByte((byte) ((mostSigBits >>> 48) & 0xff));
			data.appendByte((byte) ((mostSigBits >>> 40) & 0xff));
			data.appendByte((byte) ((mostSigBits >>> 32) & 0xff));
			data.appendByte((byte) ((mostSigBits >>> 24) & 0xff));
			data.appendByte((byte) ((mostSigBits >>> 16) & 0xff));
			data.appendByte((byte) ((mostSigBits >>> 8) & 0xff));
			data.appendByte((byte) ((mostSigBits >>> 0) & 0xff));

			data.appendByte((byte) ((leastSigBits >>> 56) & 0xff));
			data.appendByte((byte) ((leastSigBits >>> 48) & 0xff));
			data.appendByte((byte) ((leastSigBits >>> 40) & 0xff));
			data.appendByte((byte) ((leastSigBits >>> 32) & 0xff));
			data.appendByte((byte) ((leastSigBits >>> 24) & 0xff));
			data.appendByte((byte) ((leastSigBits >>> 16) & 0xff));
			data.appendByte((byte) ((leastSigBits >>> 8) & 0xff));
			data.appendByte((byte) ((leastSigBits >>> 0) & 0xff));

			return data.bytes();
		}

		/**
		 * real 0010 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
		 */
		private byte[] encodeReal(double value) {
			NSMutableData data = new NSMutableData();
			if (value > Float.MAX_VALUE || value < Float.MIN_VALUE) {
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerReal, 3));
				long bits = Double.doubleToLongBits(value);
				data.appendByte((byte) ((bits >>> 56) & 0xff));
				data.appendByte((byte) ((bits >>> 48) & 0xff));
				data.appendByte((byte) ((bits >>> 40) & 0xff));
				data.appendByte((byte) ((bits >>> 32) & 0xff));
				data.appendByte((byte) ((bits >>> 24) & 0xff));
				data.appendByte((byte) ((bits >>> 16) & 0xff));
				data.appendByte((byte) ((bits >>> 8) & 0xff));
				data.appendByte((byte) ((bits >>> 0) & 0xff));
			} else {
				data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerReal, 2));
				int bits = Float.floatToIntBits((float) value);
				data.appendByte((byte) ((bits >>> 24) & 0xff));
				data.appendByte((byte) ((bits >>> 16) & 0xff));
				data.appendByte((byte) ((bits >>> 8) & 0xff));
				data.appendByte((byte) ((bits >>> 0) & 0xff));
			}
			return data.bytes();
		}

		/**
		 * string 0101 nnnn [int] ... // ASCII string, nnnn is # of chars, else 1111 then int count, then bytes <br/>
		 * string 0110 nnnn [int] ... // Unicode string, nnnn is # of chars, else 1111 then int count, then big-endian 2-byte shorts
		 */

		private byte[] encodeString(String value) {
			try {
				NSMutableData data = new NSMutableData(value.length() * 2);
				String encoding = CharEncoding.UTF_8;
				// This is kind of funky we do a first encoding to see if we can get away with ASCII encoding
				// This is true if UTF-8 encoding yield the same length as the char count.
				byte[] theBytes = value.getBytes(encoding);
	
				if (theBytes.length == value.length()) {
					data.appendBytes(encodeCount(value.length(), Type.kCFBinaryPlistMarkerASCIIString));
				} else {
					if (Charset.isSupported("UTF-16BE")) {
						encoding = CharEncoding.UTF_16BE;
					}
					theBytes = value.getBytes(encoding);
					data.appendBytes(encodeCount(value.length(), Type.kCFBinaryPlistMarkerUnicode16String));
				}
				data.appendBytes(theBytes);
				return data.bytes();
			}
			catch (UnsupportedEncodingException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}

		/**
		 * date 0011 0011 ... // 8 byte float follows, big-endian bytes
		 */
		private byte[] encodeDate(Date value) {
			NSMutableData data = new NSMutableData(16);
			double convertedDate = _convertDate(value);
			data.appendByte((byte) typeMarker(Type.kCFBinaryPlistMarkerDate, 3));

			long bits = Double.doubleToRawLongBits(convertedDate);
			data.appendByte((byte) ((bits >>> 56) & 0xff));
			data.appendByte((byte) ((bits >>> 48) & 0xff));
			data.appendByte((byte) ((bits >>> 40) & 0xff));
			data.appendByte((byte) ((bits >>> 32) & 0xff));
			data.appendByte((byte) ((bits >>> 24) & 0xff));
			data.appendByte((byte) ((bits >>> 16) & 0xff));
			data.appendByte((byte) ((bits >>> 8) & 0xff));
			data.appendByte((byte) ((bits >>> 0) & 0xff));
			return data.bytes();
		}

		/*
		 * data 0100 nnnn [int] ... // nnnn is number of bytes unless 1111 then int count follows, followed by bytes
		 */
		private byte[] encodeData(byte[] theData) {
			NSMutableData data = new NSMutableData(theData.length + 8);
			data.appendBytes(encodeCount(theData.length, Type.kCFBinaryPlistMarkerData));
			data.appendBytes(theData);
			return data.bytes();
		}

	}

	protected ERXPropertyListSerialization() {
		throw new IllegalStateException("Can't instantiate an instance of class " + getClass().getName());
	}

	/**
	 * Converts a property list object into a string (old style plist) and returns it. All entries are indented.
	 *
	 * @param plist
	 *            the property list to convert
	 * @return <code>plist</code> as a string
	 * @see #propertyListFromString(String)
	 */
	public static String stringFromPropertyList(Object plist) {
		return stringFromPropertyList(plist, Indents);
	}

	/**
	 * Converts a property list object into a string (old style plist) and returns it.
	 *
	 * @param plist
	 *            the property list to convert
	 * @param indents
	 *            if true the resulting plist is indented (tabs and CR) if false no tabs anc CR are inserted in the result
	 * @return <code>plist</code> as a string
	 * @see #propertyListFromString(String, boolean)
	 * @since 5.4
	 */
	public static String stringFromPropertyList(Object plist, boolean indents) {
		return (new _ApplePList(indents)).stringFromPropertyList(plist);
	}

	/**
	 * Converts a property list object into an XML string and returns it. All entries are indented.
	 *
	 * @param plist
	 *            the property list to convert
	 * @return <code>plist</code> as a string
	 * @see #propertyListFromString(String)
	 * @since 5.4
	 */
	public static String xmlStringFromPropertyList(Object plist) {
		return xmlStringFromPropertyList(plist, Indents);
	}

	/**
	 * Converts a property list object into an XML string and returns it.
	 *
	 * @param plist
	 *            the property list to convert
	 * @param indents
	 *            if true the resulting plist is indented (tabs and CR) if false no tabs anc CR are inserted in the result
	 * @return <code>plist</code> as a string
	 * @see #propertyListFromString(String, boolean)
	 * @since 5.4
	 */
	public static String xmlStringFromPropertyList(Object plist, boolean indents) {
		return (new _XML(indents)).stringFromPropertyList(plist);
	}

	// According to the Wrox Press book _Professional_XML_, if an XML document starts with an ?xml node, then it can have no leading
	// whitespace, so we assume that it doesn't!
	// Strictly speaking, an XML document doesn't have to start with a ?xml node, but at present we have no other way to distinguish
	// XML plists from ASCII plists...
	static boolean startsWithXMLDeclaration(String string) {
		return string != null && string.trim().startsWith("<?xml");
	}

	/**
	 * Converts a string into a property list and returns it.
	 *
	 * @param string
	 *            the string to convert to a property list
	 * @return <code>string</code> as a property list
	 * @see #stringFromPropertyList(Object)
	 */
	public static Object propertyListFromString(String string) {
		return propertyListFromString(string, !ForceXML);
	}

	/**
	 * Converts a string into a property list and returns it.
	 *
	 * @param string
	 *            the string to convert to a property list
	 * @param forceXML
	 *            force xml decoding
	 * @return <code>string</code> as a property list
	 * @see #stringFromPropertyList(Object, boolean)
	 * @since 5.4
	 */
	public static Object propertyListFromString(String string, boolean forceXML) {
		// Old style ASCII plist NSData nodes are hexadecimal digits surrounded
		// by <> brackets so we can't assume any string starting with '<' is XML.
		// With a bit more work, we could do a much better job of detecting old
		// style ASCII NSData plist values. For now, we require an XML plist to
		// have a "prolog" starting with an '<?xml?>' declaration.
		if (forceXML || startsWithXMLDeclaration(string)) {
			return (new _XML()).parseStringIntoPlist(string);
		}
		return (new _ApplePList()).parseStringIntoPlist(string);
	}

	/**
	 * Converts a java.net.URL into a property list and returns it.
	 *
	 * @param url
	 *            the java.net.URL to convert to a property list
	 * @return the content at <code>url</code> as a property list
	 * @see #stringFromPropertyList(Object)
	 * @since 5.2.2
	 */
	public static Object propertyListWithPathURL(URL url) {
		return propertyListWithPathURL(url, !ForceXML);
	}

	/**
	 * Converts a java.net.URL into a property list and returns it.
	 *
	 * @param url
	 *            the java.net.URL to convert to a property list
	 * @param forceXML
	 *            force xml decoding
	 * @return the content at <code>url</code> as a property list
	 * @see #stringFromPropertyList (Object, boolean)
	 * @since 5.4
	 */
	public static Object propertyListWithPathURL(URL url, boolean forceXML) {
		try {
			if (url == null)
				return null;
			return propertyListFromString(_NSStringUtilities.stringFromPathURL(url), forceXML);
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to parse a property list from the URL '" + url + "'.", e);
		}
	}

	/**
	 * Convert JSON formatted string to a property list
	 *
	 * @param string
	 * @return content of string as a property list
	 * @since 5.5
	 * @see "http://www.json.org"
	 */
	public static Object propertyListFromJSONString(String string) {
		return (new _JSONPList()).parseStringIntoPlist(string);
	}

	/**
	 * Converts the property list <code>object</code> into a JSON formatted string. By default, it will not append whitespace to JSON string.
	 *
	 * @param plist
	 * @return string for a given plist
	 * @since 5.5
	 */
	public static String jsonStringFromPropertyList(Object plist) {
		return (new _JSONPList()).stringFromPropertyList(plist, true);
	}

	/**
	 * Converts the property list <code>object</code> into a JSON formatted string.
	 *
	 * @param plist
	 * @param suppressWhiteSpace
	 *            - will not format string with whitespace
	 * @return JSON formatted string for a given plist
	 * @since 5.5
	 */
	public static String jsonStringFromPropertyList(Object plist, boolean suppressWhiteSpace) {
		return (new _JSONPList()).stringFromPropertyList(plist, suppressWhiteSpace);
	}

	/**
	 * Converts the property list <code>object</code> into a string and returns it as an NSData object. This method uses the platform's default character encoding to convert the result string to byte.
	 *
	 * @deprecated use {@link #dataFromPropertyList(Object, String)}
	 * @param plist
	 *            property list object
	 * @return <code>object</code> converted to an NSData
	 * @see #propertyListFromData(NSData, java.lang.String)
	 */
	@Deprecated
	public static NSData dataFromPropertyList(Object plist) {
		return dataFromPropertyList(plist, (String)null);
	}

	/**
	 * Converts the property list <code>object</code> into a string using a character encoding and returns it as an NSData object.
	 *
	 * @param plist
	 *            the property list object to convert
	 * @param encoding
	 *            encoding used to convert the characters in the result string to byte
	 * @return <code>object</code> converted to an NSData
	 * @see #propertyListFromData(NSData, java.lang.String)
	 */
	public static NSData dataFromPropertyList(Object plist, String encoding) {
		if (plist == null)
			return null;
		return new NSData(_NSStringUtilities.bytesForString(stringFromPropertyList(plist), encoding));
	}
	
	/**
	 * For a specified property list format, create an NSData from an object.
	 *  
	 * @param plist the object to write
	 * @param type type of plist to generate
	 * @param encoding the string encoding of the bytes in the stream (ignored for binary plist format)
	 * @see PListFormat#NSPropertyListJsonFormat_v1_0
	 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
	 * @see PListFormat#NSPropertyListXMLFormat_v1_0
	 * @see PListFormat#NSPropertyListOpenStepFormat
	 * @return an NSData containing the property list output
	 * @since 5.5.3
	 */
	public static NSData dataFromPropertyList(Object plist, PListFormat type, String encoding) {
		_NSStreamingOutputData data = new _NSStreamingOutputData();
		writePropertyListToStream(plist, data, type, encoding);
		return data.dataNoCopy();
	}

	/**
	 * Converts an NSData into a property list and returns it.
	 * <p>
	 * This method uses the platform's default character encoding to convert the bytes in <code>data</code> byte array to characters in a string representation.
	 *
	 * @deprecated use {@link #propertyListFromData(NSData, String)}
	 * @param data
	 *            the byte array to be converted to a property list
	 * @return <code>data</code> as a property list
	 * @see #dataFromPropertyList(Object, java.lang.String)
	 */
	@Deprecated
	public static Object propertyListFromData(NSData data) {
		return propertyListFromData(data, (String)null);
	}

	/**
	 * Converts an NSData into a property list using a character encoding and returns it.
	 *
	 * @param data
	 *            the byte array to be converted to a property list
	 * @param encoding
	 *            encoding to use to convert the bytes in the data byte array to characters in a string representation
	 * @return <code>data</code> as a property list
	 * @see #dataFromPropertyList(Object, java.lang.String)
	 */
	public static Object propertyListFromData(NSData data, String encoding) {
		if (data == null)
			return null;
		return propertyListFromString(_NSStringUtilities.stringForBytes(data.bytes(), encoding));
	}
	
	/**
	 * Converts an NSData into a property list and returns it.
	 *
	 * @param data
	 *            the byte array to be converted to a property list
	 * @param type type of plist to generate
	 * @param encoding the string encoding of the bytes in the stream (ignored for binary plist format)
	 * @see PListFormat#NSPropertyListJsonFormat_v1_0
	 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
	 * @see PListFormat#NSPropertyListXMLFormat_v1_0
	 * @see PListFormat#NSPropertyListOpenStepFormat
	 * @return <code>data</code> as a property list
	 * @since 5.5.3
	 */
	public static Object propertyListFromData(NSData data, PListFormat type, String encoding) {
		if (data == null) {
			return null;
		}
		return propertyListWithStream(data.stream(), type, encoding);
	}

	// For now we assume that this only handles ASCII plists, not XML
	/**
	 * Parses a given string for boolean value according to the table below.
	 * <p>
	 * <table border="0">
	 * <tr>
	 * <th align="left">String</th>
	 * <th width="5"></th>
	 * <th align="left">Result</th>
	 * </tr>
	 * <tr>
	 * <td><code>"YES"</code></td>
	 * <td></td>
	 * <td><code>true</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"true"</code></td>
	 * <td></td>
	 * <td><code>true</code></td>
	 * </tr>
	 * <tr>
	 * <td>any other value</td>
	 * <td></td>
	 * <td><code>false</code></td>
	 * </tr>
	 * </table>
	 * <p>
	 * The tests for "YES" and "true" are case insensitive.
	 *
	 * @param value
	 *            the string to be parsed for boolean value
	 * @return the parsed boolean value
	 */
	public static boolean booleanForString(String value) {
		if (value != null) {
			String testValue = value.toLowerCase().trim();
			return (testValue.equals("yes") || testValue.equals("true"));
		}
		return false;
	}

	// For now we assume that this only handles ASCII plists, not XML
	/**
	 * Parses a given string and returns the corresponding integer value.
	 *
	 * @param value
	 *            the string to be parsed for integer value
	 * @return integer value of <code>string</code>; <code>0</code> if it is <code>null</code>
	 */
	public static int intForString(String value) {
		if (value != null) {
			try {
				return Integer.parseInt(value.trim());
			} catch (Exception exception) {
				throw NSForwardException._runtimeExceptionForThrowable(exception);
			}
		}
		return 0;
	}

	/**
	 * Converts a java.net.URL into a property list and returns the resulting property list as an NSArray.
	 *
	 * @param <E>
	 * @param url
	 *            the java.net.URL to convert to a property list
	 * @return Returns resulting property list as an NSArray
	 * @see #stringFromPropertyList(Object) stringFromPropertyList
	 * @since 5.4
	 */
	public static <E> NSArray<E> arrayWithPathURL(URL url) {
		return arrayWithPathURL(url, !ForceXML);
	}

	/**
	 * Converts a java.net.URL into a property list and returns the resulting property list as an NSArray.
	 *
	 * @param <E>
	 * @param url
	 *            the java.net.URL to convert to a property list
	 * @param forceXML
	 *            force xml decoding
	 * @return Returns resulting property list as an NSArray
	 * @see #stringFromPropertyList(Object, boolean) stringFromPropertyList
	 * @since 5.4
	 */
	@SuppressWarnings("unchecked")
	public static <E> NSArray<E> arrayWithPathURL(URL url, boolean forceXML) {
		Object result = propertyListWithPathURL(url, forceXML);
		return (result instanceof NSArray ? (NSArray<E>) result : NSArray.<E> emptyArray());
	}

	/**
	 * Parses the property list representation <code>string</code> and returns the resulting property list as an NSArray.
	 *
	 * @param <E>
	 * @param value
	 *            input property list representation string
	 * @return Returns resulting property list as an NSArray
	 * @see #dictionaryForString(String)
	 */
	public static <E> NSArray<E> arrayForString(String value) {
		return arrayForString(value, !ForceXML);
	}

	/**
	 * Parses the property list representation <code>string</code> and returns the resulting property list as an NSArray.
	 *
	 * @param <E>
	 * @param value
	 *            input property list representation string
	 * @param forceXML
	 *            force xml decoding
	 * @return Returns resulting property list as an NSArray
	 * @see #dictionaryForString(String)
	 * @since 5.4
	 */
	@SuppressWarnings("unchecked")
	public static <E> NSArray<E> arrayForString(String value, boolean forceXML) {
		Object result = propertyListFromString(value, forceXML);
		return (result instanceof NSArray ? (NSArray<E>) result : NSArray.<E> emptyArray());
	}

	/**
	 * Parses the JSON formatted <code>string</code> and returns the resulting property list as an NSArray. NOTE: any JSON 'null' values will be tranlated to a String placeholder value (See ERXPropertyListSerialization.NULL).
	 *
	 * @param <E>
	 * @param value
	 *            input property list representation string
	 * @return Returns resulting property list as an NSArray
	 * @see #NULL
	 * @see #dictionaryForJSONString(String)
	 * @since 5.6
	 */
	@SuppressWarnings("unchecked")
	public static <E> NSArray<E> arrayForJSONString(String value) {
		Object result = propertyListFromJSONString(value);
		return (result instanceof NSArray ? (NSArray<E>) result : NSArray.<E> emptyArray());
	}

	/**
	 * Converts a java.net.URL into a property list and returns the resulting property list as an NSDictionary.
	 *
	 * @param <K>
	 * @param <V>
	 * @param url
	 *            the java.net.URL to convert to a property list
	 * @return <code>string</code> as an NSDictionary
	 * @see #stringFromPropertyList (Object)
	 * @since 5.4
	 */
	public static <K, V> NSDictionary<K, V> dictionaryWithPathURL(URL url) {

		return dictionaryWithPathURL(url, !ForceXML);
	}

	/**
	 * Converts a java.net.URL into a property list and returns the resulting property list as an NSDictionary.
	 *
	 * @param <K>
	 * @param <V>
	 * @param url
	 *            the java.net.URL to convert to a property list
	 * @param forceXML
	 *            force xml decoding
	 * @return <code>string</code> as an NSDictionary
	 * @see #stringFromPropertyList (Object, boolean)
	 * @since 5.4
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> NSDictionary<K, V> dictionaryWithPathURL(URL url, boolean forceXML) {
		Object result = propertyListWithPathURL(url, forceXML);
		return (result instanceof NSDictionary ? (NSDictionary<K, V>) result : NSDictionary.<K, V> emptyDictionary());
	}

	/**
	 * Parses the property list representation <code>string</code> and returns the resulting property list as an NSDictionary.
	 *
	 * @param <K>
	 * @param <V>
	 * @param value
	 *            property list represented as a string
	 * @return <code>string</code> as an NSDictionary
	 * @see #arrayForString(String)
	 */
	public static <K, V> NSDictionary<K, V> dictionaryForString(String value) {
		return dictionaryForString(value, !ForceXML);
	}

	/**
	 * Return NSDictionary for a valid plist.
	 *
	 * @param <K>
	 * @param <V>
	 * @param url
	 * @return binary plists dictionary decoded as an NSDictionary or empty dictionary if invalid.
	 * @since 5.5
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> NSDictionary<K, V> dictionaryWithBinaryPropertyListPathURL(URL url) {
		if (url == null) {
			return NSDictionary.<K, V> emptyDictionary();
		}
		_BinaryPListParser parser = new _BinaryPListParser();
		NSDictionary<K, V> ret = (NSDictionary<K, V>) parser.propertyListWithURL(url);
		return (ret != null) ? ret : NSDictionary.<K, V> emptyDictionary();
	}

	/**
	 * Return NSDictionary for a valid plist when passed a binary plist stream.
	 *
	 * @param <K>
	 * @param <V>
	 * @param is
	 * @return binary plists dictionary decoded as an NSDictionary or empty dictionary if invalid.
	 * @since 5.5
	 */
	@Deprecated
	public static <K, V> NSDictionary<K, V> dictionaryForBinaryStream(InputStream is) {
		return dictionaryWithBinaryStream(is);
	}
	
	/**
	 * Return NSDictionary for a valid plist when passed a binary plist stream.
	 *
	 * @param <K>
	 * @param <V>
	 * @param is
	 * @return binary plists dictionary decoded as an NSDictionary or empty dictionary if invalid.
	 * @since 5.5.3
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> NSDictionary<K, V> dictionaryWithBinaryStream(InputStream is) {
		if (is == null) {
			return NSDictionary.<K, V> emptyDictionary();
		}
		_BinaryPListParser parser = new _BinaryPListParser();
		NSDictionary<K, V> ret = (NSDictionary<K, V>) parser.propertyListWithStream(is);
		return (ret != null) ? ret : NSDictionary.<K, V> emptyDictionary();
	}

	/**
	 * Parse binary plist to XML Document
	 *
	 * @param url
	 * @return Document for binary plist
	 * @see Document
	 * @since 5.5
	 */
	@Deprecated
	public static Document documentForBinaryPropertyListURL(URL url) {
		return documentWithBinaryPropertyListURL(url);
	}
	
	/**
	 * Parse binary plist to XML Document
	 *
	 * @param url the url to read from
	 * @return Document for binary plist
	 * @see Document
	 * @since 5.5.3
	 */
	public static Document documentWithBinaryPropertyListURL(URL url) {
		if (url == null) {
			return null;
		}
		_BinaryPListParser parser = new _BinaryPListParser();
		Document doc = parser.propertyListDocumentWithURL(url);
		return doc;
	}

	/**
	 * @param is
	 * @return NSArray or empty array if null
	 * @since 5.5.3-SNAPSHOT
	 */
	public static NSArray<?> arrayForBinaryStream(InputStream is) {
		if (is == null) {
			return NSArray.EmptyArray;
		}
		_BinaryPListParser parser = new _BinaryPListParser();
		NSArray<?> ret = (NSArray<?>)parser.propertyListWithStream(is);
		return (ret != null) ? ret : NSArray.EmptyArray;
	}

	/**
	 * Parse binary plist to XML Document
	 *
	 * @param url
	 * @return Document for binary plist
	 * @see Document
	 * @since 5.5
	 */
	@Deprecated
	public static String xmlStringForBinaryPropertyListURL(URL url) {
		return xmlStringWithBinaryPropertyListURL(url);
	}
		
	/**
	 * Parse binary plist to XML Document
	 *
	 * @param url the url to read from
	 * @return Document for binary plist
	 * @see Document
	 * @since 5.5.3
	 */
	public static String xmlStringWithBinaryPropertyListURL(URL url) {
		String ret = "";

		if (url == null) {
			return ret;
		}

		return ERXPropertyListSerialization/*_NSStringUtilities*/.convertDOMToString(documentWithBinaryPropertyListURL(url));
	}
	
	/**
	 * Reads a plist from the given URL using the specified format.
	 * 
	 * @param url the URL to read from
	 * @param type type of plist to generate
	 * @param encoding the string encoding of the bytes in the stream (ignored for binary plist format)
	 * @see PListFormat#NSPropertyListJsonFormat_v1_0
	 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
	 * @see PListFormat#NSPropertyListXMLFormat_v1_0
	 * @see PListFormat#NSPropertyListOpenStepFormat
	 * @since 5.5.3
	 */
	public static Object propertyListWithURL(URL url, PListFormat type, String encoding) {
		try {
			 URLConnection conn = url.openConnection();
			 InputStream is = conn.getInputStream();
			 try {
				 return propertyListWithStream(is, type, encoding);
			 }
			 finally {
				 is.close();
			 }
		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to decode plist at " + url, e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to decode plist at " + url, e);
		}
	}
	
	/**
	 * Reads a plist from the given inputstream using the specified format.
	 * 
	 * @param is the InputStream to read from
	 * @param type type of plist to generate
	 * @param encoding the string encoding of the bytes in the stream (ignored for binary plist format)
	 * @see PListFormat#NSPropertyListJsonFormat_v1_0
	 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
	 * @see PListFormat#NSPropertyListXMLFormat_v1_0
	 * @see PListFormat#NSPropertyListOpenStepFormat
	 * @since 5.5.3
	 */
	public static Object propertyListWithStream(InputStream is, PListFormat type, String encoding) {
		if (is == null) {
			return null;
		}
		
		Object obj;
		switch (type) {
			case NSPropertyListBinaryFormat_v1_0:
				obj = new _BinaryPListParser().propertyListWithStream(is);
				break;
	
			case NSPropertyListXMLFormat_v1_0:
				obj = propertyListFromString(_NSStringUtilities.stringFromInputStream(is, encoding), true);
				break;
	
			case NSPropertyListOpenStepFormat:
				obj = propertyListFromString(_NSStringUtilities.stringFromInputStream(is, encoding), false);
				break;
	
			case NSPropertyListJsonFormat_v1_0:
				obj = propertyListFromJSONString(_NSStringUtilities.stringFromInputStream(is, encoding));
				break;
	
			default:
				obj = propertyListFromString(_NSStringUtilities.stringFromInputStream(is, encoding));
				break;
		}
		return obj;
	}
	
	/**
	 * Reads a plist from the given NSData using the specified format.
	 * 
	 * @param data the NSData to read from
	 * @param type type of plist to generate
	 * @param encoding the string encoding of the bytes in the stream (ignored for binary plist format)
	 * @see PListFormat#NSPropertyListJsonFormat_v1_0
	 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
	 * @see PListFormat#NSPropertyListXMLFormat_v1_0
	 * @see PListFormat#NSPropertyListOpenStepFormat
	 * @since 5.5.3
	 */
	public static Object propertyListWithData(NSData data, PListFormat type, String encoding) {
		return propertyListWithStream(data.stream(), type, encoding);
	}

	/**
	 * For a specified property list format, write a plist to the provided outputstream.
	 *
	 * @param plist the object to write
	 * @param out output stream for plist
	 * @param type type of plist to generate
	 * @see PListFormat#NSPropertyListJsonFormat_v1_0
	 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
	 * @see PListFormat#NSPropertyListXMLFormat_v1_0
	 * @see PListFormat#NSPropertyListOpenStepFormat
	 * @since 5.5
	 */
	@Deprecated
	public static void propertyListWriteToStream(Object plist, OutputStream out, PListFormat type) {
		writePropertyListToStream(plist, out, type, _NSStringUtilities.defaultEncoding());
	}
	
	/**
	 * For a specified property list format, write a plist to the provided outputstream.
	 *
	 * @param plist the object to write
	 * @param out output stream for plist
	 * @param type type of plist to generate
	 * @param encoding the string encoding of the bytes in the stream (ignored for binary plist format)
	 * @see PListFormat#NSPropertyListJsonFormat_v1_0
	 * @see PListFormat#NSPropertyListBinaryFormat_v1_0
	 * @see PListFormat#NSPropertyListXMLFormat_v1_0
	 * @see PListFormat#NSPropertyListOpenStepFormat
	 * @since 5.5.3
	 */
	public static void writePropertyListToStream(Object plist, OutputStream out, PListFormat type, String encoding) {

		if (plist == null || out == null) {
			return;
		}
		String plistString = null;
		switch (type) {
			case NSPropertyListBinaryFormat_v1_0:
				_BinaryPListParser parser = new _BinaryPListParser();
				if (plist instanceof Map<?, ?>) {
					parser.writePropertyListToStream(plist, out);
				} else if (plist instanceof NSDictionary) {
	                parser.writePropertyListToStream(plist, out);
				} else if (plist instanceof List<?>) {
					parser.writePropertyListToStream(plist, out);
                } else if (plist instanceof NSArray) {
                    parser.writePropertyListToStream(plist, out);
				}
				break;

			case NSPropertyListXMLFormat_v1_0:
				plistString = xmlStringFromPropertyList(plist, false);
				if (plistString != null) {
					try {
						out.write(plistString.getBytes(encoding));
					} catch (IOException e) {
						throw new RuntimeException("Error writing xml formatted plist to outputstream.", e);
					}
				}
				break;

			case NSPropertyListOpenStepFormat:
				plistString = stringFromPropertyList(plist, false);
				if (plistString != null) {
					try {
						out.write(plistString.getBytes(encoding));
					} catch (IOException e) {
						throw new RuntimeException("Error writing ascii formatted plist to outputstream.", e);
					}
				}
				break;

			case NSPropertyListJsonFormat_v1_0:
				plistString = jsonStringFromPropertyList(plist, false);
				if (plistString != null) {
					try {
						out.write(plistString.getBytes(encoding));
					} catch (IOException e) {
						throw new RuntimeException("Error writing jsons formatted plist to outputstream.", e);
					}
				}
				break;

			default:
				// we should never get here but we'll put it here anyways
				plistString = xmlStringFromPropertyList(plist, false);
				if (plistString != null) {
					try {
						out.write(plistString.getBytes(encoding));
					} catch (IOException e) {
						throw new RuntimeException("Error writing xml formatted plist to outputstream.", e);
					}
				}
				break;
		}

	}

	/**
	 * Read a plist from an InputStream and return NSDictionary of values.
	 *
	 * @param <K>
	 * @param <V>
	 * @param is
	 * @return dictionary or null if there is an error with parsing
	 * @since 5.5
	 */
	@Deprecated
	public static <K, V> NSDictionary<K, V> dictionaryForInputStream(InputStream is) {
		return dictionaryWithInputStream(is);
	}
	
	/**
	 * Read a plist from an InputStream and return NSDictionary of values.
	 *
	 * @param <K>
	 * @param <V>
	 * @param is
	 * @return dictionary or null if there is an error with parsing
	 * @since 5.5
	 */
	@Deprecated
	public static <K, V> NSDictionary<K, V> dictionaryWithInputStream(InputStream is) {
		return dictionaryWithInputStream(is, CharEncoding.UTF_8);
	}
	
	/**
	 * Read a plist from an InputStream and return NSDictionary of values.
	 *
	 * @param <K>
	 * @param <V>
	 * @param is the input stream to read from
	 * @param encoding the string encoding of the bytes in the stream (ignored for binary plist format)
	 * @return dictionary or null if there is an error with parsing
	 * @since 5.5.3
	 */
	public static <K, V> NSDictionary<K, V> dictionaryWithInputStream(InputStream is, String encoding) {
		if (is == null) {
			return NSDictionary.<K, V> emptyDictionary();
		}
		return dictionaryForString(_NSStringUtilities.stringFromInputStream(is, encoding));
	}

	/**
	 * Parses the property list representation <code>string</code> and returns the resulting property list as an NSDictionary.
	 *
	 * @param <K>
	 * @param <V>
	 * @param value
	 *            property list represented as a string
	 * @param forceXML
	 *            force xml decoding
	 * @return <code>string</code> as an NSDictionary
	 * @see #arrayForString(String)
	 * @since 5.4
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> NSDictionary<K, V> dictionaryForString(String value, boolean forceXML) {
		Object result = propertyListFromString(value, forceXML);
		return (result instanceof NSDictionary ? (NSDictionary<K, V>) result : NSDictionary.<K, V> emptyDictionary());
	}

	/**
	 * Parses the JSON formatted <code>string</code> and returns the resulting property list as an NSDictionary. NOTE: any JSON 'null' values will be tranlated to a String placeholder value (See ERXPropertyListSerialization.NULL).
	 *
	 * @param <K>
	 * @param <V>
	 * @param value
	 *            property list represented as a string
	 * @return <code>string</code> as an NSDictionary
	 * @see #NULL
	 * @see #arrayForJSONString(String)
	 * @since 5.5.1
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> NSDictionary<K, V> dictionaryForJSONString(String value) {
		Object result = propertyListFromJSONString(value);
		return (result instanceof NSDictionary ? (NSDictionary<K, V>) result : NSDictionary.<K, V> emptyDictionary());
	}

	private static String convertDOMToString(org.w3c.dom.Document doc) {
        if (doc == null) {
            return null;
        }

        StringWriter stringOut = new StringWriter();
        try {
            OutputFormat format = new OutputFormat(doc); // Serialize DOM
            XMLSerializer serial = new XMLSerializer(stringOut, format);
            serial.asDOMSerializer(); // As a DOM serializer
            serial.serialize(doc.getDocumentElement());
        } catch (IOException e) {
            throw new NSForwardException(e);
        }
        return stringOut.toString();
    }
}
